package com.tpximpact.urlshortener.domain

/**
 * The path segment that identifies a short URL. Custom and random aliases
 * share one namespace, and one set of rules. Aliases are case-sensitive.
 *
 * See: docs > specs > R1 to R3.
 * See: docs > decisions > 0009.
 */
@JvmInline
value class Alias private constructor(
    val value: String,
) {
    companion object {
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 64

        private val validCharacters = Regex("[A-Za-z0-9_-]+")

        /**
         * Words that would shadow the API service's routes, today's or
         * tomorrow's. They're compared case-insensitively.
         *
         * They're part of the alias rules, so they live in code, not in
         * configuration. Changing them at deploy time could reserve an
         * alias that's already stored, and shadow it.
         */
        private val reserved = setOf("shorten", "urls", "api", "docs", "health", "metrics", "static", "assets")

        /** Version segments, such as `v1` and `v10`, for versioned API paths. */
        private val versionSegment = Regex("v[0-9]+", RegexOption.IGNORE_CASE)

        fun parse(value: String): Parsed<Alias> =
            when {
                value.length !in MIN_LENGTH..MAX_LENGTH -> Parsed.Invalid(Violation.ALIAS_INVALID_LENGTH)
                !validCharacters.matches(value) -> Parsed.Invalid(Violation.ALIAS_INVALID_CHARACTERS)
                value.lowercase() in reserved -> Parsed.Invalid(Violation.ALIAS_RESERVED)
                versionSegment.matches(value) -> Parsed.Invalid(Violation.ALIAS_RESERVED)
                else -> Parsed.Valid(Alias(value))
            }

        /**
         * Restores an alias that was valid when it was stored. It's not
         * checked again, so that a stored alias can still be listed and
         * deleted after a rule changes.
         */
        fun fromStorage(value: String) = Alias(value)
    }
}
