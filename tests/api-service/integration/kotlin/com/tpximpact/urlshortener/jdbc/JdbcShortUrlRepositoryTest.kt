package com.tpximpact.urlshortener.jdbc

import com.tpximpact.urlshortener.config.DatabaseConfig
import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.Parsed
import com.tpximpact.urlshortener.domain.PublicBaseUrl
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.net.URI

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
}
