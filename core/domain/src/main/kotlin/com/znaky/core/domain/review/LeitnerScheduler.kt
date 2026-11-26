package com.znaky.core.domain.review

import com.znaky.core.model.ReviewCard

/**
 * Spaced repetition with Leitner boxes. A correct answer moves the question one box up
 * (1 → 3 → 7 → 14 days, then learned); a mistake sends it back to box 1, due today.
 */
class LeitnerScheduler(
    /** Days until the next review, indexed by box - 1. */
    private val intervalsDays: List<Int> = listOf(0, 1, 3, 7, 14, 60),
) {
    init {
        require(intervalsDays.size == ReviewCard.MAX_BOX)
    }

    fun schedule(previous: ReviewCard?, questionId: String, wasCorrect: Boolean, todayEpochDay: Long): ReviewCard {
        val box = when {
            !wasCorrect -> ReviewCard.MIN_BOX
            previous == null -> ReviewCard.MIN_BOX + 1
            else -> (previous.box + 1).coerceAtMost(ReviewCard.MAX_BOX)
        }
        return ReviewCard(
            questionId = questionId,
            box = box,
            dueEpochDay = todayEpochDay + intervalsDays[box - 1],
        )
    }
}
