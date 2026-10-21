package com.tpximpact.urlshortener.domain

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** Stands in for the database, in tests that don't need a real one. */
class InMemoryShortUrlRepository : ShortUrlRepository {
    val stored = ConcurrentHashMap<Alias, DestinationUrl>()

    /**
     * When each short URL in [stored] was created. Each insert is a second
     * later than the one before. One that's missing was created at the epoch.
     */
    val createdAt = ConcurrentHashMap<Alias, Instant>()

    private val clock = AtomicLong()

    /** How many times an alias has been looked up, by [find] or [delete]. */
    val lookups = AtomicInteger()

    /** While true, every call fails, as if the database were down. */
    @Volatile
    var unavailable = false

    override suspend fun insert(
        alias: Alias,
        destinationUrl: DestinationUrl,
    ): Boolean {
        checkAvailable()
        if (stored.putIfAbsent(alias, destinationUrl) != null) return false
        createdAt[alias] = Instant.ofEpochSecond(clock.incrementAndGet())
        return true
    }

    override suspend fun find(alias: Alias): DestinationUrl? {
        lookups.incrementAndGet()
        checkAvailable()
        return stored[alias]
    }

    override suspend fun list(): List<ShortUrl> {
        checkAvailable()
        return stored
            .map { (alias, destinationUrl) -> ShortUrl(alias, destinationUrl, createdAt[alias] ?: Instant.EPOCH) }
            .sortedWith(ShortUrl.listOrder)
    }

    override suspend fun delete(alias: Alias): Boolean {
        lookups.incrementAndGet()
        checkAvailable()
        createdAt.remove(alias)
        return stored.remove(alias) != null
    }

    private fun checkAvailable() {
        if (unavailable) throw StorageUnavailableException(RuntimeException("Secret internal detail"))
    }
}
