package com.znaky.core.domain.progress

/**
 * Days in a row with at least one answer, ending today. Today still counts as
 * "not over yet": if the user has not practised today, the streak ends yesterday.
 */
fun streakLength(activeDays: Set<Long>, todayEpochDay: Long): Int {
    var day = todayEpochDay
    if (day !in activeDays) day -= 1
    var streak = 0
    while (day in activeDays) { streak++; day-- }
    return streak
}
