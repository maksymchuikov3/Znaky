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
}
