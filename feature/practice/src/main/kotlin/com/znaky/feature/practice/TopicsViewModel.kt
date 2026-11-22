package com.znaky.feature.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.Topic
import com.znaky.core.model.TopicProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TopicItem(val topic: Topic, val progress: TopicProgress?)

@HiltViewModel
class TopicsViewModel @Inject constructor(
    questions: QuestionRepository,
    progress: ProgressRepository,
) : ViewModel() {
    val topics: StateFlow<List<TopicItem>?> = combine(
        questions.observeTopics(),
        progress.observeTopicProgress(),
    ) { topics, progressList ->
        val byTopic = progressList.associateBy { it.topicId }
        topics.map { TopicItem(it, byTopic[it.id]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
