package com.znaky.app.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.znaky.app.AppViewModel
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.feature.car.AnatomyRoute
import com.znaky.feature.car.CarHubRoute
import com.znaky.feature.car.GlossaryRoute
import com.znaky.feature.car.carScreens
import com.znaky.feature.exam.ExamIntroRoute
import com.znaky.feature.exam.ExamRoute
import com.znaky.feature.exam.examScreens
import com.znaky.feature.exam.navigateToExam
import com.znaky.feature.home.HomeRoute
import com.znaky.feature.home.HomeSection
import com.znaky.feature.home.LearnHubRoute
import com.znaky.feature.home.OnboardingRoute
import com.znaky.feature.home.TrainingRoute
import com.znaky.feature.home.homeScreen
import com.znaky.feature.home.hubScreens
import com.znaky.feature.home.onboardingScreen
import com.znaky.feature.learn.MarkingsRoute
import com.znaky.feature.learn.RulesRoute
import com.znaky.feature.learn.SignSprintRoute
import com.znaky.feature.learn.SignsRoute
import com.znaky.feature.learn.learnScreens
import com.znaky.feature.practice.BookmarksRoute
import com.znaky.feature.practice.PracticeMode
import com.znaky.feature.practice.RepetitionRoute
import com.znaky.feature.practice.TopicsRoute
import com.znaky.feature.practice.navigateToPractice
import com.znaky.feature.practice.navigateToReview
import com.znaky.feature.practice.navigateToTopic
import com.znaky.feature.practice.practiceScreens
import com.znaky.feature.stats.StatsRoute
import com.znaky.feature.stats.statsScreen
import com.znaky.feature.visual.ExplainRoute
import com.znaky.feature.visual.OfficerRoute
import com.znaky.feature.visual.visualScreens

/** [icon] null means the custom steering-wheel glyph. */
private enum class Tab(val label: String, val icon: ImageVector?, val route: Any, val matches: (NavDestination) -> Boolean) {
    HOME("Головна", Icons.Rounded.Home, HomeRoute, { it.hasRoute<HomeRoute>() }),
    LEARN("Вчити", Icons.AutoMirrored.Rounded.MenuBook, LearnHubRoute, { it.hasRoute<LearnHubRoute>() }),
    TRAINING("Тренування", null, TrainingRoute, { it.hasRoute<TrainingRoute>() }),
    PROGRESS("Прогрес", Icons.Rounded.BarChart, StatsRoute, { it.hasRoute<StatsRoute>() }),
}

private fun NavDestination?.tab(): Tab? = this?.let { d -> Tab.entries.firstOrNull { it.matches(d) } }

