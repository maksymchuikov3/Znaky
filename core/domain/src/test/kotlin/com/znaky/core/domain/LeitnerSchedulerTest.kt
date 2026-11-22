package com.znaky.core.domain

import com.znaky.core.domain.review.LeitnerScheduler
import com.znaky.core.model.ReviewCard
import org.junit.Assert.assertEquals
import org.junit.Test

class LeitnerSchedulerTest {
    private val scheduler = LeitnerScheduler()

    @Test
    fun `mistake resets to box 1 due today`() {
        val card = scheduler.schedule(ReviewCard("q", 4, 100), "q", wasCorrect = false, todayEpochDay = 50)
        assertEquals(ReviewCard("q", 1, 50), card)
    }

    @Test
    fun `correct answer moves one box up`() {
        val card = scheduler.schedule(ReviewCard("q", 2, 0), "q", wasCorrect = true, todayEpochDay = 10)
        assertEquals(ReviewCard("q", 3, 13), card)
    }

    @Test
    fun `box is capped at max`() {
        val card = scheduler.schedule(ReviewCard("q", 5, 0), "q", wasCorrect = true, todayEpochDay = 0)
        assertEquals(ReviewCard.MAX_BOX, card.box)
    }
}
