package com.znaky.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.znaky.core.designsystem.component.QuestionContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PracticeScreen(onBack: () -> Unit, viewModel: PracticeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title ?: stringResource(R.string.practice_review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.practice_back))
                    }
                },
            )
        },
    ) { padding ->
        val current = state.current
        when {
            state.isLoading -> LoadingBox(Modifier.padding(padding))
            state.questions.isEmpty() -> Message(stringResource(R.string.practice_empty), Modifier.padding(padding))
            state.isFinished -> Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.practice_done_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.practice_done_summary, state.correctCount, state.questions.size))
                Button(onClick = onBack) { Text(stringResource(R.string.practice_back)) }
            }
            current != null -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.practice_counter, state.index + 1, state.questions.size),
                    style = MaterialTheme.typography.labelLarge,
                )
                QuestionContent(
                    question = current,
                    selectedAnswerId = state.selectedAnswerId,
                    revealed = state.revealed,
                    showExplanation = true,
                    onAnswer = viewModel::answer,
                )
                if (state.revealed) {
                    val last = state.index + 1 >= state.questions.size
                    Button(onClick = viewModel::next, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(if (last) R.string.practice_finish else R.string.practice_next))
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
}
