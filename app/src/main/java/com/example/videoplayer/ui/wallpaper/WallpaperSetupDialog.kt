package com.example.videoplayer.ui.wallpaper

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.AuroraViolet
import com.example.videoplayer.ui.theme.DeepSpace
import com.example.videoplayer.ui.theme.TextPrimary
import com.example.videoplayer.ui.theme.TextSecondary
import com.example.videoplayer.util.mediaThumbnailRequest

@Composable
fun WallpaperSetupDialog(
    media: GalleryMedia,
    initialBlurDp: Float,
    onDismiss: () -> Unit,
    onSave: (blurDp: Float) -> Unit,
    onClear: () -> Unit,
) {
    var blurDp by remember(media.id) { mutableFloatStateOf(initialBlurDp.coerceIn(0f, 40f)) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(DeepSpace.copy(alpha = 0.96f), AuroraViolet.copy(alpha = 0.18f)),
                    ),
                )
                .padding(20.dp),
        ) {
            Text(
                text = "App background",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = media.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepSpace),
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
                        .background(DeepSpace.copy(alpha = 0.25f)),
                )
                Text(
                    text = "Preview",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                )
            }

            Text(
                text = "Blur · ${blurDp.toInt()} dp",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Slider(
                value = blurDp,
                onValueChange = { blurDp = it },
                valueRange = 0f..40f,
                colors = SliderDefaults.colors(
                    thumbColor = AuroraCyan,
                    activeTrackColor = AuroraCyan,
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = { onSave(blurDp) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AuroraCyan, contentColor = DeepSpace),
                ) {
                    Text("Save")
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
