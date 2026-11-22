package com.znaky.feature.exam

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ExamRoute

fun NavController.navigateToExam(navOptions: NavOptions? = null) = navigate(ExamRoute, navOptions)

fun NavGraphBuilder.examScreen(onExit: () -> Unit, onRetry: () -> Unit) {
    composable<ExamRoute> { ExamScreen(onExit = onExit, onRetry = onRetry) }
}
