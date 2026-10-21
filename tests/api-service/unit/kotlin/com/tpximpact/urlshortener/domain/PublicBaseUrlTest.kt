package com.tpximpact.urlshortener.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class PublicBaseUrlTest {
    @Test
    fun `Links to an alias, under the base URL`() {
        val alias = Alias.parse("my-alias").valid()

        assertEquals("http://localhost:8080/my-alias", PublicBaseUrl(URI("http://localhost:8080")).linkTo(alias))
        assertEquals("https://sho.rt/my-alias", PublicBaseUrl(URI("https://sho.rt/")).linkTo(alias))
    }
}
