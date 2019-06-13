package com.amazon.opendistroforelasticsearch.alerting.client

import com.amazon.opendistroforelasticsearch.alerting.core.model.HttpInput
import org.apache.http.HttpResponse
import java.net.URI
import java.lang.Exception
import java.net.URISyntaxException
import org.apache.http.client.utils.URIBuilder
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

    @Throws(Exception::class)
    fun getHttpResponse(input: HttpInput): CloseableHttpResponse {
        var uri: URI?
        val httpGetRequest = HttpGet("http://localhost:9200/_cluster/health")
        uri = buildUri(input.host[0], input.port, input.path)
        // httpGetRequest.uri = uri
        return httpClient.execute(httpGetRequest)
    }

    /**
     * URI building function to fit my parameters from HttpInput, should be modified later to support other types of URI better
     */
    @Throws(Exception::class)
    private fun buildUri(host: String?, port: Int, path: String?): URI {
        try {
                val uriBuilder = URIBuilder()
                return uriBuilder.setScheme("http").setHost(host).setPort(port).setPath(path).build()
        } catch (exception: URISyntaxException) {
            logger.error("Error occurred while building Uri")
            throw IllegalStateException("Error creating URI")
        }
    }

    @Throws(IOException::class)
    fun getResponseString(response: CloseableHttpResponse): String {
        val entity = response.entity ?: return "{}"

        val responseString = EntityUtils.toString(entity)
        logger.debug("Http response: $responseString")

        return responseString
    }

    @Throws(IOException::class)
    private fun validateResponseStatus(response: HttpResponse) {
        val statusCode = response.statusLine.statusCode

        if (!VALID_RESPONSE_STATUS.contains(statusCode)) {
            throw IOException("Failed: $response")
        }
    }
    fun setHttpClient(httpClient: CloseableHttpClient) {
        this.httpClient = httpClient
    }

    fun publish(input: HttpInput): HttpInputResponse {
        try {

            val response = execute(input)
            return HttpInputResponse(response, RestStatus.OK.status)
        } catch (ex: Exception) {
            logger.error("Exception publishing Input: $input", ex)
            throw IllegalStateException(ex)
        }
    }

    /**
     * This function is created in order to prevent NetPermission error to occur, all
     * the required actions are nested in this function.
     */
    fun privilegedPublish(httpInput: HttpInput): HttpInputResponse {
        return AccessController.doPrivileged(PrivilegedAction<HttpInputResponse>({
            this.publish(httpInput)
        } as () -> HttpInputResponse))
    }
}
