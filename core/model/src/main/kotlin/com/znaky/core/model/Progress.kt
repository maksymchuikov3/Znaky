package com.znaky.core.model

/** One answer the user gave, in practice or in an exam. */
data class AnswerRecord(
    val questionId: String,
    val answerId: String,
    val isCorrect: Boolean,
    val answeredAtMillis: Long,
)

/** Spaced-repetition state of a single question (Leitner box system). */
data class ReviewCard(
    val questionId: String,
    /** 1..[MAX_BOX]. Box 1 = needs review soon, higher boxes = well known. */
    val box: Int,
    /** Epoch day (days since 1970-01-01) on which the question is due for review. */
    val dueEpochDay: Long,
) {
    companion object {
        const val MIN_BOX = 1
        const val MAX_BOX = 5
    }
}

data class ExamResult(
    val id: Long = 0,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val mistakes: Int,
    val passed: Boolean,
)

/** Per-topic progress, used for the topic list and readiness. */
data class TopicProgress(
    val topicId: String,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctlyAnsweredQuestions: Int,
) {
    val completion: Float
        get() = if (totalQuestions == 0) 0f else correctlyAnsweredQuestions.toFloat() / totalQuestions
}
