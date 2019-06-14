package com.amazon.opendistroforelasticsearch.alerting.core.model

import org.elasticsearch.common.ParseField
import org.elasticsearch.common.CheckedFunction
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException
/**
 * This class is a "Http" type of input that supports user to enter a Http location in order to perform actions such as monitoring another cluster's health information
 */
data class HttpInput(val scheme: String, val host: String, val port: Int, val path: String?, val body: String?) : Input {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
                .startObject(HTTP_FIELD)
                .field(HOST_FIELD, host)
                .field(PORT_FIELD, port)
                .field(PATH_FIELD, path)
                .field(BODY_FIELD, body)
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
        const val HTTP_FIELD = "http"
        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(Input::class.java, ParseField("http"), CheckedFunction { parseInner(it) })

        /**
         * This parse function uses XContentParser to parse JSON input and store corresponding fields to create a HttpInput object
         */
        @JvmStatic @Throws(IOException::class)
        private fun parseInner(xcp: XContentParser): HttpInput {
            var scheme = "http"
            var host = ""
            var port: Int = -1
            var path: String? = null
            var body: String? = null
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
                }
            }
            // TODO: Check whether each field has value then decide how to create object
            return HttpInput(scheme, host, port, path, body)
        }
    }
}
