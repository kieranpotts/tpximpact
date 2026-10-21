package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.app.ShortUrlService
import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.Parsed
import com.tpximpact.urlshortener.domain.PublicBaseUrl
import com.tpximpact.urlshortener.domain.Violation
import com.tpximpact.urlshortener.ui.FieldError
import com.tpximpact.urlshortener.ui.ProblemException
import com.tpximpact.urlshortener.ui.ProblemType
import com.tpximpact.urlshortener.ui.receiveJsonObject
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
private data class ShortenResponse(
    val shortUrl: String,
)

/**
 * `POST /shorten` creates a short URL, with a custom alias or a random one.
 *
 * See: docs > specs > B1 to B4, B12, and B13.
 */
fun Route.shortenRoute(
    shortUrls: ShortUrlService,
    publicBaseUrl: PublicBaseUrl,
) {
    post("/shorten") {
        val body = call.receiveJsonObject()
        val (destinationUrl, customAlias) = parseShortenRequest(body, publicBaseUrl)

        val alias = shortUrls.shorten(destinationUrl, customAlias)

        call.respondText(
            Json.encodeToString(ShortenResponse(publicBaseUrl.linkTo(alias))),
            ContentType.Application.Json,
            HttpStatusCode.Created,
        )
    }
}

/**
 * Checks every field, so that a `validation-failed` problem lists every one
 * that's invalid, not only the first. Properties that the contract doesn't
 * define are ignored.
 */
private fun parseShortenRequest(
    body: JsonObject,
    publicBaseUrl: PublicBaseUrl,
): Pair<DestinationUrl, Alias?> {
    val errors = mutableListOf<FieldError>()

    val destinationUrl =
        when (val value = body["destinationUrl"]) {
            null, JsonNull -> Parsed.Invalid(Violation.URL_REQUIRED)
            else -> value.text()?.let { DestinationUrl.parse(it, publicBaseUrl) } ?: Parsed.Invalid(Violation.URL_INVALID)
        }.valueOrNull { errors += fieldError("destinationUrl", it) }

    // Only an absent or null alias asks for a random one. An empty one is
    // invalid. See: docs > specs > R6.
    val customAlias =
        when (val value = body["customAlias"]) {
            null, JsonNull -> null
            else -> value.text()?.let(Alias::parse) ?: Parsed.Invalid(Violation.ALIAS_INVALID_CHARACTERS)
        }?.valueOrNull { errors += fieldError("customAlias", it) }

    if (errors.isNotEmpty()) {
        val count = if (errors.size == 1) "1 invalid field" else "${errors.size} invalid fields"
        throw ProblemException(ProblemType.VALIDATION_FAILED, "The request has $count.", errors)
    }
    return Pair(destinationUrl!!, customAlias)
}

/** A JSON string's content, or null for any other kind of JSON value. */
private fun JsonElement.text(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.content

private inline fun <T> Parsed<T>.valueOrNull(onInvalid: (Violation) -> Unit): T? =
    when (this) {
        is Parsed.Valid -> value
        is Parsed.Invalid -> null.also { onInvalid(violation) }
    }

private fun fieldError(
    field: String,
    violation: Violation,
): FieldError {
    val (code, message) =
        when (violation) {
            Violation.ALIAS_INVALID_LENGTH -> {
                "alias-invalid-length" to "Alias must be between ${Alias.MIN_LENGTH} and ${Alias.MAX_LENGTH} characters"
            }

            Violation.ALIAS_INVALID_CHARACTERS -> {
                "alias-invalid-characters" to "Alias must only include letters a to z, numbers, hyphens and underscores"
            }

            Violation.ALIAS_RESERVED -> {
                "alias-reserved" to "Alias is reserved for the URL shortener's own use. Enter a different alias"
            }

            Violation.URL_REQUIRED -> {
                "url-required" to "Enter a URL"
            }

            Violation.URL_TOO_LONG -> {
                "url-too-long" to "URL must be 2,048 characters or fewer"
            }

            Violation.URL_INVALID -> {
                "url-invalid" to "Enter a URL in the correct format, like https://www.example.com"
            }

            Violation.URL_UNSUPPORTED_SCHEME -> {
                "url-unsupported-scheme" to "Enter a URL starting with http:// or https://"
            }

            Violation.URL_HAS_CREDENTIALS -> {
                "url-has-credentials" to "Enter a URL without a user name or password"
            }

            Violation.URL_SELF_REFERENTIAL -> {
                "url-self-referential" to "Enter a URL that is not a short URL from this URL shortener"
            }
        }
    return FieldError(field, code, message)
}
