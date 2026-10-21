package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.app.ShortUrlService
import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.Parsed
import com.tpximpact.urlshortener.ui.ProblemException
import com.tpximpact.urlshortener.ui.ProblemType
import io.ktor.http.HttpHeaders
import io.ktor.server.response.header
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * `GET /{alias}` redirects to the alias's destination URL, exactly as stored.
 *
 * See: docs > specs > B5 and R7.
 * See: docs > design > scenarios > follow-short-url.
 */
fun Route.redirectRoute(shortUrls: ShortUrlService) {
    get("/{alias}") {
        // A path segment that can't be an alias can't exist, so the database
        // isn't asked.
        val alias = (Alias.parse(call.parameters["alias"]!!) as? Parsed.Valid)?.value
        val destinationUrl = alias?.let { shortUrls.resolve(it) } ?: throw ProblemException(ProblemType.NOT_FOUND)

        // Never cached, so a deleted alias stops redirecting at once.
        call.response.header(HttpHeaders.CacheControl, "no-store")
        call.respondRedirect(destinationUrl.value, permanent = false)
    }
}
