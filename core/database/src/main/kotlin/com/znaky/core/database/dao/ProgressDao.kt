package com.znaky.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.znaky.core.database.entity.AnswerLogEntity
import com.znaky.core.database.entity.ExamResultEntity
import com.znaky.core.database.entity.ReviewCardEntity
import kotlinx.coroutines.flow.Flow

data class TopicProgressRow(
    val topicId: String,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctlyAnsweredQuestions: Int,
)

@Dao
interface ProgressDao {
    @Insert
    suspend fun insertAnswer(answer: AnswerLogEntity)

    @Query("SELECT * FROM review_cards WHERE questionId = :questionId")
    suspend fun getReviewCard(questionId: String): ReviewCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReviewCard(card: ReviewCardEntity)

    @Query("SELECT questionId FROM review_cards WHERE dueEpochDay <= :todayEpochDay ORDER BY box, dueEpochDay")
    fun observeDue(todayEpochDay: Long): Flow<List<String>>

    @Insert
    suspend fun insertExamResult(result: ExamResultEntity)

    @Query("SELECT * FROM exam_results ORDER BY finishedAtMillis DESC")
    fun observeExamResults(): Flow<List<ExamResultEntity>>

    /**
     * A question counts as "correct" when its latest answer was correct.
     */
    @Query(
        """
        SELECT q.topicId AS topicId,
               COUNT(q.id) AS totalQuestions,
               SUM(CASE WHEN last.isCorrect IS NOT NULL THEN 1 ELSE 0 END) AS answeredQuestions,
               SUM(CASE WHEN last.isCorrect = 1 THEN 1 ELSE 0 END) AS correctlyAnsweredQuestions
        FROM questions q
        LEFT JOIN (
            SELECT a.questionId, a.isCorrect FROM answer_log a
            WHERE a.id = (SELECT MAX(b.id) FROM answer_log b WHERE b.questionId = a.questionId)
        ) last ON last.questionId = q.id
        GROUP BY q.topicId
        """,
    )
    fun observeTopicProgress(): Flow<List<TopicProgressRow>>
}
