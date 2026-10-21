package com.tpximpact.urlshortener.ui.routes

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

private val welcomePage =
    """
    <!doctype html>
    <html lang="en">
      <head>
        <meta charset="utf-8">
        <title>URL shortener</title>
      </head>
      <body>
        <h1>Welcome to the URL shortener</h1>
      </body>
    </html>
    """.trimIndent() + "\n"

/**
 * `GET /` greets anyone who visits the bare public base URL. No alias can be
 * empty, so the root path never shadows a short URL.
 *
 * See: docs > specs, B15.
 */
fun Route.welcomeRoute() {
    get("/") {
        call.respondText(welcomePage, ContentType.Text.Html)
    }
}
