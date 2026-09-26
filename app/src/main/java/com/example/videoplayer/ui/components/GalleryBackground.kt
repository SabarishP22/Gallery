package com.example.videoplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.AuroraViolet
import com.example.videoplayer.ui.theme.DeepSpace
import com.example.videoplayer.ui.theme.DeepSpaceElevated

@Composable
fun GalleryBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepSpaceElevated,
                        DeepSpace,
                        DeepSpace,
                    ),
                ),
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        AuroraViolet.copy(alpha = 0.12f),
                        AuroraCyan.copy(alpha = 0.05f),
                        DeepSpace.copy(alpha = 0f),
                    ),
                    radius = 1200f,
                ),
            ),
    ) {
        content()
    }
}
