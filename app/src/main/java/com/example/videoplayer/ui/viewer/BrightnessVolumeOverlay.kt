package com.example.videoplayer.ui.viewer

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.GlassWhiteStrong
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BrightnessVolumeGestureLayer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (!enabled) return

    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    var brightness by remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness?.takeIf { it in 0.01f..1f } ?: 0.5f,
        )
    }
    var volume by remember {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
    var showBrightness by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var hideBrightnessJob by remember { mutableStateOf<Job?>(null) }
    var hideVolumeJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun applyBrightness(value: Float) {
        val window = activity?.window ?: return
        brightness = value.coerceIn(0.05f, 1f)
        val attrs = window.attributes
        attrs.screenBrightness = brightness
        window.attributes = attrs
    }

    Box(modifier = modifier.zIndex(10f)) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(96.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        val delta = -dragAmount / size.height.toFloat() * 1.1f
                        applyBrightness(brightness + delta)
                        showBrightness = true
                        hideBrightnessJob?.cancel()
                        hideBrightnessJob = scope.launch {
                            delay(900)
                            showBrightness = false
                        }
                    }
                },
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(96.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount < 0f) {
                                audioManager.adjustStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    AudioManager.ADJUST_RAISE,
                                    AudioManager.FLAG_SHOW_UI,
                                )
                            } else if (dragAmount > 0f) {
                                audioManager.adjustStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    AudioManager.ADJUST_LOWER,
                                    AudioManager.FLAG_SHOW_UI,
                                )
                            }
                            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            showVolume = true
                            hideVolumeJob?.cancel()
                            hideVolumeJob = scope.launch {
                                delay(900)
                                showVolume = false
                            }
                        },
                    )
                },
        )

        AnimatedVisibility(
            visible = showBrightness,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp),
        ) {
            GestureIndicator(
                icon = { Icon(Icons.Default.Brightness6, contentDescription = null, tint = AuroraCyan) },
                progress = brightness,
            )
        }
        AnimatedVisibility(
            visible = showVolume,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
        ) {
            GestureIndicator(
                icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = AuroraCyan) },
                progress = volume.toFloat() / maxVolume.toFloat(),
            )
        }
    }
}

@Composable
private fun GestureIndicator(
    icon: @Composable () -> Unit,
    progress: Float,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhiteStrong)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(48.dp),
                color = AuroraCyan,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
