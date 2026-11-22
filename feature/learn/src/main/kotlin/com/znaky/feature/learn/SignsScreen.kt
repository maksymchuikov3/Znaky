package com.znaky.feature.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.SearchField
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

@Composable
internal fun SignsScreen(onBack: () -> Unit, onOpenCards: (group: String, code: String?) -> Unit, onPlay: () -> Unit) {
    val c = ZnakyTheme.colors
    var selectedGroup by rememberSaveable { mutableStateOf("2") }
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(selectedGroup, query) {
        if (query.isBlank()) SignCatalog.inGroup(selectedGroup)
        else SignCatalog.signs.filter { it.code.startsWith(query.trim()) || it.name.contains(query.trim(), ignoreCase = true) }
    }
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "top") {
            TopBar(onBack = onBack) {
                Pill("Гра", icon = Icons.Rounded.Bolt, onClick = onPlay)
                Pill("Картки", icon = Icons.Rounded.Style, onClick = { onOpenCards(selectedGroup, null) })
            }
        }
        item(key = "title") { LargeTitle("Дорожні знаки", subtitle = "${SignCatalog.signs.size} знаків у ${SignCatalog.groups.size} групах") }
        item(key = "search") { SearchField(query, { query = it }, "Номер або назва знака") }
        if (query.isBlank()) {
            item(key = "groups") {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(SignCatalog.groups, key = { it.number }) { g ->
                        val selected = g.number == selectedGroup
                        Column(
                            Modifier
                                .width(112.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(c.surface)
                                .then(if (selected) Modifier.border(2.dp, g.color, RoundedCornerShape(18.dp)) else Modifier)
                                .clickable(role = Role.Tab) { selectedGroup = g.number }
                                .semantics { this.selected = selected }
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(g.number, style = ZnakyType.title, color = g.color)
                            Text(g.name, style = ZnakyType.footnote, color = c.textPrimary, minLines = 2, maxLines = 2)
                            Text("${SignCatalog.inGroup(g.number).size} знаків", style = ZnakyType.caption, color = c.textTertiary)
                        }
                    }
                }
            }
            item(key = "note") {
                Text(
                    SignCatalog.groups.first { it.number == selectedGroup }.note,
                    style = ZnakyType.subhead,
                    color = c.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
        item(key = "list:$selectedGroup:$query") {
            GroupedList(Modifier.padding(horizontal = 16.dp)) {
                shown.forEachIndexed { i, sign ->
                    ListRow(
                        title = sign.name,
                        subtitle = sign.code,
                        showDivider = i > 0,
                        onClick = { onOpenCards(sign.group, sign.code) },
                        leading = { RoadSign(sign.marks, "Знак ${sign.code}", Modifier.size(44.dp)) },
                    )
                }
                if (shown.isEmpty()) ListRow(title = "Нічого не знайдено")
            }
        }
        item(key = "footer") {
            Text(
                "Знаки намальовано схематично. Каталог поповнюється з кожним оновленням.",
                style = ZnakyType.footnote.copy(fontWeight = ZnakyType.subhead.fontWeight),
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}
