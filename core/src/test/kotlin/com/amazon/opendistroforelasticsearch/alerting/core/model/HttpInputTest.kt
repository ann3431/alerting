package com.amazon.opendistroforelasticsearch.alerting.core.model

import junit.framework.Assert.fail
import java.lang.IllegalStateException
import kotlin.test.assertEquals

class HttpInputTest {
    // Test invalid url with different format in one function
    fun `test invalid urls`() = try {
        // Invalid scheme
        val invalidSchemeHttpInput = HttpInput("htttttp", "localhost", 9200, "_cluster/health", null, null, "", 5000, 5000)
        fail("Invalid scheme when creating HttpInput should fail.")
    } catch (e: IllegalStateException) {
    }
    // Test valid url field by field
    fun `test valid url`() {
        val validHttpInput = HttpInput("", "", -1, "", null, null, "http://localhost:9200/_cluster/health/", 5000, 5000)
        assertEquals(validHttpInput.url, "http://localhost:9200/_cluster/health/")
    }
}
