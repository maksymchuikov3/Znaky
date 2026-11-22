package com.znaky.feature.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.progress.streakLength
import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.ExamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class WeekDay(val label: String, val active: Boolean, val isToday: Boolean)

@Immutable
data class WeakTopic(val id: String, val title: String, val completion: Float)

@Immutable
data class StatsUiState(
    val readiness: ReadinessCalculator.Readiness,
    val week: List<WeekDay>,
    val streak: Int,
    /** Oldest first, for the bar chart. */
    val recentExams: List<ExamResult>,
    val weakTopics: List<WeakTopic>,
)

private val dayLabels = listOf("П", "В", "С", "Ч", "П", "С", "Н")

@HiltViewModel
class StatsViewModel @Inject constructor(
    progress: ProgressRepository,
    questions: QuestionRepository,
    calculator: ReadinessCalculator,
) : ViewModel() {
    private val today = LocalDate.now()
    private val monday = today.with(DayOfWeek.MONDAY)

    val state: StateFlow<StatsUiState?> = combine(
        progress.observeExamResults(),
        progress.observeTopicProgress(),
        questions.observeTopics(),
        // 60 days back is enough to count a realistic streak.
        progress.observeActiveDays(today.minusDays(60).toEpochDay()),
    ) { exams, topicProgress, topics, activeDays ->
        val week = (0..6).map { i ->
            val day = monday.plusDays(i.toLong())
            WeekDay(dayLabels[i], day.toEpochDay() in activeDays, day == today)
        }
        val streak = streakLength(activeDays, today.toEpochDay())

        val titles = topics.associate { it.id to it.title }
        val weak = topicProgress
            .filter { it.totalQuestions > 0 && it.completion < 0.9f }
            .sortedBy { it.completion }
            .take(3)
            .map { WeakTopic(it.topicId, titles[it.topicId] ?: it.topicId, it.completion) }

        StatsUiState(
            readiness = calculator.calculate(exams, topicProgress),
            week = week,
            streak = streak,
            recentExams = exams.take(8).reversed(),
            weakTopics = weak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
