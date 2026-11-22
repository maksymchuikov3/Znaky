package com.znaky.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object ContentError : HomeUiState
    data class Ready(val readiness: ReadinessCalculator.Readiness, val dueForReview: Int) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    questions: QuestionRepository,
    progress: ProgressRepository,
    calculator: ReadinessCalculator,
) : ViewModel() {
    private val contentState = MutableStateFlow<Boolean?>(null)

    init {
        viewModelScope.launch {
            contentState.value = runCatching { questions.ensureContentLoaded() }.isSuccess
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        contentState,
        progress.observeExamResults(),
        progress.observeTopicProgress(),
        progress.observeDueForReview(),
    ) { loaded, exams, topics, due ->
        when (loaded) {
            null -> HomeUiState.Loading
            false -> HomeUiState.ContentError
            true -> HomeUiState.Ready(calculator.calculate(exams, topics), due.size)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}
