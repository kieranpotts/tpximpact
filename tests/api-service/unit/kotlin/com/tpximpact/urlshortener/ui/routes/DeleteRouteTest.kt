package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.InMemoryShortUrlRepository
import com.tpximpact.urlshortener.domain.valid
import com.tpximpact.urlshortener.ui.Problem
import com.tpximpact.urlshortener.ui.apiTest
import com.tpximpact.urlshortener.ui.publicBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
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

class DeleteRouteTest {
    private val repository = InMemoryShortUrlRepository()

    private fun test(block: suspend (client: HttpClient) -> Unit) = apiTest(repository, block = block)

    // Requirement: B6
    @Test
    fun `Deletes a short URL, which then stops redirecting`() =
        test { client ->
            store("my-alias", "https://example.com/")

            val response = client.delete("/my-alias")

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertEquals("", response.bodyAsText())
            assertTrue(repository.stored.isEmpty())
            assertEquals(HttpStatusCode.NotFound, client.get("/my-alias").status)
        }

    // Requirement: B6
    @Test
    fun `Deletes only the short URL with the alias given`() =
        test { client ->
            store("my-alias", "https://example.com/")
            store("other", "https://example.com/")

            client.delete("/my-alias")

            assertEquals(setOf(Alias.parse("other").valid()), repository.stored.keys)
        }

    // Requirement: R10
    @Test
    fun `Frees a deleted alias for reuse, straight away`() =
        test { client ->
            store("reused", "https://first.example/")
            client.delete("/reused")

            val response = client.shorten("""{"destinationUrl": "https://second.example/", "customAlias": "reused"}""")

            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals("https://second.example/", client.get("/reused").headers[HttpHeaders.Location])
        }

    // Requirement: R3
    @Test
    fun `Deletes only an alias typed in the case it was created in`() =
        test { client ->
            store("MyLink", "https://example.com/")

            assertEquals(HttpStatusCode.NotFound, client.delete("/mylink").status)
            assertEquals(HttpStatusCode.NoContent, client.delete("/MyLink").status)
        }

    // Requirement: B6
    @Test
    fun `Doesn't find an alias that doesn't exist`() =
        test { client ->
            val response = client.delete("/no-such-alias")

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals(Problem("urn:problem:url-shortener:not-found", "The alias was not found", 404), response.problem())
        }

    // Requirement: B6
    @Test
    fun `Doesn't find an alias that's already deleted`() =
        test { client ->
            store("my-alias", "https://example.com/")
            client.delete("/my-alias")

            assertEquals(HttpStatusCode.NotFound, client.delete("/my-alias").status)
        }

    @Test
    fun `Answers a browser with a problem, not a page, for an alias that doesn't exist`() =
        test { client ->
            val response = client.delete("/no-such-alias") { header(HttpHeaders.Accept, "text/html") }

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("urn:problem:url-shortener:not-found", response.problem().type)
        }

    // Requirements: B6, R1, R2
    @ParameterizedTest
    @ValueSource(strings = ["ab", "a.b.c", "a%20b", "shorten", "Urls", "V1"])
    fun `Doesn't find what can't be an alias, without looking it up`(path: String) =
        test { client ->
            val response = client.delete("/$path")

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("urn:problem:url-shortener:not-found", response.problem().type)
            assertEquals(0, repository.lookups.get())
        }

    @Test
    fun `Fails with service-unavailable, and deletes nothing, while the database is down`() =
        test { client ->
            store("my-alias", "https://example.com/")
            repository.unavailable = true

            val response = client.delete("/my-alias")

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
            assertFalse(response.bodyAsText().contains("Secret"))
            assertEquals(1, repository.stored.size)
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
