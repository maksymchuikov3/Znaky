package com.znaky.feature.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.model.AnswerRecord
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.Question
import com.znaky.core.model.ReviewCard
import com.znaky.core.model.BOAT_TOPIC_PREFIX
import com.znaky.core.model.MOTO_TOPIC_PREFIX
import com.znaky.core.model.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Questions in a «20 випадкових» session. */
internal const val RANDOM_COUNT = 20

data class PracticeUiState(
    val mode: String = PracticeMode.TOPIC,
    val title: String = "",
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val index: Int = 0,
    val selectedAnswerId: String? = null,
    /** Per question: true/false once answered, null before. */
    val results: List<Boolean?> = emptyList(),
    /** Topic id to its label, e.g. «Розділ 16 · Проїзд перехресть». */
    val topicLabels: Map<String, String> = emptyMap(),
    /** Rule point to clause text, quoted under the explanation. */
    val rules: Map<String, String> = emptyMap(),
    /** Time of the latest mistake per question, captured when the session starts. */
    val lastMistakes: Map<String, Long> = emptyMap(),
    /** The repetition card after the current answer (review mode shows its track). */
    val cardAfter: ReviewCard? = null,
    val isFinished: Boolean = false,
) {
    val current: Question? get() = questions.getOrNull(index)
    val revealed: Boolean get() = selectedAnswerId != null
    val correctCount: Int get() = results.count { it == true }
}

internal fun ExamBlock.shortTitle() = when (this) {
    ExamBlock.TRAFFIC_RULES -> "ПДР"
    ExamBlock.ROAD_SAFETY -> "Безпека руху"
    ExamBlock.VEHICLE -> "Будова авто"
    ExamBlock.FIRST_AID -> "Домедична допомога"
}

/** «Розділ 16 · Проїзд перехресть» for rule sections, «Мото · …», «Судноводій · …» for the extra courses, «Будова авто · Гальма» otherwise. */
internal fun Topic.label(): String = when {
    id.startsWith(MOTO_TOPIC_PREFIX) -> "Мото · $title"
    id.startsWith(BOAT_TOPIC_PREFIX) -> "Судноводій · $title"
    number.toIntOrNull() != null -> "Розділ $number · $title"
    else -> "${block.shortTitle()} · $title"
}

@HiltViewModel
class PracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val questions: QuestionRepository,
    private val progress: ProgressRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<PracticeRoute>()
    private val _state = MutableStateFlow(PracticeUiState(mode = route.mode))
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    val bookmarks: StateFlow<Set<String>> = progress.observeBookmarks()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    init {
        viewModelScope.launch {
            questions.ensureContentLoaded()
            val list: List<Question> = when (route.mode) {
                PracticeMode.REVIEW -> questions.getQuestions(progress.observeDueForReview().first())
                PracticeMode.RANDOM -> questions.getQuestionPool(settings.licenseCategory.first()).shuffled().take(RANDOM_COUNT)
                PracticeMode.MISTAKES -> questions.getQuestions(progress.observeMistakes().first().keys.toList())
                PracticeMode.BOOKMARKS -> questions.getQuestions(progress.observeBookmarks().first())
                    .filter { q -> route.filter == null || blockFilter(route.filter, q.block) }
                else -> route.topicId?.let { questions.getQuestionsForTopic(it).shuffled() }.orEmpty()
            }
            val topics = questions.observeTopics().first()
            val title = when (route.mode) {
                PracticeMode.REVIEW -> "Повторення"
                PracticeMode.RANDOM -> "$RANDOM_COUNT випадкових"
                PracticeMode.MISTAKES -> "Мої помилки"
                PracticeMode.BOOKMARKS -> "Закладки"
                else -> topics.firstOrNull { it.id == route.topicId }?.title ?: "Тема"
            }
            _state.value = PracticeUiState(
                mode = route.mode,
                title = title,
                isLoading = false,
                questions = list.map { it.withShuffledAnswers() },
                results = List(list.size) { null },
                topicLabels = topics.associate { it.id to it.label() },
                rules = questions.getRuleClauses(),
                lastMistakes = progress.observeLastMistakes().first(),
            )
        }
    }

    fun answer(answerId: String) {
        val s = _state.value
        val question = s.current ?: return
        if (s.revealed) return
        val correct = question.isCorrect(answerId)
        _state.update { st ->
            st.copy(selectedAnswerId = answerId, results = st.results.toMutableList().also { it[st.index] = correct }, cardAfter = null)
        }
        viewModelScope.launch {
            val card = progress.recordAnswer(AnswerRecord(question.id, answerId, correct, System.currentTimeMillis()))
            _state.update { if (it.current?.id == question.id) it.copy(cardAfter = card) else it }
        }
    }

    fun toggleBookmark() {
        val id = _state.value.current?.id ?: return
        val saved = id in bookmarks.value
        viewModelScope.launch { progress.setBookmarked(id, !saved) }
    }

    fun next() = _state.update {
        if (it.index + 1 >= it.questions.size) it.copy(isFinished = true)
        else it.copy(index = it.index + 1, selectedAnswerId = null, cardAfter = null)
    }
}

/** Bookmark filters: «rules» = road rules and safety, «vehicle» = vehicle and first aid. */
internal fun blockFilter(filter: String, block: ExamBlock): Boolean = when (filter) {
    "rules" -> block == ExamBlock.TRAFFIC_RULES || block == ExamBlock.ROAD_SAFETY
    "vehicle" -> block == ExamBlock.VEHICLE || block == ExamBlock.FIRST_AID
    else -> true
}

/** Answer order is shuffled each time so users learn the content, not the position. */
internal fun Question.withShuffledAnswers(): Question = copy(answers = answers.shuffled())
