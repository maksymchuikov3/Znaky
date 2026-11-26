package com.znaky.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.reminder.ReminderScheduler
import com.znaky.core.domain.repository.ExamHorizon
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.model.LicenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    /** 0 = welcome, 1 = category, 2 = exam date, 3 = reminder. */
    val step: Int = 0,
    val category: LicenseCategory = LicenseCategory.B,
    val horizon: ExamHorizon = ExamHorizon.MONTH,
    val reminderHour: Int = 19,
    val finished: Boolean = false,
)

/** Daily minutes and study days per week the plan suggests for an exam [ExamHorizon]. */
internal fun ExamHorizon.plan(): Pair<Int, Int> = when (this) {
    ExamHorizon.TWO_WEEKS -> 40 to 6
    ExamHorizon.MONTH -> 25 to 5
    ExamHorizon.TWO_MONTHS -> 15 to 5
    ExamHorizon.UNKNOWN -> 20 to 5
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val reminders: ReminderScheduler,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun next() = _state.update { it.copy(step = (it.step + 1).coerceAtMost(3)) }

    fun back() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }

    fun selectCategory(category: LicenseCategory) = _state.update { it.copy(category = category) }

    fun selectHorizon(horizon: ExamHorizon) = _state.update { it.copy(horizon = horizon) }

    fun selectHour(hour: Int) = _state.update { it.copy(reminderHour = hour) }

    /** Saves the choices; [remind] turns the daily reminder on at the chosen hour. */
    fun finish(remind: Boolean) {
        val s = _state.value
        viewModelScope.launch {
            settings.setLicenseCategory(s.category)
            settings.setExamHorizon(s.horizon)
            val hour = if (remind) s.reminderHour else null
            settings.setReminderHour(hour)
            reminders.schedule(hour)
            settings.setOnboardingDone(true)
            _state.update { it.copy(finished = true) }
        }
    }
}
