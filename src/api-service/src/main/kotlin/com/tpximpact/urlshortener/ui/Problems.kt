package com.tpximpact.urlshortener.ui

import com.tpximpact.urlshortener.app.AliasTakenException
import com.tpximpact.urlshortener.domain.StorageUnavailableException
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.acceptItems
import io.ktor.server.request.httpMethod
import io.ktor.server.response.header
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
    ALIAS_TAKEN("alias-taken", HttpStatusCode.BadRequest, "The alias is already taken"),
    INTERNAL_ERROR("internal-error", HttpStatusCode.InternalServerError, "Something went wrong"),
    MALFORMED_REQUEST("malformed-request", HttpStatusCode.BadRequest, "The request body is not valid JSON"),
    NOT_FOUND("not-found", HttpStatusCode.NotFound, "The alias was not found"),
    PAYLOAD_TOO_LARGE("payload-too-large", HttpStatusCode.PayloadTooLarge, "The request body is too large"),
    SERVICE_UNAVAILABLE(
        "service-unavailable",
        HttpStatusCode.ServiceUnavailable,
        "The service is temporarily unavailable",
    ),
    UNSUPPORTED_MEDIA_TYPE(
        "unsupported-media-type",
        HttpStatusCode.UnsupportedMediaType,
        "The content type is not supported",
    ),
    VALIDATION_FAILED("validation-failed", HttpStatusCode.BadRequest, "The request is not valid"),
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
    /** Only in a `validation-failed` problem. */
    val errors: List<FieldError>? = null,
)

/** One broken rule, in a `validation-failed` problem. */
@Serializable
data class FieldError(
    val field: String,
    val code: String,
    /** Written for link editors, because the web client shows it verbatim. */
    val message: String,
)

/**
 * Thrown wherever a request turns out to be bad, so that it's answered with a
 * problem details response.
 */
class ProblemException(
    val type: ProblemType,
    val detail: String? = null,
    val errors: List<FieldError>? = null,
) : Exception(type.title)

suspend fun ApplicationCall.respondProblem(
    type: ProblemType,
    detail: String? = null,
    errors: List<FieldError>? = null,
) {
    val problem = Problem(type.uri, type.title, type.status.value, detail, errors)
    respondText(Json.encodeToString(problem), ContentType.Application.ProblemJson, type.status)
}

/** What a browser shows when it follows a short URL that doesn't exist. */
private val linkNotFoundPage =
    """
    <!doctype html>
    <html lang="en">
      <head>
        <meta charset="utf-8">
        <title>Link not found - URL shortener</title>
      </head>
      <body>
        <h1>Link not found</h1>
        <p>Check the link is correct. It may have been deleted.</p>
      </body>
    </html>
    """.trimIndent() + "\n"

/**
 * Answers a request for something that doesn't exist. A browser following a
 * short URL gets an HTML page, and everything else a problem.
 */
private suspend fun ApplicationCall.respondNotFound() {
    if (request.httpMethod == HttpMethod.Get && acceptsHtml()) {
        respondText(linkNotFoundPage, ContentType.Text.Html, HttpStatusCode.NotFound)
    } else {
        respondProblem(ProblemType.NOT_FOUND)
    }
}

/** Whether the `Accept` header names HTML, as every browser's does for a page. */
private fun ApplicationCall.acceptsHtml() = request.acceptItems().any { it.value == ContentType.Text.Html.toString() && it.quality > 0.0 }

/** Tells a client that sent the wrong content type which one to send. */
private const val ACCEPT_POST = "Accept-Post"

private val log = LoggerFactory.getLogger("com.tpximpact.urlshortener.ui.Problems")

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
            call.respondNotFound()
        }

        exception<ProblemException> { call, cause ->
            when (cause.type) {
                ProblemType.NOT_FOUND -> {
                    call.respondNotFound()
                }

                ProblemType.UNSUPPORTED_MEDIA_TYPE -> {
                    call.response.header(ACCEPT_POST, ContentType.Application.Json.toString())
                    call.respondProblem(cause.type, cause.detail, cause.errors)
                }

                else -> {
                    call.respondProblem(cause.type, cause.detail, cause.errors)
                }
            }
        }

        exception<AliasTakenException> { call, _ ->
            call.respondProblem(ProblemType.ALIAS_TAKEN)
        }

        // The database is down, or too slow. It's expected to recover, so this
        // isn't a fault in the service.
        exception<StorageUnavailableException> { call, cause ->
            log.error("The database is unavailable", cause)
            call.respondProblem(
                ProblemType.SERVICE_UNAVAILABLE,
                "Try again later. Quote request ID ${call.callId} when reporting this.",
            )
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
