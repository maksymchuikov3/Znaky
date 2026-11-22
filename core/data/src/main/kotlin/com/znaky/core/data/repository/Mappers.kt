package com.znaky.core.data.repository

import com.znaky.core.data.content.QuestionDto
import com.znaky.core.data.content.TopicDto
import com.znaky.core.database.dao.TopicProgressRow
import com.znaky.core.database.dao.TopicWithCount
import com.znaky.core.database.entity.ExamResultEntity
import com.znaky.core.database.entity.QuestionEntity
import com.znaky.core.database.entity.TopicEntity
import com.znaky.core.model.Answer
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.ExamResult
import com.znaky.core.model.LicenseCategory
import com.znaky.core.model.Question
import com.znaky.core.model.Topic
import com.znaky.core.model.TopicProgress
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val json = Json
private val answersSerializer = ListSerializer(com.znaky.core.data.content.AnswerDto.serializer())

internal fun TopicDto.toEntity(sortOrder: Int) = TopicEntity(id, number, title, block, sortOrder)

internal fun QuestionDto.toEntity(block: String) = QuestionEntity(
    id = id,
    topicId = topicId,
    block = block,
    text = text,
    answersJson = json.encodeToString(answersSerializer, answers),
    correctAnswerId = correctAnswerId,
    imageAsset = image,
    ruleRefs = ruleRefs.joinToString(","),
    explanation = explanation,
    categories = categories.joinToString(","),
)

internal fun QuestionEntity.toModel() = Question(
    id = id,
    topicId = topicId,
    block = ExamBlock.valueOf(block),
    text = text,
    answers = json.decodeFromString(answersSerializer, answersJson).map { Answer(it.id, it.text) },
    correctAnswerId = correctAnswerId,
    imageAsset = imageAsset,
    ruleRefs = ruleRefs.split(',').filter { it.isNotBlank() },
    explanation = explanation,
    categories = categories.split(',').filter { it.isNotBlank() }.map(LicenseCategory::valueOf).toSet(),
)

internal fun TopicWithCount.toModel() = Topic(id, number, title, ExamBlock.valueOf(block), questionCount)

internal fun TopicEntity.toModel() = Topic(id, number, title, ExamBlock.valueOf(block))

internal fun TopicProgressRow.toModel() =
    TopicProgress(topicId, totalQuestions, answeredQuestions, correctlyAnsweredQuestions)

internal fun ExamResultEntity.toModel() =
    ExamResult(id, startedAtMillis, finishedAtMillis, totalQuestions, correctAnswers, mistakes, passed)

internal fun ExamResult.toEntity() =
    ExamResultEntity(id, startedAtMillis, finishedAtMillis, totalQuestions, correctAnswers, mistakes, passed)
