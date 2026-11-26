package com.znaky.core.domain.repository

import com.znaky.core.model.LicenseCategory
import com.znaky.core.model.Question
import com.znaky.core.model.Topic
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun observeTopics(): Flow<List<Topic>>

    suspend fun getTopic(topicId: String): Topic?

    suspend fun getQuestionsForTopic(topicId: String): List<Question>

    suspend fun getQuestions(ids: Collection<String>): List<Question>

    suspend fun getQuestionPool(category: LicenseCategory): List<Question>

    /** Rule point ("12.4") to its clause text, quoted under explanations. */
    suspend fun getRuleClauses(): Map<String, String>

    /** Loads bundled content into local storage if it is missing or outdated. */
    suspend fun ensureContentLoaded()
}
