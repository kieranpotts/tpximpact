package com.tpximpact.urlshortener.ui

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.header
import java.util.UUID

/** The logging context key that holds each request's ID. */
const val REQUEST_ID_MDC_KEY = "requestId"

/**
 * A caller's request ID is echoed in a header and written to logs, so it's
 * limited to a short run of visible ASCII characters.
 */
private val validRequestId = Regex("[!-~]{1,128}")

/**
 * Gives every request an ID, accepted from the caller's `X-Request-Id` header
 * or generated, echoes it on every response, and adds it to every log line.
 *
 * See: docs > design > concepts > error-handling.
 */
fun Application.installRequestIds() {
    install(CallId) {
        // A caller's invalid ID is replaced, not rejected, so it can't cause
        // an error response.
        retrieve { call -> call.request.header(HttpHeaders.XRequestId)?.takeIf(validRequestId::matches) }
        generate { UUID.randomUUID().toString() }
        verify { true }
        replyToHeader(HttpHeaders.XRequestId)
    }
    install(CallLogging) {
        callIdMdc(REQUEST_ID_MDC_KEY)
        // Logs go to files and log collectors, not terminals.
        disableDefaultColors()
    }
}
