package com.znaky.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.znaky.core.database.entity.ContentMetaEntity
import com.znaky.core.database.entity.QuestionEntity
import com.znaky.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

data class TopicWithCount(
    val id: String,
    val number: String,
    val title: String,
    val block: String,
    val questionCount: Int,
)

@Dao
interface ContentDao {
    @Query(
        """
        SELECT t.id, t.number, t.title, t.block, COUNT(q.id) AS questionCount
        FROM topics t LEFT JOIN questions q ON q.topicId = t.id
        GROUP BY t.id ORDER BY t.sortOrder
        """,
    )
    fun observeTopics(): Flow<List<TopicWithCount>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getTopic(id: String): TopicEntity?

    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    suspend fun getQuestionsForTopic(topicId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestions(ids: Collection<String>): List<QuestionEntity>

    /** Categories are stored comma-separated; wrap in commas to match whole tokens. */
    @Query("SELECT * FROM questions WHERE ',' || categories || ',' LIKE '%,' || :category || ',%'")
    suspend fun getQuestionsForCategory(category: String): List<QuestionEntity>

    @Query("SELECT * FROM content_meta WHERE `key` = 'content'")
    suspend fun getMeta(): ContentMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(meta: ContentMetaEntity)

    @Query("DELETE FROM questions")
    suspend fun deleteQuestions()

    @Query("DELETE FROM topics")
    suspend fun deleteTopics()

    /** Replaces all content atomically. User progress lives in other tables and is kept. */
    @Transaction
    suspend fun replaceContent(topics: List<TopicEntity>, questions: List<QuestionEntity>, meta: ContentMetaEntity) {
        deleteQuestions()
        deleteTopics()
        insertTopics(topics)
        insertQuestions(questions)
        upsertMeta(meta)
    }
}
