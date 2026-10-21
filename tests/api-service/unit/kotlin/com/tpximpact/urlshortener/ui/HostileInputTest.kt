package com.tpximpact.urlshortener.ui

import com.tpximpact.urlshortener.domain.InMemoryShortUrlRepository
import com.tpximpact.urlshortener.ui.routes.shorten
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

// Requirement: Q2

/**
 * However malformed the input, the API service never answers with a `5xx`.
 * Every response is validated against the contract too, so each one is
 * documented.
 */
class HostileInputTest {
    companion object {
        private const val URL = "https://example.com/"

        @JvmStatic
        fun bodies() =
            listOf(
                // Deeply nested JSON, up to the size limit.
                "[".repeat(8_000) + "]".repeat(8_000),
                """{"a":""".repeat(2_700) + "1" + "}".repeat(2_700),
                """{"destinationUrl": ${"[".repeat(5_000)}${"]".repeat(5_000)}}""",
                // Numbers that don't fit any machine type.
                """{"destinationUrl": 1e999999999}""",
                """{"destinationUrl": ${"9".repeat(15_000)}}""",
                // Strings that are long, or full of escapes and odd characters.
                """{"destinationUrl": "$URL${"a".repeat(15_000)}"}""",
                """{"destinationUrl": "${"\\u0000".repeat(2_500)}"}""",
                """{"destinationUrl": "$URL\u0000", "customAlias": "nul\u0000"}""",
                """{"destinationUrl": "$URL\ud800", "customAlias": "\udfff\ud800"}""",
                """{"destinationUrl": "$URL\r\nSet-Cookie: x=y"}""",
                """{"destinationUrl": "$URL%0d%0aSet-Cookie:%20x=y", "customAlias": "crlf-encoded"}""",
                """{"destinationUrl": "https://example.com/😀", "customAlias": "😀😀😀"}""",
                """{"destinationUrl": "https://${"a".repeat(300)}.example/"}""",
                """{"destinationUrl": "https://[::1/"}""",
                """{"destinationUrl": "https://example.com:-1/"}""",
                """{"destinationUrl": "http://%41%41%41/"}""",
                // Values of the wrong type.
                """{"destinationUrl": {"${'$'}ne": null}, "customAlias": {"${'$'}gt": ""}}""",
                """{"destinationUrl": true, "customAlias": false}""",
                """{"destinationUrl": "$URL", "customAlias": ["abc"]}""",
                """{"destinationUrl": "$URL", "customAlias": 1234}""",
                // Duplicate and odd keys.
                """{"destinationUrl": "$URL", "destinationUrl": "javascript:alert(1)"}""",
                """{"": "", "\u0000": null}""",
                // Not JSON, or not quite.
                "{\"destinationUrl\": \"$URL\"}",
                "{'destinationUrl': '$URL'}",
                "{\"destinationUrl\": \"$URL\"} trailing",
                "{\"destinationUrl\": \"$URL\"}{}",
                "/* comment */ {}",
                "NaN",
                "\u0000",
            )

        @JvmStatic
        fun aliasPaths() =
            listOf(
                "/" + "a".repeat(10_000),
                "/%00%00%00",
                "/abc%0d%0aSet-Cookie:%20x=y",
                "/%F0%9F%98%80%F0%9F%98%80",
                "/%C3%28%C3%28",
                "/%2e%2e%2fetc%2fpasswd",
                "/abc%2Fdef",
                "/abc;def",
                "/'%20OR%201=1--",
            )
    }

    private val repository = InMemoryShortUrlRepository()

    @ParameterizedTest
    @MethodSource("bodies")
    fun `A hostile body never gets a server error`(body: String) =
        apiTest(repository) { client ->
            val response = client.shorten(body)

            assertTrue(response.status.value < 500, "Got ${response.status}")
        }

    @Test
    fun `A body that isn't UTF-8 gets a client error`() =
        apiTest(repository) { client ->
            val body = byteArrayOf(*"""{"destinationUrl": "$URL""".toByteArray(), 0xC3.toByte(), 0x28, '"'.code.toByte(), '}'.code.toByte())

            val response =
                client.post("/shorten") {
                    headers.append("Content-Type", "application/json")
                    setBody(body)
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(0, repository.stored.size)
        }

    @Test
    fun `An oversized body of unknown length gets a client error`() =
        apiTest(repository) { client ->
            val response =
                client.post("/shorten") {
                    setBody(
                        object : OutgoingContent.WriteChannelContent() {
                            override val contentType = ContentType.Application.Json

                            override suspend fun writeTo(channel: ByteWriteChannel) {
                                channel.writeFully("""{"destinationUrl": "$URL", "padding": """".toByteArray())
                                repeat(1_024) { channel.writeFully(ByteArray(1_024) { 'a'.code.toByte() }) }
                                channel.writeFully("\"}".toByteArray())
                            }
                        },
                    )
                }

            assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
            assertEquals(0, repository.stored.size)
        }

    @ParameterizedTest
    @MethodSource("aliasPaths")
    fun `A hostile alias never gets a server error, nor a lookup`(path: String) =
        apiTest(repository) { client ->
            val response = client.get(path)

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals(0, repository.lookups.get())
        }

    @ParameterizedTest
    @MethodSource("aliasPaths")
    fun `A hostile alias to delete never gets a server error, nor a lookup`(path: String) =
        apiTest(repository) { client ->
            val response = client.delete(path)

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals(0, repository.lookups.get())
        }

    @Test
    fun `A GET of a fixed route's path gets a not-found problem`() =
        apiTest(repository) { client ->
            val response = client.get("/shorten")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
}
