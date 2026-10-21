package com.tpximpact.urlshortener.app

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.AliasGenerator
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.ShortUrl
import com.tpximpact.urlshortener.domain.ShortUrlRepository

/**
 * The API service's use cases.
 *
 * See: docs > design > logical > api-service.
 */
class ShortUrlService(
    private val repository: ShortUrlRepository,
    private val aliasGenerator: AliasGenerator,
) {
    /**
     * Creates a short URL, with [customAlias], or with a random alias when
     * it's null, and returns its alias. Every call creates a new short URL,
     * even for a destination URL that's already been shortened.
     *
     * Throws [AliasTakenException] if [customAlias] is already in use.
     */
    suspend fun shorten(
        destinationUrl: DestinationUrl,
        customAlias: Alias?,
    ): Alias {
        if (customAlias != null) {
            if (!repository.insert(customAlias, destinationUrl)) throw AliasTakenException(customAlias)
            return customAlias
        }

        // A random alias collides with a stored one so rarely that running
        // out of attempts means something is wrong.
        // See: docs > decisions > 0010.
        repeat(MAX_RANDOM_ALIAS_ATTEMPTS) {
            val alias = aliasGenerator.next()
            if (repository.insert(alias, destinationUrl)) return alias
        }
        error("Every one of $MAX_RANDOM_ALIAS_ATTEMPTS random aliases was already taken.")
    }

    /** Returns the destination URL of [alias], or null if it doesn't exist. */
    suspend fun resolve(alias: Alias): DestinationUrl? = repository.find(alias)

    /** Returns every short URL, newest first. */
    suspend fun list(): List<ShortUrl> = repository.list()

    /**
     * Deletes the short URL with [alias], so the alias is free to use again
     * at once. Returns false if it doesn't exist.
     */
    suspend fun delete(alias: Alias): Boolean = repository.delete(alias)

    companion object {
        const val MAX_RANDOM_ALIAS_ATTEMPTS = 5
    }
}

class AliasTakenException(
    val alias: Alias,
) : Exception("The alias ${alias.value} is already taken.")
