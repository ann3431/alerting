package com.amazon.opendistroforelasticsearch.alerting.client

import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.xcontent.XContentHelper

/**
 * This class is created to make converting HttpInputResponse from String to ByteReference more convenient.
 * Since HttpInputResponse format is not consistent, we simply parse it into a type of XContentBuilder and preserve the original String value.
 */
class HttpInputResponse(val responseContent: String?, val statusCode: Int) : ToXContentObject {
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject(responseContent)
                .endObject()
    }

    class Builder {
        private var responseContent: String? = null
        private var statusCode: Int? = null

        fun withResponseContent(responseContent: String): HttpInputResponse.Builder {
            this.responseContent = responseContent
            return this
        }

        fun withStatusCode(statusCode: Int?): HttpInputResponse.Builder {
            this.statusCode = statusCode
            return this
        }

        fun build(): HttpInputResponse {
            return HttpInputResponse(responseContent, statusCode!!)
        }
    }

    /**
     * Function that takes in a JSON String and converts it to Map<String, Any>
     */
    fun convertToMap(): Map<String, Any> {
        // Convert to ByteReference
        val HttpInputResponseByteReference = XContentHelper.toXContent(this, XContentType.JSON, false)
        // Convert to Map<String, Any> and add to result
        return XContentHelper.convertToMap(HttpInputResponseByteReference, false, XContentType.JSON).v2()
    }
}
