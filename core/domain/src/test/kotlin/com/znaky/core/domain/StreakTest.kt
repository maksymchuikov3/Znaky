package com.znaky.core.domain

import com.znaky.core.domain.progress.streakLength
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakTest {
    @Test fun countsDaysEndingToday() = assertEquals(3, streakLength(setOf(8L, 9L, 10L), 10L))

    @Test fun todayNotStartedKeepsYesterdaysStreak() = assertEquals(2, streakLength(setOf(8L, 9L), 10L))

    @Test fun gapBreaksStreak() = assertEquals(1, streakLength(setOf(7L, 10L), 10L))

    @Test fun noActivity() = assertEquals(0, streakLength(emptySet(), 10L))
}
