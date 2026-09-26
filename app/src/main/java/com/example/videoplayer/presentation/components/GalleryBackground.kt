package com.example.videoplayer.presentation.components

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.DeepSpaceElevated

@Composable
fun GalleryBackground(
    wallpaperUri: String?,
    wallpaperBlurDp: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val blurPx = with(LocalDensity.current) { wallpaperBlurDp.coerceIn(0f, 40f).dp.toPx() }
    val wallpaperRequest = remember(wallpaperUri) {
        wallpaperUri?.let {
            ImageRequest.Builder(context)
                .data(Uri.parse(it))
                .crossfade(400)
                .build()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                },
        ) {
            if (wallpaperRequest != null) {
                AsyncImage(
                    model = wallpaperRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurPx > 0.5f) {
                                Modifier.graphicsLayer {
                                    renderEffect = androidx.compose.ui.graphics.BlurEffect(
                                        blurPx,
                                        blurPx,
                                        androidx.compose.ui.graphics.TileMode.Clamp,
                                    )
                                }
                            } else if (blurPx > 0.5f) {
                                Modifier.graphicsLayer {
                                    scaleX = 1.15f
                                    scaleY = 1.15f
                                }
                            } else {
                                Modifier
                            },
                        ),
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
        }
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
