package com.tpximpact.urlshortener.ui.routes

import com.tpximpact.urlshortener.ui.apiTest
import com.tpximpact.urlshortener.ui.contractPath
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ContractRouteTest {
    @Test
    fun `Serves the contract, as YAML`() =
        apiTest { client ->
            val response = client.get("/openapi.yaml")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType("application", "yaml"), response.contentType())
            assertArrayEquals(Files.readAllBytes(contractPath), response.bodyAsBytes())
        }
}
