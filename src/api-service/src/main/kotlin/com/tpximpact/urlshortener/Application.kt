package com.tpximpact.urlshortener

import com.tpximpact.urlshortener.config.Config
import com.tpximpact.urlshortener.config.ConfigException
import com.tpximpact.urlshortener.http.contractRoute
import com.tpximpact.urlshortener.http.installProblems
import com.tpximpact.urlshortener.http.installRequestIds
import com.tpximpact.urlshortener.jdbc.createDataSource
import com.tpximpact.urlshortener.jdbc.migrate
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
        module()
    }.start(wait = true)
}

fun Application.module() {
    installRequestIds()
    installProblems()
    routing {
        contractRoute()
    }
}
