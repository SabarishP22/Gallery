package com.example.videoplayer.presentation.components

import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import com.example.videoplayer.util.softenWallpaperForDisplay
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

    LaunchedEffect(wallpaperUri, contentRevision, blurPx) {
        bitmap = withContext(Dispatchers.IO) {
            val path = wallpaperUri.wallpaperFilePath() ?: return@withContext null
            val file = File(path)
            if (!file.isFile || file.length() <= 0L) return@withContext null
            val decoded = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext null
            val display = softenWallpaperForDisplay(decoded, blurPx)
            if (display !== decoded) {
                decoded.recycle()
            }
            display.asImageBitmap()
        }
    }

    val image = bitmap ?: return

    Image(
        bitmap = image,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
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
