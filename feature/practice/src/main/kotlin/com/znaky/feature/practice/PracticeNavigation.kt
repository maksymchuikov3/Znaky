package com.znaky.feature.practice

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object TopicsRoute

/** Either a topic session ([topicId] set) or a spaced-repetition review session. */
@Serializable
data class PracticeRoute(val topicId: String? = null, val review: Boolean = false)

fun NavController.navigateToTopics() = navigate(TopicsRoute)

fun NavController.navigateToReview() = navigate(PracticeRoute(review = true))

fun NavGraphBuilder.practiceScreens(navController: NavController, onBack: () -> Unit) {
    composable<TopicsRoute> {
        TopicsScreen(onTopicClick = { navController.navigate(PracticeRoute(topicId = it)) }, onBack = onBack)
    }
    composable<PracticeRoute> {
        PracticeScreen(onBack = onBack)
    }
}
