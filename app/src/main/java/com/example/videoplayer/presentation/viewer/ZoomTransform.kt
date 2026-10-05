package com.example.videoplayer.presentation.viewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

const val ZOOM_MIN = 1f
const val ZOOM_MAX = 8f
const val ZOOM_DOUBLE_TAP = 2.75f
const val ZOOMED_THRESHOLD = 1.02f

@Stable
class ZoomTransformState internal constructor(
    private val scope: kotlinx.coroutines.CoroutineScope,
) {
    var containerSize by mutableStateOf(Size.Zero)
        internal set

    var scale by mutableFloatStateOf(ZOOM_MIN)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    var gestureActive by mutableStateOf(false)
        private set

    val scaleAnim = Animatable(ZOOM_MIN)
    val offsetXAnim = Animatable(0f)
    val offsetYAnim = Animatable(0f)

    val isZoomed: Boolean get() = scale > ZOOMED_THRESHOLD

    val displayScale: Float get() = if (gestureActive) scale else scaleAnim.value

    val displayOffset: Offset
        get() = if (gestureActive) offset else Offset(offsetXAnim.value, offsetYAnim.value)

    fun resetImmediate() {
        scale = ZOOM_MIN
        offset = Offset.Zero
        gestureActive = false
        scope.launch {
            scaleAnim.snapTo(ZOOM_MIN)
            offsetXAnim.snapTo(0f)
            offsetYAnim.snapTo(0f)
        }
    }

    internal fun clampOffset(raw: Offset, zoom: Float): Offset {
        if (zoom <= ZOOM_MIN || containerSize == Size.Zero) return Offset.Zero
        val maxX = containerSize.width * (zoom - ZOOM_MIN) / 2f
        val maxY = containerSize.height * (zoom - ZOOM_MIN) / 2f
        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY),
        )
    }

    internal fun applyPinch(centroid: Offset, pan: Offset, zoomFactor: Float) {
        if (abs(zoomFactor - 1f) < 0.001f && pan == Offset.Zero) return
        val previousScale = scale
        val newScale = (scale * zoomFactor).coerceIn(ZOOM_MIN, ZOOM_MAX)
        val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
        val pinchVector = centroid - center
        val newOffset = (offset + pinchVector) * (newScale / previousScale) - pinchVector + pan
        scale = newScale
        if (scale <= ZOOM_MIN) {
            scale = ZOOM_MIN
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

    fun doubleTapAt(tapPosition: Offset) {
        if (scale > ZOOMED_THRESHOLD) {
            animateTo(ZOOM_MIN, Offset.Zero)
        } else {
            val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
            val focal = tapPosition - center
            val targetScale = ZOOM_DOUBLE_TAP.coerceAtMost(ZOOM_MAX)
            val targetOffset = clampOffset(focal * (1f - targetScale), targetScale)
            scale = targetScale
            offset = targetOffset
            animateTo(targetScale, targetOffset)
        }
    }

    internal fun onPinchGestureEnd() {
        gestureActive = false
    }

    internal fun setGestureActive(active: Boolean) {
        gestureActive = active
    }

    internal fun panBy(panChange: Offset) {
        offset = clampOffset(offset + panChange, scale)
    }
}

@Composable
fun rememberZoomTransformState(resetKey: Any): ZoomTransformState {
    val scope = rememberCoroutineScope()
    val state = remember(resetKey) { ZoomTransformState(scope) }
    LaunchedEffect(resetKey) {
        state.resetImmediate()
    }
    return state
}

fun Modifier.zoomContentTransform(state: ZoomTransformState): Modifier = this.graphicsLayer {
    scaleX = state.displayScale
    scaleY = state.displayScale
    translationX = state.displayOffset.x
    translationY = state.displayOffset.y
}

fun Modifier.zoomPinchPan(state: ZoomTransformState): Modifier = pointerInput(state.scale, state.containerSize) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val pressed = event.changes.count { it.pressed }
            if (pressed >= 2) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                val centroid = event.calculateCentroid(useCurrent = false)
                event.consumePositionChanges()
                state.setGestureActive(true)
                state.applyPinch(centroid, panChange, zoomChange)
            } else if (state.scale > ZOOMED_THRESHOLD && pressed == 1) {
                val panChange = event.calculatePan()
                if (panChange != Offset.Zero) {
                    event.consumePositionChanges()
                    state.setGestureActive(true)
                    state.panBy(panChange)
                }
            }
        } while (event.changes.any { it.pressed })
        state.onPinchGestureEnd()
    }
}

@Composable
fun ZoomableBox(
    state: ZoomTransformState,
    modifier: Modifier = Modifier,
    enableDoubleTap: Boolean = true,
    onSingleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    LaunchedEffect(state.scale, state.offset, state.gestureActive) {
        if (!state.gestureActive) {
            state.scaleAnim.snapTo(state.scale)
            state.offsetXAnim.snapTo(state.offset.x)
            state.offsetYAnim.snapTo(state.offset.y)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { state.containerSize = it.toSize() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zoomContentTransform(state),
            content = content,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onSingleTap != null || enableDoubleTap || onLongPress != null) {
                        Modifier.pointerInput(state, enableDoubleTap) {
                            detectTapGestures(
                                onTap = { onSingleTap?.invoke() },
                                onDoubleTap = if (enableDoubleTap) {
                                    { tap -> state.doubleTapAt(tap) }
                                } else {
                                    null
                                },
                                onLongPress = onLongPress?.let { { it() } },
                            )
                        }
                    } else {
                        Modifier
                    },
                )
                .zoomPinchPan(state),
        )
    }
}

private fun PointerEvent.consumePositionChanges() {
    changes.forEach { change: PointerInputChange ->
        if (change.positionChanged()) {
            change.consume()
        }
    }
}
