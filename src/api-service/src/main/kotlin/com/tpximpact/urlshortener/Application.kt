package com.tpximpact.urlshortener

import com.tpximpact.urlshortener.app.ShortUrlService
import com.tpximpact.urlshortener.domain.PublicBaseUrl
import com.tpximpact.urlshortener.domain.RandomAliasGenerator
import com.tpximpact.urlshortener.infra.config.Config
import com.tpximpact.urlshortener.infra.config.ConfigException
import com.tpximpact.urlshortener.infra.persistence.JdbcShortUrlRepository
import com.tpximpact.urlshortener.infra.persistence.createDataSource
import com.tpximpact.urlshortener.infra.persistence.migrate
import com.tpximpact.urlshortener.ui.installProblems
import com.tpximpact.urlshortener.ui.installRequestIds
import com.tpximpact.urlshortener.ui.routes.contractRoute
import com.tpximpact.urlshortener.ui.routes.deleteRoute
import com.tpximpact.urlshortener.ui.routes.listRoute
import com.tpximpact.urlshortener.ui.routes.redirectRoute
import com.tpximpact.urlshortener.ui.routes.shortenRoute
import com.tpximpact.urlshortener.ui.routes.welcomeRoute
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val log = LoggerFactory.getLogger("com.tpximpact.urlshortener.Application")

/**
 * The composition root. It's the only place that knows about every layer.
 *
 * See: docs > design > process, for the startup and shutdown sequences.
 */
fun main() {
    val config =
        try {
            Config.fromEnvironment()
        } catch (e: ConfigException) {
            log.error(e.message)
            exitProcess(1)
        }

    val dataSource =
        try {
            migrate(config.database)
            createDataSource(config.database)
        } catch (e: Exception) {
            log.error("Can't prepare the database, at ${config.database.url}", e)
            exitProcess(1)
        }

    val publicBaseUrl = PublicBaseUrl(config.publicBaseUrl)
    val shortUrls = ShortUrlService(JdbcShortUrlRepository(dataSource), RandomAliasGenerator())

    // Ktor stops the server on `SIGTERM`, with this grace period and timeout.
    embeddedServer(
        Netty,
        configure = {
            connector { port = 8080 }
            shutdownGracePeriod = 2_000
            shutdownTimeout = 10_000
        },
    ) {
        monitor.subscribe(ApplicationStopped) { dataSource.close() }
        module(shortUrls, publicBaseUrl)
    }.start(wait = true)
}

fun Application.module(
    shortUrls: ShortUrlService,
    publicBaseUrl: PublicBaseUrl,
) {
    installRequestIds()
    installProblems()
    routing {
        welcomeRoute()
        contractRoute()
        shortenRoute(shortUrls, publicBaseUrl)
        listRoute(shortUrls, publicBaseUrl)
        redirectRoute(shortUrls)
        deleteRoute(shortUrls)
    }
}
