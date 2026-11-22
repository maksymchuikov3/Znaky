package com.znaky.core.domain.repository

import com.znaky.core.model.LicenseCategory
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val licenseCategory: Flow<LicenseCategory>

    suspend fun setLicenseCategory(category: LicenseCategory)
}
