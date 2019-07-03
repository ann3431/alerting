package com.amazon.opendistroforelasticsearch.alerting.core.model

import org.apache.http.client.utils.URIBuilder
import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.CheckedFunction
import org.elasticsearch.common.ParseField
import org.elasticsearch.common.Strings
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.IOException
import java.net.URISyntaxException

/**
 * This class is a "Http" type of input that supports user to enter a Http location in order to perform actions such as monitoring another cluster's health information
 */
data class HttpInput(
    val scheme: String,
    val host: String?,
    val port: Int,
    val path: String?,
    val params: Map<String, String>,
    val body: SearchSourceBuilder?,
    var url: String,
    val connection_timeout: Int,
    val socket_timeout: Int
) : Input {
    private val logger = LogManager.getLogger(HttpInput::class.java)
    // Verify that url is valid during creation
    init {
        try {
            if (Strings.isNullOrEmpty(url)) {
                val uriBuilder = URIBuilder()
                if (Strings.isNullOrEmpty(scheme)) {
                    uriBuilder.scheme = "https"
                } else
                    uriBuilder.scheme = scheme
                if (params != null) {
                    for (e in params.entries)
                        uriBuilder.addParameter(e.key, e.value)
                }
                uriBuilder.setHost(host).setPort(port).setPath(path)
                // If uri created by fields is valid, set url field to the uri constructed
                url = uriBuilder.toString()
            }
            // Use regular expression to verify url
            val regex = """^((((https?)://)|(mailto:|news:))" +
            "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)" +
                    "([).!';/?:,][[:blank:]])?$"""".toRegex()
            require(regex.matches(url)) { "Invalid URL: $url" }
        } catch (exception: URISyntaxException) {
            logger.error("Error occurred while building Uri")
            throw IllegalStateException("Error creating URI")
        }
        require(!(Strings.isNullOrEmpty(url) && Strings.isNullOrEmpty(host))) {
                "Url or Host name must be provided."
        }
    }
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
                .startObject(HTTP_FIELD)
                .field(SCHEME_FIELD, scheme)
                .field(HOST_FIELD, host)
                .field(PORT_FIELD, port)
                .field(PATH_FIELD, path)
                .field(PARAMS_FIELD, this.params)
                .field(BODY_FIELD, body)
                .field(URL_FIELD, url)
                .field(CONNECTION_TIMEOUT_FIELD, connection_timeout)
                .field(SOCKET_TIMEOUT_FIELD, socket_timeout)
                .endObject()
                .endObject()
    }
    override fun name(): String {
        return HTTP_FIELD
    }
    companion object {
        const val SCHEME_FIELD = "scheme"
        const val HOST_FIELD = "host"
        const val PORT_FIELD = "port"
        const val PATH_FIELD = "path"
        const val PARAMS_FIELD = "params"
        const val BODY_FIELD = "body"
        const val URL_FIELD = "url"
        const val CONNECTION_TIMEOUT_FIELD = "connection_timeout"
        const val SOCKET_TIMEOUT_FIELD = "socket_timeout"
        const val HTTP_FIELD = "http"
        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(Input::class.java, ParseField("http"), CheckedFunction { parseInner(it) })

        /**
         * This parse function uses XContentParser to parse JSON input and store corresponding fields to create a HttpInput object
         */
        @JvmStatic @Throws(IOException::class)
        private fun parseInner(xcp: XContentParser): HttpInput {
            var scheme = "http"
            var host: String? = null
            var port: Int = -1
            var path: String? = null
            var params: Map<String, String> = mutableMapOf()
            var body = SearchSourceBuilder()
            var url: String = ""
            var connectionTimeout = TimeValue.timeValueSeconds(5).millis().toInt()
            var socketTimeout = TimeValue.timeValueSeconds(50).millis().toInt()
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp::getTokenLocation)

            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    SCHEME_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_STRING, xcp.currentToken(),
                                xcp::getTokenLocation)
                        scheme = xcp.text()
                    }
                    HOST_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_STRING, xcp.currentToken(),
                                xcp::getTokenLocation)
                        host = xcp.text()
                    }
                    PORT_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_NUMBER, xcp.currentToken(),
                                xcp::getTokenLocation)
                        port = xcp.intValue()
                    }
                    PATH_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_STRING, xcp.currentToken(),
                                xcp::getTokenLocation)
                        path = xcp.text()
                    }
                    BODY_FIELD -> {
                        body = SearchSourceBuilder.fromXContent(xcp, false)
                    }
                    PARAMS_FIELD -> {
                        params = xcp.mapStrings()
                    }
                    URL_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_STRING, xcp.currentToken(),
                                xcp::getTokenLocation)
                        url = xcp.text()
                    }
                    CONNECTION_TIMEOUT_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_NUMBER, xcp.currentToken(),
                                xcp::getTokenLocation)
                        connectionTimeout = xcp.intValue()
                    }
                    SOCKET_TIMEOUT_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_NUMBER, xcp.currentToken(),
                                xcp::getTokenLocation)
                        socketTimeout = xcp.intValue()
                    }
                }
            }
                return HttpInput(scheme, host, port, path, params, body, url, connectionTimeout, socketTimeout)
        }
    }
}
