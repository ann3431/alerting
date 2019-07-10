/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.alerting.client

import com.amazon.opendistroforelasticsearch.alerting.core.model.HttpInput
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import java.io.IOException
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.Collections
import java.util.HashSet

/**
 * This class takes HttpInputs and performs GET requests to given URIs
 */
class HttpInputClient {

    private val logger = LogManager.getLogger(HttpInputClient::class.java)

    // TODO: If possible, these settings should be implemented as changeable via the "_cluster/settings" API.
    private val MAX_CONNECTIONS = 60
    private val MAX_CONNECTIONS_PER_ROUTE = 20
    private val TIMEOUT_MILLISECONDS = TimeValue.timeValueSeconds(10).millis().toInt()
    private val SOCKET_TIMEOUT_MILLISECONDS = TimeValue.timeValueSeconds(10).millis().toInt()

    private val VALID_RESPONSE_STATUS = Collections.unmodifiableSet(HashSet(
            listOf(RestStatus.OK.status,
                    RestStatus.CREATED.status,
                    RestStatus.ACCEPTED.status,
                    RestStatus.NON_AUTHORITATIVE_INFORMATION.status,
                    RestStatus.NO_CONTENT.status,
                    RestStatus.RESET_CONTENT.status,
                    RestStatus.PARTIAL_CONTENT.status,
                    RestStatus.MULTI_STATUS.status)))

    private var httpClient = createHttpClient()

    private fun createHttpClient(): CloseableHttpClient {
        val config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MILLISECONDS)
                .setConnectionRequestTimeout(TIMEOUT_MILLISECONDS)
                .setSocketTimeout(SOCKET_TIMEOUT_MILLISECONDS)
                .build()

        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = MAX_CONNECTIONS
        connectionManager.defaultMaxPerRoute = MAX_CONNECTIONS_PER_ROUTE

        // Create HttpClient as a PrivilegedAction in order to avoid java.net.NetPerission error.
        return AccessController.doPrivileged(PrivilegedAction<CloseableHttpClient>({
            HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .setConnectionManager(connectionManager)
                    .setRetryHandler(DefaultHttpRequestRetryHandler())
                    .useSystemProperties()
                    .build()
        } as () -> CloseableHttpClient))
    }

    /**
     * This function is created for testing purpose.
     */
    fun setHttpClient(httpClient: CloseableHttpClient) {
        this.httpClient = httpClient
    }

    fun collectHttpInputResultAsMap(input: HttpInput): Map<String, Any> {
        val httpInputResponse = performRequest(input)
        val httpInputResponseParser = XContentType.JSON.xContent().createParser(
                NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, httpInputResponse)
        return httpInputResponseParser.map()
    }

    /**
     * This function provides a centralized place to perform the [httpClient].execute() function as a [PrivilegedAction] to avoid NetPermission errors.
    */
    fun performRequest(httpInput: HttpInput): String {
        return AccessController.doPrivileged(PrivilegedAction<String> {
            this.execute(httpInput)
        })
    }

    @Throws(Exception::class)
    fun execute(input: HttpInput): String {
        var response: CloseableHttpResponse? = null
        try {
            response = getHttpResponse(input)
            validateResponseStatus(response)
            return getResponseString(response)
        } finally {
            // Consume entity no matter what the status code is.
            if (response != null) {
                EntityUtils.consumeQuietly(response.entity)
            }
        }
    }

    /**
     * Creates a Http GET request with configuration provided by HttpInput and executes the request
     * @return CloseableHttpResponse The response from GET request.
     */
    @Throws(Exception::class)
    fun getHttpResponse(input: HttpInput): CloseableHttpResponse {
        val requestConfig = RequestConfig.custom()
                .setConnectTimeout(input.connection_timeout)
                .setSocketTimeout(input.socket_timeout)
                .build()
        val httpGetRequest = HttpGet(input.url)
        httpGetRequest.config = requestConfig
        return httpClient.execute(httpGetRequest)
    }

    @Throws(IOException::class)
    private fun validateResponseStatus(response: HttpResponse) {
        val statusCode = response.statusLine.statusCode
        if (statusCode !in VALID_RESPONSE_STATUS) {
            logger.error("HttpInputClient failed to get valid response status, response message: $response")
            throw IOException("HttpInputClient failed to get valid response status, response message: $response")
        }
    }

    @Throws(IOException::class)
    fun getResponseString(response: CloseableHttpResponse): String {
        val entity = response.entity ?: return "{}"

        val responseString = EntityUtils.toString(entity)
        logger.debug("Http response: $responseString")
        return responseString
    }
}
