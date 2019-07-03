package com.amazon.opendistroforelasticsearch.alerting.core.model

import java.lang.IllegalStateException

class HttpInputTest{
    // Test invalid url with different format in one function
    fun `test invalid urls`() = try{
        // Invalid scheme
        val invalidSchemeHttpInput = HttpInput("htttttp", "localhost", 9200, "_cluster/health", "", "")
    } catch (e: IllegalStateException) {

    }
    // Test valid url field by field
    fun `test valid url`() {

    }
}
