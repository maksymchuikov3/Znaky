package com.znaky.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Decides the first screen: onboarding on the first launch, home afterwards. */
@HiltViewModel
class AppViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
    val onboardingDone: StateFlow<Boolean?> = settings.onboardingDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
