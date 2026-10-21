package com.tpximpact.urlshortener.http

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class ProblemsTest {
    @Test
    fun `An unknown path gets a not-found problem`() =
        apiTest { client ->
            val response = client.get("/no-such-alias")

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals(
                Problem("urn:problem:url-shortener:not-found", "The alias was not found", 404),
                response.problem(),
            )
        }

    @Test
    fun `An unexpected fault gets an internal-error problem, with only the request ID`() =
        apiTest(routes = { get("/fault") { error("Secret internal detail") } }) { client ->
            val response = client.get("/fault")
            val requestId = response.headers[HttpHeaders.XRequestId]

            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals(
                Problem(
                    "urn:problem:url-shortener:internal-error",
                    "Something went wrong",
                    500,
                    "Quote request ID $requestId when reporting this.",
                ),
                response.problem(),
            )
            assertFalse(response.bodyAsText().contains("Secret"))
        }

    private suspend fun HttpResponse.problem(): Problem {
        assertEquals(ContentType.Application.ProblemJson, contentType()?.withoutParameters())
        return Json.decodeFromString(bodyAsText())
    }
}
