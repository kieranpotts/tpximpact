package com.tpximpact.urlshortener.infra.persistence

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.Parsed
import com.tpximpact.urlshortener.domain.PublicBaseUrl
import com.tpximpact.urlshortener.domain.ShortUrl
import com.tpximpact.urlshortener.domain.StorageUnavailableException
import com.tpximpact.urlshortener.infra.config.DatabaseConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.net.URI
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

@Testcontainers
class JdbcShortUrlRepositoryTest {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:18")

        private lateinit var dataSource: HikariDataSource

        @BeforeAll
        @JvmStatic
        fun migrateAndPool() {
            val config = DatabaseConfig(postgres.jdbcUrl, postgres.username, postgres.password)
            migrate(config)
            dataSource = createDataSource(config)
        }

        @AfterAll
        @JvmStatic
        fun closePool() = dataSource.close()

        private val publicBaseUrl = PublicBaseUrl(URI("http://sho.rt"))

        private fun alias(value: String) = (Alias.parse(value) as Parsed.Valid).value

        private fun url(value: String) = (DestinationUrl.parse(value, publicBaseUrl) as Parsed.Valid).value
    }

    private val repository = JdbcShortUrlRepository(dataSource)

    @BeforeEach
    fun emptyTable() {
        dataSource.connection.use { it.createStatement().use { s -> s.execute("TRUNCATE short_url") } }
    }

    /** Every stored row, as alias to destination URL. */
    private fun rows(): Map<String, String> =
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT alias, full_url FROM short_url").use { rs ->
                    buildMap { while (rs.next()) put(rs.getString(1), rs.getString(2)) }
                }
            }
        }

    @Test
    fun `Stores a short URL`() =
        runBlocking {
            assertTrue(repository.insert(alias("my-alias"), url("https://example.com/")))

            assertEquals(mapOf("my-alias" to "https://example.com/"), rows())
        }

    // Requirement: R7
    @Test
    fun `Stores the destination URL exactly as given`() =
        runBlocking {
            val longest = "https://example.com/?q=" + "%7E".repeat((2_048 - 23) / 3)
            repository.insert(alias("odd"), url("HTTPS://Example.COM:443/a/../b?x=%7E&y=1#Top"))
            repository.insert(alias("longest"), url(longest))

            assertEquals(mapOf("odd" to "HTTPS://Example.COM:443/a/../b?x=%7E&y=1#Top", "longest" to longest), rows())
        }

    // Requirement: B4
    @Test
    fun `Refuses an alias that's taken, and leaves it unchanged`() =
        runBlocking {
            repository.insert(alias("taken"), url("https://first.example/"))

            assertFalse(repository.insert(alias("taken"), url("https://second.example/")))
            assertEquals(mapOf("taken" to "https://first.example/"), rows())
        }

    // Requirement: R3
    @Test
    fun `Treats aliases that differ only in case as different`() =
        runBlocking {
            assertTrue(repository.insert(alias("MyLink"), url("https://upper.example/")))
            assertTrue(repository.insert(alias("mylink"), url("https://lower.example/")))

            assertEquals(mapOf("MyLink" to "https://upper.example/", "mylink" to "https://lower.example/"), rows())
        }

    @Test
    fun `Stores the longest alias`() =
        runBlocking {
            assertTrue(repository.insert(alias("a".repeat(64)), url("https://example.com/")))
        }

    @Test
    fun `Lets exactly one of many concurrent inserts of one alias succeed`() =
        runBlocking(Dispatchers.IO) {
            val results =
                List(20) { i -> async { repository.insert(alias("raced"), url("https://example.com/$i")) } }.awaitAll()

            assertEquals(1, results.count { it })
            assertEquals(1, rows().size)
        }

    // Requirement: B5
    @Test
    fun `Finds the destination URL of a stored alias`() =
        runBlocking {
            repository.insert(alias("my-alias"), url("https://example.com/"))

            assertEquals(url("https://example.com/"), repository.find(alias("my-alias")))
        }

    // Requirement: B5
    @Test
    fun `Finds nothing for an alias that isn't stored`() =
        runBlocking {
            assertNull(repository.find(alias("my-alias")))
        }

    // Requirement: R3
    @Test
    fun `Finds an alias only in the case it was stored in`() =
        runBlocking {
            repository.insert(alias("MyLink"), url("https://example.com/"))

            assertNull(repository.find(alias("mylink")))
        }

    // Requirement: R7
    @Test
    fun `Finds the destination URL exactly as stored`() =
        runBlocking {
            repository.insert(alias("odd"), url("HTTPS://Example.COM:443/a/../b?x=%7E&y=1#Top"))

            assertEquals("HTTPS://Example.COM:443/a/../b?x=%7E&y=1#Top", repository.find(alias("odd"))?.value)
        }

    // Requirement: B6
    @Test
    fun `Deletes a stored short URL`() =
        runBlocking {
            repository.insert(alias("my-alias"), url("https://example.com/"))
            repository.insert(alias("other"), url("https://example.com/"))

            assertTrue(repository.delete(alias("my-alias")))
            assertNull(repository.find(alias("my-alias")))
            assertEquals(setOf("other"), rows().keys)
        }

    // Requirement: B6
    @Test
    fun `Deletes nothing for an alias that isn't stored`() =
        runBlocking {
            assertFalse(repository.delete(alias("my-alias")))
        }

    // Requirement: R3
    @Test
    fun `Deletes an alias only in the case it was stored in`() =
        runBlocking {
            repository.insert(alias("MyLink"), url("https://example.com/"))

            assertFalse(repository.delete(alias("mylink")))
            assertEquals(setOf("MyLink"), rows().keys)
        }

    // Requirement: R10
    @Test
    fun `Stores a deleted alias again, straight away`() =
        runBlocking {
            repository.insert(alias("reused"), url("https://first.example/"))
            repository.delete(alias("reused"))

            assertTrue(repository.insert(alias("reused"), url("https://second.example/")))
            assertEquals(url("https://second.example/"), repository.find(alias("reused")))
        }

    // Requirement: B7
    @Test
    fun `Lists every stored short URL`() =
        runBlocking {
            val before = Instant.now().minusSeconds(60)
            repository.insert(alias("my-alias"), url("HTTPS://Example.COM/a/../b"))

            val listed = repository.list().single()

            assertEquals(alias("my-alias"), listed.alias)
            assertEquals("HTTPS://Example.COM/a/../b", listed.destinationUrl.value)
            assertTrue(listed.createdAt.isAfter(before), "Created at ${listed.createdAt}")
        }

    // Requirement: B7
    @Test
    fun `Lists nothing when nothing is stored`() =
        runBlocking {
            assertEquals(emptyList<ShortUrl>(), repository.list())
        }

    // Requirement: R11
    @Test
    fun `Lists the newest first`() =
        runBlocking {
            repository.insert(alias("first"), url("https://example.com/"))
            repository.insert(alias("second"), url("https://example.com/"))
            repository.insert(alias("third"), url("https://example.com/"))

            assertEquals(listOf("third", "second", "first"), repository.list().map { it.alias.value })
        }

    // Requirement: R11
    @Test
    fun `Lists those created at the same moment by alias, character by character`() =
        runBlocking {
            // Inserted together, so that they share a moment. A locale's
            // collation would put `Tie-c` among the others, or ignore the
            // hyphens.
            dataSource.connection.use { connection ->
                connection.createStatement().use {
                    it.execute(
                        "INSERT INTO short_url (alias, full_url, created_at) VALUES " +
                            "('tie_b', 'https://example.com/', '2026-01-01T00:00:00Z'), " +
                            "('Tie-c', 'https://example.com/', '2026-01-01T00:00:00Z'), " +
                            "('tie-a', 'https://example.com/', '2026-01-01T00:00:00Z'), " +
                            "('tiea', 'https://example.com/', '2026-01-01T00:00:00Z'), " +
                            "('older', 'https://example.com/', '2025-01-01T00:00:00Z')",
                    )
                }
            }

            val listed = repository.list()

            assertEquals(listOf("Tie-c", "tie-a", "tie_b", "tiea", "older"), listed.map { it.alias.value })
            assertEquals(listed.sortedWith(ShortUrl.listOrder), listed)
        }

    @Test
    fun `Reports the database unavailable, promptly, once it stops`() {
        // A database of its own, so that stopping it doesn't affect other tests.
        PostgreSQLContainer("postgres:18").use { doomed ->
            doomed.start()
            val config = DatabaseConfig(doomed.jdbcUrl, doomed.username, doomed.password)
            migrate(config)
            createDataSource(config).use { pool ->
                val repository = JdbcShortUrlRepository(pool)
                runBlocking { repository.insert(alias("my-alias"), url("https://example.com/")) }

                doomed.stop()

                val elapsed =
                    measureTime {
                        assertThrows<StorageUnavailableException> { runBlocking { repository.find(alias("my-alias")) } }
                        assertThrows<StorageUnavailableException> {
                            runBlocking { repository.insert(alias("other"), url("https://example.com/")) }
                        }
                        assertThrows<StorageUnavailableException> { runBlocking { repository.list() } }
                        assertThrows<StorageUnavailableException> { runBlocking { repository.delete(alias("my-alias")) } }
                    }
                // Each call waits at most the pool's timeout for a connection, and
                // a little to give up.
                assertTrue(elapsed < (4 * DATABASE_TIMEOUT_MILLIS + 1_000).milliseconds, "Took $elapsed")
            }
        }
    }
}
