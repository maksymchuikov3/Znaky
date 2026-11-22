package com.znaky.feature.practice

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object TopicsRoute

/** Either a topic session ([topicId] set) or a spaced-repetition review session. */
@Serializable
data class PracticeRoute(val topicId: String? = null, val review: Boolean = false)

fun NavController.navigateToTopics(navOptions: NavOptions? = null) = navigate(TopicsRoute, navOptions)

fun NavController.navigateToReview() = navigate(PracticeRoute(review = true))

fun NavController.navigateToTopic(topicId: String) = navigate(PracticeRoute(topicId = topicId))

fun NavGraphBuilder.practiceScreens(navController: NavController, onBack: () -> Unit) {
    composable<TopicsRoute> {
        TopicsScreen(onTopicClick = { navController.navigateToTopic(it) }, onBack = onBack)
    }
    composable<PracticeRoute> {
        PracticeScreen(onBack = onBack)
    }
}
