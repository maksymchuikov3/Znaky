package com.znaky.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.znaky.core.database.entity.AnswerLogEntity
import com.znaky.core.database.entity.BookmarkEntity
import com.znaky.core.database.entity.ExamResultEntity
import com.znaky.core.database.entity.ReviewCardEntity
import kotlinx.coroutines.flow.Flow

data class TopicProgressRow(
    val topicId: String,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctlyAnsweredQuestions: Int,
)

data class MistakeRow(val questionId: String, val answeredAtMillis: Long)

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

    /** Distinct days with answers; [offsetMillis] shifts UTC to the user's time zone. */
    @Query(
        "SELECT DISTINCT (answeredAtMillis + :offsetMillis) / 86400000 FROM answer_log " +
            "WHERE answeredAtMillis >= :sinceMillis",
    )
    fun observeActiveDays(sinceMillis: Long, offsetMillis: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM answer_log WHERE answeredAtMillis >= :sinceMillis")
    fun observeAnswerCount(sinceMillis: Long): Flow<Int>

    @Query("SELECT * FROM review_cards")
    fun observeReviewCards(): Flow<List<ReviewCardEntity>>

    /** Questions whose latest answer was wrong, newest mistake first, with the time of that answer. */
    @Query(
        """
        SELECT a.questionId AS questionId, a.answeredAtMillis AS answeredAtMillis FROM answer_log a
        WHERE a.id = (SELECT MAX(b.id) FROM answer_log b WHERE b.questionId = a.questionId) AND a.isCorrect = 0
        ORDER BY a.answeredAtMillis DESC
        """,
    )
    fun observeMistakes(): Flow<List<MistakeRow>>

    /** Time of the latest wrong answer per question. */
    @Query("SELECT questionId AS questionId, MAX(answeredAtMillis) AS answeredAtMillis FROM answer_log WHERE isCorrect = 0 GROUP BY questionId")
    fun observeLastMistakes(): Flow<List<MistakeRow>>

    @Query("SELECT questionId FROM bookmarks ORDER BY createdAtMillis DESC")
    fun observeBookmarks(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE questionId = :questionId")
    suspend fun deleteBookmark(questionId: String)

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
               SUM(CASE WHEN latest.isCorrect IS NOT NULL THEN 1 ELSE 0 END) AS answeredQuestions,
               SUM(CASE WHEN latest.isCorrect = 1 THEN 1 ELSE 0 END) AS correctlyAnsweredQuestions
        FROM questions q
        LEFT JOIN (
            SELECT a.questionId, a.isCorrect FROM answer_log a
            WHERE a.id = (SELECT MAX(b.id) FROM answer_log b WHERE b.questionId = a.questionId)
        ) latest ON latest.questionId = q.id
        GROUP BY q.topicId
        """,
    )
    fun observeTopicProgress(): Flow<List<TopicProgressRow>>
}
