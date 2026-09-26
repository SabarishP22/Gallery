package com.example.videoplayer.presentation.wallpaper

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.components.GlassSurfaceStyle
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.DialogSurfaceFill
import com.example.videoplayer.presentation.theme.DialogSurfaceStroke
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary
import com.example.videoplayer.util.mediaThumbnailRequest
import kotlinx.coroutines.delay

@Composable
fun WallpaperSetupDialog(
    media: GalleryMedia,
    initialBlurDp: Float,
    onDismiss: () -> Unit,
    onSave: (blurDp: Float) -> Unit,
    onClear: () -> Unit,
) {
    var blurDp by remember(media.id) { mutableFloatStateOf(initialBlurDp.coerceIn(0f, 40f)) }
    var entered by remember(media.id) { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(media.id) {
        entered = false
        delay(16)
        entered = true
    }

    val dialogScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "dialogScale",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .scale(dialogScale)
                .clip(RoundedCornerShape(28.dp))
                .background(DialogSurfaceFill)
                .border(
                    width = 1.dp,
                    color = DialogSurfaceStroke,
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AuroraCyan.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = AuroraCyan)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            text = "Customize background",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = media.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                AnimatedVisibility(
                    visible = entered,
                    enter = fadeIn(tween(220)) +
                        scaleIn(
                            initialScale = 0.94f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        ),
                    exit = fadeOut() + scaleOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                            .border(
                                1.dp,
                                AuroraCyan.copy(alpha = 0.25f),
                                RoundedCornerShape(20.dp),
                            ),
                    ) {
                        AsyncImage(
                            model = mediaThumbnailRequest(context, media, pixelSize = 900),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .wallpaperBlur(blurDp),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            DeepSpace.copy(alpha = 0.45f),
                                        ),
                                    ),
                                ),
                        )
                        Text(
                            text = "Live preview",
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                        )
                    }
                }

                GlassSurface(
                    style = GlassSurfaceStyle.Dialog,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Background blur",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                            Text(
                                text = "${blurDp.toInt()} dp",
                                style = MaterialTheme.typography.labelLarge,
                                color = AuroraCyan,
                            )
                        }
                        Slider(
                            value = blurDp,
                            onValueChange = { blurDp = it },
                            valueRange = 0f..40f,
                            colors = SliderDefaults.colors(
                                thumbColor = AuroraCyan,
                                activeTrackColor = AuroraCyan,
                                inactiveTrackColor = TextSecondary.copy(alpha = 0.35f),
                            ),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onClear,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepSpace.copy(alpha = 0.85f),
                            contentColor = TextPrimary,
                        ),
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Reset", modifier = Modifier.padding(start = 6.dp))
                    }
                    Button(
                        onClick = { onSave(blurDp) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuroraCyan,
                            contentColor = DeepSpace,
                        ),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Apply", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.wallpaperBlur(blurDp: Float): Modifier {
    if (blurDp <= 0.5f) return this
    val blurPx = with(LocalDensity.current) { blurDp.dp.toPx() }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        graphicsLayer {
            renderEffect = androidx.compose.ui.graphics.BlurEffect(
                blurPx,
                blurPx,
                androidx.compose.ui.graphics.TileMode.Clamp,
            )
        }
    } else {
        graphicsLayer {
            scaleX = 1.12f
            scaleY = 1.12f
            alpha = 0.92f
        }
    }
}
