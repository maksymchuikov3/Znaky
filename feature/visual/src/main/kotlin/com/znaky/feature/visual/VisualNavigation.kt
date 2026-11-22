package com.znaky.feature.visual

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object ExplainRoute
@Serializable data object PriorityRoute
@Serializable data object OfficerRoute

fun NavGraphBuilder.visualScreens(navController: NavController) {
    val back: () -> Unit = { navController.popBackStack() }
    composable<ExplainRoute> {
        ExplainScreen(
            onBack = back,
            onOpenPriority = { navController.navigate(PriorityRoute) },
            onOpenOfficer = { navController.navigate(OfficerRoute) },
        )
    }
    composable<PriorityRoute> { PriorityScreen(onBack = back) }
    composable<OfficerRoute> { OfficerScreen(onBack = back) }
}
