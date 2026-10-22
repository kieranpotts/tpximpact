package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.app.ShortUrlService
import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.Parsed
import com.tpximpact.urlshortener.ui.ProblemException
import com.tpximpact.urlshortener.ui.ProblemType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete

/**
 * `DELETE /{alias}` deletes a short URL. The alias stops redirecting, and is
 * free to use again, at once. Deleting an alias that doesn't exist succeeds
 * too, so a retried or repeated delete gets the same answer.
 *
 * See: docs > specs > B6 and R10.
 * See: docs > design > scenarios > delete-short-url.
 */
fun Route.deleteRoute(shortUrls: ShortUrlService) {
    delete("/{alias}") {
        // A path segment that can't be an alias can't exist, so the database
        // isn't asked.
        val alias = (Alias.parse(call.parameters["alias"]!!) as? Parsed.Valid)?.value
        if (alias == null) throw ProblemException(ProblemType.NOT_FOUND)

        shortUrls.delete(alias)

        call.respond(HttpStatusCode.NoContent)
    }
}
