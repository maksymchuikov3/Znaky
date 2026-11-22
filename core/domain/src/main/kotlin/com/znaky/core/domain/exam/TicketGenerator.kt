package com.znaky.core.domain.exam

import com.znaky.core.model.Question
import kotlin.random.Random

/**
 * Builds a random exam ticket following [ExamRules.blockDistribution].
 * If a block has too few questions (e.g. a partial content pack), the gap is filled
 * from the remaining pool so the ticket still has [ExamRules.questionCount] questions
 * whenever the pool is large enough.
 */
class TicketGenerator(
    private val rules: ExamRules = ExamRules.Default,
    private val random: Random = Random.Default,
    /** Shuffle answer order so users learn the content, not the position. */
    private val shuffleAnswers: Boolean = true,
) {
    fun generate(pool: List<Question>): List<Question> {
        val byBlock = pool.groupBy { it.block }
        val picked = LinkedHashSet<Question>()
        rules.blockDistribution.forEach { (block, count) ->
            byBlock[block].orEmpty().shuffled(random).take(count).forEach(picked::add)
        }
        if (picked.size < rules.questionCount) {
            pool.filterNot { it in picked }
                .shuffled(random)
                .take(rules.questionCount - picked.size)
                .forEach(picked::add)
        }
        return picked.map { q -> if (shuffleAnswers) q.copy(answers = q.answers.shuffled(random)) else q }
    }
}
