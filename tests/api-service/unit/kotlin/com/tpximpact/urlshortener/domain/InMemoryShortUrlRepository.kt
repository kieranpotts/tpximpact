package com.tpximpact.urlshortener.domain

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Stands in for the database, in tests that don't need a real one. */
class InMemoryShortUrlRepository : ShortUrlRepository {
    val stored = ConcurrentHashMap<Alias, DestinationUrl>()

    /** How many times [find] has been called. */
    val lookups = AtomicInteger()

    /** While true, every call fails, as if the database were down. */
    @Volatile
    var unavailable = false

    override suspend fun insert(
        alias: Alias,
        destinationUrl: DestinationUrl,
    ): Boolean {
        checkAvailable()
        return stored.putIfAbsent(alias, destinationUrl) == null
    }

    override suspend fun find(alias: Alias): DestinationUrl? {
        lookups.incrementAndGet()
        checkAvailable()
        return stored[alias]
    }

    private fun checkAvailable() {
        if (unavailable) throw StorageUnavailableException(RuntimeException("Secret internal detail"))
    }
}
