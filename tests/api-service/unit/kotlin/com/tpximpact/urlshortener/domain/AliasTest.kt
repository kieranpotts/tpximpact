package com.tpximpact.urlshortener.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AliasTest {
    // Requirement: R1
    @ParameterizedTest
    @ValueSource(strings = ["abc", "my-custom-alias", "My_Link_2026", "0-_", "aZ3k9Qx"])
    fun `Accepts 3 to 64 letters, digits, hyphens, and underscores`(value: String) {
        assertEquals(value, Alias.parse(value).valid().value)
    }

    // Requirement: R1
    @Test
    fun `Accepts 64 characters, but no more`() {
        assertEquals(Parsed.Valid::class, Alias.parse("a".repeat(64))::class)
        assertEquals(Parsed.Invalid(Violation.ALIAS_INVALID_LENGTH), Alias.parse("a".repeat(65)))
    }

    // Requirements: R1, R6
    @ParameterizedTest
    @ValueSource(strings = ["", "a", "ab"])
    fun `Rejects fewer than 3 characters, including none`(value: String) {
        assertEquals(Parsed.Invalid(Violation.ALIAS_INVALID_LENGTH), Alias.parse(value))
    }

    // Requirement: R1
    @ParameterizedTest
    @ValueSource(strings = ["has space", "dot.ted", "sl/ash", "per%20cent", "café", "emoji-😀", "tab\there", "abc\n"])
    fun `Rejects any other character`(value: String) {
        assertEquals(Parsed.Invalid(Violation.ALIAS_INVALID_CHARACTERS), Alias.parse(value))
    }

    // Requirement: R2
    @ParameterizedTest
    @ValueSource(strings = ["shorten", "urls", "api", "docs", "health", "metrics", "static", "assets", "SHORTEN", "Urls"])
    fun `Rejects reserved words, whatever their case`(value: String) {
        assertEquals(Parsed.Invalid(Violation.ALIAS_RESERVED), Alias.parse(value))
    }

    // Requirement: R2
    @ParameterizedTest
    @ValueSource(strings = ["v10", "V10", "v123", "v2026", "v000"])
    fun `Rejects version segments, whatever their case`(value: String) {
        assertEquals(Parsed.Invalid(Violation.ALIAS_RESERVED), Alias.parse(value))
    }

    // Requirement: R2
    @ParameterizedTest
    @ValueSource(strings = ["shortened", "my-api", "urls2", "v1a", "v-10", "vv10", "v10x", "version1"])
    fun `Accepts words that only contain a reserved word`(value: String) {
        assertEquals(value, Alias.parse(value).valid().value)
    }

    // Requirement: R3
    @Test
    fun `Is case-sensitive`() {
        assertNotEquals(Alias.parse("MyLink").valid(), Alias.parse("mylink").valid())
    }
}

fun <T> Parsed<T>.valid(): T =
    when (this) {
        is Parsed.Valid -> value
        is Parsed.Invalid -> throw AssertionError("Expected a valid value, but it breaks $violation")
    }
