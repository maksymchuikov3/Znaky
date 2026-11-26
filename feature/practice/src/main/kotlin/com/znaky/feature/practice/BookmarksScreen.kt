package com.znaky.feature.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.znaky.core.art.RoadSigns
import com.znaky.core.designsystem.art.ArtImage
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.SegmentedControl
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class BookmarkItem(val question: Question, val label: String)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val progress: ProgressRepository,
    private val questions: QuestionRepository,
) : ViewModel() {
    val items: StateFlow<List<BookmarkItem>?> = combine(progress.observeBookmarks(), questions.observeTopics()) { ids, topics ->
        val labels = topics.associate { it.id to it.label() }
        questions.getQuestions(ids).map { BookmarkItem(it, labels[it.topicId].orEmpty()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun remove(id: String) {
        viewModelScope.launch { progress.setBookmarked(id, false) }
    }
}

private val Filters = listOf(null, "rules", "vehicle")

@Composable
internal fun BookmarksScreen(onBack: () -> Unit, onStart: (String?) -> Unit, viewModel: BookmarksViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val all = items ?: return LoadingBox()
    val c = ZnakyTheme.colors
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val filter = Filters[tab]
    val shown = all.filter { filter == null || blockFilter(filter, it.question.block) }
    val counts = Filters.map { f -> all.count { f == null || blockFilter(f, it.question.block) } }
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "back") { TopBar(onBack = onBack, backLabel = "Тренування") }
            item(key = "title") {
                LargeTitle("Закладки", subtitle = "${all.size} ${questionsWord(all.size)}, до яких ти хотів повернутись")
            }
            item(key = "segments") {
                SegmentedControl(
                    listOf("Усі · ${counts[0]}", "ПДР · ${counts[1]}", "Будова · ${counts[2]}"),
                    selectedIndex = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            if (shown.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "Тут поки порожньо. Під час тренування натисніть значок закладки вгорі, щоб зберегти питання.",
                        style = ZnakyType.callout,
                        color = c.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    )
                }
            } else {
                item(key = "list") {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(c.surface)) {
                        shown.forEachIndexed { i, item ->
                            if (i > 0) HorizontalDivider(Modifier.padding(start = 64.dp), thickness = 1.dp, color = c.separator)
                            BookmarkRow(item, onRemove = { viewModel.remove(item.question.id) })
                        }
                    }
                }
                item(key = "hint") {
                    Text(
                        "Натисніть закладку, щоб прибрати питання",
                        style = ZnakyType.footnote,
                        color = c.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (shown.isNotEmpty()) {
            ZButton(
                "Пройти всі ${shown.size}",
                onClick = { onStart(filter) },
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun BookmarkRow(item: BookmarkItem, onRemove: () -> Unit) {
    val c = ZnakyTheme.colors
    Row(Modifier.fillMaxWidth().padding(start = 14.dp, top = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val sign = item.question.imageAsset?.takeIf { it.startsWith("art:sign:") }?.removePrefix("art:sign:")
        Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
            if (sign != null && RoadSigns.has(sign)) {
                ArtImage("Знак $sign", Modifier.size(36.dp)) { RoadSigns.draw(this, sign) }
            } else {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(c.primaryTint), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null, tint = c.link, modifier = Modifier.size(20.dp))
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.label.uppercase(), style = ZnakyType.caption, color = c.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.question.text, style = ZnakyType.bodyStrong, color = c.textPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Rounded.Bookmark, contentDescription = "Прибрати із закладок", tint = c.accent)
        }
    }
}

