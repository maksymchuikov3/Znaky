package com.znaky.feature.car

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data object CarHubRoute
@Serializable data class CarTopicRoute(val id: String, val step: Int = 0)
@Serializable data object GlossaryRoute

fun NavGraphBuilder.carScreens(navController: NavController, onTrainTopic: (topicId: String) -> Unit) {
    val back: () -> Unit = { navController.popBackStack() }
    composable<CarHubRoute> {
        CarHubScreen(
            onBack = back,
            onOpenTopic = { id, step -> navController.navigate(CarTopicRoute(id, step)) },
            onOpenGlossary = { navController.navigate(GlossaryRoute) },
        )
    }
    composable<CarTopicRoute> { entry ->
        val route = entry.toRoute<CarTopicRoute>()
        CarTopicScreen(route.id, route.step, onBack = back, onTrain = onTrainTopic)
    }
    composable<GlossaryRoute> { GlossaryScreen(onBack = back) }
}
