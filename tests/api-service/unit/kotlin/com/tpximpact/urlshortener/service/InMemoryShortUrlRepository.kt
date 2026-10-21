package com.tpximpact.urlshortener.service

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import java.util.concurrent.ConcurrentHashMap

/** Stands in for the database, in tests that don't need a real one. */
class InMemoryShortUrlRepository : ShortUrlRepository {
    val stored = ConcurrentHashMap<Alias, DestinationUrl>()

    override suspend fun insert(
        alias: Alias,
        destinationUrl: DestinationUrl,
    ): Boolean = stored.putIfAbsent(alias, destinationUrl) == null
}
