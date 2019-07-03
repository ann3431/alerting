package com.amazon.opendistroforelasticsearch.alerting.http

import com.amazon.opendistroforelasticsearch.alerting.client.HttpInputClient
import org.elasticsearch.test.ESTestCase
import java.io.IOException
import java.time.Instant

class HttpInputClientTests : ESTestCase() {
     // Test response in non JSON format
     fun `test non JSON string response`() {
       try {
           // generate a httpInput with non json api call?? Mock the response
           val httpInputClient= HttpInputClient()
       } catch (e: IOException){ }
     }
}
