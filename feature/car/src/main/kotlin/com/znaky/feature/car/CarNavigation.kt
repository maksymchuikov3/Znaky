package com.znaky.feature.car

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data object CarHubRoute
@Serializable data class CarTopicRoute(val id: String, val step: Int = 0)
@Serializable data object GlossaryRoute

/** «Будова авто»: [kind] is ice, ev, moto or boat. */
@Serializable data class AnatomyRoute(val kind: String = "ice")

/** A theory course outside the car programme: «moto» (A, A1) or «boat» (судноводій). */
@Serializable data class CourseRoute(val id: String)
@Serializable data class CourseLessonRoute(val courseId: String, val chapterId: String)

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
    composable<CourseRoute> { entry ->
        val id = entry.toRoute<CourseRoute>().id
        CourseScreen(
            courseId = id,
            onBack = back,
            onOpenChapter = { navController.navigate(CourseLessonRoute(id, it)) },
            onOpenAnatomy = { navController.navigate(AnatomyRoute(it)) },
            onTrain = onTrainTopic,
        )
    }
    composable<CourseLessonRoute> { entry ->
        val route = entry.toRoute<CourseLessonRoute>()
        CourseLessonScreen(
            courseId = route.courseId,
            chapterId = route.chapterId,
            onBack = back,
            onTrain = onTrainTopic,
            onOpenAnatomy = { navController.navigate(AnatomyRoute(it)) },
        )
    }
    composable<AnatomyRoute> { entry ->
        AnatomyScreen(entry.toRoute<AnatomyRoute>().kind, onBack = back, onOpenLessons = { kind -> if (kind == "moto" || kind == "boat") navController.navigate(CourseRoute(kind)) else navController.navigate(CarHubRoute) })
    }
}
