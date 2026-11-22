package com.znaky.core.model

data class Question(
    val id: String,
    val topicId: String,
    val block: ExamBlock,
    val text: String,
    val answers: List<Answer>,
    val correctAnswerId: String,
    /** Asset path of the illustration, if the question has one. */
    val imageAsset: String? = null,
    /** Road rules points this question is based on, e.g. "12.4". */
    val ruleRefs: List<String> = emptyList(),
    /** Our own explanation. Never copied from third-party services. */
    val explanation: String? = null,
    val categories: Set<LicenseCategory> = emptySet(),
) {
    init {
        require(answers.size >= 2) { "Question $id must have at least two answers" }
        require(answers.any { it.id == correctAnswerId }) {
            "Question $id: correct answer $correctAnswerId is not among its answers"
        }
    }

    fun isCorrect(answerId: String): Boolean = answerId == correctAnswerId
}

data class Answer(
    val id: String,
    val text: String,
)
