package com.example.videoplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.videoplayer.presentation.theme.GlassWhite
import com.example.videoplayer.presentation.theme.GlassWhiteDialog
import com.example.videoplayer.presentation.theme.GlassWhiteStrong
import com.example.videoplayer.presentation.theme.GlassWhiteStrongDialog

enum class GlassSurfaceStyle {
    Default,
    /** Same glass look as the app, slightly less transparent for modals. */
    Dialog,
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    style: GlassSurfaceStyle = GlassSurfaceStyle.Default,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fill = when (style) {
        GlassSurfaceStyle.Default -> GlassWhite
        GlassSurfaceStyle.Dialog -> GlassWhiteDialog
    }
    val stroke = when (style) {
        GlassSurfaceStyle.Default -> GlassWhiteStrong
        GlassSurfaceStyle.Dialog -> GlassWhiteStrongDialog
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(1.dp, stroke, shape),
    ) {
        content()
    }
}
