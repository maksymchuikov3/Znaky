package com.znaky.core.domain

import com.znaky.core.domain.exam.ExamRules
import com.znaky.core.domain.exam.ExamSession
import com.znaky.core.domain.exam.ExamSession.FinishReason
import com.znaky.core.domain.exam.TicketGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ExamSessionTest {
    private val rules = ExamRules.Default
    private val questions = TicketGenerator(rules, Random(1)).generate(testPool())

    private fun answerAll(session: ExamSession, wrong: Int): ExamSession {
        var s = session
        questions.forEachIndexed { i, q -> s = s.answer(q.id, if (i < wrong) "b" else "a", nowMillis = 1_000L + i) }
        return s
    }

    @Test
    fun `ticket follows the 10-4-4-2 distribution`() {
        assertEquals(20, questions.size)
        val byBlock = questions.groupingBy { it.block }.eachCount()
        assertEquals(rules.blockDistribution, byBlock)
    }

    @Test
    fun `two mistakes still pass`() {
        val s = answerAll(ExamSession.start(rules, questions, 0), wrong = 2)
        assertEquals(FinishReason.ALL_ANSWERED, s.finishReason)
        assertTrue(s.isPassed)
        assertEquals(18, s.correctAnswers)
    }

    @Test
    fun `third mistake ends the exam immediately`() {
        var s = ExamSession.start(rules, questions, 0)
        questions.take(3).forEach { s = s.answer(it.id, "b", 1_000) }
        assertEquals(FinishReason.TOO_MANY_MISTAKES, s.finishReason)
        assertFalse(s.isPassed)
        assertEquals(3, s.answers.size)
    }

    @Test
    fun `answer cannot be changed`() {
        val q = questions.first()
        val s = ExamSession.start(rules, questions, 0)
            .answer(q.id, "b", 1)
            .answer(q.id, "a", 2)
        assertEquals("b", s.answers[q.id])
    }

    @Test
    fun `time out finishes and fails the exam`() {
        val s = ExamSession.start(rules, questions, 0).tick(rules.durationSeconds * 1000L)
        assertEquals(FinishReason.TIME_OUT, s.finishReason)
        assertFalse(s.isPassed)
    }

    @Test
    fun `answer after time is up is ignored`() {
        val q = questions.first()
        val s = ExamSession.start(rules, questions, 0).answer(q.id, "a", rules.durationSeconds * 1000L + 1)
        assertEquals(FinishReason.TIME_OUT, s.finishReason)
        assertTrue(s.answers.isEmpty())
    }

    @Test
    fun `skipped question is offered again after the last one`() {
        var s = ExamSession.start(rules, questions, 0).goTo(1)
        questions.drop(1).forEach { s = s.answer(it.id, "a", 10) }
        assertEquals(0, s.currentIndex)
        assertFalse(s.isFinished)
    }

    @Test
    fun `small pool is filled from other blocks`() {
        val pool = testPool(perBlock = 30).filter { it.block.name != "FIRST_AID" }
        assertEquals(20, TicketGenerator(rules, Random(2)).generate(pool).distinct().size)
    }
}
