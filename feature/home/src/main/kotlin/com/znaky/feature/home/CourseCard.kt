package com.znaky.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.znaky.core.art.CourseScenes
import com.znaky.core.designsystem.art.ArtImage
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

/** A course outside the car programme, shown on Learn for moto and boat learners. */
internal enum class ExtraCourse(
    val title: String,
    val subtitle: String,
    val accent: Long,
    val scene: CourseScenes.Kind,
    val course: HomeSection,
    val anatomy: HomeSection,
    val anatomyLabel: String,
) {
    MOTO(
        "Курс мотоцикліста", "6 розділів · шолом, швидкість, пасажир, техстан", 0xFFE07A2E,
        CourseScenes.Kind.MOTO_VISIBLE, HomeSection.MOTO_COURSE, HomeSection.ANATOMY_MOTO, "Мотоцикл у 3D",
    ),
    BOAT(
        "Курс судноводія", "8 розділів · правила плавання, жилети, гідроцикли", 0xFF1C8DB8,
        CourseScenes.Kind.BOAT_HEADON, HomeSection.BOAT_COURSE, HomeSection.ANATOMY_BOAT, "Катер у 3D",
    ),
}

@Composable
internal fun CourseCard(course: ExtraCourse, onOpenSection: (HomeSection) -> Unit, modifier: Modifier = Modifier) {
    val c = ZnakyTheme.colors
    val accent = Color(course.accent)
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(c.surface),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(if (c.isDark) Color(0xFF141D27) else Color(0xFFEEF2F7))
                .clickable(role = Role.Button) { onOpenSection(course.course) },
        ) {
            ArtImage(null, Modifier.fillMaxWidth().height(150.dp)) { CourseScenes.draw(this, course.scene, 0.45f, c.isDark) }
            Text(
                "НОВЕ",
                style = ZnakyType.caption,
                color = Color.White,
                modifier = Modifier.padding(12.dp).clip(CircleShape).background(accent).padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { onOpenSection(course.course) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(course.title, style = ZnakyType.title, color = c.textPrimary)
                Text(course.subtitle, style = ZnakyType.footnote, color = c.textSecondary)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = accent)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { onOpenSection(course.anatomy) }
                .padding(horizontal = 16.dp)
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ViewInAr, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Text(course.anatomyLabel, style = ZnakyType.bodyStrong, color = c.link)
        }
    }
}
