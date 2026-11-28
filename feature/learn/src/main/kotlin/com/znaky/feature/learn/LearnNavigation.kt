package com.znaky.feature.learn

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data object SignsRoute
@Serializable data class SignCardsRoute(val group: String, val startCode: String? = null)
@Serializable data object SignSprintRoute
/** [book]: «pdr» or «boat» (see [PdrText]); [moto] marks the points about motorcycles. */
@Serializable data class RulesRoute(val book: String = "pdr", val moto: Boolean = false)
@Serializable data class RuleSectionRoute(val section: Int, val book: String = "pdr", val moto: Boolean = false)
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
    composable<RulesRoute> { entry ->
        val r = entry.toRoute<RulesRoute>()
        RulesScreen(r.book, r.moto, onBack = back, onOpen = { navController.navigate(RuleSectionRoute(it, r.book, r.moto)) })
    }
    composable<RuleSectionRoute> { entry ->
        val r = entry.toRoute<RuleSectionRoute>()
        RuleSectionScreen(
            r.section,
            r.book,
            r.moto,
            onBack = back,
            onTrain = onTrainTopic,
            onOpenSign = { group, code -> navController.navigate(SignCardsRoute(group, code)) },
            onOpenMarkings = { navController.navigate(MarkingsRoute) },
        )
    }
    composable<MarkingsRoute> { MarkingsScreen(onBack = back) }
}
