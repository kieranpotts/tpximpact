package com.tpximpact.urlshortener.app

import com.tpximpact.urlshortener.domain.Alias
import com.tpximpact.urlshortener.domain.AliasGenerator
import com.tpximpact.urlshortener.domain.DestinationUrl
import com.tpximpact.urlshortener.domain.InMemoryShortUrlRepository
import com.tpximpact.urlshortener.domain.PublicBaseUrl
import com.tpximpact.urlshortener.domain.RandomAliasGenerator
import com.tpximpact.urlshortener.domain.valid
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class ShortUrlServiceTest {
    private val repository = InMemoryShortUrlRepository()
    private val destination = DestinationUrl.parse("https://example.com/", PublicBaseUrl(URI("http://sho.rt"))).valid()

    private fun alias(value: String) = Alias.parse(value).valid()

    private fun service(aliasGenerator: AliasGenerator = RandomAliasGenerator()) = ShortUrlService(repository, aliasGenerator)

    // Requirement: B1
    @Test
    fun `Stores a destination URL under a random alias`() =
        runBlocking {
            val alias = service { alias("aZ3k9Qx") }.shorten(destination, customAlias = null)

            assertEquals(alias("aZ3k9Qx"), alias)
            assertEquals(mapOf(alias to destination), repository.stored)
        }

    // Requirement: B2
    @Test
    fun `Stores a destination URL under a custom alias`() =
        runBlocking {
            val alias = service().shorten(destination, alias("my-alias"))

            assertEquals(alias("my-alias"), alias)
            assertEquals(mapOf(alias to destination), repository.stored)
        }

    // Requirement: B4
    @Test
    fun `Refuses a custom alias that's taken, and leaves it unchanged`() =
        runBlocking {
            val other = DestinationUrl.parse("https://other.example/", PublicBaseUrl(URI("http://sho.rt"))).valid()
            service().shorten(destination, alias("my-alias"))

            assertThrows<AliasTakenException> { service().shorten(other, alias("my-alias")) }
            assertEquals(mapOf(alias("my-alias") to destination), repository.stored)
        }

    // Requirement: R3
    @Test
    fun `Treats custom aliases that differ only in case as different`() =
        runBlocking {
            service().shorten(destination, alias("MyLink"))
            service().shorten(destination, alias("mylink"))

            assertEquals(setOf(alias("MyLink"), alias("mylink")), repository.stored.keys)
        }

    // Requirement: R1
    @Test
    fun `Refuses a custom alias that's taken by a random one`() =
        runBlocking {
            service { alias("aZ3k9Qx") }.shorten(destination, customAlias = null)

            assertThrows<AliasTakenException> { service().shorten(destination, alias("aZ3k9Qx")) }
        }

    // Requirement: R8
    @Test
    fun `Makes a new alias every time, for the same destination URL`() =
        runBlocking {
            val first = service().shorten(destination, customAlias = null)
            val second = service().shorten(destination, customAlias = null)

            assertNotEquals(first, second)
            assertEquals(mapOf(first to destination, second to destination), repository.stored)
        }

    // Requirement: B5
    @Test
    fun `Resolves an alias to its destination URL`() =
        runBlocking {
            service().shorten(destination, alias("my-alias"))

            assertEquals(destination, service().resolve(alias("my-alias")))
        }

    // Requirement: B5
    @Test
    fun `Resolves an alias that doesn't exist to nothing`() =
        runBlocking {
            assertNull(service().resolve(alias("my-alias")))
        }

    // Requirement: B6
    @Test
    fun `Deletes a short URL, which then resolves to nothing`() =
        runBlocking {
            service().shorten(destination, alias("my-alias"))

            assertTrue(service().delete(alias("my-alias")))
            assertNull(service().resolve(alias("my-alias")))
        }

    // Requirement: B6
    @Test
    fun `Deletes nothing for an alias that doesn't exist`() =
        runBlocking {
            assertFalse(service().delete(alias("my-alias")))
        }

    // Requirement: R10
    @Test
    fun `Lets a deleted alias be used again, straight away`() =
        runBlocking {
            val other = DestinationUrl.parse("https://other.example/", PublicBaseUrl(URI("http://sho.rt"))).valid()
            service().shorten(destination, alias("reused"))
            service().delete(alias("reused"))

            service().shorten(other, alias("reused"))

            assertEquals(other, service().resolve(alias("reused")))
        }

    // Requirements: B7, R11
    @Test
    fun `Lists every short URL, newest first`() =
        runBlocking {
            service().shorten(destination, alias("first"))
            service().shorten(destination, alias("second"))

            assertEquals(listOf(alias("second"), alias("first")), service().list().map { it.alias })
        }

    @Test
    fun `Tries another random alias when one is taken`() =
        runBlocking {
            repository.stored[alias("taken01")] = destination
            repository.stored[alias("taken02")] = destination
            val aliases = listOf("taken01", "taken02", "free003").map(::alias).iterator()

            val alias = service { aliases.next() }.shorten(destination, customAlias = null)

            assertEquals(alias("free003"), alias)
        }

    @Test
    fun `Gives up after 5 random aliases are taken`() =
        runBlocking {
            repository.stored[alias("taken01")] = destination
            var attempts = 0

            assertThrows<IllegalStateException> {
                service {
                    attempts++
                    alias("taken01")
                }.shorten(destination, customAlias = null)
            }
            assertEquals(5, attempts)
        }
}
