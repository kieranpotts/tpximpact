package com.tpximpact.urlshortener.domain

/**
 * The outcome of parsing raw input into a domain value: either the value, or
 * the business rule that the input breaks.
 */
sealed interface Parsed<out T> {
    data class Valid<out T>(
        val value: T,
    ) : Parsed<T>

    data class Invalid(
        val violation: Violation,
    ) : Parsed<Nothing>
}

/**
 * Every way that input can break a business rule. Each names one rule from
 * the specification.
 *
 * See: docs > specs > R1 to R6.
 */
enum class Violation {
    ALIAS_INVALID_LENGTH,
    ALIAS_INVALID_CHARACTERS,
    ALIAS_RESERVED,
    URL_REQUIRED,
    URL_TOO_LONG,
    URL_INVALID,
    URL_UNSUPPORTED_SCHEME,
    URL_HAS_CREDENTIALS,
    URL_SELF_REFERENTIAL,
}
