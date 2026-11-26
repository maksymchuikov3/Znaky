package com.znaky.feature.learn

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.znaky.core.art.RoadSigns
import com.znaky.core.designsystem.art.ArtImage

/** Draws road sign [code] (ДСТУ 4100 shapes from core:art), scaled to fit [modifier]. */
@Composable
internal fun RoadSign(code: String, description: String, modifier: Modifier = Modifier) {
    ArtImage(description, modifier) { RoadSigns.draw(this, code) }
}
