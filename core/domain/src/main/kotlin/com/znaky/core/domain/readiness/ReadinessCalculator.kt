package com.znaky.core.domain.readiness

import com.znaky.core.model.ExamResult
import com.znaky.core.model.TopicProgress

/**
 * Answers "am I ready for the exam?" with a 0..100 score.
 *
 * Score = 60% recent exam performance + 40% topic coverage.
 * The user is considered [Level.READY] only after at least [minExams] mock exams,
 * a high enough score and every one of the last [minExams] exams passed.
 */
class ReadinessCalculator(
    private val minExams: Int = 3,
    private val recentWindow: Int = 5,
) {
    enum class Level { TOO_EARLY, GETTING_THERE, ALMOST, READY }

    data class Readiness(
        val score: Int,
        val level: Level,
        val examsTaken: Int,
        val topicCoverage: Float,
    )

    fun calculate(exams: List<ExamResult>, topics: List<TopicProgress>): Readiness {
        val recent = exams.sortedByDescending { it.finishedAtMillis }.take(recentWindow)
        val examScore = if (recent.isEmpty()) 0f else {
            recent.map { it.correctAnswers.toFloat() / it.totalQuestions.coerceAtLeast(1) }.average().toFloat()
        }
        val totalQuestions = topics.sumOf { it.totalQuestions }
        val coverage = if (totalQuestions == 0) 0f else
            topics.sumOf { it.correctlyAnsweredQuestions }.toFloat() / totalQuestions

        val score = ((examScore * 0.6f + coverage * 0.4f) * 100).toInt().coerceIn(0, 100)
        val lastPassed = recent.size >= minExams && recent.take(minExams).all { it.passed }
        val level = when {
            exams.size < minExams -> Level.TOO_EARLY
            score >= 85 && lastPassed -> Level.READY
            score >= 70 -> Level.ALMOST
            else -> Level.GETTING_THERE
        }
        return Readiness(score = score, level = level, examsTaken = exams.size, topicCoverage = coverage)
    }
}
