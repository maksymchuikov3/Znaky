package com.znaky.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

/** Rounded white (dark: graphite) card, the basic building block of every screen. */
@Composable
fun ZCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    radius: Dp = 22.dp,
    color: Color = ZnakyTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(color)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** iOS-style inset grouped list: rows separated by hairlines inside one rounded card. */
@Composable
fun GroupedList(
    modifier: Modifier = Modifier,
    header: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (header != null) {
            Text(
                header.uppercase(),
                style = ZnakyType.footnote,
                color = ZnakyTheme.colors.textTertiary,
                modifier = Modifier.padding(horizontal = 6.dp).semantics { heading() },
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ZnakyTheme.colors.surface),
            content = content,
        )
    }
}

@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
) {
    Column(modifier.fillMaxWidth()) {
        if (showDivider) HorizontalDivider(Modifier.padding(start = if (leading != null) 60.dp else 16.dp), thickness = 1.dp, color = ZnakyTheme.colors.separator)
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .heightIn(min = 56.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            leading?.invoke()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = ZnakyType.bodyStrong, color = ZnakyTheme.colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) Text(subtitle, style = ZnakyType.footnote, color = ZnakyTheme.colors.textTertiary)
            }
            trailing?.invoke(this)
            if (showChevron) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = ZnakyTheme.colors.textTertiary)
            }
        }
    }
}

/** Coloured rounded square with a white glyph, like iOS settings icons. */
@Composable
fun IconTile(icon: ImageVector, background: Color, modifier: Modifier = Modifier, size: Dp = 34.dp, tint: Color = Color.White) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.58f))
    }
}

/** Back (or close) button with optional label, and optional actions on the right. */
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backLabel: String? = null,
    close: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    if (close) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = if (close) "Закрити" else "Назад",
                    tint = if (close) ZnakyTheme.colors.textSecondary else ZnakyTheme.colors.link,
                )
            }
        }
        if (backLabel != null) {
            Text(backLabel, style = ZnakyType.callout, color = ZnakyTheme.colors.textTertiary, maxLines = 1)
        }
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            content = actions,
        )
    }
}

/** Big bold screen title with an optional subtitle, iOS large-title style. */
@Composable
fun LargeTitle(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = ZnakyType.largeTitle, color = ZnakyTheme.colors.textPrimary, modifier = Modifier.semantics { heading() })
        if (subtitle != null) Text(subtitle, style = ZnakyType.callout, color = ZnakyTheme.colors.textSecondary)
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier, trailing: String? = null, onTrailingClick: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.Bottom) {
        Text(title, style = ZnakyType.title2, color = ZnakyTheme.colors.textPrimary, modifier = Modifier.weight(1f).semantics { heading() })
        if (trailing != null) {
            Text(
                trailing,
                style = ZnakyType.footnote,
                color = if (onTrailingClick != null) ZnakyTheme.colors.link else ZnakyTheme.colors.textTertiary,
                modifier = if (onTrailingClick != null) Modifier.clickable(onClick = onTrailingClick) else Modifier,
            )
        }
    }
}

@Composable
fun HSpacer(width: Dp) = Spacer(Modifier.width(width))
