package com.tpximpact.urlshortener.jdbc

import com.tpximpact.urlshortener.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

/**
 * How long a request waits for a connection from the pool, and for any one
 * statement, so a slow database fails requests fast.
 *
 * See: docs > design > process.
 */
const val DATABASE_TIMEOUT_MILLIS = 2_000L

/**
 * Brings the schema up to date, with the Flyway migrations bundled into the
 * service. Throws if the database can't be reached, or a migration fails.
 *
 * Flyway uses its own connection, not the pool, so migrations aren't bound by
 * the pool's statement timeout. See: docs > decisions > 0004.
 */
fun migrate(config: DatabaseConfig) {
    Flyway
        .configure()
        .dataSource(config.url, config.user, config.password)
        .locations("classpath:db/migration")
        .load()
        .migrate()
}

/**
 * Creates the connection pool. Throws if the database can't be reached.
 *
 * See: docs > decisions > 0005.
 */
fun createDataSource(config: DatabaseConfig): HikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            poolName = "api-service"
            jdbcUrl = config.url
            username = config.user
            password = config.password
            maximumPoolSize = 10
            connectionTimeout = DATABASE_TIMEOUT_MILLIS
            addDataSourceProperty("options", "-c statement_timeout=$DATABASE_TIMEOUT_MILLIS")
        },
    )
