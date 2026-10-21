package com.tpximpact.urlshortener.jdbc

import com.tpximpact.urlshortener.config.DatabaseConfig
import org.flywaydb.core.api.FlywayException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.sql.SQLException

@Testcontainers
class DatabaseTest {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:18")

        // Nothing listens on port 1, so connections are refused at once.
        private val unreachable = DatabaseConfig("jdbc:postgresql://localhost:1/none", "none", "none")
    }

    private val config get() = DatabaseConfig(postgres.jdbcUrl, postgres.username, postgres.password)

    @Test
    fun `Migrates the schema, again and again`() {
        assertDoesNotThrow {
            migrate(config)
            migrate(config)
        }
    }

    @Test
    fun `Fails to migrate an unreachable database`() {
        assertThrows<FlywayException> { migrate(unreachable) }
    }

    @Test
    fun `Pools connections to PostgreSQL 18`() {
        createDataSource(config).use { dataSource ->
            dataSource.connection.use { connection ->
                val version = connection.metaData.databaseProductVersion

                assertTrue(version.startsWith("18."), "Expected PostgreSQL 18, got $version")
            }
        }
    }

    @Test
    fun `Fails to create a pool for an unreachable database`() {
        assertThrows<RuntimeException> { createDataSource(unreachable) }
    }

    @Test
    fun `Cancels statements that run for longer than the timeout`() {
        createDataSource(config).use { dataSource ->
            dataSource.connection.use { connection ->
                val e =
                    assertThrows<SQLException> {
                        connection.createStatement().use { it.execute("SELECT pg_sleep(${DATABASE_TIMEOUT_MILLIS / 1000 + 1})") }
                    }

                // PostgreSQL's `query_canceled` error code.
                assertEquals("57014", e.sqlState)
            }
        }
    }
}
