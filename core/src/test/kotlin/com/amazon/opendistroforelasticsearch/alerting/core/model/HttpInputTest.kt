package com.amazon.opendistroforelasticsearch.alerting.core.model

import junit.framework.Assert.fail
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals

class HttpInputTest {
    // Test invalid url with different format in one function
    fun `test invalid urls`() {
        try {
            // Invalid scheme
            val invalidSchemeHttpInput = HttpInput("htttttp", "localhost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid scheme when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid host
            val invalidHostHttpInput = HttpInput("http", "locohost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid host when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid port
            val invalidPortHttpInput = HttpInput("http", "localhost", -1, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid port when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid path
            val invalidPathHttpInput = HttpInput("http", "localhost", 9200, "////////", null, null, "", 5000, 5000)
            fail("Invalid path when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        /** try {
            // Invalid params
            val invalidParamsHttpInput = HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid params when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid body
            val invalidBodyHttpInput = HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid body when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        } */
        try {
            // Invalid url
            val invalidUrlHttpInput = HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid url when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid connection timeout
            val invalidConnectionTimeoutHttpInput = HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", -5000, 5000)
            fail("Invalid connection timeout when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid socket timeout
            val invalidSocketTimeoutHttpInput = HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", 5000, -5000)
            fail("Invalid socket timeout when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
    }
    // Test valid url field by field
    fun `test valid url`() {
        val validHttpInput = HttpInput("", "", -1, "", null, null, "http://localhost:9200/_cluster/health/", 5000, 5000)
        assertEquals(validHttpInput.url, "http://localhost:9200/_cluster/health/")
    }
}
