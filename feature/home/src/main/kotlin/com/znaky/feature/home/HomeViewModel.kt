package com.znaky.feature.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.progress.streakLength
import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.Topic
import com.znaky.core.model.TopicProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Questions per day the home screen asks for. */
internal const val DAILY_GOAL = 20

/** Readiness score from which the app suggests going to the real exam. */
internal const val READY_THRESHOLD = 85

/** A topic on the study route, in the order of the road rules. */
@Immutable
data class PlanTopic(val id: String, val number: String, val title: String, val completion: Float)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object ContentError : HomeUiState

    @Immutable
    data class Ready(
        val readiness: ReadinessCalculator.Readiness,
        /** Of the last [recentExams] mock exams (at most 3), how many were passed. */
        val passedRecent: Int,
        val recentExams: Int,
        /** First topic of the route that is not learned yet; null when all are done. */
        val nextTopic: PlanTopic?,
        val dueForReview: Int,
        val answersToday: Int,
        val streak: Int,
    ) : HomeUiState
}

/** Topics in route order (by rule section number), with their completion. */
internal fun studyRoute(topics: List<Topic>, progress: List<TopicProgress>): List<PlanTopic> {
    val byId = progress.associateBy { it.topicId }
    return topics
        .sortedWith(compareBy({ it.number.substringBefore('.').toIntOrNull() ?: Int.MAX_VALUE }, { it.number }))
        .map { PlanTopic(it.id, it.number, it.title, byId[it.id]?.completion ?: 0f) }
}

/** A topic counts as learned when 90% of its questions were last answered correctly. */
internal const val LEARNED = 0.9f

@HiltViewModel
class HomeViewModel @Inject constructor(
    questions: QuestionRepository,
    progress: ProgressRepository,
    calculator: ReadinessCalculator,
) : ViewModel() {
    private val contentState = MutableStateFlow<Boolean?>(null)
    private val today = LocalDate.now().toEpochDay()

    init {
        viewModelScope.launch {
            contentState.value = runCatching { questions.ensureContentLoaded() }.isSuccess
        }
    }

    private val study = combine(
        progress.observeExamResults(),
        progress.observeTopicProgress(),
        questions.observeTopics(),
    ) { exams, topicProgress, topics ->
        val recent = exams.sortedByDescending { it.finishedAtMillis }.take(3)
        Triple(
            calculator.calculate(exams, topicProgress),
            recent.count { it.passed } to recent.size,
            studyRoute(topics, topicProgress).firstOrNull { it.completion < LEARNED },
        )
    }

    private val daily = combine(
        progress.observeDueForReview(),
        progress.observeAnswerCountSince(today),
        // 60 days back is enough to count a realistic streak.
        progress.observeActiveDays(today - 60),
    ) { due, answers, activeDays -> Triple(due.size, answers, streakLength(activeDays, today)) }

    val uiState: StateFlow<HomeUiState> = combine(contentState, study, daily) { loaded, s, d ->
        when (loaded) {
            null -> HomeUiState.Loading
            false -> HomeUiState.ContentError
            true -> HomeUiState.Ready(
                readiness = s.first,
                passedRecent = s.second.first,
                recentExams = s.second.second,
                nextTopic = s.third,
                dueForReview = d.first,
                answersToday = d.second,
                streak = d.third,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}
