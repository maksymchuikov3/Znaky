package com.znaky.feature.practice

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object TopicsRoute

/** «Повторення»: what is due today and how questions travel to «Вивчено». */
@Serializable
data object RepetitionRoute

/** Saved questions. */
@Serializable
data object BookmarksRoute

/** Which questions a practice session takes. */
object PracticeMode {
    const val TOPIC = "topic"
    const val REVIEW = "review"
    const val RANDOM = "random"
    const val MISTAKES = "mistakes"
    const val BOOKMARKS = "bookmarks"
}

/**
 * A practice session without a timer. [topicId] is used in [PracticeMode.TOPIC];
 * [filter] narrows bookmarks to one exam block (null = all).
 */
@Serializable
data class PracticeRoute(
    val mode: String = PracticeMode.TOPIC,
    val topicId: String? = null,
    val filter: String? = null,
)

fun NavController.navigateToTopics(navOptions: NavOptions? = null) = navigate(TopicsRoute, navOptions)

fun NavController.navigateToReview() = navigate(PracticeRoute(mode = PracticeMode.REVIEW))

fun NavController.navigateToTopic(topicId: String) = navigate(PracticeRoute(mode = PracticeMode.TOPIC, topicId = topicId))

fun NavController.navigateToPractice(mode: String) = navigate(PracticeRoute(mode = mode))

fun NavGraphBuilder.practiceScreens(navController: NavController, onBack: () -> Unit, onOpenTopics: () -> Unit) {
    composable<TopicsRoute> {
        TopicsScreen(onTopicClick = { navController.navigateToTopic(it) }, onBack = onBack)
    }
    composable<PracticeRoute> {
        PracticeScreen(onBack = onBack)
    }
    composable<RepetitionRoute> {
        RepetitionScreen(
            onBack = onBack,
            onStart = { navController.navigateToReview() },
            onMistakes = { navController.navigateToPractice(PracticeMode.MISTAKES) },
            onBookmarks = { navController.navigate(BookmarksRoute) },
            onWeakTopics = onOpenTopics,
        )
    }
    composable<BookmarksRoute> {
        BookmarksScreen(
            onBack = onBack,
            onStart = { filter -> navController.navigate(PracticeRoute(mode = PracticeMode.BOOKMARKS, filter = filter)) },
        )
    }
}
