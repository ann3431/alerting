package com.amazon.opendistroforelasticsearch.alerting.core.model

import org.elasticsearch.common.ParseField
import org.elasticsearch.common.CheckedFunction
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException
/**
 * This class is a "Http" type of input that supports user to enter a Http location in order to perform actions such as monitoring another cluster's health information
 */
data class HttpInput(
    val scheme: String,
    val host: String?,
    val port: Int,
    val path: String?,
    val body: String?,
    val url: String?,
    val connection_timeout: Int,
    val socket_timeout: Int,
    val max_connection_per_route: Int
) : Input {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
                .startObject(HTTP_FIELD)
                .field(SCHEME_FIELD, scheme)
                .field(HOST_FIELD, host)
                .field(PORT_FIELD, port)
                .field(PATH_FIELD, path)
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
        const val BODY_FIELD = "body"
        const val URL_FIELD = "url"
        const val CONNECTION_TIMEOUT_FIELD = "connection_timeout"
        const val SOCKET_TIMEOUT_FIELD = "socket_timeout"
        const val MAX_CONNECTION_PER_ROUTE_FIELD = "max_connection_per_route"
        const val HTTP_FIELD = "http"
        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(Input::class.java, ParseField("http"), CheckedFunction { parseInner(it) })

        /**
         * This parse function uses XContentParser to parse JSON input and store corresponding fields to create a HttpInput object
         */
        @JvmStatic @Throws(IOException::class)
        private fun parseInner(xcp: XContentParser): HttpInput {
            var scheme = "http"
            var host: String? = ""
            var port: Int = -1
            var path: String? = ""
            var body: String? = ""
            var url: String? = ""
            var connectionTimeout = TimeValue.timeValueSeconds(5).millis().toInt()
            var socketTimeout = TimeValue.timeValueSeconds(50).millis().toInt()
            var maxConnectionPerRoute = 20
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
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_STRING, xcp.currentToken(),
                                xcp::getTokenLocation)
                        body = xcp.text()
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
                    MAX_CONNECTION_PER_ROUTE_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.VALUE_NUMBER, xcp.currentToken(),
                                xcp::getTokenLocation)
                        maxConnectionPerRoute = xcp.intValue()
                    }

                }
            }
                return HttpInput(scheme, host, port, path, body, url, connectionTimeout, socketTimeout, maxConnectionPerRoute)
        }
    }
}
