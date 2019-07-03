package com.amazon.opendistroforelasticsearch.alerting.client

import com.amazon.opendistroforelasticsearch.alerting.core.model.HttpInput
import org.apache.http.HttpResponse
import java.lang.Exception
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import java.io.IOException
import org.apache.logging.log4j.LogManager
import org.elasticsearch.rest.RestStatus
import java.util.Arrays
import java.util.HashSet
import java.util.Collections
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.client.config.RequestConfig
import org.elasticsearch.common.unit.TimeValue
import org.apache.http.client.methods.HttpGet
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * This class takes HttpInputs and perform GET requests to given URIs
 */
class HttpInputClient {

    private val logger = LogManager.getLogger(HttpInputClient::class.java)

    private val MAX_CONNECTIONS = 60
    private val MAX_CONNECTIONS_PER_ROUTE = 20
    private val TIMEOUT_MILLISECONDS = TimeValue.timeValueSeconds(5).millis().toInt()
    private val SOCKET_TIMEOUT_MILLISECONDS = TimeValue.timeValueSeconds(50).millis().toInt()

    private val VALID_RESPONSE_STATUS = Collections.unmodifiableSet(HashSet(
            Arrays.asList(RestStatus.OK.status, RestStatus.CREATED.status, RestStatus.ACCEPTED.status,
                    RestStatus.NON_AUTHORITATIVE_INFORMATION.status, RestStatus.NO_CONTENT.status,
                    RestStatus.RESET_CONTENT.status, RestStatus.PARTIAL_CONTENT.status,
                    RestStatus.MULTI_STATUS.status)))

    private var httpClient = createHttpClient()

    fun createHttpClient(): CloseableHttpClient {
        val config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MILLISECONDS)
                .setConnectionRequestTimeout(TIMEOUT_MILLISECONDS)
                .setSocketTimeout(SOCKET_TIMEOUT_MILLISECONDS)
                .build()

        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = MAX_CONNECTIONS
        connectionManager.defaultMaxPerRoute = MAX_CONNECTIONS_PER_ROUTE
        return AccessController.doPrivileged(PrivilegedAction<CloseableHttpClient>({
            HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .setConnectionManager(connectionManager)
                    .setRetryHandler(DefaultHttpRequestRetryHandler())
                    .useSystemProperties()
                    .build()
        } as () -> CloseableHttpClient))
    }

    fun setHttpClient(httpClient: CloseableHttpClient) {
        this.httpClient = httpClient
    }

    /**
     * This function is created in order to prevent NetPermission error to occur.
     * What this does is to run execute() as a privileged action so that it will not run into NetPermission error or SocketPermission error etc.
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
                .setConnectTimeout(input.connection_timeout).setSocketTimeout(input.socket_timeout).build()
        val httpGetRequest = HttpGet(input.url)
        httpGetRequest.config = requestConfig
        return httpClient.execute(httpGetRequest)
    }

    @Throws(IOException::class)
    private fun validateResponseStatus(response: HttpResponse) {
        val statusCode = response.statusLine.statusCode

        if (statusCode !in VALID_RESPONSE_STATUS) {
            throw IOException("Failed: $response")
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
