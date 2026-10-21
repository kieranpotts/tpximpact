package com.tpximpact.urlshortener.domain

/**
 * Where short URLs are stored. The domain defines it, and the infrastructure
 * layer implements it.
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
}
