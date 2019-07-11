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
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.Strings
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import java.io.IOException
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.Collections
import java.util.HashSet

/**
 * This class takes [HttpInput]s and performs GET requests to given URIs.
 */
class HttpInputClient {

    private val logger = LogManager.getLogger(HttpInputClient::class.java)

    // TODO: If possible, these settings should be implemented as changeable via the "_cluster/settings" API.
    private val MAX_CONNECTIONS = 60
    private val MAX_CONNECTIONS_PER_ROUTE = 20
    private val TIMEOUT_MILLISECONDS = TimeValue.timeValueSeconds(10).millis().toInt()
    private val SOCKET_TIMEOUT_MILLISECONDS = TimeValue.timeValueSeconds(10).millis().toInt()

    val host = HttpHost("http://localhost:9200")
    val httpClient = createHttpClient()


    private val VALID_RESPONSE_STATUS = Collections.unmodifiableSet(HashSet(
            listOf(RestStatus.OK.status,
                    RestStatus.CREATED.status,
                    RestStatus.ACCEPTED.status,
                    RestStatus.NON_AUTHORITATIVE_INFORMATION.status,
                    RestStatus.NO_CONTENT.status,
                    RestStatus.RESET_CONTENT.status,
                    RestStatus.PARTIAL_CONTENT.status,
                    RestStatus.MULTI_STATUS.status)))

    private fun createHttpClient(): CloseableHttpAsyncClient {
        val config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MILLISECONDS)
                .setConnectionRequestTimeout(TIMEOUT_MILLISECONDS)
                .setSocketTimeout(SOCKET_TIMEOUT_MILLISECONDS)
                .build()

        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = MAX_CONNECTIONS
        connectionManager.defaultMaxPerRoute = MAX_CONNECTIONS_PER_ROUTE



        // Create HttpClient as a PrivilegedAction in order to avoid java.net.NetPermission error.
        return AccessController.doPrivileged(PrivilegedAction<CloseableHttpAsyncClient>({
                HttpAsyncClientBuilder.create()
                        .setDefaultRequestConfig(config)
                        .useSystemProperties()
                        .build()
        } as () -> CloseableHttpAsyncClient))
    }

    /**
     * This function provides a centralized place to perform the [HttpInputClient].execute() function as a [PrivilegedAction] to avoid NetPermission errors.
    */
    fun performRequest(httpInput: HttpInput): XContentParser {
        return AccessController.doPrivileged(PrivilegedAction<XContentParser> {
            XContentType.JSON.xContent().createParser(
                    NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, this.execute(httpInput))
        })
    }

    @Throws(Exception::class)
    fun execute(input: HttpInput): String {
        var response = ""
        try {
            response = getHttpResponse(input)
//            validateResponseStatus(response)
            return response
//            return getResponseString(response)
        } finally {
            // Consume entity no matter what the status code is.
            if (response != null) {
//                EntityUtils.consumeQuietly("")
            }
        }
    }

    /**
     * Creates a HTTP GET request with configuration provided by [HttpInput] and executes the request
     * @return CloseableHttpResponse The response from GET request.
     */
    @Throws(Exception::class)
    fun getHttpResponse(input: HttpInput): String {
        val requestConfig = RequestConfig.custom()
                .setConnectTimeout(input.connection_timeout)
                .setSocketTimeout(input.socket_timeout)
                .build()
        // If url field is null or empty, construct an url field by field.
        val constructedUrl = if (Strings.isNullOrEmpty(input.url)) {
            val uriBuilder = URIBuilder()
            uriBuilder.scheme = input.scheme
            uriBuilder.host = input.host
            uriBuilder.port = input.port
            uriBuilder.path = input.path
            for (e in input.params.entries)
                uriBuilder.addParameter(e.key, e.value)
            uriBuilder.build().toString()
        } else {
            input.url
        }
        val httpGetRequest = HttpGet(constructedUrl)
        httpGetRequest.config = requestConfig
//        httpClient.execute(HttpHost(input.host), httpGetRequest)
        return ""
    }

    fun getRequest(input: HttpInput): HttpGet {
        val constructedUrl = if (Strings.isNullOrEmpty(input.url)) {
            val uriBuilder = URIBuilder()
            uriBuilder.scheme = input.scheme
            uriBuilder.host = input.host
            uriBuilder.port = input.port
            uriBuilder.path = input.path
            for (e in input.params.entries)
                uriBuilder.addParameter(e.key, e.value)
            uriBuilder.build().toString()
        } else {
            input.url
        }
        logger.info("Constructed url: $constructedUrl")
        return HttpGet(constructedUrl)
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
    fun getResponseString(response: HttpResponse): String {
        val entity = response.entity ?: return "{}"

        val responseString = EntityUtils.toString(entity)
        logger.debug("Http response: $responseString")
        return responseString
    }
}
