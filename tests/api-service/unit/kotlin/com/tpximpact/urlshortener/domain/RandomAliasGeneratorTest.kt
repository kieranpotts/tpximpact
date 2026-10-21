package com.tpximpact.urlshortener.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.security.SecureRandom

// Requirement: R4
class RandomAliasGeneratorTest {
    private val generator = RandomAliasGenerator()

    @Test
    fun `Makes aliases of 7 letters and digits`() {
        repeat(1_000) {
            val alias = generator.next().value

            assertTrue(alias.matches(Regex("[A-Za-z0-9]{7}")), "Unexpected alias $alias")
        }
    }

    @Test
    fun `Doesn't repeat itself`() {
        val aliases = List(10_000) { generator.next() }

        assertEquals(aliases.size, aliases.toSet().size)
    }

    @Test
    fun `Draws from all 62 letters and digits`() {
        val characters = List(1_000) { generator.next().value }.joinToString("").toSet()

        assertEquals(RandomAliasGenerator.ALPHABET.toSet(), characters)
    }

    @Test
    fun `Never makes a reserved alias`() {
        // A random source that spells out "shorten", then "aaaaaaa".
        val indices = ("shorten" + "aaaaaaa").map(RandomAliasGenerator.ALPHABET::indexOf).iterator()
        val scripted =
            object : SecureRandom() {
                override fun nextInt(bound: Int) = indices.next()
            }

        assertEquals("aaaaaaa", RandomAliasGenerator(scripted).next().value)
    }
}
