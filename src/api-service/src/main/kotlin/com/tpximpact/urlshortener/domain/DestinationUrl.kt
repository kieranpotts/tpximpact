package com.tpximpact.urlshortener.domain

import java.net.URI
import java.net.URISyntaxException

/**
 * Where a short URL redirects to. It's stored and redirected to exactly as
 * given, once trimmed.
 *
 * See: docs > specs > R5 and R7.
 * See: docs > decisions > 0011.
 */
@JvmInline
value class DestinationUrl private constructor(
    val value: String,
) {
    companion object {
        const val MAX_LENGTH = 2_048

        private val schemes = setOf("http", "https")

        fun parse(
            value: String,
            publicBaseUrl: PublicBaseUrl,
        ): Parsed<DestinationUrl> {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return Parsed.Invalid(Violation.URL_REQUIRED)
            if (trimmed.length > MAX_LENGTH) return Parsed.Invalid(Violation.URL_TOO_LONG)

            // A URL is sent back in the `Location` header of every redirect,
            // which can't carry anything but ASCII. Non-ASCII text must be
            // percent-encoded, and host names must be given in their ASCII
            // form. This also keeps out spaces and control characters, so a
            // stored URL can't split the header.
            if (!trimmed.all { it in '!'..'~' }) return Parsed.Invalid(Violation.URL_INVALID)

            val uri =
                try {
                    URI(trimmed)
                } catch (_: URISyntaxException) {
                    return Parsed.Invalid(Violation.URL_INVALID)
                }

            return when {
                uri.scheme?.lowercase() !in schemes -> Parsed.Invalid(Violation.URL_UNSUPPORTED_SCHEME)

                uri.rawUserInfo != null -> Parsed.Invalid(Violation.URL_HAS_CREDENTIALS)

                // `URI` leaves the host null when the authority isn't a valid
                // host name.
                uri.host.isNullOrEmpty() -> Parsed.Invalid(Violation.URL_INVALID)

                sameHost(uri.host, publicBaseUrl.host) -> Parsed.Invalid(Violation.URL_SELF_REFERENTIAL)

                else -> Parsed.Valid(DestinationUrl(trimmed))
            }
        }

        /**
         * Restores a destination URL that was valid when it was stored. It's
         * not checked again, so that a stored URL still redirects after a
         * rule, or the public base URL, changes.
         */
        fun fromStorage(value: String) = DestinationUrl(value)

        /** Host names are case-insensitive, and may end with the root's `.`. */
        private fun sameHost(
            a: String,
            b: String,
        ) = a.removeSuffix(".").equals(b.removeSuffix("."), ignoreCase = true)
    }
}
