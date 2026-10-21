package com.tpximpact.urlshortener.http

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentLength
import io.ktor.server.request.header
import io.ktor.server.request.receiveChannel
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

/** About five times the size of the largest valid body. */
const val MAX_BODY_BYTES = 16 * 1024

/** Far deeper than any valid body, which is one JSON object. */
private const val MAX_NESTING_DEPTH = 32

/**
 * Receives a request body that must be a JSON object. Throws a
 * [ProblemException] if it isn't, so that no client input can cause an
 * unexpected fault.
 *
 * The content type and size are checked before the body is parsed. An
 * oversized `Content-Length` is refused without reading the body, and
 * otherwise reading stops at the limit, so memory is bounded whatever a
 * client sends.
 *
 * See: docs > decisions > 0015.
 */
suspend fun ApplicationCall.receiveJsonObject(): JsonObject {
    if (!isJson(request.header(HttpHeaders.ContentType))) {
        throw ProblemException(ProblemType.UNSUPPORTED_MEDIA_TYPE)
    }

    val declaredLength = request.contentLength()
    if (declaredLength != null && declaredLength > MAX_BODY_BYTES) {
        throw ProblemException(ProblemType.PAYLOAD_TOO_LARGE)
    }
    val bytes = receiveChannel().readBuffer(MAX_BODY_BYTES + 1L).readByteArray()
    if (bytes.size > MAX_BODY_BYTES) {
        throw ProblemException(ProblemType.PAYLOAD_TOO_LARGE)
    }

    val text =
        try {
            decodeUtf8(bytes)
        } catch (_: CharacterCodingException) {
            throw ProblemException(ProblemType.MALFORMED_REQUEST, "The request body is not valid UTF-8.")
        }
    // The parser recurses into nested arrays, so deep enough nesting would
    // overflow the stack, even within the size limit.
    if (nestsDeeperThan(text, MAX_NESTING_DEPTH)) {
        throw ProblemException(ProblemType.MALFORMED_REQUEST, "The request body is nested too deeply.")
    }
    val json =
        try {
            Json.parseToJsonElement(text)
        } catch (_: SerializationException) {
            throw ProblemException(ProblemType.MALFORMED_REQUEST)
        }
    return json as? JsonObject
        ?: throw ProblemException(ProblemType.MALFORMED_REQUEST, "The request body is not a JSON object.")
}

/**
 * Whether a `Content-Type` header names JSON. Parameters, such as `charset`,
 * are allowed. A header that can't be parsed isn't JSON.
 */
private fun isJson(header: String?): Boolean {
    if (header == null) return false
    val contentType =
        try {
            ContentType.parse(header)
        } catch (_: Exception) {
            return false
        }
    return contentType.withoutParameters() == ContentType.Application.Json
}

/**
 * JSON is always UTF-8 (RFC 8259). Bytes that aren't valid UTF-8 are refused,
 * not replaced, so they can't reach a stored value.
 */
private fun decodeUtf8(bytes: ByteArray): String =
    Charsets.UTF_8
        .newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes))
        .toString()

/**
 * Whether JSON text nests arrays and objects more than [max] deep. Brackets
 * inside strings don't count. It doesn't check that the text is valid JSON.
 */
private fun nestsDeeperThan(
    text: String,
    max: Int,
): Boolean {
    var depth = 0
    var inString = false
    var escaped = false
    for (c in text) {
        when {
            escaped -> {
                escaped = false
            }

            inString && c == '\\' -> {
                escaped = true
            }

            c == '"' -> {
                inString = !inString
            }

            inString -> {}

            c == '[' || c == '{' -> {
                if (++depth > max) return true
            }

            c == ']' || c == '}' -> {
                depth--
            }
        }
    }
    return false
}
