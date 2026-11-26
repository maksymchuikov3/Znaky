package com.znaky.feature.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.progress.streakLength
import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.LicenseCategory
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

/** Readiness score from which the app suggests going to the real exam. */
internal const val READY_THRESHOLD = 85

/** A topic counts as learned when 90% of its questions were last answered correctly. */
internal const val LEARNED = 0.9f

/** How many questions each block contributes to the 20-question exam ticket. */
internal fun ExamBlock.examShare() = when (this) {
    ExamBlock.TRAFFIC_RULES -> 10
    ExamBlock.ROAD_SAFETY -> 4
    ExamBlock.VEHICLE -> 4
    ExamBlock.FIRST_AID -> 2
}

/** Order of the stops on the road to the exam (the shortest block first). */
internal val StopOrder = listOf(ExamBlock.FIRST_AID, ExamBlock.TRAFFIC_RULES, ExamBlock.ROAD_SAFETY, ExamBlock.VEHICLE)

/** A topic on the study route, in the order of the road rules. */
@Immutable
data class PlanTopic(
    val id: String,
    val number: String,
    val title: String,
    val completion: Float,
    val block: ExamBlock = ExamBlock.TRAFFIC_RULES,
    val questions: Int = 0,
)

/** Topics in route order (by rule section number), with their completion. */
internal fun studyRoute(topics: List<Topic>, progress: List<TopicProgress>): List<PlanTopic> {
    val byId = progress.associateBy { it.topicId }
    return topics
        .sortedWith(compareBy({ it.number.substringBefore('.').toIntOrNull() ?: Int.MAX_VALUE }, { it.number }))
        .map {
            val p = byId[it.id]
            PlanTopic(it.id, it.number, it.title, p?.completion ?: 0f, it.block, p?.totalQuestions ?: it.questionCount)
        }
}

/** Share of a block's questions last answered correctly. */
internal fun blockCompletion(route: List<PlanTopic>): Map<ExamBlock, Float> =
    ExamBlock.entries.associateWith { block ->
        val topics = route.filter { it.block == block }
        val total = topics.sumOf { it.questions }
        if (total == 0) 0f else topics.sumOf { (it.completion * it.questions).toDouble() }.toFloat() / total
    }

/** The next topic: the first unlearned one, walking the stops in [StopOrder]. */
internal fun nextTopic(route: List<PlanTopic>): PlanTopic? =
    StopOrder.firstNotNullOfOrNull { block -> route.firstOrNull { it.block == block && it.completion < LEARNED } }

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object ContentError : HomeUiState

    @Immutable
    data class Ready(
        val readiness: ReadinessCalculator.Readiness,
        /** Of the last [recentExams] mock exams (at most 3), how many were passed. */
        val passedRecent: Int,
        val recentExams: Int,
        val nextTopic: PlanTopic?,
        val blocks: Map<ExamBlock, Float>,
        val mistakes: Int,
        val dueForReview: Int,
        val streak: Int,
        val category: LicenseCategory = LicenseCategory.B,
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    questions: QuestionRepository,
    progress: ProgressRepository,
    calculator: ReadinessCalculator,
    settings: SettingsRepository,
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
        val route = studyRoute(topics, topicProgress)
        StudyPart(calculator.calculate(exams, topicProgress), recent.count { it.passed }, recent.size, nextTopic(route), blockCompletion(route))
    }

    private val daily = combine(
        progress.observeDueForReview(),
        progress.observeMistakes(),
        // 60 days back is enough to count a realistic streak.
        progress.observeActiveDays(today - 60),
    ) { due, mistakes, activeDays -> Triple(due.size, mistakes.size, streakLength(activeDays, today)) }

    val uiState: StateFlow<HomeUiState> = combine(contentState, study, daily, settings.licenseCategory) { loaded, s, d, category ->
        when (loaded) {
            null -> HomeUiState.Loading
            false -> HomeUiState.ContentError
            true -> HomeUiState.Ready(
                readiness = s.readiness,
                passedRecent = s.passed,
                recentExams = s.recent,
                nextTopic = s.next,
                blocks = s.blocks,
                dueForReview = d.first,
                mistakes = d.second,
                streak = d.third,
                category = category,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}

private data class StudyPart(
    val readiness: ReadinessCalculator.Readiness,
    val passed: Int,
    val recent: Int,
    val next: PlanTopic?,
    val blocks: Map<ExamBlock, Float>,
)
