package com.example.videoplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.DialogSurfaceFill
import com.example.videoplayer.presentation.theme.DialogSurfaceStroke
import com.example.videoplayer.presentation.theme.GlassWhite
import com.example.videoplayer.presentation.theme.GlassWhiteStrong

enum class GlassSurfaceStyle {
    Default,
    /** Solid dark panel for modals (readable over photos). */
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
    when (style) {
        GlassSurfaceStyle.Default -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(GlassWhite)
                    .border(1.dp, GlassWhiteStrong, shape),
            ) {
                content()
            }
        }
        GlassSurfaceStyle.Dialog -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(DialogSurfaceFill)
                    .border(1.dp, DialogSurfaceStroke, shape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    AuroraViolet.copy(alpha = 0.07f),
                                    Color.Transparent,
                                    AuroraCyan.copy(alpha = 0.05f),
                                ),
                            ),
                        ),
                )
                Box { content() }
            }
        }
    }
}
