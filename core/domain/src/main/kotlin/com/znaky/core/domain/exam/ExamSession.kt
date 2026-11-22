package com.znaky.core.domain.exam

import com.znaky.core.model.Question

/**
 * Immutable state of one exam attempt. Every action returns a new session, which
 * keeps the logic easy to test and easy to hold in a ViewModel's StateFlow.
 *
 * Rules mirrored from the real exam:
 * - an answer cannot be changed once given;
 * - the attempt ends as soon as mistakes exceed [ExamRules.maxMistakes];
 * - the attempt ends when time runs out.
 */
data class ExamSession(
    val rules: ExamRules,
    val questions: List<Question>,
    val startedAtMillis: Long,
    /** questionId -> answerId */
    val answers: Map<String, String> = emptyMap(),
    val currentIndex: Int = 0,
    val finishedAtMillis: Long? = null,
    val finishReason: FinishReason? = null,
) {
    enum class FinishReason { ALL_ANSWERED, TOO_MANY_MISTAKES, TIME_OUT, ABANDONED }

    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)

    val mistakes: Int get() = questions.count { q -> answers[q.id]?.let { !q.isCorrect(it) } ?: false }

    val correctAnswers: Int get() = questions.count { q -> answers[q.id]?.let(q::isCorrect) ?: false }

    val isFinished: Boolean get() = finishReason != null

    /** Passed only if every question was answered within the allowed mistakes. */
    val isPassed: Boolean
        get() = finishReason == FinishReason.ALL_ANSWERED && mistakes <= rules.maxMistakes

    fun remainingSeconds(nowMillis: Long): Int {
        val elapsed = ((nowMillis - startedAtMillis) / 1000).toInt()
        return (rules.durationSeconds - elapsed).coerceAtLeast(0)
    }

    fun answer(questionId: String, answerId: String, nowMillis: Long): ExamSession {
        if (isFinished || questionId in answers) return this
        val question = questions.firstOrNull { it.id == questionId } ?: return this
        require(question.answers.any { it.id == answerId }) { "Unknown answer $answerId" }

        val timedOut = tick(nowMillis)
        if (timedOut.isFinished) return timedOut

        val updated = copy(answers = answers + (questionId to answerId))
        return when {
            updated.mistakes > rules.maxMistakes ->
                updated.finish(FinishReason.TOO_MANY_MISTAKES, nowMillis)
            updated.answers.size == questions.size ->
                updated.finish(FinishReason.ALL_ANSWERED, nowMillis)
            else -> updated.copy(currentIndex = updated.nextUnansweredIndex(after = currentIndex))
        }
    }

    /** Moves to any question, e.g. from the ticket's number strip. */
    fun goTo(index: Int): ExamSession =
        if (isFinished || index !in questions.indices) this else copy(currentIndex = index)

    /** Call periodically; ends the attempt when time is up. */
    fun tick(nowMillis: Long): ExamSession =
        if (!isFinished && remainingSeconds(nowMillis) == 0) finish(FinishReason.TIME_OUT, nowMillis) else this

    fun abandon(nowMillis: Long): ExamSession =
        if (isFinished) this else finish(FinishReason.ABANDONED, nowMillis)

    private fun finish(reason: FinishReason, nowMillis: Long) =
        copy(finishReason = reason, finishedAtMillis = nowMillis)

    private fun nextUnansweredIndex(after: Int): Int {
        val n = questions.size
        for (step in 1..n) {
            val i = (after + step) % n
            if (questions[i].id !in answers) return i
        }
        return after
    }

    companion object {
        fun start(rules: ExamRules, questions: List<Question>, nowMillis: Long): ExamSession {
            require(questions.isNotEmpty()) { "An exam needs at least one question" }
            return ExamSession(rules = rules, questions = questions, startedAtMillis = nowMillis)
        }
    }
}
