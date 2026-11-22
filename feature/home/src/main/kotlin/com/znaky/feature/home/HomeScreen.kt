package com.znaky.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.domain.readiness.ReadinessCalculator.Level
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeScreen(
    onStartExam: () -> Unit,
    onOpenTopics: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenStats: () -> Unit,
) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HomeScreen(state, onStartExam, onOpenTopics, onOpenReview, onOpenStats)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onStartExam: () -> Unit,
    onOpenTopics: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenStats: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) }) { padding ->
        when (state) {
            HomeUiState.Loading -> LoadingBox(Modifier.padding(padding))
            HomeUiState.ContentError -> Text(
                stringResource(R.string.home_content_error),
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            is HomeUiState.Ready -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReadinessCard(state)
                MenuCard(Icons.Default.Timer, stringResource(R.string.home_exam), stringResource(R.string.home_exam_subtitle), onStartExam)
                MenuCard(Icons.AutoMirrored.Filled.List, stringResource(R.string.home_topics), null, onOpenTopics)
                MenuCard(
                    Icons.Default.Refresh,
                    stringResource(R.string.home_review),
                    stringResource(R.string.home_review_subtitle, state.dueForReview),
                    onOpenReview,
                )
                MenuCard(Icons.Default.Insights, stringResource(R.string.home_stats), null, onOpenStats)
                Text(
                    stringResource(R.string.home_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReadinessCard(state: HomeUiState.Ready) {
    val readiness = state.readiness
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.home_readiness), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.home_readiness_score, readiness.score),
                style = MaterialTheme.typography.displaySmall,
            )
            LinearProgressIndicator(progress = { readiness.score / 100f }, modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(
                    when (readiness.level) {
                        Level.TOO_EARLY -> R.string.home_level_too_early
                        Level.GETTING_THERE -> R.string.home_level_getting_there
                        Level.ALMOST -> R.string.home_level_almost
                        Level.READY -> R.string.home_level_ready
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MenuCard(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = if (subtitle != null) { { Text(subtitle) } } else null,
            leadingContent = { Icon(icon, contentDescription = null) },
        )
    }
}
