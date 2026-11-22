package com.znaky.core.domain

import com.znaky.core.model.Answer
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.Question

/** Builds a question whose correct answer is "a"; answer "b" is wrong. */
fun testQuestion(id: String, block: ExamBlock = ExamBlock.TRAFFIC_RULES) = Question(
    id = id,
    topicId = "t-${block.name}",
    block = block,
    text = "Question $id",
    answers = listOf(Answer("a", "Right"), Answer("b", "Wrong")),
    correctAnswerId = "a",
)

fun testPool(perBlock: Int = 30): List<Question> =
    ExamBlock.entries.flatMap { block -> (1..perBlock).map { testQuestion("${block.name}-$it", block) } }
