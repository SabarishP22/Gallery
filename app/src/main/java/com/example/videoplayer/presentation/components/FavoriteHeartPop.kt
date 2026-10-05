package com.example.videoplayer.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FavoriteHeartPop(
    playKey: Long,
    modifier: Modifier = Modifier,
) {
    if (playKey == 0L) return
    key(playKey) {
        var popPhase by remember { mutableStateOf(true) }
        LaunchedEffect(playKey) {
            delay(2_000)
            popPhase = false
        }
        val scale by animateFloatAsState(
            targetValue = if (popPhase) 1f else 0.2f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            label = "heartPopScale",
        )
        val alpha by animateFloatAsState(
            targetValue = if (popPhase) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "heartPopAlpha",
        )
        if (alpha > 0.02f) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier
                        .size(44.dp)
                        .scale(scale)
                        .alpha(alpha),
                )
            }
        }
    }
}
