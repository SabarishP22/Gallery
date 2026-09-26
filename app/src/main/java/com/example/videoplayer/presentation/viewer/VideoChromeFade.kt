package com.example.videoplayer.presentation.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Short fade so chrome feels instant but not abrupt. */
const val VIDEO_CHROME_FADE_MS = 100

private val videoChromeEnter = fadeIn(
    animationSpec = tween(durationMillis = VIDEO_CHROME_FADE_MS, easing = LinearEasing),
)
private val videoChromeExit = fadeOut(
    animationSpec = tween(durationMillis = VIDEO_CHROME_FADE_MS, easing = LinearEasing),
)

@Composable
fun AnimatedVideoChrome(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = videoChromeEnter,
        exit = videoChromeExit,
        modifier = modifier,
    ) {
        content()
    }
}
