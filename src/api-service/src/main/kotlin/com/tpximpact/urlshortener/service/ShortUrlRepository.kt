package com.tpximpact.urlshortener.service

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl

/**
 * Where short URLs are stored. This is the application's port, which an
 * outbound adapter implements.
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
