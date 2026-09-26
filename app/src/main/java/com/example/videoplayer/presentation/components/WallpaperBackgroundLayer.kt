package com.example.videoplayer.presentation.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun WallpaperBackgroundLayer(
    wallpaperUri: String?,
    contentRevision: Long,
    blurPx: Float,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(wallpaperUri, contentRevision) {
        bitmap = withContext(Dispatchers.IO) {
            val path = wallpaperUri.wallpaperFilePath() ?: return@withContext null
            val file = File(path)
            if (!file.isFile || file.length() <= 0L) return@withContext null
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }
    }

    val image = bitmap ?: return

    Image(
        bitmap = image,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxSize()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurPx > 0.5f) {
                    Modifier.graphicsLayer {
                        renderEffect = androidx.compose.ui.graphics.BlurEffect(
                            blurPx,
                            blurPx,
                            androidx.compose.ui.graphics.TileMode.Clamp,
                        )
                    }
                } else if (blurPx > 0.5f) {
                    Modifier.graphicsLayer {
                        scaleX = 1.12f
                        scaleY = 1.12f
                    }
                } else {
                    Modifier
                },
            ),
    )
}

internal fun String?.wallpaperFilePath(): String? {
    if (this.isNullOrBlank()) return null
    return if (startsWith("file://", ignoreCase = true)) {
        Uri.parse(this).path
    } else {
        this
    }
}
