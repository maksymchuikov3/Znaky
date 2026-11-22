package com.znaky.core.domain

import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.readiness.ReadinessCalculator.Level
import com.znaky.core.model.ExamResult
import com.znaky.core.model.TopicProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadinessCalculatorTest {
    private val calc = ReadinessCalculator()

    private fun exam(correct: Int, at: Long) =
        ExamResult(startedAtMillis = at, finishedAtMillis = at, totalQuestions = 20, correctAnswers = correct, mistakes = 20 - correct, passed = correct >= 18)

    @Test
    fun `too early before three exams`() {
        val r = calc.calculate(listOf(exam(20, 1), exam(20, 2)), listOf(TopicProgress("t", 10, 10, 10)))
        assertEquals(Level.TOO_EARLY, r.level)
    }

    @Test
    fun `ready after three passed exams and full coverage`() {
        val r = calc.calculate((1L..3L).map { exam(19, it) }, listOf(TopicProgress("t", 10, 10, 10)))
        assertEquals(Level.READY, r.level)
        assertEquals(97, r.score)
    }

    @Test
    fun `a recent failed exam blocks READY`() {
        val exams = listOf(exam(19, 1), exam(19, 2), exam(15, 3))
        val r = calc.calculate(exams, listOf(TopicProgress("t", 10, 10, 10)))
        assertEquals(Level.ALMOST, r.level)
    }
}
