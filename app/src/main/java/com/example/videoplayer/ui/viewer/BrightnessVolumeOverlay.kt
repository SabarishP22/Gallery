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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.GlassWhiteStrong
import androidx.compose.runtime.rememberCoroutineScope
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
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    var brightness by remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness?.takeIf { it >= 0f } ?: 0.5f,
        )
    }
    var volume by remember {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
    var showBrightness by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun hideBrightnessLater() {
        scope.launch {
            delay(900)
            showBrightness = false
        }
    }

    fun hideVolumeLater() {
        scope.launch {
            delay(900)
            showVolume = false
        }
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(72.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        val delta = -dragAmount / size.height
                        brightness = (brightness + delta * 1.4f).coerceIn(0.05f, 1f)
                        activity?.window?.let { window ->
                            val attrs = window.attributes
                            attrs.screenBrightness = brightness
                            window.attributes = attrs
                        }
                        showBrightness = true
                        hideBrightnessLater()
                    }
                },
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(72.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        val delta = (-dragAmount / size.height * maxVolume * 1.2f).toInt()
                        if (delta != 0) {
                            volume = (volume + delta).coerceIn(0, maxVolume)
                            audioManager.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                volume,
                                0,
                            )
                            showVolume = true
                            hideVolumeLater()
                        }
                    }
                },
        )

        AnimatedVisibility(
            visible = showBrightness,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp),
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
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp),
        ) {
            GestureIndicator(
                icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = AuroraCyan) },
                progress = if (maxVolume > 0) volume.toFloat() / maxVolume else 0f,
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
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
