package com.tpximpact.urlshortener.infra.config

import java.net.URI
import java.net.URISyntaxException

/**
 * The API service's settings. They're read from the environment once, at
 * startup, and nothing else reads the environment.
 *
 * See: docs > design > concepts > configuration.
 */
data class Config(
    /** The prefix of every short URL, with no trailing slash. */
    val publicBaseUrl: URI,
    val database: DatabaseConfig,
) {
    companion object {
        /**
         * Reads and validates every setting. Throws [ConfigException], listing
         * every missing or invalid setting, not just the first.
         */
        fun fromEnvironment(env: Map<String, String> = System.getenv()): Config {
            val problems = mutableListOf<String>()

            fun required(name: String): String? {
                val value = env[name]
                if (value.isNullOrBlank()) {
                    problems += "$name is not set."
                    return null
                }
                return value
            }

            fun <T : Any> valid(
                name: String,
                parse: (String) -> T?,
                rule: String,
            ): T? =
                required(name)?.let { value ->
                    parse(value) ?: null.also { problems += "$name $rule" }
                }

            val publicBaseUrl =
                valid(
                    "PUBLIC_BASE_URL",
                    ::parsePublicBaseUrl,
                    "must be an http or https URL with a host, and no path, query, fragment, or user-info. " +
                        "For example, http://localhost:8080.",
                )
            val databaseUrl =
                valid(
                    "DATABASE_URL",
                    { it.takeIf { url -> url.startsWith("jdbc:postgresql://") } },
                    "must be a PostgreSQL JDBC URL. For example, jdbc:postgresql://db:5432/url_shortener.",
                )
            val databaseUser = required("DATABASE_USER")
            val databasePassword = required("DATABASE_PASSWORD")

            if (problems.isNotEmpty()) {
                throw ConfigException(problems)
            }
            return Config(
                publicBaseUrl = publicBaseUrl!!,
                database = DatabaseConfig(databaseUrl!!, databaseUser!!, databasePassword!!),
            )
        }
    }
}

data class DatabaseConfig(
    /** A PostgreSQL JDBC URL. */
    val url: String,
    val user: String,
    val password: String,
) {
    // Keeps the password out of logs and error messages.
    override fun toString() = "DatabaseConfig(url=$url, user=$user, password=***)"
}

class ConfigException(
    val problems: List<String>,
) : Exception("Invalid configuration. " + problems.joinToString(" "))

/**
 * Short URLs are the public base URL followed by `/` and an alias, so the base
 * URL can't carry anything after its authority.
 */
private fun parsePublicBaseUrl(value: String): URI? {
    val uri =
        try {
            URI(value)
        } catch (_: URISyntaxException) {
            return null
        }
    val valid =
        uri.scheme?.lowercase() in setOf("http", "https") &&
            !uri.host.isNullOrEmpty() &&
            uri.rawUserInfo == null &&
            uri.rawPath.orEmpty() in setOf("", "/") &&
            uri.rawQuery == null &&
            uri.rawFragment == null
    return if (valid) URI(value.removeSuffix("/")) else null
}
