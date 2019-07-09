/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

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
            HttpInput("notAValidScheme", "localhost", 9200, "_cluster/health", mapOf(), "", 5000, 5000)
            fail("Invalid scheme when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid host
            HttpInput("http", "loco//host", 9200, "_cluster/health", mapOf(), "", 5000, 5000)
            fail("Invalid host when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid port
            HttpInput("http", "localhost", -500, "_cluster/health", mapOf(), "", 5000, 5000)
            fail("Invalid port when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid path
            HttpInput("http", "localhost", 9200, "////////", mapOf(), "", 5000, 5000)
            fail("Invalid path when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid url
            HttpInput("http", "localhost", 9200, "_cluster/health", mapOf(), "", 5000, 5000)
            fail("Invalid url when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid connection timeout
            HttpInput("http", "localhost", 9200, "_cluster/health", mapOf(), "", -5000, 5000)
            fail("Invalid connection timeout when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
        try {
            // Invalid socket timeout
            HttpInput("http", "localhost", 9200, "_cluster/health", mapOf(), "", 5000, -5000)
            fail("Invalid socket timeout when creating HttpInput should fail.")
        } catch (e: IllegalArgumentException) {
        }
    }

    // Test valid url with complete url
    @Test
    fun `test valid url`() {
        val validHttpInput = HttpInput("", "", -1, "", mapOf(), "http://localhost:9200/_cluster/health/", 5000, 5000)
        assertEquals(validHttpInput.url, "http://localhost:9200/_cluster/health/")
    }
}
