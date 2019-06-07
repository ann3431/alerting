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
data class HttpInput(val host: List<String>, val port: Int?, val path: String?, val body: String?) : Input {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
                .startObject(HttpInput.HTTP_FIELD)
                .field(HttpInput.HOST_FIELD, host.toTypedArray())
                .field(HttpInput.PORT_FIELD, port)
                .field(HttpInput.PATH_FIELD, path)
                .field(HttpInput.BODY_FIELD, body)
                .endObject()
                .endObject()
    }
// Probably need to change the name for this
    override fun name(): String {
        return HttpInput.HTTP_FIELD
    }
    companion object {
        const val HOST_FIELD = "host"
        const val PORT_FIELD = "port"
        const val PATH_FIELD = "path"
        const val HTTP_FIELD = "http"
        const val BODY_FIELD = "body"

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(Input::class.java, ParseField("http"), CheckedFunction { parseInner(it) })

        @JvmStatic @Throws(IOException::class)
        private fun parseInner(xcp: XContentParser): HttpInput {
            val host = mutableListOf<String>()
            var port: Int? = null
            var path: String? = null
            var body: String? = null
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp::getTokenLocation)

            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    HOST_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp::getTokenLocation)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            host.add(xcp.text())
                        }
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
            // Check whether each field has value then decide how to create object
            return HttpInput(host, port, path, body)
        }
    }
}
