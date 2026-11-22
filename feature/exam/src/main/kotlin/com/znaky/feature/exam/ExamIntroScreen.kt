package com.znaky.feature.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

@Composable
internal fun ExamIntroScreen(onStart: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LargeTitle("Пробний іспит", Modifier.padding(top = 16.dp), subtitle = "Як у сервісному центрі МВС: ті самі умови й підрахунок помилок.")
            ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Fact(Icons.Rounded.Quiz, c.primary, c.primaryTint, "20 питань", "10 з правил, по 4 з безпеки й будови авто, 2 з домедичної допомоги")
                    Fact(Icons.Rounded.Timer, c.onAccentTint, c.accentTint, "20 хвилин", "Коли час вийде, іспит завершиться")
                    Fact(Icons.Rounded.ErrorOutline, c.error, c.errorTint, "До 2 помилок", "Третя помилка одразу завершує спробу")
                    Fact(Icons.Rounded.Lock, c.teal, c.tealTint, "Без підказок", "Відповідь не можна змінити, пояснення будуть після іспиту")
                }
            }
        }
        ZButton("Почати іспит", onClick = onStart, icon = Icons.Rounded.Timer, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
}

@Composable
private fun Fact(icon: ImageVector, tint: Color, bg: Color, title: String, text: String) {
    val c = ZnakyTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = ZnakyType.headline, color = c.textPrimary)
            Text(text, style = ZnakyType.subhead, color = c.textSecondary)
        }
    }
    Spacer(Modifier.size(0.dp))
}
