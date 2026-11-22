package com.znaky.feature.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.StartExamUseCase
import com.znaky.core.domain.exam.ExamSession
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.AnswerRecord
import com.znaky.core.model.ExamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExamUiState {
    data object Loading : ExamUiState
    data object NotEnoughQuestions : ExamUiState
    data class Running(val session: ExamSession, val remainingSeconds: Int) : ExamUiState
}

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val startExam: StartExamUseCase,
    private val questions: QuestionRepository,
    private val progress: ProgressRepository,
) : ViewModel() {
    private val clock: () -> Long = System::currentTimeMillis
    private val _state = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val state: StateFlow<ExamUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            questions.ensureContentLoaded()
            val session = startExam()
            if (session.questions.size < session.rules.questionCount) {
                _state.value = ExamUiState.NotEnoughQuestions
                return@launch
            }
            _state.value = ExamUiState.Running(session, session.remainingSeconds(clock()))
            runTimer()
        }
    }

    private suspend fun runTimer() {
        while (viewModelScope.isActive) {
            delay(1_000)
            val running = _state.value as? ExamUiState.Running ?: return
            if (running.session.isFinished) return
            update(running.session.tick(clock()))
        }
    }

    fun answer(answerId: String) {
        val running = _state.value as? ExamUiState.Running ?: return
        val question = running.session.currentQuestion ?: return
        val now = clock()
        val next = running.session.answer(question.id, answerId, now)
        if (next.answers[question.id] == answerId && question.id !in running.session.answers) {
            viewModelScope.launch {
                progress.recordAnswer(AnswerRecord(question.id, answerId, question.isCorrect(answerId), now))
            }
        }
        update(next)
    }

    fun goTo(index: Int) {
        val running = _state.value as? ExamUiState.Running ?: return
        update(running.session.goTo(index))
    }

    fun abandon() {
        val running = _state.value as? ExamUiState.Running ?: return
        update(running.session.abandon(clock()))
    }

    private fun update(session: ExamSession) {
        val previous = (_state.value as? ExamUiState.Running)?.session
        _state.value = ExamUiState.Running(session, session.remainingSeconds(clock()))
        if (session.isFinished && previous?.isFinished == false) saveResult(session)
    }

    private fun saveResult(session: ExamSession) = viewModelScope.launch {
        progress.saveExamResult(
            ExamResult(
                startedAtMillis = session.startedAtMillis,
                finishedAtMillis = session.finishedAtMillis ?: clock(),
                totalQuestions = session.questions.size,
                correctAnswers = session.correctAnswers,
                mistakes = session.mistakes,
                passed = session.isPassed,
            ),
        )
    }
}
