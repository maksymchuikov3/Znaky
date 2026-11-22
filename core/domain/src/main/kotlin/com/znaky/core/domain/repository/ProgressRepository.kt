package com.znaky.core.domain.repository

import com.znaky.core.model.AnswerRecord
import com.znaky.core.model.ExamResult
import com.znaky.core.model.TopicProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    /** Records an answer and updates the question's spaced-repetition card. */
    suspend fun recordAnswer(record: AnswerRecord)

    suspend fun saveExamResult(result: ExamResult)

    fun observeExamResults(): Flow<List<ExamResult>>

    fun observeTopicProgress(): Flow<List<TopicProgress>>

    /** Ids of questions due for review today, most urgent first. */
    fun observeDueForReview(): Flow<List<String>>
}
