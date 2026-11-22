package com.znaky.feature.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.ButtonKind
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import kotlinx.coroutines.launch

/** Flashcards for one sign group; swipe or use the buttons. */
@Composable
internal fun SignCardsScreen(group: String, startCode: String?, onBack: () -> Unit) {
    val c = ZnakyTheme.colors
    val signs = remember(group) { SignCatalog.inGroup(group) }
    val groupInfo = remember(group) { SignCatalog.groups.firstOrNull { it.number == group } }
    val start = signs.indexOfFirst { it.code == startCode }.coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = start) { signs.size }
    val scope = rememberCoroutineScope()
    val next: () -> Unit = { scope.launch { pager.animateScrollToPage((pager.currentPage + 1) % signs.size.coerceAtLeast(1)) } }
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        TopBar(onBack = onBack, close = true) {
            LinearBar((pager.currentPage + 1f) / signs.size.coerceAtLeast(1), Modifier.weight(1f), height = 8.dp)
            Text("${pager.currentPage + 1}/${signs.size}", style = ZnakyType.footnote, color = c.textSecondary)
        }
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.weight(1f),
            key = { signs[it].code },
        ) { page ->
            val sign = signs[page]
            ZCard(Modifier.fillMaxWidth().padding(vertical = 12.dp), radius = 28.dp, contentPadding = PaddingValues(22.dp)) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text((groupInfo?.name ?: "").uppercase(), style = ZnakyType.caption, color = c.textTertiary)
                    RoadSign(sign.marks, "Знак ${sign.code} ${sign.name}", Modifier.size(168.dp))
                    Text(sign.code, style = ZnakyType.headline, color = c.link)
                    Text(sign.name, style = ZnakyType.title, color = c.textPrimary, textAlign = TextAlign.Center)
                    Text(sign.description, style = ZnakyType.callout, color = c.textSecondary, textAlign = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("п. ${sign.ruleRef} ПДР")
                        Pill("Додаток 1")
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ZButton("Ще не знаю", onClick = next, kind = ButtonKind.Danger, icon = Icons.Rounded.Replay, modifier = Modifier.weight(1f))
            ZButton("Знаю", onClick = next, kind = ButtonKind.Success, icon = Icons.Rounded.Check, modifier = Modifier.weight(1f))
        }
    }
}
