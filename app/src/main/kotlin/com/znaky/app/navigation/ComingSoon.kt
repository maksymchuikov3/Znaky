package com.znaky.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import kotlinx.serialization.Serializable

/** Temporary screen for sections that arrive in the next release. */
@Serializable
data class ComingSoonRoute(val title: String)

fun NavGraphBuilder.comingSoonScreen(onBack: () -> Unit) {
    composable<ComingSoonRoute> { entry ->
        val title = entry.toRoute<ComingSoonRoute>().title
        val c = ZnakyTheme.colors
        Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TopBar(onBack = onBack)
            LargeTitle(title)
            Text("Розділ з’явиться в наступному оновленні.", style = ZnakyType.body, color = c.textSecondary, modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}
