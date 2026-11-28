package com.znaky.feature.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.progress.streakLength
import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.domain.repository.ThemeMode
import com.znaky.core.model.ExamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class WeekDay(val label: String, val active: Boolean, val isToday: Boolean)

@Immutable
data class WeakTopic(val id: String, val title: String, val completion: Float)

@Immutable
data class TopicRow(val id: String, val number: String, val title: String, val completion: Float)

@Immutable
data class StatsUiState(
    val readiness: ReadinessCalculator.Readiness,
    val week: List<WeekDay>,
    val streak: Int,
    /** Oldest first, for the bar chart. */
    val recentExams: List<ExamResult>,
    val weakTopics: List<WeakTopic>,
    /** Every topic, weakest first. */
    val topics: List<TopicRow> = emptyList(),
    /** Of the last (at most 5) exams, how many were passed, and how many of the latest in a row. */
    val passedOfLast5: Int = 0,
    val lastExamsCount: Int = 0,
    val passedInARow: Int = 0,
)

private val dayLabels = listOf("П", "В", "С", "Ч", "П", "С", "Н")

@HiltViewModel
class StatsViewModel @Inject constructor(
    progress: ProgressRepository,
    questions: QuestionRepository,
    calculator: ReadinessCalculator,
    private val settings: SettingsRepository,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.LIGHT)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

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
            .take(5)
            .map { WeakTopic(it.topicId, titles[it.topicId] ?: it.topicId, it.completion) }

        val byId = topicProgress.associateBy { it.topicId }
        val rows = topics
            .map { t -> TopicRow(t.id, t.number.takeIf { n -> n.toIntOrNull() != null }.orEmpty(), t.title, byId[t.id]?.completion ?: 0f) }
            .sortedBy { it.completion }
        val last5 = exams.sortedByDescending { it.finishedAtMillis }.take(5)
        StatsUiState(
            readiness = calculator.calculate(exams, topicProgress),
            week = week,
            streak = streak,
            recentExams = exams.take(8).reversed(),
            weakTopics = weak,
            topics = rows,
            passedOfLast5 = last5.count { it.passed },
            lastExamsCount = last5.size,
            passedInARow = last5.takeWhile { it.passed }.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