/** Switches tabs the standard way: one copy of each tab, state kept when you come back. */
private fun NavController.openTab(tab: Tab) = navigate(
    tab.route,
    navOptions {
        popUpTo(HomeRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    },
)

@Composable
fun ZnakyApp(appViewModel: AppViewModel = hiltViewModel()) {
    val done by appViewModel.onboardingDone.collectAsStateWithLifecycle()
    val c = ZnakyTheme.colors
    // The start screen is decided once, when the setting is first read; finishing
    // onboarding later must not rebuild the navigation graph.
    val loaded = done != null
    val startRoute: Any? = remember(loaded) { if (!loaded) null else if (done == true) HomeRoute else OnboardingRoute }
    if (startRoute == null) {
        Box(Modifier.fillMaxSize().background(c.background))
        return
    }
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentTab = entry?.destination.tab()
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Column(Modifier.fillMaxSize().background(c.background)) {
        NavHost(navController = navController, startDestination = startRoute, modifier = Modifier.weight(1f)) {
            val openSection: (HomeSection) -> Unit = { section ->
                when (section) {
                    HomeSection.EXAM -> navController.navigateToExam()
                    HomeSection.EXAM_INTRO -> navController.navigate(ExamIntroRoute)
                    HomeSection.TOPICS -> navController.navigate(TopicsRoute) { launchSingleTop = true }
                    HomeSection.PROGRESS -> navController.openTab(Tab.PROGRESS)
                    HomeSection.LEARN -> navController.openTab(Tab.LEARN)
                    HomeSection.TRAINING -> navController.openTab(Tab.TRAINING)
                    HomeSection.SIGNS -> navController.navigate(SignsRoute)
                    HomeSection.RULES -> navController.navigate(RulesRoute)
                    HomeSection.MARKINGS -> navController.navigate(MarkingsRoute)
                    HomeSection.EXPLAIN -> navController.navigate(ExplainRoute)
                    HomeSection.CAR -> navController.navigate(CarHubRoute)
                    HomeSection.SIGN_GAME -> navController.navigate(SignSprintRoute)
                    HomeSection.REPETITION -> navController.navigate(RepetitionRoute) { launchSingleTop = true }
                    HomeSection.MISTAKES -> navController.navigateToPractice(PracticeMode.MISTAKES)
                    HomeSection.BOOKMARKS -> navController.navigate(BookmarksRoute) { launchSingleTop = true }
                    HomeSection.RANDOM -> navController.navigateToPractice(PracticeMode.RANDOM)
                    HomeSection.ANATOMY -> navController.navigate(AnatomyRoute())
                    HomeSection.GLOSSARY -> navController.navigate(GlossaryRoute)
                    HomeSection.OFFICER -> navController.navigate(OfficerRoute)
                }
            }
            val openTopic: (String) -> Unit = { navController.navigateToTopic(it) }
            val openReview: () -> Unit = { navController.navigateToReview() }
            onboardingScreen(
                onDone = {
                    navController.navigate(HomeRoute) { popUpTo(OnboardingRoute) { inclusive = true } }
                },
                onRequestNotifications = {
                    if (Build.VERSION.SDK_INT >= 33) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
            )
            homeScreen(onOpenSection = openSection, onOpenTopic = openTopic, onOpenReview = openReview)
            hubScreens(onOpenSection = openSection, onOpenTopic = openTopic, onOpenReview = openReview)
            practiceScreens(
                navController,
                onBack = { navController.popBackStack() },
                onOpenTopics = { navController.navigate(TopicsRoute) { launchSingleTop = true } },
            )
            examScreens(
                onStart = { navController.navigateToExam() },
                onExit = { navController.popBackStack() },
                // Replace the finished exam with a fresh one instead of stacking attempts.
                onRetry = { navController.navigateToExam(navOptions { popUpTo(ExamRoute) { inclusive = true } }) },
            )
            statsScreen(onTrainTopic = { navController.navigateToTopic(it) })
            learnScreens(navController, onTrainTopic = { navController.navigateToTopic(it) })
            visualScreens(navController)
            carScreens(navController, onTrainTopic = { navController.navigateToTopic(it) })
        }
        AnimatedVisibility(currentTab != null, enter = fadeIn(), exit = fadeOut()) {
            TabBar(currentTab) { navController.openTab(it) }
        }
    }
}

/** Floating capsule tab bar; the selected tab sits in a tinted pill. */
@Composable
private fun TabBar(current: Tab?, onSelect: (Tab) -> Unit) {
    val c = ZnakyTheme.colors
    Box(Modifier.fillMaxWidth().background(c.background).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 4.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp))
                .background(c.surface)
                .padding(6.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Tab.entries.forEach { tab ->
                val selected = tab == current
                val color = if (selected) c.link else c.textTertiary
                val bg by animateColorAsState(if (selected) c.primaryTint else Color.Transparent, label = "tab")
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(bg)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(tab) })
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val icon = tab.icon
                    if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    else SteeringWheel(color, Modifier.size(24.dp))
                    Text(tab.label, style = ZnakyType.caption.copy(letterSpacing = ZnakyType.footnote.letterSpacing), color = color, maxLines = 1)
                }
            }
        }
    }
}

/** Steering wheel glyph: rim, hub and three spokes. */
@Composable
private fun SteeringWheel(color: Color, modifier: Modifier) {
    Canvas(modifier.padding(2.dp)) {
        val stroke = 2.dp.toPx()
        val r = size.minDimension / 2 - stroke / 2
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color, r, center, style = Stroke(stroke))
        drawCircle(color, r * 0.28f, center, style = Stroke(stroke))
        drawLine(color, Offset(center.x - r, center.y), Offset(center.x - r * 0.28f, center.y), stroke, StrokeCap.Round)
        drawLine(color, Offset(center.x + r * 0.28f, center.y), Offset(center.x + r, center.y), stroke, StrokeCap.Round)
        drawLine(color, Offset(center.x, center.y + r * 0.28f), Offset(center.x, center.y + r), stroke, StrokeCap.Round)
    }
}

