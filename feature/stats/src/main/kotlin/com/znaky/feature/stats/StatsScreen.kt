package com.znaky.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.theme.FeedbackColors
import kotlinx.serialization.Serializable
import java.text.DateFormat
import java.util.Date

@Serializable
data object StatsRoute

fun NavController.navigateToStats() = navigate(StatsRoute)

fun NavGraphBuilder.statsScreen(onBack: () -> Unit) {
    composable<StatsRoute> { StatsScreen(onBack) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatsScreen(onBack: () -> Unit, viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.stats_back))
                    }
                },
            )
        },
    ) { padding ->
        val s = state
        if (s == null) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.stats_readiness, s.readiness.score), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.stats_coverage, (s.readiness.topicCoverage * 100).toInt()))
            Text(stringResource(R.string.stats_exams_taken, s.readiness.examsTaken))
            Text(stringResource(R.string.stats_recent_exams), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            if (s.recentExams.isEmpty()) {
                Text(stringResource(R.string.stats_no_exams))
            }
            val format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            s.recentExams.forEach { exam ->
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(
                                R.string.stats_exam_row,
                                format.format(Date(exam.finishedAtMillis)),
                                exam.correctAnswers,
                                exam.totalQuestions,
                            ),
                        )
                    },
                    trailingContent = {
                        Text(
                            stringResource(if (exam.passed) R.string.stats_passed else R.string.stats_failed),
                            color = if (exam.passed) FeedbackColors.Correct else FeedbackColors.Wrong,
                        )
                    },
                )
            }
        }
    }
}
