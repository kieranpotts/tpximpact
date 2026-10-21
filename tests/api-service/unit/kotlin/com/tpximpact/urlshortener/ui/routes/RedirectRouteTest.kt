package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.InMemoryShortUrlRepository
import com.tpximpact.urlshortener.domain.valid
import com.tpximpact.urlshortener.ui.Problem
import com.tpximpact.urlshortener.ui.apiTest
import com.tpximpact.urlshortener.ui.publicBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RedirectRouteTest {
    private val repository = InMemoryShortUrlRepository()

    private fun test(block: suspend (client: HttpClient) -> Unit) = apiTest(repository, block = block)

    // Requirement: B5
    @Test
    fun `Redirects an alias to its destination URL, uncached`() =
        test { client ->
            store("my-alias", "https://example.com/very/long/url")

            val response = client.get("/my-alias")

            assertEquals(HttpStatusCode.Found, response.status)
            assertEquals("https://example.com/very/long/url", response.headers[HttpHeaders.Location])
            assertEquals("no-store", response.headers[HttpHeaders.CacheControl])
        }

    // Requirement: R7
    @Test
    fun `Redirects to the destination URL exactly as stored`() =
        test { client ->
            store("as-given", "HTTPS://Example.COM:443/a/../b?x=%7E&y=1#Top")

            val response = client.get("/as-given")

            assertEquals("HTTPS://Example.COM:443/a/../b?x=%7E&y=1#Top", response.headers[HttpHeaders.Location])
        }

    // Requirement: R3
    @Test
    fun `Redirects only an alias typed in the case it was created in`() =
        test { client ->
            store("MyLink", "https://example.com/")

            assertEquals(HttpStatusCode.Found, client.get("/MyLink").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/mylink").status)
        }

    // Requirement: B5
    @Test
    fun `Doesn't find an alias that doesn't exist`() =
        test { client ->
            val response = client.get("/no-such-alias")

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals(Problem("urn:problem:url-shortener:not-found", "The alias was not found", 404), response.problem())
        }

    // Requirements: B5, R1, R2
    @ParameterizedTest
    @ValueSource(strings = ["ab", "a.b.c", "a%20b", "shorten", "Api", "V1"])
    fun `Doesn't find what can't be an alias, without looking it up`(path: String) =
        test { client ->
            val response = client.get("/$path")

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("urn:problem:url-shortener:not-found", response.problem().type)
            assertEquals(0, repository.lookups.get())
        }

    @Test
    fun `Shows a browser a page, not a problem, for an alias that doesn't exist`() =
        test { client ->
            val response =
                client.get("/no-such-alias") {
                    header(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            assertTrue("Link not found" in response.bodyAsText())
        }

    @Test
    fun `Fails with service-unavailable, and no internal detail, while the database is down`() =
        test { client ->
            store("my-alias", "https://example.com/")
            repository.unavailable = true

            val response = client.get("/my-alias")
            val requestId = response.headers[HttpHeaders.XRequestId]

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
            assertEquals(
                Problem(
                    "urn:problem:url-shortener:service-unavailable",
                    "The service is temporarily unavailable",
                    503,
                    "Try again later. Quote request ID $requestId when reporting this.",
                ),
                response.problem(),
            )
            assertFalse(response.bodyAsText().contains("Secret"))
        }

    @Test
    fun `Redirects again once the database is back`() =
        test { client ->
            store("my-alias", "https://example.com/")
            repository.unavailable = true
            client.get("/my-alias")
            repository.unavailable = false

            assertEquals(HttpStatusCode.Found, client.get("/my-alias").status)
        }

    private fun store(
        alias: String,
        destinationUrl: String,
    ) {
        repository.stored[Alias.parse(alias).valid()] = DestinationUrl.parse(destinationUrl, publicBaseUrl).valid()
    }

    private suspend fun HttpResponse.problem(): Problem {
        assertEquals(ContentType.Application.ProblemJson, contentType()?.withoutParameters())
        return Json.decodeFromString(bodyAsText())
    }
}
