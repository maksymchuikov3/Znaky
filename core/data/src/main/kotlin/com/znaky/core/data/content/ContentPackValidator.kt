package com.znaky.core.data.content

import com.znaky.core.model.ExamBlock
import com.znaky.core.model.LicenseCategory

/** Catches broken content before it reaches users. Returns a list of human-readable problems. */
object ContentPackValidator {
    fun validate(pack: ContentPack): List<String> = buildList {
        val blocks = ExamBlock.entries.map { it.name }.toSet()
        val categories = LicenseCategory.entries.map { it.name }.toSet()
        val topicIds = pack.topics.map { it.id }

        topicIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            .forEach { add("Duplicate topic id: $it") }
        pack.topics.filter { it.block !in blocks }
            .forEach { add("Topic ${it.id}: unknown block ${it.block}") }

        pack.questions.map { it.id }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            .forEach { add("Duplicate question id: $it") }

        pack.questions.forEach { q ->
            if (q.topicId !in topicIds) add("Question ${q.id}: unknown topic ${q.topicId}")
            if (q.text.isBlank()) add("Question ${q.id}: empty text")
            if (q.answers.size < 2) add("Question ${q.id}: fewer than two answers")
            if (q.answers.map { it.id }.toSet().size != q.answers.size) add("Question ${q.id}: duplicate answer ids")
            if (q.answers.none { it.id == q.correctAnswerId }) add("Question ${q.id}: correct answer not in answers")
            if (q.categories.isEmpty()) add("Question ${q.id}: no licence categories")
            q.categories.filter { it !in categories }.forEach { add("Question ${q.id}: unknown category $it") }
            if (pack.rules.isNotEmpty()) {
                q.ruleRefs.filter { it !in pack.rules }.forEach { add("Question ${q.id}: rule $it has no clause text") }
            }
        }
    }
}
