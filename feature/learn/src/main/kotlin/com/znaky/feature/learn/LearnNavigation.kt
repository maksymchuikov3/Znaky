package com.znaky.feature.learn

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data object SignsRoute
@Serializable data class SignCardsRoute(val group: String, val startCode: String? = null)
@Serializable data object SignSprintRoute
@Serializable data object RulesRoute
@Serializable data class RuleSectionRoute(val section: Int)
@Serializable data object MarkingsRoute

fun NavGraphBuilder.learnScreens(
    navController: NavController,
    onTrainTopic: (topicId: String) -> Unit,
) {
    val back: () -> Unit = { navController.popBackStack() }
    composable<SignsRoute> {
        SignsScreen(
            onBack = back,
            onOpenCards = { group, code -> navController.navigate(SignCardsRoute(group, code)) },
            onPlay = { navController.navigate(SignSprintRoute) },
        )
    }
    composable<SignCardsRoute> { entry ->
        val route = entry.toRoute<SignCardsRoute>()
        SignCardsScreen(route.group, route.startCode, onBack = back)
    }
    composable<SignSprintRoute> { SignSprintScreen(onBack = back) }
    composable<RulesRoute> { RulesScreen(onBack = back, onOpen = { navController.navigate(RuleSectionRoute(it)) }) }
    composable<RuleSectionRoute> { entry ->
        RuleSectionScreen(entry.toRoute<RuleSectionRoute>().section, onBack = back, onTrain = onTrainTopic)
    }
    composable<MarkingsRoute> { MarkingsScreen(onBack = back) }
}
