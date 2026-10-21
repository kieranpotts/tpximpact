package com.tpximpact.urlshortener.ui

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WelcomeRouteTest {
    @Test
    fun `Serves a welcome page, as HTML, at the root path`() =
        apiTest { client ->
            val response = client.get("/")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            assertTrue("Welcome to the URL shortener" in response.bodyAsText())
        }
}
