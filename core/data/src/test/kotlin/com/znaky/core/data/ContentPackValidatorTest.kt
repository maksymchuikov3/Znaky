package com.znaky.core.data

import com.znaky.core.data.content.AnswerDto
import com.znaky.core.data.content.ContentPack
import com.znaky.core.data.content.ContentPackValidator
import com.znaky.core.data.content.QuestionDto
import com.znaky.core.data.content.TopicDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentPackValidatorTest {
    private val topic = TopicDto("t1", "1", "Topic", "TRAFFIC_RULES")
    private fun question(id: String = "q1", correct: String = "a", topicId: String = "t1", categories: List<String> = listOf("B")) =
        QuestionDto(id, topicId, "Text", listOf(AnswerDto("a", "A"), AnswerDto("b", "B")), correct, categories = categories)

    private fun problems(vararg q: QuestionDto) = ContentPackValidator.validate(ContentPack(1, "test", listOf(topic), q.toList()))

    @Test
    fun `valid pack has no problems`() = assertEquals(emptyList<String>(), problems(question()))

    @Test
    fun `detects wrong correct answer`() = assertEquals(1, problems(question(correct = "z")).size)

    @Test
    fun `detects unknown topic and category`() = assertEquals(2, problems(question(topicId = "nope", categories = listOf("X"))).size)

    @Test
    fun `detects duplicate ids`() = assertEquals(1, problems(question(), question()).size)
}
