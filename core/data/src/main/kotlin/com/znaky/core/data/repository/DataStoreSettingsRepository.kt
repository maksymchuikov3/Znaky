package com.znaky.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.znaky.core.domain.repository.SettingsRepository
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

    override val licenseCategory: Flow<LicenseCategory> = dataStore.data.map { prefs ->
        prefs[categoryKey]?.let { runCatching { LicenseCategory.valueOf(it) }.getOrNull() } ?: LicenseCategory.B
    }

    override suspend fun setLicenseCategory(category: LicenseCategory) {
        dataStore.edit { it[categoryKey] = category.name }
    }
}
