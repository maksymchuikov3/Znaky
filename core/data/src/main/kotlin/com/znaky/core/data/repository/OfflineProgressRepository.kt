package com.znaky.core.data.repository

import com.znaky.core.database.dao.ProgressDao
import com.znaky.core.database.entity.AnswerLogEntity
import com.znaky.core.database.entity.ReviewCardEntity
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.review.LeitnerScheduler
import com.znaky.core.model.AnswerRecord
import com.znaky.core.model.ExamResult
import com.znaky.core.model.ReviewCard
import com.znaky.core.model.TopicProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineProgressRepository @Inject constructor(
    private val dao: ProgressDao,
    private val scheduler: LeitnerScheduler,
) : ProgressRepository {

    private fun today(): Long = LocalDate.now().toEpochDay()

    override suspend fun recordAnswer(record: AnswerRecord) {
        dao.insertAnswer(
            AnswerLogEntity(
                questionId = record.questionId,
                answerId = record.answerId,
                isCorrect = record.isCorrect,
                answeredAtMillis = record.answeredAtMillis,
            ),
        )
        val previous = dao.getReviewCard(record.questionId)?.let { ReviewCard(it.questionId, it.box, it.dueEpochDay) }
        val next = scheduler.schedule(previous, record.questionId, record.isCorrect, today())
        dao.upsertReviewCard(ReviewCardEntity(next.questionId, next.box, next.dueEpochDay))
    }

    override suspend fun saveExamResult(result: ExamResult) = dao.insertExamResult(result.toEntity())

    override fun observeExamResults(): Flow<List<ExamResult>> =
        dao.observeExamResults().map { rows -> rows.map { it.toModel() } }

    override fun observeTopicProgress(): Flow<List<TopicProgress>> =
        dao.observeTopicProgress().map { rows -> rows.map { it.toModel() } }

    override fun observeDueForReview(): Flow<List<String>> = dao.observeDue(today())

    override fun observeActiveDays(sinceEpochDay: Long): Flow<Set<Long>> {
        val offset = TimeZone.getDefault().rawOffset.toLong()
        val since = sinceEpochDay * 86_400_000L - offset
        return dao.observeActiveDays(since, offset).map { it.toSet() }
    }

    override fun observeAnswerCountSince(epochDay: Long): Flow<Int> =
        dao.observeAnswerCount(epochDay * 86_400_000L - TimeZone.getDefault().rawOffset.toLong())
}
