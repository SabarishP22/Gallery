package com.example.videoplayer.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.presentation.gallery.ViewerTransitionOrigin
import com.example.videoplayer.presentation.viewer.MediaViewerScreen
import kotlinx.coroutines.launch

@Composable
fun ViewerTransitionOverlay(
    viewerMediaId: Long,
    items: List<GalleryMedia>,
    startIndex: Int,
    favoriteIds: Set<Long>,
    viewerFavoriteBurstNonce: Long,
    transitionOrigin: ViewerTransitionOrigin?,
    onDismissComplete: () -> Unit,
    onCurrentMediaChanged: (Long) -> Unit,
    onToggleFavorite: (GalleryMedia) -> Unit,
    onWallpaperRequest: (GalleryMedia) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val progress = remember(viewerMediaId) { Animatable(0f) }
    var closing by remember(viewerMediaId) { mutableStateOf(false) }

    LaunchedEffect(viewerMediaId) {
        progress.snapTo(0f)
        progress.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    fun dismissWithAnimation() {
        if (closing) return
        closing = true
        scope.launch {
            progress.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
            onDismissComplete()
        }
    }

    BackHandler { dismissWithAnimation() }

    val origin = transitionOrigin
    val pivotX = origin?.centerXFraction ?: 0.5f
    val pivotY = origin?.centerYFraction ?: 0.5f
    val startScale = origin?.startScale ?: 0.88f
    val scale = lerp(startScale, 1f, progress.value)
    val contentAlpha = lerp(0.2f, 1f, progress.value)
    val scrimAlpha = lerp(0f, 1f, progress.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = scrimAlpha * 0.98f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = contentAlpha
                    transformOrigin = TransformOrigin(pivotX, pivotY)
                },
        ) {
            MediaViewerScreen(
                items = items,
                startIndex = startIndex,
                favoriteIds = favoriteIds,
                viewerFavoriteBurstNonce = viewerFavoriteBurstNonce,
                onBack = { dismissWithAnimation() },
                onCurrentMediaChanged = onCurrentMediaChanged,
                onToggleFavorite = onToggleFavorite,
                onWallpaperRequest = onWallpaperRequest,
                lockLandscape = true,
            )
        }
    }
}
