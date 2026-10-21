package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.app.ShortUrlService
import com.tpximpact.urlshortener.domain.PublicBaseUrl
import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ShortUrlResponse(
    val alias: String,
    val destinationUrl: String,
    val shortUrl: String,
)

/**
 * `GET /urls` lists every short URL, newest first.
 *
 * See: docs > specs > B7 and R11.
 */
fun Route.listRoute(
    shortUrls: ShortUrlService,
    publicBaseUrl: PublicBaseUrl,
) {
    get("/urls") {
        val body =
            shortUrls.list().map {
                ShortUrlResponse(it.alias.value, it.destinationUrl.value, publicBaseUrl.linkTo(it.alias))
            }

        call.respondText(Json.encodeToString(body), ContentType.Application.Json)
    }
}
