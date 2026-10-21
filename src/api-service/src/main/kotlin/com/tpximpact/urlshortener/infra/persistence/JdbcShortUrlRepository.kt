package com.tpximpact.urlshortener.infra.persistence

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.ShortUrlRepository
import com.tpximpact.urlshortener.domain.StorageUnavailableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLNonTransientConnectionException
import java.sql.SQLRecoverableException
import java.sql.SQLTransientException
import javax.sql.DataSource

/**
 * Stores short URLs in PostgreSQL. All the API service's SQL is here, as
 * prepared statements, each in auto-commit mode.
 *
 * JDBC blocks, so every call runs on the I/O dispatcher, not on the thread
 * that handles the request.
 *
 * See: docs > design > concepts > persistence.
 * See: docs > decisions > 0005.
 */
class JdbcShortUrlRepository(
    private val dataSource: DataSource,
) : ShortUrlRepository {
    override suspend fun insert(
        alias: Alias,
        destinationUrl: DestinationUrl,
    ): Boolean =
        withConnection { connection ->
            // The primary key serialises concurrent inserts of one alias.
            // Exactly one of them inserts a row.
            connection.prepareStatement(INSERT).use { statement ->
                statement.setString(1, alias.value)
                statement.setString(2, destinationUrl.value)
                statement.executeUpdate() == 1
            }
        }

    override suspend fun find(alias: Alias): DestinationUrl? =
        withConnection { connection ->
            connection.prepareStatement(SELECT_BY_ALIAS).use { statement ->
                statement.setString(1, alias.value)
                statement.executeQuery().use { rs ->
                    if (rs.next()) DestinationUrl.fromStorage(rs.getString(1)) else null
                }
            }
        }

    /**
     * Runs [block] with a pooled connection, on the I/O dispatcher. Throws
     * [StorageUnavailableException] if the database can't be reached, or
     * doesn't answer in time. Any other [SQLException] is a fault, and is
     * thrown as it is.
     */
    private suspend fun <T> withConnection(block: (Connection) -> T): T =
        withContext(Dispatchers.IO) {
            try {
                dataSource.connection.use(block)
            } catch (e: SQLException) {
                if (e.isOutage()) throw StorageUnavailableException(e)
                throw e
            }
        }

    private companion object {
        const val INSERT = "INSERT INTO short_url (alias, full_url) VALUES (?, ?) ON CONFLICT (alias) DO NOTHING"
        const val SELECT_BY_ALIAS = "SELECT full_url FROM short_url WHERE alias = ?"

        /**
         * SQLSTATE classes that mean the database is down, overloaded, or
         * shutting down, or that a statement timed out: connection exceptions,
         * insufficient resources, and operator intervention.
         */
        val outageSqlStateClasses = setOf("08", "53", "57")

        /**
         * Whether an error says the database is unavailable, not that a
         * statement is wrong. The pool reports a timeout waiting for a
         * connection as a transient exception, and the PostgreSQL driver
         * reports a lost connection only by its SQLSTATE.
         */
        fun SQLException.isOutage() =
            this is SQLTransientException ||
                this is SQLRecoverableException ||
                this is SQLNonTransientConnectionException ||
                sqlState?.take(2) in outageSqlStateClasses
    }
}
