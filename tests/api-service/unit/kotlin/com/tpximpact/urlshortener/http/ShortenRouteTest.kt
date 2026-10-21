package com.tpximpact.urlshortener.http

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.valid
import com.tpximpact.urlshortener.service.InMemoryShortUrlRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class ShortenRouteTest {
    private val repository = InMemoryShortUrlRepository()

    private fun test(block: suspend (client: HttpClient) -> Unit) = apiTest(repository, block = block)

    // Requirement: B1
    @Test
    fun `Shortens a URL with a random alias`() =
        test { client ->
            val response = client.shorten("""{"destinationUrl": "https://example.com/very/long/url"}""")

            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())
            val alias = repository.stored.keys.single()
            assertEquals("http://localhost:8080/${alias.value}", response.shortUrl())
            assertEquals("https://example.com/very/long/url", repository.stored[alias]?.value)
        }

    // Requirements: B1, R6
    @Test
    fun `Shortens a URL with a random alias, when the custom alias is null`() =
        test { client ->
            val response = client.shorten("""{"destinationUrl": "https://example.com/", "customAlias": null}""")

            assertEquals(HttpStatusCode.Created, response.status)
            assertTrue(response.shortUrl().matches(Regex("http://localhost:8080/[A-Za-z0-9]{7}")))
        }

    // Requirement: B2
    @Test
    fun `Shortens a URL with a custom alias`() =
        test { client ->
            val response = client.shorten("""{"destinationUrl": "https://example.com/", "customAlias": "my-custom-alias"}""")

            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals("http://localhost:8080/my-custom-alias", response.shortUrl())
            assertEquals("https://example.com/", repository.stored[alias("my-custom-alias")]?.value)
        }

    // Requirement: R7
    @Test
    fun `Stores the destination URL trimmed, and otherwise as given`() =
        test { client ->
            client.shorten("""{"destinationUrl": "  HTTPS://Example.COM/a/../b?x=%7E#Top\n", "customAlias": "as-given"}""")

            assertEquals("HTTPS://Example.COM/a/../b?x=%7E#Top", repository.stored[alias("as-given")]?.value)
        }

    // Requirement: R8
    @Test
    fun `Makes a new short URL every time, for the same destination URL`() =
        test { client ->
            val first = client.shorten("""{"destinationUrl": "https://example.com/"}""")
            val second = client.shorten("""{"destinationUrl": "https://example.com/"}""")

            assertTrue(first.shortUrl() != second.shortUrl())
            assertEquals(2, repository.stored.size)
        }

    @Test
    fun `Ignores properties that the contract doesn't define`() =
        test { client ->
            val response = client.shorten("""{"destinationUrl": "https://example.com/", "expiry": "never"}""")

            assertEquals(HttpStatusCode.Created, response.status)
        }

    @Test
    fun `Accepts JSON with a charset`() =
        test { client ->
            val response =
                client.shorten("""{"destinationUrl": "https://example.com/"}""", contentType = "application/json; charset=utf-8")

            assertEquals(HttpStatusCode.Created, response.status)
        }

    // Requirement: B4
    @Test
    fun `Rejects an alias that's taken, and leaves it unchanged`() =
        test { client ->
            client.shorten("""{"destinationUrl": "https://first.example/", "customAlias": "taken"}""")

            val response = client.shorten("""{"destinationUrl": "https://second.example/", "customAlias": "taken"}""")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(Problem("urn:problem:url-shortener:alias-taken", "The alias is already taken", 400), response.problem())
            assertEquals("https://first.example/", repository.stored[alias("taken")]?.value)
        }

    // Requirement: R3
    @Test
    fun `Treats aliases that differ only in case as different`() =
        test { client ->
            val first = client.shorten("""{"destinationUrl": "https://example.com/", "customAlias": "MyLink"}""")
            val second = client.shorten("""{"destinationUrl": "https://example.com/", "customAlias": "mylink"}""")

            assertEquals(HttpStatusCode.Created, first.status)
            assertEquals(HttpStatusCode.Created, second.status)
        }

    // Requirements: B3, R1, R2, R5, R6
    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = [
            "{} | destinationUrl | url-required",
            "{\"destinationUrl\": null} | destinationUrl | url-required",
            "{\"destinationUrl\": \"  \"} | destinationUrl | url-required",
            "{\"destinationUrl\": 42} | destinationUrl | url-invalid",
            "{\"destinationUrl\": [\"https://example.com/\"]} | destinationUrl | url-invalid",
            "{\"destinationUrl\": \"https://exa mple.com/\"} | destinationUrl | url-invalid",
            "{\"destinationUrl\": \"javascript:alert(1)\"} | destinationUrl | url-unsupported-scheme",
            "{\"destinationUrl\": \"example.com\"} | destinationUrl | url-unsupported-scheme",
            "{\"destinationUrl\": \"https://user:pass@example.com/\"} | destinationUrl | url-has-credentials",
            "{\"destinationUrl\": \"http://localhost:8080/abc\"} | destinationUrl | url-self-referential",
            "{\"destinationUrl\": \"https://example.com/\", \"customAlias\": \"\"} | customAlias | alias-invalid-length",
            "{\"destinationUrl\": \"https://example.com/\", \"customAlias\": \"ab\"} | customAlias | alias-invalid-length",
            "{\"destinationUrl\": \"https://example.com/\", \"customAlias\": \"a.b.c\"} | customAlias | alias-invalid-characters",
            "{\"destinationUrl\": \"https://example.com/\", \"customAlias\": 123} | customAlias | alias-invalid-characters",
            "{\"destinationUrl\": \"https://example.com/\", \"customAlias\": \"URLS\"} | customAlias | alias-reserved",
        ],
    )
    fun `Rejects an invalid field, saying which and why, and stores nothing`(
        body: String,
        field: String,
        code: String,
    ) = test { client ->
        val response = client.shorten(body)

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val problem = response.problem()
        assertEquals("urn:problem:url-shortener:validation-failed", problem.type)
        assertEquals("The request has 1 invalid field.", problem.detail)
        assertEquals(listOf(field to code), problem.errors?.map { it.field to it.code })
        assertTrue(
            problem.errors!!
                .single()
                .message
                .isNotBlank(),
        )
        assertEquals(0, repository.stored.size)
    }

    // Requirement: B3
    @Test
    fun `Lists every invalid field`() =
        test { client ->
            val response = client.shorten("""{"destinationUrl": "ftp://example.com/", "customAlias": "ab"}""")

            assertEquals(
                Problem(
                    "urn:problem:url-shortener:validation-failed",
                    "The request is not valid",
                    400,
                    "The request has 2 invalid fields.",
                    listOf(
                        FieldError("destinationUrl", "url-unsupported-scheme", "Enter a URL starting with http:// or https://"),
                        FieldError("customAlias", "alias-invalid-length", "Alias must be between 3 and 64 characters"),
                    ),
                ),
                response.problem(),
            )
        }

    // Requirement: B3
    @ParameterizedTest
    @ValueSource(
        strings = ["", "not json", "{", """{"destinationUrl": "https://example.com/",}""", "[]", "\"https://example.com/\"", "null", "42"],
    )
    fun `Rejects a body that isn't a JSON object, and stores nothing`(body: String) =
        test { client ->
            val response = client.shorten(body)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("urn:problem:url-shortener:malformed-request", response.problem().type)
            assertEquals(0, repository.stored.size)
        }

    // Requirement: B12
    @ParameterizedTest
    @ValueSource(strings = ["text/plain", "application/x-www-form-urlencoded", "application/jsonx", "application/problem+json"])
    fun `Rejects any content type but JSON, and stores nothing`(contentType: String) =
        test { client ->
            val response = client.shorten("""{"destinationUrl": "https://example.com/"}""", contentType)

            assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
            assertEquals("application/json", response.headers["Accept-Post"])
            assertEquals(
                Problem("urn:problem:url-shortener:unsupported-media-type", "The content type is not supported", 415),
                response.problem(),
            )
            assertEquals(0, repository.stored.size)
        }

    // Requirement: B12
    @Test
    fun `Rejects a body with no content type`() =
        test { client ->
            val response = client.shorten("""{"destinationUrl": "https://example.com/"}""", contentType = null)

            assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
        }

    // Requirement: B13
    @Test
    fun `Accepts a body of 16 KiB`() =
        test { client ->
            val response = client.shorten(padded("""{"destinationUrl": "https://example.com/"}""", MAX_BODY_BYTES))

            assertEquals(HttpStatusCode.Created, response.status)
        }

    // Requirement: B13
    @Test
    fun `Rejects a body larger than 16 KiB, and stores nothing`() =
        test { client ->
            val response = client.shorten(padded("""{"destinationUrl": "https://example.com/"}""", MAX_BODY_BYTES + 1))

            assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
            assertEquals(
                Problem("urn:problem:url-shortener:payload-too-large", "The request body is too large", 413),
                response.problem(),
            )
            assertEquals(0, repository.stored.size)
        }

    @Test
    fun `Fails with an internal error when every random alias is taken`() =
        apiTest(repository, aliasGenerator = { alias("taken01") }) { client ->
            client.shorten("""{"destinationUrl": "https://example.com/", "customAlias": "taken01"}""")

            val response = client.shorten("""{"destinationUrl": "https://example.com/"}""")

            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("urn:problem:url-shortener:internal-error", response.problem().type)
        }

    private fun alias(value: String) = Alias.parse(value).valid()

    /** Pads a JSON object with whitespace, to exactly [size] bytes. */
    private fun padded(
        json: String,
        size: Int,
    ) = json.dropLast(1) + " ".repeat(size - json.length) + "}"

    private suspend fun HttpResponse.shortUrl() =
        Json
            .parseToJsonElement(bodyAsText())
            .jsonObject["shortUrl"]!!
            .jsonPrimitive.content

    private suspend fun HttpResponse.problem(): Problem {
        assertEquals(ContentType.Application.ProblemJson, contentType()?.withoutParameters())
        return Json.decodeFromString(bodyAsText())
    }
}

suspend fun HttpClient.shorten(
    body: String,
    contentType: String? = "application/json",
) = post("/shorten") {
    // Set as a raw header, so that a test can send a malformed one.
    if (contentType != null) headers.append(HttpHeaders.ContentType, contentType)
    setBody(body)
}
