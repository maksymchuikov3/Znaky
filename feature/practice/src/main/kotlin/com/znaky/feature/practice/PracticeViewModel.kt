package com.znaky.feature.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.AnswerRecord
import com.znaky.core.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PracticeUiState(
    val title: String? = null,
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val index: Int = 0,
    val selectedAnswerId: String? = null,
    val correctCount: Int = 0,
    val isFinished: Boolean = false,
) {
    val current: Question? get() = questions.getOrNull(index)
    val revealed: Boolean get() = selectedAnswerId != null
}

@HiltViewModel
class PracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val questions: QuestionRepository,
    private val progress: ProgressRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<PracticeRoute>()
    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            questions.ensureContentLoaded()
            val list = when {
                route.review -> questions.getQuestions(progress.observeDueForReview().first())
                route.topicId != null -> questions.getQuestionsForTopic(route.topicId).shuffled()
                else -> emptyList()
            }
            val title = route.topicId?.let { questions.getTopic(it)?.title }
            _state.value = PracticeUiState(title = title, isLoading = false, questions = list.map { it.withShuffledAnswers() })
        }
    }

    fun answer(answerId: String) {
        val s = _state.value
        val question = s.current ?: return
        if (s.revealed) return
        val correct = question.isCorrect(answerId)
        _state.update { it.copy(selectedAnswerId = answerId, correctCount = it.correctCount + if (correct) 1 else 0) }
        viewModelScope.launch {
            progress.recordAnswer(AnswerRecord(question.id, answerId, correct, System.currentTimeMillis()))
        }
    }

    fun next() = _state.update {
        if (it.index + 1 >= it.questions.size) it.copy(isFinished = true)
        else it.copy(index = it.index + 1, selectedAnswerId = null)
    }
}

/** Answer order is shuffled each time so users learn the content, not the position. */
internal fun Question.withShuffledAnswers(): Question = copy(answers = answers.shuffled())
