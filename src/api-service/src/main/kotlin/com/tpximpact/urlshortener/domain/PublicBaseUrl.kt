package com.tpximpact.urlshortener.domain

import java.net.URI

/**
 * Where visitors reach the API service, and so the prefix of every short URL.
 *
 * See: docs > design > concepts > configuration.
 */
class PublicBaseUrl(
    /** An `http` or `https` URL with a host, and nothing after its authority. */
    val value: URI,
) {
    val host: String = checkNotNull(value.host) { "The public base URL has no host." }

    fun linkTo(alias: Alias): String = value.toString().removeSuffix("/") + "/" + alias.value
}
