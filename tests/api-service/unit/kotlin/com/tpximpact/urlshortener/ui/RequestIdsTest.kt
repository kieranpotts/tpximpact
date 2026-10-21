package com.tpximpact.urlshortener.ui

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.UUID

class RequestIdsTest {
    @Test
    fun `Generates a request ID when the caller sends none`() =
        apiTest { client ->
            val response = client.get("/openapi.yaml")

            assertNotNull(UUID.fromString(response.headers[HttpHeaders.XRequestId]))
        }

    @ParameterizedTest
    @ValueSource(strings = ["6f1c0f8e-2c1d-4a53-9e55-1d3c1b7a9c10", "web:1729.42_abc"])
    fun `Echoes the caller's request ID`(requestId: String) =
        apiTest { client ->
            val response = client.get("/openapi.yaml") { header(HttpHeaders.XRequestId, requestId) }

            assertEquals(requestId, response.headers[HttpHeaders.XRequestId])
        }

    @ParameterizedTest
    @ValueSource(strings = ["has spaces", "non-ascii-é", ""])
    fun `Replaces an invalid request ID`(requestId: String) =
        apiTest { client ->
            val response = client.get("/openapi.yaml") { header(HttpHeaders.XRequestId, requestId) }

            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(UUID.fromString(response.headers[HttpHeaders.XRequestId]))
        }

    @Test
    fun `Replaces a request ID longer than 128 characters`() =
        apiTest { client ->
            val requestId = "a".repeat(129)

            val response = client.get("/openapi.yaml") { header(HttpHeaders.XRequestId, requestId) }

            assertNotEquals(requestId, response.headers[HttpHeaders.XRequestId])
        }

    @Test
    fun `Error responses carry the request ID too`() =
        apiTest(routes = { get("/fault") { error("Fault") } }) { client ->
            val notFound = client.get("/no-such-alias") { header(HttpHeaders.XRequestId, "not-found-id") }
            val fault = client.get("/fault") { header(HttpHeaders.XRequestId, "fault-id") }

            assertEquals("not-found-id", notFound.headers[HttpHeaders.XRequestId])
            assertEquals("fault-id", fault.headers[HttpHeaders.XRequestId])
        }
}
