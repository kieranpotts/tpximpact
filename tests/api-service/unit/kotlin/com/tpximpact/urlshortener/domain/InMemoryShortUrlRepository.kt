package com.tpximpact.urlshortener.domain

import java.util.concurrent.ConcurrentHashMap

/** Stands in for the database, in tests that don't need a real one. */
class InMemoryShortUrlRepository : ShortUrlRepository {
    val stored = ConcurrentHashMap<Alias, DestinationUrl>()

    override suspend fun insert(
        alias: Alias,
        destinationUrl: DestinationUrl,
    ): Boolean = stored.putIfAbsent(alias, destinationUrl) == null
}
