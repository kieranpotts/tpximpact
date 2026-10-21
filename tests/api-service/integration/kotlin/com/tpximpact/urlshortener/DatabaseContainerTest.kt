package com.tpximpact.urlshortener

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.sql.DriverManager

@Testcontainers
class DatabaseContainerTest {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:18")
    }

    @Test
    fun `connects to PostgreSQL 18`() {
        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { connection ->
            val version = connection.metaData.databaseProductVersion

            assertTrue(version.startsWith("18."), "Expected PostgreSQL 18, got $version")
        }
    }
}
