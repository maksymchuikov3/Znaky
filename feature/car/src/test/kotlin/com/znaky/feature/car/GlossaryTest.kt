package com.znaky.feature.car

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlossaryTest {
    @Test
    fun everyChapterHasTermsAndNumbersAreInOrder() {
        TermCategory.entries.forEachIndexed { i, ch ->
            assertEquals(i + 1, ch.number)
            assertTrue("${ch.name} is empty", GlossaryTerms.any { it.category == ch })
        }
    }

    @Test
    fun chapterOneHasAllRulesTerms() {
        // ПДР п. 1.10 defines 79 terms; chapter 1 retells each of them.
        assertEquals(79, GlossaryTerms.count { it.category == TermCategory.PDR })
    }

    @Test
    fun termsAreUniqueAndFilledIn() {
        val names = GlossaryTerms.map { it.term.lowercase() }
        assertEquals(names.size, names.toSet().size)
        GlossaryTerms.forEach {
            assertTrue(it.term, it.text.length > 10)
            assertTrue(it.term, it.source.isNotBlank())
        }
    }

    @Test
    fun searchFindsByNameSynonymAndText() {
        assertEquals("Обгін", filterGlossary(GlossaryTerms, null, "обгін").first().term)
        assertTrue(filterGlossary(GlossaryTerms, null, "ДВЗ").any { it.term == "Двигун внутрішнього згоряння" })
        assertTrue(filterGlossary(GlossaryTerms, TermCategory.WATER, "обгін").isEmpty())
    }

    @Test
    fun termsCountDeclinesInUkrainian() {
        assertEquals("1 термін", termsCount(1))
        assertEquals("3 терміни", termsCount(3))
        assertEquals("11 термінів", termsCount(11))
        assertEquals("415 термінів", termsCount(415))
    }
}
