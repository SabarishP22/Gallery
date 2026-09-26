package com.example.videoplayer.presentation.viewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.util.viewerPhotoRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 8f
private const val DOUBLE_TAP_ZOOM = 2.75f
private const val ZOOMED_THRESHOLD = 1.02f

@Composable
fun ZoomablePhoto(
    media: GalleryMedia,
    onToggleChrome: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var containerSize by remember(media.id) { mutableStateOf(Size.Zero) }
    var scale by remember(media.id) { mutableFloatStateOf(MIN_ZOOM) }
    var offset by remember(media.id) { mutableStateOf(Offset.Zero) }
    var gestureActive by remember(media.id) { mutableStateOf(false) }

    val scaleAnim = remember(media.id) { Animatable(MIN_ZOOM) }
    val offsetXAnim = remember(media.id) { Animatable(0f) }
    val offsetYAnim = remember(media.id) { Animatable(0f) }

    LaunchedEffect(media.id) {
        scale = MIN_ZOOM
        offset = Offset.Zero
        scaleAnim.snapTo(MIN_ZOOM)
        offsetXAnim.snapTo(0f)
        offsetYAnim.snapTo(0f)
    }

    LaunchedEffect(scale, offset, gestureActive) {
        if (!gestureActive) {
            scaleAnim.snapTo(scale)
            offsetXAnim.snapTo(offset.x)
            offsetYAnim.snapTo(offset.y)
        }
        onZoomChanged(scale > ZOOMED_THRESHOLD)
    }

    val displayScale = if (gestureActive) scale else scaleAnim.value
    val displayOffset = if (gestureActive) {
        offset
    } else {
        Offset(offsetXAnim.value, offsetYAnim.value)
    }

    fun clampOffset(raw: Offset, zoom: Float): Offset {
        if (zoom <= MIN_ZOOM || containerSize == Size.Zero) return Offset.Zero
        val maxX = containerSize.width * (zoom - MIN_ZOOM) / 2f
        val maxY = containerSize.height * (zoom - MIN_ZOOM) / 2f
        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY),
        )
    }

    fun applyPinch(centroid: Offset, pan: Offset, zoomFactor: Float) {
        if (abs(zoomFactor - 1f) < 0.001f && pan == Offset.Zero) return
        val previousScale = scale
        val newScale = (scale * zoomFactor).coerceIn(MIN_ZOOM, MAX_ZOOM)
        val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
        val pinchVector = centroid - center
        var newOffset = (offset + pinchVector) * (newScale / previousScale) - pinchVector + pan
        scale = newScale
        if (scale <= MIN_ZOOM) {
            scale = MIN_ZOOM
            offset = Offset.Zero
        } else {
            offset = clampOffset(newOffset, scale)
        }
    }

    fun animateTo(scaleTarget: Float, offsetTarget: Offset) {
        scope.launch {
            gestureActive = false
            coroutineScope {
                launch {
                    scaleAnim.animateTo(
                        scaleTarget,
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                }
                launch {
                    offsetXAnim.animateTo(
                        offsetTarget.x,
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                }
                launch {
                    offsetYAnim.animateTo(
                        offsetTarget.y,
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                }
            }
            scale = scaleTarget
            offset = offsetTarget
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it.toSize() },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = viewerPhotoRequest(context, media),
            contentDescription = media.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = displayScale
                    scaleY = displayScale
                    translationX = displayOffset.x
                    translationY = displayOffset.y
                }
                .pointerInput(media.id, containerSize) {
                    coroutineScope {
                        launch {
                            while (true) {
                                detectTransformGestures { centroid, pan, zoom, _ ->
                                    gestureActive = true
                                    applyPinch(centroid, pan, zoom)
                                }
                                gestureActive = false
                            }
                        }
                        launch {
                            detectTapGestures(
                                onTap = { onToggleChrome() },
                                onDoubleTap = { tapPosition ->
                                    if (scale > ZOOMED_THRESHOLD) {
                                        animateTo(MIN_ZOOM, Offset.Zero)
                                    } else {
                                        val center = Offset(
                                            containerSize.width / 2f,
                                            containerSize.height / 2f,
                                        )
                                        val focal = tapPosition - center
                                        val targetScale = DOUBLE_TAP_ZOOM.coerceAtMost(MAX_ZOOM)
                                        val targetOffset = clampOffset(
                                            focal * (1f - targetScale),
                                            targetScale,
                                        )
                                        scale = targetScale
                                        offset = targetOffset
                                        animateTo(targetScale, targetOffset)
                                    }
                                },
                                onLongPress = { onLongPress() },
                            )
                        }
                    }
                },
        )
    }
}
