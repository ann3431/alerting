package com.amazon.opendistroforelasticsearch.alerting.core.model

import org.elasticsearch.common.ParseField
import org.elasticsearch.common.xcontent.*
import org.elasticsearch.common.CheckedFunction
import java.io.IOException

data class HtmlInput (val host: List<String>, val port: String, val path :String) : Input {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
                .startObject(HtmlInput.HTML_FIELD)
                .field(HtmlInput.HOST_FIELD,host)
                .field(HtmlInput.PORT_FIELD,port)
                .field(HtmlInput.PATH_FIELD,path)
                .endObject()
                .endObject()
                .endObject()
    }


    override fun name(): String {
        return HtmlInput.HTML_FIELD
    }

    companion object {
        const val HOST_FIELD = "host"
        const val PORT_FIELD = "port"
        const val PATH_FIELD = "path"
        const val HTML_FIELD = "html"


        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(Input::class.java, ParseField("Html"), CheckedFunction { parseInner(it) })

        @JvmStatic @Throws(IOException::class)
        private fun parseInner(xcp: XContentParser): HtmlInput {


            //Do I need to make it a lateinit var since of "type" is local, there might not be any hosts
            val host = mutableListOf<String>()
            lateinit var port : String
            lateinit var path : String
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

                    PORT_FIELD ->{
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp::getTokenLocation)
                        port=xcp.text()
                    }

                    PATH_FIELD ->{
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp::getTokenLocation)
                        path=xcp.text()
                    }


                }
            }

            return HtmlInput(host,port,path)
        }
    }


}
