package com.znaky.core.domain.repository

import com.znaky.core.model.LicenseCategory
import kotlinx.coroutines.flow.Flow

/** When the user plans to take the exam, chosen during onboarding. */
enum class ExamHorizon(val label: String, val days: Int?) {
    TWO_WEEKS("За 2 тижні", 14),
    MONTH("За місяць", 30),
    TWO_MONTHS("За 2 місяці", 60),
    UNKNOWN("Ще не знаю", null),
}

/** App colours: light by default, or dark, or following the phone. */
enum class ThemeMode(val label: String) {
    LIGHT("Світла"),
    DARK("Темна"),
    SYSTEM("Як у телефоні"),
}

interface SettingsRepository {
    val licenseCategory: Flow<LicenseCategory>

    suspend fun setLicenseCategory(category: LicenseCategory)

    /** Whether the three onboarding steps were completed (or skipped). */
    val onboardingDone: Flow<Boolean>

    suspend fun setOnboardingDone(done: Boolean)

    val examHorizon: Flow<ExamHorizon>

    suspend fun setExamHorizon(horizon: ExamHorizon)

    /** Hour of the daily reminder (0..23), or null when reminders are off. */
    val reminderHour: Flow<Int?>

    suspend fun setReminderHour(hour: Int?)

    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
