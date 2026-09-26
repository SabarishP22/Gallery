package com.example.videoplayer.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.DialogPanelBackground
import com.example.videoplayer.presentation.theme.DialogPanelBorder
import com.example.videoplayer.presentation.theme.TextPrimary
import kotlinx.coroutines.delay

private enum class SnackbarTone {
    Success,
    Error,
    Info,
}

@Composable
fun PixLabSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data -> PixLabSnackbarContent(data = data) },
    )
}

@Composable
private fun PixLabSnackbarContent(data: SnackbarData) {
    val message = data.visuals.message
    val tone = remember(message) { toneForMessage(message) }

    var animateIn by remember(message) { mutableStateOf(false) }
    LaunchedEffect(message) {
        animateIn = false
        delay(40)
        animateIn = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "snackbarScale",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "snackbarIconScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "snackbarAlpha",
    )

    val style = toneStyle(tone)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .scale(cardScale),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DialogPanelBackground)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            style.glow.copy(alpha = 0.14f * contentAlpha),
                            Color.Transparent,
                        ),
                    ),
                )
                .border(1.dp, DialogPanelBorder, RoundedCornerShape(18.dp))
                .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(iconScale)
                    .clip(CircleShape)
                    .background(style.tint.copy(alpha = 0.18f))
                    .border(1.dp, style.tint.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.tint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = message,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary.copy(alpha = contentAlpha.coerceIn(0f, 1f)),
            )
            IconButton(
                onClick = { data.dismiss() },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextPrimary.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun toneForMessage(message: String): SnackbarTone {
    val lower = message.lowercase()
    return when {
        lower.contains("could not") || lower.contains("failed") -> SnackbarTone.Error
        lower.contains("success") ||
            lower.contains("updated") ||
            lower.contains("reset to default") -> SnackbarTone.Success
        else -> SnackbarTone.Info
    }
}

private data class ToneStyle(
    val icon: ImageVector,
    val tint: Color,
    val glow: Color,
)

private fun toneStyle(tone: SnackbarTone): ToneStyle {
    return when (tone) {
        SnackbarTone.Success -> ToneStyle(
            icon = Icons.Default.Check,
            tint = Color(0xFF4CAF50),
            glow = Color(0xFF4CAF50),
        )
        SnackbarTone.Error -> ToneStyle(
            icon = Icons.Default.Close,
            tint = Color(0xFFFF5252),
            glow = Color(0xFFFF5252),
        )
        SnackbarTone.Info -> ToneStyle(
            icon = Icons.Default.Info,
            tint = AuroraCyan,
            glow = AuroraViolet,
        )
    }
}
