package com.znaky.core.data

import com.znaky.core.data.content.ContentPack
import com.znaky.core.data.content.ContentPackValidator
import com.znaky.core.domain.exam.ExamRules
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the bundled content: CI fails if questions.json is broken. */
class BundledContentTest {
    private val pack: ContentPack = Json.decodeFromString(
        ContentPack.serializer(),
        File("src/main/assets/content/questions.json").readText(),
    )

    @Test
    fun `bundled content is valid`() {
        assertEquals(emptyList<String>(), ContentPackValidator.validate(pack))
    }

    @Test
    fun `category B has enough questions for a full ticket`() {
        val forB = pack.questions.count { "B" in it.categories }
        assertTrue("Only $forB questions for B", forB >= ExamRules.Default.questionCount)
    }

    @Test
    fun `moto and boat learners have enough questions for a full ticket`() {
        for (category in listOf("A", "A1", "BOAT")) {
            val n = pack.questions.count { category in it.categories }
            assertTrue("Only $n questions for $category", n >= ExamRules.Default.questionCount)
        }
    }

    @Test
    fun `every moto and boat topic has questions only for its learners`() {
        for (topic in pack.topics.filter { it.id.startsWith("mo-") || it.id.startsWith("bt-") }) {
            val questions = pack.questions.filter { it.topicId == topic.id }
            assertTrue("${topic.id} has no questions", questions.isNotEmpty())
            val allowed = if (topic.id.startsWith("bt-")) setOf("BOAT") else setOf("A", "A1")
            questions.forEach { assertEquals("${it.id} categories", allowed, it.categories.toSet()) }
        }
    }
}
