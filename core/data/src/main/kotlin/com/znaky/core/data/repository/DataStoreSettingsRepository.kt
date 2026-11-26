package com.znaky.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.znaky.core.domain.repository.ExamHorizon
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.domain.repository.ThemeMode
import com.znaky.core.model.LicenseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    private val categoryKey = stringPreferencesKey("license_category")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val horizonKey = stringPreferencesKey("exam_horizon")
    private val reminderKey = intPreferencesKey("reminder_hour")
    private val themeKey = stringPreferencesKey("theme_mode")

    override val licenseCategory: Flow<LicenseCategory> = dataStore.data.map { prefs ->
        prefs[categoryKey]?.let { runCatching { LicenseCategory.valueOf(it) }.getOrNull() } ?: LicenseCategory.B
    }

    override suspend fun setLicenseCategory(category: LicenseCategory) {
        dataStore.edit { it[categoryKey] = category.name }
    }

    override val onboardingDone: Flow<Boolean> = dataStore.data.map { it[onboardingKey] ?: false }

    override suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { it[onboardingKey] = done }
    }

    override val examHorizon: Flow<ExamHorizon> = dataStore.data.map { prefs ->
        prefs[horizonKey]?.let { runCatching { ExamHorizon.valueOf(it) }.getOrNull() } ?: ExamHorizon.MONTH
    }

    override suspend fun setExamHorizon(horizon: ExamHorizon) {
        dataStore.edit { it[horizonKey] = horizon.name }
    }

    /** Stored as -1 when off. */
    override val reminderHour: Flow<Int?> = dataStore.data.map { prefs -> prefs[reminderKey]?.takeIf { it in 0..23 } }

    override suspend fun setReminderHour(hour: Int?) {
        dataStore.edit { it[reminderKey] = hour ?: -1 }
    }

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[themeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.LIGHT
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[themeKey] = mode.name }
    }
}
