package com.znaky.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val number: String,
    val title: String,
    val block: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(entity = TopicEntity::class, parentColumns = ["id"], childColumns = ["topicId"])],
    indices = [Index("topicId")],
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val block: String,
    val text: String,
    /** JSON array of {id, text}; see Converters. */
    val answersJson: String,
    val correctAnswerId: String,
    val imageAsset: String?,
    /** Comma-separated rule points, e.g. "12.4,12.6". */
    val ruleRefs: String,
    val explanation: String?,
    /** Comma-separated licence categories, e.g. "B,C". */
    val categories: String,
)

@Entity(tableName = "answer_log", indices = [Index("questionId")])
data class AnswerLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val answerId: String,
    val isCorrect: Boolean,
    val answeredAtMillis: Long,
)

@Entity(tableName = "review_cards")
data class ReviewCardEntity(
    @PrimaryKey val questionId: String,
    val box: Int,
    val dueEpochDay: Long,
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val mistakes: Int,
    val passed: Boolean,
)

/** Single-row table with the version of the bundled content that is loaded. */
@Entity(tableName = "content_meta")
data class ContentMetaEntity(
    @PrimaryKey val key: String = KEY,
    val version: Int,
    val rulesEdition: String,
) {
    companion object {
        const val KEY = "content"
    }
}
