package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.InMemoryShortUrlRepository
import com.tpximpact.urlshortener.domain.valid
import com.tpximpact.urlshortener.ui.apiTest
import com.tpximpact.urlshortener.ui.publicBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ListRouteTest {
    private val repository = InMemoryShortUrlRepository()

    private fun test(block: suspend (client: HttpClient) -> Unit) = apiTest(repository, block = block)

    // Requirement: B7
    @Test
    fun `Lists every short URL, with its alias, destination URL, and short URL`() =
        test { client ->
            store("my-alias", "https://example.com/very/long/url")

            val response = client.get("/urls")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())
            assertEquals(
                Json.parseToJsonElement(
                    """
                    [{
                      "alias": "my-alias",
                      "destinationUrl": "https://example.com/very/long/url",
                      "shortUrl": "http://localhost:8080/my-alias"
                    }]
                    """,
                ),
                response.items(),
            )
        }

    // Requirement: B7
    @Test
    fun `Lists nothing when nothing is stored`() =
        test { client ->
            val response = client.get("/urls")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(JsonArray(emptyList()), response.items())
        }

    // Requirement: R11
    @Test
    fun `Lists the newest first, and those created at the same moment by alias`() =
        test { client ->
            store("oldest", "https://example.com/", Instant.ofEpochSecond(1))
            store("tie-b", "https://example.com/", Instant.ofEpochSecond(2))
            store("Tie-c", "https://example.com/", Instant.ofEpochSecond(2))
            store("tie-a", "https://example.com/", Instant.ofEpochSecond(2))
            store("newest", "https://example.com/", Instant.ofEpochSecond(3))

            val aliases = client.get("/urls").items().map { it.jsonObject["alias"]!!.jsonPrimitive.content }

            assertEquals(listOf("newest", "Tie-c", "tie-a", "tie-b", "oldest"), aliases)
        }

    @Test
    fun `Fails with service-unavailable while the database is down`() =
        test { client ->
            repository.unavailable = true

            val response = client.get("/urls")

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }

    private fun store(
        alias: String,
        destinationUrl: String,
        createdAt: Instant = Instant.now(),
    ) {
        val key = Alias.parse(alias).valid()
        repository.stored[key] = DestinationUrl.parse(destinationUrl, publicBaseUrl).valid()
        repository.createdAt[key] = createdAt
    }

    private suspend fun HttpResponse.items() = Json.parseToJsonElement(bodyAsText()).jsonArray
}
