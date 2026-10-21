package com.tpximpact.urlshortener

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * The composition root. It's the only place that knows about every layer.
 */
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    routing {
        // Placeholder, until the API service has real routes.
        get("/") {
            call.respondText("URL shortener API service", ContentType.Text.Plain)
        }
    }
}
