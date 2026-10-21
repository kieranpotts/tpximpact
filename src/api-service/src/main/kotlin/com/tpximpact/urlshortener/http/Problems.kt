package com.tpximpact.urlshortener.http

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * The kinds of error the API service reports, each with a fixed URN `type`,
 * status, and title, as the contract defines them.
 *
 * See: docs > design > concepts > error-handling.
 */
enum class ProblemType(
    slug: String,
    val status: HttpStatusCode,
    val title: String,
) {
    INTERNAL_ERROR("internal-error", HttpStatusCode.InternalServerError, "Something went wrong"),
    NOT_FOUND("not-found", HttpStatusCode.NotFound, "The alias was not found"),
    ;

    val uri = "urn:problem:url-shortener:$slug"
}

/** An RFC 9457 problem details body. See: docs > decisions > 0014. */
@Serializable
data class Problem(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String? = null,
)

suspend fun ApplicationCall.respondProblem(
    type: ProblemType,
    detail: String? = null,
) {
    val problem = Problem(type.uri, type.title, type.status.value, detail)
    respondText(Json.encodeToString(problem), ContentType.Application.ProblemJson, type.status)
}

private val log = LoggerFactory.getLogger("com.tpximpact.urlshortener.http.Problems")

/**
 * Maps every error to a problem details response, in one place, so route
 * handlers need no `try`/`catch`.
 */
fun Application.installProblems() {
    install(StatusPages) {
        // No route handled the request. Any unmatched path could be an alias.
        // This isn't a `status(NotFound)` handler, because that would replace
        // the body of every `404`, including those that routes send.
        unhandled { call ->
            call.respondProblem(ProblemType.NOT_FOUND)
        }

        // Anything unexpected. The body never carries the exception's detail,
        // only the request ID, which the log line carries too.
        exception<Throwable> { call, cause ->
            log.error("Unexpected fault", cause)
            call.respondProblem(
                ProblemType.INTERNAL_ERROR,
                "Quote request ID ${call.callId} when reporting this.",
            )
        }
    }
}
