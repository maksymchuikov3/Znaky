package com.znaky.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.model.ExamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val readiness: ReadinessCalculator.Readiness,
    val recentExams: List<ExamResult>,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    progress: ProgressRepository,
    calculator: ReadinessCalculator,
) : ViewModel() {
    val state: StateFlow<StatsUiState?> = combine(
        progress.observeExamResults(),
        progress.observeTopicProgress(),
    ) { exams, topics ->
        StatsUiState(calculator.calculate(exams, topics), exams.take(10))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
