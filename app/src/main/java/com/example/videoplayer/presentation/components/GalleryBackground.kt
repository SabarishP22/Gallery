package com.example.videoplayer.presentation.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.DeepSpaceElevated

@Composable
fun GalleryBackground(
    wallpaperUri: String?,
    wallpaperBlurDp: Float,
    wallpaperContentVersion: Long,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val blurPx = with(LocalDensity.current) { wallpaperBlurDp.coerceIn(0f, 40f).dp.toPx() }
    val hasWallpaper = !wallpaperUri.isNullOrBlank() &&
        wallpaperUri.wallpaperFilePath()?.let { java.io.File(it).exists() } == true

    key(wallpaperUri, wallpaperContentVersion, wallpaperBlurDp) {
        Box(modifier = modifier.fillMaxSize()) {
            if (hasWallpaper) {
                WallpaperBackgroundLayer(
                    wallpaperUri = wallpaperUri,
                    contentRevision = wallpaperContentVersion,
                    blurPx = blurPx,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepSpace.copy(alpha = 0.55f)),
                )
            } else {
                Box(
                    modifier = Modifier
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
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
