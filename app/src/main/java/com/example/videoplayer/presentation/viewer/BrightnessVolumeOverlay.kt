package com.example.videoplayer.presentation.viewer

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.content.res.Configuration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.TextPrimary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BrightnessVolumeGestureLayer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (!enabled) return

    val context = LocalContext.current
    val activity = context as? Activity
    val density = LocalDensity.current
    val sweepPx = remember(density) { with(density) { 100.dp.toPx() } }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    val initialWindowBrightness = remember(activity) {
        activity?.window?.attributes?.screenBrightness?.takeIf { it >= 0f }
            ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }
    val brightnessHolder = remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness?.takeIf { it in 0.01f..1f } ?: 0.5f,
        )
    }
    val volumeHolder = remember {
        mutableFloatStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume.toFloat(),
        )
    }
    var brightnessUi by brightnessHolder
    var volumeUi by volumeHolder

    var showBrightness by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var hideBrightnessJob by remember { mutableStateOf<Job?>(null) }
    var hideVolumeJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(activity) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window?.let { w ->
                val attrs = w.attributes
                attrs.screenBrightness = initialWindowBrightness
                w.attributes = attrs
            }
        }
    }

    fun applyWindowBrightness(value: Float) {
        val window = activity?.window ?: return
        val clamped = value.coerceIn(0.05f, 1f)
        brightnessHolder.floatValue = clamped
        val attrs = window.attributes
        attrs.screenBrightness = clamped
        window.attributes = attrs
    }

    fun applyVolumeFraction(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        volumeHolder.floatValue = clamped
        val index = (clamped * maxVolume).roundToInt().coerceIn(0, maxVolume)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            index,
            0,
        )
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = modifier) {
        val edgeWidth = maxWidth * 0.26f

        GestureEdge(
            modifier = Modifier
                .align(if (isLandscape) Alignment.CenterStart else Alignment.CenterStart)
                .fillMaxHeight()
                .padding(top = 88.dp, bottom = 100.dp)
                .width(edgeWidth.coerceAtLeast(96.dp)),
            sweepPx = sweepPx,
            onDragDeltaY = { deltaY, startValue ->
                applyWindowBrightness(startValue + (-deltaY / sweepPx))
                showBrightness = true
                hideBrightnessJob?.cancel()
                hideBrightnessJob = scope.launch {
                    delay(1_200)
                    showBrightness = false
                }
            },
            startValue = { brightnessHolder.floatValue },
        )
        GestureEdge(
            modifier = Modifier
                .align(if (isLandscape) Alignment.CenterEnd else Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(top = 88.dp, bottom = 100.dp)
                .width(edgeWidth.coerceAtLeast(96.dp)),
            sweepPx = sweepPx,
            onDragDeltaY = { deltaY, startValue ->
                applyVolumeFraction(startValue + (-deltaY / sweepPx))
                showVolume = true
                hideVolumeJob?.cancel()
                hideVolumeJob = scope.launch {
                    delay(1_200)
                    showVolume = false
                }
            },
            startValue = { volumeHolder.floatValue },
        )

        VerticalHud(
            visible = showBrightness,
            progress = brightnessUi,
            label = "${(brightnessUi * 100).roundToInt()}%",
            icon = {
                Icon(Icons.Default.Brightness6, contentDescription = null, tint = AuroraCyan, modifier = Modifier.size(22.dp))
            },
            modifier = Modifier.align(Alignment.CenterStart).padding(start = if (isLandscape) 16.dp else 28.dp),
        )
        VerticalHud(
            visible = showVolume,
            progress = volumeUi,
            label = "${(volumeUi * 100).roundToInt()}%",
            icon = {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = AuroraViolet, modifier = Modifier.size(22.dp))
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = if (isLandscape) 16.dp else 28.dp),
        )
    }
}

@Composable
private fun GestureEdge(
    modifier: Modifier,
    sweepPx: Float,
    startValue: () -> Float,
    onDragDeltaY: (Float, Float) -> Unit,
) {
    Box(
        modifier = modifier.pointerInput(sweepPx) {
            var dragSum = 0f
            var start = startValue()
            detectDragGestures(
                onDragStart = {
                    dragSum = 0f
                    start = startValue()
                },
                onDragEnd = { dragSum = 0f },
                onDragCancel = { dragSum = 0f },
                onDrag = { _, dragAmount ->
                    dragSum += dragAmount.y
                    onDragDeltaY(dragSum, start)
                },
            )
        },
    )
}

@Composable
private fun VerticalHud(
    visible: Boolean,
    progress: Float,
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = spring(stiffness = 400f),
        label = "hudScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "hudAlpha",
    )
    if (alpha <= 0.01f) return

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(20.dp))
            .background(DeepSpace.copy(alpha = 0.82f))
            .padding(horizontal = 14.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
            )
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TextPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(AuroraCyan, AuroraViolet),
                            ),
                        ),
                )
            }
        }
    }
}
