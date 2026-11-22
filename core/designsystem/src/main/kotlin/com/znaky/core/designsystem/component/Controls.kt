package com.znaky.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

enum class ButtonKind { Primary, Tinted, Plain, Danger, Success }

/** 56 dp full-width button. Primary = filled blue, Tinted = light blue. */
@Composable
fun ZButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ButtonKind = ButtonKind.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    /** Spoken label for icon-only buttons (empty [text]). */
    contentDescription: String? = null,
) {
    val c = ZnakyTheme.colors
    val (bg, fg) = when (kind) {
        ButtonKind.Primary -> c.primary to c.onPrimary
        ButtonKind.Tinted -> c.primaryTint to c.onPrimaryTint
        ButtonKind.Plain -> c.surface to c.link
        ButtonKind.Danger -> c.errorTint to c.error
        ButtonKind.Success -> c.successTint to c.success
    }
    Row(
        modifier
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) bg else c.surfaceMuted)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (text.isEmpty()) 0.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = contentDescription, tint = if (enabled) fg else c.textTertiary, modifier = Modifier.size(20.dp))
            if (text.isNotEmpty()) HSpacer(8.dp)
        }
        if (text.isNotEmpty()) Text(text, style = ZnakyType.button, color = if (enabled) fg else c.textTertiary, textAlign = TextAlign.Center)
    }
}

/** Small rounded pill: chips, badges, rule references. */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = ZnakyTheme.colors.primaryTint,
    contentColor: Color = ZnakyTheme.colors.onPrimaryTint,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .heightIn(min = 32.dp)
            .clip(CircleShape)
            .background(background)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
            HSpacer(5.dp)
        }
        Text(text, style = ZnakyType.footnote, color = contentColor, maxLines = 1)
    }
}

/** iOS segmented control. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ZnakyTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceMuted)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            val bg by animateColorAsState(if (selected) c.surface else Color.Transparent, label = "segment")
            Box(
                Modifier
                    .weight(1f)
                    .height(34.dp)
                    .then(if (selected) Modifier.shadow(1.dp, RoundedCornerShape(9.dp)) else Modifier)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = ZnakyType.footnote.copy(fontSize = ZnakyType.subhead.fontSize), color = if (selected) c.textPrimary else c.textSecondary, maxLines = 1)
            }
        }
    }
}

/** Numbered badge used in answers and order slots. */
@Composable
fun NumberBadge(number: String, background: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(26.dp).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Text(number, style = ZnakyType.footnote, color = contentColor)
    }
}

/** Outline for answer cards and similar selectable blocks. */
fun Modifier.optionFrame(selectedColor: Color?, idle: Color, fill: Color): Modifier =
    this
        .clip(RoundedCornerShape(16.dp))
        .background(fill)
        .border(if (selectedColor != null) 2.dp else 1.dp, selectedColor ?: idle, RoundedCornerShape(16.dp))
