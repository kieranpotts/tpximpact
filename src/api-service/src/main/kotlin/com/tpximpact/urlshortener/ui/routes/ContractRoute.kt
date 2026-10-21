package com.tpximpact.urlshortener.ui.routes

import io.ktor.http.ContentType
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

private val yaml = ContentType("application", "yaml")

/**
 * Serves the API contract, as bundled into the service at build time.
 *
 * See: docs > decisions > 0013.
 */
fun Route.contractRoute() {
    // Read once, when the routes are built, so a missing contract fails
    // startup, not a request.
    val contract =
        checkNotNull(object {}.javaClass.getResource("/contract/openapi.yaml")) {
            "The contract isn't bundled into the service."
        }.readBytes()

    get("/openapi.yaml") {
        call.respondBytes(contract, yaml)
    }
}
