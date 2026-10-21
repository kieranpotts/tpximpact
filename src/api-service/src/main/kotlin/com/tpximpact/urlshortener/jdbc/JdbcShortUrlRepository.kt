package com.tpximpact.urlshortener.jdbc

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.service.ShortUrlRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                // The primary key serialises concurrent inserts of one alias.
                // Exactly one of them inserts a row.
                connection.prepareStatement(INSERT).use { statement ->
                    statement.setString(1, alias.value)
                    statement.setString(2, destinationUrl.value)
                    statement.executeUpdate() == 1
                }
            }
        }

    private companion object {
        const val INSERT = "INSERT INTO short_url (alias, full_url) VALUES (?, ?) ON CONFLICT (alias) DO NOTHING"
    }
}
