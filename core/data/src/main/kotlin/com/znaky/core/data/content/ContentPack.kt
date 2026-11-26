package com.znaky.core.data.content

import kotlinx.serialization.Serializable

/**
 * On-disk format of a content pack (assets/content/questions.json).
 * The schema is documented in content/README.md at the repository root.
 */
@Serializable
data class ContentPack(
    /** Bump on every content change; a higher version replaces the stored content. */
    val version: Int,
    /** Edition of the road rules the pack is based on, shown to the user. */
    val rulesEdition: String,
    val topics: List<TopicDto>,
    val questions: List<QuestionDto>,
    /** Rule point ("12.4") to the text of that clause, quoted under explanations. */
    val rules: Map<String, String> = emptyMap(),
)

@Serializable
data class TopicDto(
    val id: String,
    val number: String,
    val title: String,
    val block: String,
)

@Serializable
data class QuestionDto(
    val id: String,
    val topicId: String,
    val text: String,
    val answers: List<AnswerDto>,
    val correctAnswerId: String,
    val image: String? = null,
    val ruleRefs: List<String> = emptyList(),
    val explanation: String? = null,
    val categories: List<String>,
)

@Serializable
data class AnswerDto(
    val id: String,
    val text: String,
)
