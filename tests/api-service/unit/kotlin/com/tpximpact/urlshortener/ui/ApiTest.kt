package com.tpximpact.urlshortener.ui

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.report.LevelResolver
import com.atlassian.oai.validator.report.ValidationReport
import com.tpximpact.urlshortener.app.ShortUrlService
import com.tpximpact.urlshortener.domain.AliasGenerator
import com.tpximpact.urlshortener.domain.InMemoryShortUrlRepository
import com.tpximpact.urlshortener.domain.PublicBaseUrl
import com.tpximpact.urlshortener.domain.RandomAliasGenerator
import com.tpximpact.urlshortener.domain.ShortUrlRepository
import com.tpximpact.urlshortener.module
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.request
import io.ktor.http.contentType
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.net.URI
import java.nio.file.Path

/**
 * The contract's source file, not the copy bundled into the service, which a
 * test of the bundling would make a tautology.
 */
val contractPath: Path = Path.of(System.getProperty("contract.path"))

private val validator =
    OpenApiInteractionValidator
        .createForSpecificationUrl(contractPath.toUri().toString())
        .withLevelResolver(
            LevelResolver
                .create()
                // By default, the validator forbids properties that a schema
                // doesn't list, branch by branch, so every body built with
                // `allOf` fails. The contract doesn't forbid them, and it
                // shouldn't, so that the API can add fields.
                .withLevel("validation.schema.additionalProperties", ValidationReport.Level.IGNORE)
                // The validator can't follow the contract's discriminator
                // mappings. `oneOf` still requires one branch, and only one, to
                // match, and each branch's `type` is a different fixed value.
                .withLevel(
                    "validation.response.body.schema.discriminator.oneOf.no_match_found",
                    ValidationReport.Level.IGNORE,
                ).build(),
        ).build()

/**
 * Fails the request if its response doesn't match the contract: an
 * undocumented path, method, or status code, a missing required header, or a
 * body of the wrong media type or shape.
 */
private val ContractValidation =
    createClientPlugin("ContractValidation") {
        onResponse { response ->
            val method = response.request.method.value
            val path = response.request.url.encodedPath
            val actual =
                SimpleResponse.Builder
                    .status(response.status.value)
                    .withBody(response.bodyAsBytes())
                    .apply {
                        response.headers.forEach { name, values -> withHeader(name, values) }
                        response.contentType()?.let { withContentType(it.toString()) }
                    }.build()

            val report = validator.validateResponse(path, Request.Method.valueOf(method), actual)
            if (report.hasErrors()) {
                val errors =
                    report.messages
                        .filter { it.level == ValidationReport.Level.ERROR }
                        .joinToString("\n") { "- ${it.key}: ${it.message}" }
                throw AssertionError("The response to $method $path doesn't match the contract.\n$errors")
            }
        }
    }

/** The public base URL of the API service under test. */
val publicBaseUrl = PublicBaseUrl(URI("http://localhost:8080"))

/**
 * Runs [block] against the API service, in memory. Every response its client
 * gets is validated against the contract, and redirects aren't followed.
 *
 * Short URLs are kept in [repository], which a test can inspect, and random
 * aliases come from [aliasGenerator].
 *
 * [routes] are added to the service's own, so a test can provoke what no real
 * route does, such as an unexpected fault.
 */
fun apiTest(
    repository: ShortUrlRepository = InMemoryShortUrlRepository(),
    aliasGenerator: AliasGenerator = RandomAliasGenerator(),
    routes: Route.() -> Unit = {},
    block: suspend (client: HttpClient) -> Unit,
) = testApplication {
    application {
        module(ShortUrlService(repository, aliasGenerator), publicBaseUrl)
        routing(routes)
    }
    block(
        createClient {
            // A test sees each redirect, rather than following it out of the
            // service.
            followRedirects = false
            install(ContractValidation)
        },
    )
}
