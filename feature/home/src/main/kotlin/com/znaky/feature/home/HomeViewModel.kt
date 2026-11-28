package com.znaky.feature.home

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.progress.streakLength
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.LicenseCategory
import com.znaky.core.model.Topic
import com.znaky.core.model.TopicProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** A topic counts as learned when 90% of its questions were last answered correctly. */
internal const val LEARNED = 0.9f

/** How many questions each block contributes to the 20-question exam ticket. */
internal fun ExamBlock.examShare() = when (this) {
    ExamBlock.TRAFFIC_RULES -> 10
    ExamBlock.ROAD_SAFETY -> 4
    ExamBlock.VEHICLE -> 4
    ExamBlock.FIRST_AID -> 2
}

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

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object ContentError : HomeUiState

    @Immutable
    data class Ready(
        val chapters: List<BookChapter>,
        /** Index of the chapter to do next; -1 once the whole book is done. */
        val current: Int,
        /** Bookmarked questions plus questions last answered wrong. */
        val saved: Int,
        val streak: Int,
        val category: LicenseCategory = LicenseCategory.B,
    ) : HomeUiState {
        val doneCount: Int get() = chapters.count { it.done }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    questions: QuestionRepository,
    progress: ProgressRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val contentState = MutableStateFlow<Boolean?>(null)
    private val readState = MutableStateFlow(readChapters(context))
    private val today = LocalDate.now().toEpochDay()

    init {
        viewModelScope.launch {
            contentState.value = runCatching { questions.ensureContentLoaded() }.isSuccess
        }
    }

    /** Reading progress lives outside Room, so the screen asks for a refresh when it comes back. */
    fun refresh() {
        readState.value = readChapters(context)
    }

    fun setCategory(category: LicenseCategory) {
        viewModelScope.launch { settings.setLicenseCategory(category) }
    }

    private val book = settings.licenseCategory
        .mapLatest { category -> category to loadBook(context, category) }
        .flowOn(Dispatchers.IO)

    private val route = combine(questions.observeTopics(), progress.observeTopicProgress()) { topics, p -> studyRoute(topics, p) }

    private val saved = combine(progress.observeBookmarks(), progress.observeMistakes()) { b, m -> (b.toSet() + m.keys).size }

    // 60 days back is enough to count a realistic streak.
    private val streak = combine(progress.observeActiveDays(today - 60), saved) { days, s -> streakLength(days, today) to s }

    val uiState: StateFlow<HomeUiState> = combine(contentState, book, route, readState, streak) { loaded, b, r, read, st ->
        when (loaded) {
            null -> HomeUiState.Loading
            false -> HomeUiState.ContentError
            true -> {
                val (category, specs) = b
                val chapters = buildBook(specs, r, read, category)
                HomeUiState.Ready(chapters, chapters.indexOfFirst { !it.done }, st.second, st.first, category)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}
