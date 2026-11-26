package com.znaky.feature.home

import com.znaky.core.model.ExamBlock
import com.znaky.core.model.LicenseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookTest {
    private val specs = listOf(
        ChapterSpec("theory:pdr-1", "1", "Загальні положення", PART_RULES),
        ChapterSpec("theory:pdr-2", "2", "Обов'язки водіїв", PART_RULES),
        ChapterSpec("theory:safe-1", "Б1", "Безпечне водіння", "Безпека руху"),
    )
    private val route = listOf(
        PlanTopic("tr-02", "2", "Обов'язки водіїв", completion = 0.95f, questions = 10),
        PlanTopic("rs-01", "Б1", "Основи безпечного водіння", completion = 0f, block = ExamBlock.ROAD_SAFETY, questions = 4),
        PlanTopic("fa-01", "Д1", "Домедична допомога", completion = 0f, block = ExamBlock.FIRST_AID, questions = 2),
    )

    @Test
    fun `a chapter is done once it is read and its questions are learned`() {
        val book = buildBook(specs, route, read = setOf("theory:pdr-1", "theory:pdr-2"), LicenseCategory.B)
        assertTrue(book[0].done) // read, no questions
        assertTrue(book[1].done) // read, 95% right
        assertEquals("tr-02", book[1].topicId)
        assertFalse(book[2].done)
        assertEquals(2, book.indexOfFirst { !it.done })
    }

    @Test
    fun `unread chapter is not done even when its questions are learned`() {
        val book = buildBook(specs, route, read = emptySet(), LicenseCategory.B)
        assertFalse(book[1].done)
        assertEquals(0, book.indexOfFirst { !it.done })
    }

    @Test
    fun `block topics attach to their theory group or become chapters at the end`() {
        val book = buildBook(specs, route, read = emptySet(), LicenseCategory.B)
        assertEquals("rs-01", book[2].topicId)
        assertEquals(4, book.size)
        assertEquals("fa-01", book.last().topicId)
        assertEquals(null, book.last().readTarget)
    }

    @Test
    fun `chapters without text or questions do not block the way`() {
        val book = buildBook(listOf(ChapterSpec(null, "1", "Загальні положення", PART_RULES)), emptyList(), emptySet(), LicenseCategory.B)
        assertTrue(book.single().done)
    }
}
