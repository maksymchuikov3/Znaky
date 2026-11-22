package com.znaky.feature.practice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.znaky.core.designsystem.component.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopicsScreen(
    onTopicClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TopicsViewModel = hiltViewModel(),
) {
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.practice_topics_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.practice_back))
                    }
                },
            )
        },
    ) { padding ->
        val items = topics
        if (items == null) {
            LoadingBox(Modifier.padding(padding))
        } else {
            LazyColumn(contentPadding = padding) {
                items(items, key = { it.topic.id }) { item ->
                    TopicRow(item, onClick = { onTopicClick(item.topic.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TopicRow(item: TopicItem, onClick: () -> Unit) {
    val total = item.topic.questionCount
    val correct = item.progress?.correctlyAnsweredQuestions ?: 0
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        overlineContent = { Text(item.topic.number) },
        headlineContent = { Text(item.topic.title) },
        supportingContent = {
            Column {
                Text(stringResource(R.string.practice_topic_progress, correct, total))
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else correct.toFloat() / total },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        },
    )
}
