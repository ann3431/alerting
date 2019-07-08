package com.amazon.opendistroforelasticsearch.alerting.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HttpInputTest {
    // Test invalid url with different format in one function
    @Test
    fun `test invalid urls`() {
        try {
            // Invalid scheme
            HttpInput("notAValidScheme", "localhost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid scheme when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid host
            HttpInput("http", "locohost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid host when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid port
            HttpInput("http", "localhost", -1, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid port when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid path
            HttpInput("http", "localhost", 9200, "////////", null, null, "", 5000, 5000)
            fail("Invalid path when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid url
            HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", 5000, 5000)
            fail("Invalid url when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid connection timeout
            HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", -5000, 5000)
            fail("Invalid connection timeout when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid socket timeout
            HttpInput("http", "localhost", 9200, "_cluster/health", null, null, "", 5000, -5000)
            fail("Invalid socket timeout when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
    }
    // Test valid url field by field
    @Test
    fun `test valid url`() {
        val validHttpInput = HttpInput("", "", -1, "", null, null, "http://localhost:9200/_cluster/health/", 5000, 5000)
        assertEquals(validHttpInput.url, "http://localhost:9200/_cluster/health/")
    }
}
