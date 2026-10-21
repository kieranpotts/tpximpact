package com.tpximpact.urlshortener.domain

import java.time.Instant

/**
 * A stored mapping from an alias to a destination URL. It's identified by its
 * alias, and never changes once it's created.
 *
 * See: docs > specs > R9.
 * See: docs > design > concepts > domain-model.
 */
data class ShortUrl(
    val alias: Alias,
    val destinationUrl: DestinationUrl,
    val createdAt: Instant,
) {
    companion object {
        /**
         * Newest first, and short URLs created at the same moment by alias,
         * compared character by character, so the order doesn't depend on a
         * locale.
         *
         * See: docs > specs > R11.
         */
        val listOrder: Comparator<ShortUrl> =
            compareByDescending<ShortUrl> { it.createdAt }.thenBy { it.alias.value }
    }
}
