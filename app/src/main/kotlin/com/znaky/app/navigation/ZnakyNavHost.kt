package com.znaky.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.feature.exam.ExamIntroRoute
import com.znaky.feature.exam.ExamRoute
import com.znaky.feature.exam.examScreens
import com.znaky.feature.exam.navigateToExam
import com.znaky.feature.home.HomeRoute
import com.znaky.feature.home.HomeSection
import com.znaky.feature.home.homeScreen
import com.znaky.feature.practice.TopicsRoute
import com.znaky.feature.practice.navigateToReview
import com.znaky.feature.practice.navigateToTopic
import com.znaky.feature.practice.practiceScreens
import com.znaky.feature.stats.StatsRoute
import com.znaky.feature.stats.statsScreen

private enum class Tab(val label: String, val icon: ImageVector, val route: Any, val matches: (NavDestination) -> Boolean) {
    HOME("Головна", Icons.Rounded.Home, HomeRoute, { it.hasRoute<HomeRoute>() }),
    TESTS("Тести", Icons.Rounded.Quiz, TopicsRoute, { it.hasRoute<TopicsRoute>() }),
    EXAM("Іспит", Icons.Rounded.Timer, ExamIntroRoute, { it.hasRoute<ExamIntroRoute>() }),
    PROGRESS("Прогрес", Icons.Rounded.BarChart, StatsRoute, { it.hasRoute<StatsRoute>() }),
}

private fun NavDestination?.tab(): Tab? = this?.let { d -> Tab.entries.firstOrNull { it.matches(d) } }

/** Switches tabs the standard way: one copy of each tab, state kept when you come back. */
private fun NavController.openTab(tab: Tab) = navigate(
    tab.route,
    navOptions {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    },
)

@Composable
fun ZnakyApp() {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentTab = entry?.destination.tab()
    Column(Modifier.fillMaxSize().background(ZnakyTheme.colors.background)) {
        NavHost(navController = navController, startDestination = HomeRoute, modifier = Modifier.weight(1f)) {
            homeScreen(
                onOpenSection = { section ->
                    when (section) {
                        HomeSection.TESTS -> navController.openTab(Tab.TESTS)
                        HomeSection.EXAM -> navController.navigateToExam()
                        HomeSection.PROGRESS -> navController.openTab(Tab.PROGRESS)
                        else -> navController.navigate(ComingSoonRoute(sectionTitle(section)))
                    }
                },
                onOpenReview = { navController.navigateToReview() },
            )
            practiceScreens(navController, onBack = { navController.popBackStack() })
            examScreens(
                onStart = { navController.navigateToExam() },
                onExit = { navController.popBackStack() },
                // Replace the finished exam with a fresh one instead of stacking attempts.
                onRetry = { navController.navigateToExam(navOptions { popUpTo(ExamRoute) { inclusive = true } }) },
            )
            statsScreen(onTrainTopic = { navController.navigateToTopic(it) })
            comingSoonScreen(onBack = { navController.popBackStack() })
        }
        AnimatedVisibility(currentTab != null, enter = fadeIn(), exit = fadeOut()) {
            TabBar(currentTab) { navController.openTab(it) }
        }
    }
}

private fun sectionTitle(section: HomeSection) = when (section) {
    HomeSection.RULES -> "Правила"
    HomeSection.SIGNS -> "Знаки"
    HomeSection.MARKINGS -> "Розмітка"
    HomeSection.EXPLAIN -> "Пояснення"
    HomeSection.CAR -> "3D-машина"
    HomeSection.TESTS -> "Тести"
    HomeSection.EXAM -> "Іспит"
    HomeSection.PROGRESS -> "Прогрес"
}

@Composable
private fun TabBar(current: Tab?, onSelect: (Tab) -> Unit) {
    val c = ZnakyTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding()) {
        HorizontalDivider(thickness = 1.dp, color = c.separator)
        Row(Modifier.fillMaxWidth().height(58.dp).selectableGroup(), horizontalArrangement = Arrangement.SpaceAround) {
            Tab.entries.forEach { tab ->
                val selected = tab == current
                val color = if (selected) c.link else c.textTertiary
                Column(
                    Modifier
                        .weight(1f)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(tab) })
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
                    Text(tab.label, style = ZnakyType.caption.copy(letterSpacing = ZnakyType.footnote.letterSpacing), color = color)
                }
            }
        }
    }
}
