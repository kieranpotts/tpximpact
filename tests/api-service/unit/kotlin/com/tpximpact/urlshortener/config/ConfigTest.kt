package com.tpximpact.urlshortener.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI

class ConfigTest {
    private val valid =
        mapOf(
            "PUBLIC_BASE_URL" to "http://localhost:8080",
            "DATABASE_URL" to "jdbc:postgresql://db:5432/url_shortener",
            "DATABASE_USER" to "url_shortener",
            "DATABASE_PASSWORD" to "secret",
        )

    @Test
    fun `Reads every setting`() {
        val config = Config.fromEnvironment(valid)

        assertEquals(URI("http://localhost:8080"), config.publicBaseUrl)
        assertEquals(
            DatabaseConfig("jdbc:postgresql://db:5432/url_shortener", "url_shortener", "secret"),
            config.database,
        )
    }

    @Test
    fun `Drops a trailing slash from the public base URL`() {
        val config = Config.fromEnvironment(valid + ("PUBLIC_BASE_URL" to "https://sho.rt/"))

        assertEquals(URI("https://sho.rt"), config.publicBaseUrl)
    }

    @Test
    fun `Lists every missing setting`() {
        val e = assertThrows<ConfigException> { Config.fromEnvironment(mapOf("DATABASE_USER" to " ")) }

        assertEquals(
            listOf(
                "PUBLIC_BASE_URL is not set.",
                "DATABASE_URL is not set.",
                "DATABASE_USER is not set.",
                "DATABASE_PASSWORD is not set.",
            ),
            e.problems,
        )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "localhost:8080",
            "ftp://localhost",
            "http://",
            "http://user:pass@localhost",
            "http://localhost/path",
            "http://localhost?query",
            "http://localhost#fragment",
            "http://local host",
        ],
    )
    fun `Rejects an invalid public base URL`(value: String) {
        val e = assertThrows<ConfigException> { Config.fromEnvironment(valid + ("PUBLIC_BASE_URL" to value)) }

        assertEquals(1, e.problems.size)
        assertEquals(true, e.problems.single().startsWith("PUBLIC_BASE_URL must be"))
    }

    @Test
    fun `Rejects a database URL that isn't a PostgreSQL JDBC URL`() {
        val e =
            assertThrows<ConfigException> {
                Config.fromEnvironment(valid + ("DATABASE_URL" to "postgres://db:5432/url_shortener"))
            }

        assertEquals(1, e.problems.size)
        assertEquals(true, e.problems.single().startsWith("DATABASE_URL must be"))
    }

    @Test
    fun `Never shows the database password`() {
        val config = Config.fromEnvironment(valid)

        assertFalse(config.toString().contains("secret"))
    }
}
