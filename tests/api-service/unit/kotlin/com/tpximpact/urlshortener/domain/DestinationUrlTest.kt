package com.tpximpact.urlshortener.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI

// Requirement: R5
class DestinationUrlTest {
    private val publicBaseUrl = PublicBaseUrl(URI("http://sho.rt:8080"))

    private fun parse(value: String) = DestinationUrl.parse(value, publicBaseUrl)

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://example.com",
            "http://example.com/very/long/url",
            "HTTPS://EXAMPLE.COM/Path?q=1&r=two#fragment",
            "https://example.com:8443/",
            "http://127.0.0.1/",
            "http://[2001:db8::1]/",
            "https://xn--bcher-kva.example/",
            "https://example.com/path%20with%20escapes",
            "https://sub.sho.rt/",
        ],
    )
    fun `Accepts an absolute http or https URL with a host`(value: String) {
        assertEquals(value, parse(value).valid().value)
    }

    // Requirement: R7
    @Test
    fun `Trims surrounding whitespace, and changes nothing else`() {
        assertEquals("HTTPS://Example.com:443/a/../b?x=%7E#Top", parse(" \t\nHTTPS://Example.com:443/a/../b?x=%7E#Top \r\n").valid().value)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t\n"])
    fun `Requires a URL`(value: String) {
        assertEquals(Parsed.Invalid(Violation.URL_REQUIRED), parse(value))
    }

    @Test
    fun `Accepts 2,048 characters, but no more`() {
        val prefix = "https://example.com/"
        val longest = prefix + "a".repeat(2_048 - prefix.length)

        assertEquals(longest, parse("  $longest  ").valid().value)
        assertEquals(Parsed.Invalid(Violation.URL_TOO_LONG), parse(longest + "a"))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "javascript:alert(1)",
            "data:text/html,hello",
            "ftp://example.com/file",
            "file:///etc/passwd",
            "mailto:someone@example.com",
            "example.com",
            "//example.com/path",
            "/relative/path",
        ],
    )
    fun `Rejects any scheme but http or https, or none`(value: String) {
        assertEquals(Parsed.Invalid(Violation.URL_UNSUPPORTED_SCHEME), parse(value))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://example.com/has space",
            "https://example.com/\r\nSet-Cookie: x=y",
            "https://example.com/\u0000",
            "https://exa mple.com/",
            "https://example.com/<>",
            "https://example.com/%zz",
            "http:",
            "https://",
            "http:example.com",
            "https:///path",
            "https://bücher.example/",
            "https://example.com/café",
            "https://example.com/\uD800",
            "https://example.com/\u007F",
            "https://example.com/\u00A0x",
            "https://under_score.example/",
            "https://example.com:99999999999/",
        ],
    )
    fun `Rejects a URL that doesn't parse, or has no host`(value: String) {
        assertEquals(Parsed.Invalid(Violation.URL_INVALID), parse(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://user:pass@example.com/", "https://user@example.com/", "https://:@example.com/"])
    fun `Rejects a URL with a user name or password`(value: String) {
        assertEquals(Parsed.Invalid(Violation.URL_HAS_CREDENTIALS), parse(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["http://sho.rt:8080/abc", "https://sho.rt/", "http://SHO.RT:9000/", "http://sho.rt./abc"])
    fun `Rejects a URL on the public base URL's host, on any port`(value: String) {
        assertEquals(Parsed.Invalid(Violation.URL_SELF_REFERENTIAL), parse(value))
    }
}
