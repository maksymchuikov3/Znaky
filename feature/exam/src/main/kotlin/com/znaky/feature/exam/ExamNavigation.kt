package com.znaky.feature.exam

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** Exam tab: rules of the exam and a start button. */
@Serializable
data object ExamIntroRoute

/** A running exam attempt; shows the result screen when finished. */
@Serializable
data object ExamRoute

fun NavController.navigateToExam(navOptions: NavOptions? = null) = navigate(ExamRoute, navOptions)

fun NavGraphBuilder.examScreens(onStart: () -> Unit, onExit: () -> Unit, onRetry: () -> Unit) {
    composable<ExamIntroRoute> { ExamIntroScreen(onStart = onStart) }
    composable<ExamRoute> { ExamScreen(onExit = onExit, onRetry = onRetry) }
}
