package com.tpximpact.urlshortener.domain

/**
 * Where short URLs are stored. The domain defines it, and the infrastructure
 * layer implements it.
 *
 * Every method throws [StorageUnavailableException] if the store can't be
 * reached, or doesn't answer in time.
 *
 * See: docs > design > concepts > persistence.
 */
interface ShortUrlRepository {
    /**
     * Stores a new short URL, and returns true once it's stored. Returns
     * false, and changes nothing, if the alias is already taken.
     */
    suspend fun insert(
        alias: Alias,
        destinationUrl: DestinationUrl,
    ): Boolean

    /** Returns the destination URL stored under [alias], or null if there's none. */
    suspend fun find(alias: Alias): DestinationUrl?

    /** Returns every stored short URL, in [ShortUrl.listOrder]. */
    suspend fun list(): List<ShortUrl>

    /**
     * Removes the short URL stored under [alias], and returns true once it's
     * removed. Returns false if there's none.
     */
    suspend fun delete(alias: Alias): Boolean
}

/**
 * The store can't be reached, or doesn't answer in time. It's expected to
 * recover, so a request that fails with it can be tried again later.
 */
class StorageUnavailableException(
    cause: Throwable,
) : Exception("The store of short URLs is unavailable.", cause)
