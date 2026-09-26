package com.example.videoplayer.presentation.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.videoplayer.domain.model.StorageStats
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary
import com.example.videoplayer.util.formatFileSize
import kotlinx.coroutines.delay

@Composable
fun StorageDetailsDialog(
    visible: Boolean,
    loading: Boolean,
    stats: StorageStats?,
    errorMessage: String?,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(220)) + scaleIn(
                initialScale = 0.92f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
            exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.94f),
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 8.dp),
                cornerRadius = 28.dp,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AuroraCyan.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.SdStorage, contentDescription = null, tint = AuroraCyan)
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(
                                text = "Storage",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                            Text(
                                text = "Device & media breakdown",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    when {
                        loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = AuroraCyan, strokeWidth = 2.dp)
                            }
                        }
                        errorMessage != null -> {
                            Text(text = errorMessage, color = TextSecondary)
                        }
                        stats != null -> {
                            AnimatedStorageOverview(stats = stats)
                            AnimatedStorageBreakdown(stats = stats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedStorageOverview(stats: StorageStats) {
    val total = stats.totalBytes.coerceAtLeast(1L)
    val photoFrac = stats.photoBytes.toFloat() / total
    val videoFrac = stats.videoBytes.toFloat() / total
    val trashFrac = stats.trashBytes.toFloat() / total
    val otherFrac = stats.otherBytes.toFloat() / total
    val freeFrac = stats.freeBytes.toFloat() / total

    var ringStarted by remember(stats) { mutableStateOf(false) }
    LaunchedEffect(stats) {
        ringStarted = false
        delay(80)
        ringStarted = true
    }
    val ringProgress by animateFloatAsState(
        targetValue = if (ringStarted) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "storageRing",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(168.dp)) {
            Canvas(modifier = Modifier.size(168.dp)) {
                val stroke = 22f
                val diameter = size.minDimension - stroke
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                var start = -90f
                fun drawSegment(fraction: Float, color: Color) {
                    if (fraction <= 0f) return
                    val sweep = 360f * fraction * ringProgress
                    drawArc(
                        color = color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    start += sweep
                }
                drawArc(
                    color = DeepSpace.copy(alpha = 0.35f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawSegment(photoFrac, AuroraCyan)
                drawSegment(videoFrac, AuroraViolet)
                drawSegment(trashFrac, Color(0xFFFF5252).copy(alpha = 0.85f))
                drawSegment(otherFrac, TextSecondary.copy(alpha = 0.55f))
                drawSegment(freeFrac, Color(0xFF4CAF50).copy(alpha = 0.75f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedSizeLabel(
                    targetBytes = stats.usedBytes,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = "used", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                AnimatedSizeLabel(
                    targetBytes = stats.totalBytes,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    prefix = "of ",
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LegendDot(color = AuroraCyan, label = "Photos")
            LegendDot(color = AuroraViolet, label = "Videos")
            LegendDot(color = Color(0xFFFF5252), label = "Trash")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun AnimatedStorageBreakdown(stats: StorageStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedStorageRow(
            icon = Icons.Default.Image,
            title = "Photos",
            subtitle = "${stats.photoCount} on device · ${stats.libraryPhotoCount} in PixLab",
            bytes = stats.photoBytes,
            total = stats.totalBytes,
            color = AuroraCyan,
            delayMs = 0,
        )
        AnimatedStorageRow(
            icon = Icons.Default.Videocam,
            title = "Videos",
            subtitle = "${stats.videoCount} on device · ${stats.libraryVideoCount} in PixLab",
            bytes = stats.videoBytes,
            total = stats.totalBytes,
            color = AuroraViolet,
            delayMs = 80,
        )
        AnimatedStorageRow(
            icon = Icons.Default.DeleteOutline,
            title = "Trash",
            subtitle = "${stats.trashCount} items in trash",
            bytes = stats.trashBytes,
            total = stats.totalBytes,
            color = Color(0xFFFF5252),
            delayMs = 160,
        )
        AnimatedStorageRow(
            icon = Icons.Outlined.Folder,
            title = "Other apps & files",
            subtitle = "Everything else on this device",
            bytes = stats.otherBytes,
            total = stats.totalBytes,
            color = TextSecondary.copy(alpha = 0.7f),
            delayMs = 240,
        )
        AnimatedStorageRow(
            icon = Icons.Default.SdStorage,
            title = "Free space",
            subtitle = "Available to use",
            bytes = stats.freeBytes,
            total = stats.totalBytes,
            color = Color(0xFF4CAF50),
            delayMs = 320,
        )
    }
}

@Composable
private fun AnimatedStorageRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    bytes: Long,
    total: Long,
    color: Color,
    delayMs: Int,
) {
    var barStarted by remember(bytes, total) { mutableStateOf(false) }
    LaunchedEffect(bytes, total) {
        barStarted = false
        delay(delayMs.toLong())
        barStarted = true
    }
    val target = if (total > 0L) bytes.toFloat() / total else 0f
    val barAnim by animateFloatAsState(
        targetValue = if (barStarted) target.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "storageBar",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepSpace.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            AnimatedSizeLabel(
                targetBytes = bytes,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TextSecondary.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barAnim.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun AnimatedSizeLabel(
    targetBytes: Long,
    style: androidx.compose.ui.text.TextStyle,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = TextPrimary,
    prefix: String = "",
) {
    val displayAnim by animateFloatAsState(
        targetValue = targetBytes.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "storageBytes",
    )
    Text(
        text = prefix + formatFileSize(displayAnim.toLong()),
        style = style,
        fontWeight = fontWeight,
        color = color,
    )
}
