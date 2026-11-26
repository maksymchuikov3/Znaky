package com.znaky.core.domain.repository

import com.znaky.core.model.AnswerRecord
import com.znaky.core.model.ExamResult
import com.znaky.core.model.ReviewCard
import com.znaky.core.model.TopicProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    /** Records an answer and updates the question's spaced-repetition card. */
    suspend fun recordAnswer(record: AnswerRecord): ReviewCard

    /** Spaced-repetition cards of every question answered at least once. */
    fun observeReviewCards(): Flow<List<ReviewCard>>

    /** Question id to the time of its latest answer, for questions whose latest answer was wrong; newest first. */
    fun observeMistakes(): Flow<Map<String, Long>>

    /** Question id to the time of its latest wrong answer, even if answered correctly since. */
    fun observeLastMistakes(): Flow<Map<String, Long>>

    /** Bookmarked question ids, newest first. */
    fun observeBookmarks(): Flow<List<String>>

    suspend fun setBookmarked(questionId: String, bookmarked: Boolean)

    suspend fun saveExamResult(result: ExamResult)

    fun observeExamResults(): Flow<List<ExamResult>>

    fun observeTopicProgress(): Flow<List<TopicProgress>>

    /** Ids of questions due for review today, most urgent first. */
    fun observeDueForReview(): Flow<List<String>>

    /** Local epoch days (since 1970-01-01) on which the user answered at least one question. */
    fun observeActiveDays(sinceEpochDay: Long): Flow<Set<Long>>

    /** Number of answers given since local midnight of [epochDay] (the daily goal counter). */
    fun observeAnswerCountSince(epochDay: Long): Flow<Int>
}
