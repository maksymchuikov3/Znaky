package com.znaky.core.domain.reminder

/** Schedules the one daily study reminder. Implemented by the app with the system alarm service. */
interface ReminderScheduler {
    /** Reminds every day at [hour]:00 local time; null cancels the reminder. */
    fun schedule(hour: Int?)
}
