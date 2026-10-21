package com.tpximpact.urlshortener.domain

import java.security.SecureRandom

/** Makes aliases for short URLs that are created without a custom alias. */
fun interface AliasGenerator {
    fun next(): Alias
}

/**
 * Makes unpredictable aliases, of 7 characters drawn uniformly from the 62
 * letters and digits, with a cryptographically secure random source. An
 * alias that breaks a rule, by being reserved, is never returned.
 *
 * See: docs > specs > R4.
 * See: docs > decisions > 0010.
 */
class RandomAliasGenerator(
    private val random: SecureRandom = SecureRandom(),
) : AliasGenerator {
    override fun next(): Alias {
        while (true) {
            val candidate = String(CharArray(LENGTH) { ALPHABET[random.nextInt(ALPHABET.length)] })
            val parsed = Alias.parse(candidate)
            if (parsed is Parsed.Valid) return parsed.value
        }
    }

    companion object {
        const val LENGTH = 7
        const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    }
}
