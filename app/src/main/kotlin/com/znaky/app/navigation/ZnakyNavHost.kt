package com.znaky.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.znaky.feature.exam.ExamRoute
import com.znaky.feature.exam.examScreen
import com.znaky.feature.exam.navigateToExam
import com.znaky.feature.home.HomeRoute
import com.znaky.feature.home.homeScreen
import com.znaky.feature.practice.navigateToReview
import com.znaky.feature.practice.navigateToTopics
import com.znaky.feature.practice.practiceScreens
import com.znaky.feature.stats.navigateToStats
import com.znaky.feature.stats.statsScreen

@Composable
fun ZnakyNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HomeRoute) {
        homeScreen(
            onStartExam = { navController.navigateToExam() },
            onOpenTopics = { navController.navigateToTopics() },
            onOpenReview = { navController.navigateToReview() },
            onOpenStats = { navController.navigateToStats() },
        )
        practiceScreens(navController, onBack = { navController.popBackStack() })
        examScreen(
            onExit = { navController.popBackStack(HomeRoute, inclusive = false) },
            // Replace the finished exam with a fresh one instead of stacking attempts.
            onRetry = { navController.navigateToExam(navOptions { popUpTo(ExamRoute) { inclusive = true } }) },
        )
        statsScreen(onBack = { navController.popBackStack() })
    }
}
