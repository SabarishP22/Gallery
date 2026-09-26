package com.example.videoplayer.data.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

class WallpaperFileStorage(
    private val context: Context,
) {

    suspend fun persistFromSourceUri(sourceUri: Uri): PersistedWallpaper = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
        val outFile = File(dir, FILE_NAME)
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } ?: error("Could not read image")
        val sampleSize = computeSampleSize(
            width = boundsOptions.outWidth.coerceAtLeast(1),
            height = boundsOptions.outHeight.coerceAtLeast(1),
            maxEdge = MAX_SAVE_EDGE_PX,
        )
        val decoded = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(
                stream,
                null,
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                },
            )
        } ?: error("Could not decode image")

        val scaled = scaleDownIfNeeded(decoded, MAX_SAVE_EDGE_PX)
        if (scaled != decoded && !decoded.isRecycled) decoded.recycle()

        FileOutputStream(outFile).use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }
        if (!scaled.isRecycled) scaled.recycle()

        PersistedWallpaper(
            uriString = Uri.fromFile(outFile).toString(),
            contentVersion = outFile.lastModified(),
        )
    }

    fun deleteStoredWallpaper() {
        File(context.filesDir, DIR_NAME).deleteRecursively()
    }

    fun storedFileUri(): String? {
        val file = File(File(context.filesDir, DIR_NAME), FILE_NAME)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else null
    }

    fun storedContentVersion(): Long {
        val file = File(File(context.filesDir, DIR_NAME), FILE_NAME)
        return if (file.exists()) file.lastModified() else 0L
    }

    private fun scaleDownIfNeeded(source: Bitmap, maxEdge: Int): Bitmap {
        val w = source.width
        val h = source.height
        val longest = max(w, h)
        if (longest <= maxEdge) return source
        val scale = maxEdge.toFloat() / longest
        val targetW = (w * scale).roundToInt().coerceAtLeast(1)
        val targetH = (h * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun computeSampleSize(width: Int, height: Int, maxEdge: Int): Int {
        var sample = 1
        val longest = max(width, height)
        while (longest / sample > maxEdge * 2) {
            sample *= 2
        }
        return sample
    }

    data class PersistedWallpaper(
        val uriString: String,
        val contentVersion: Long,
    )

    companion object {
        private const val DIR_NAME = "wallpaper"
        private const val FILE_NAME = "background.jpg"
        private const val MAX_SAVE_EDGE_PX = 1920
        private const val JPEG_QUALITY = 88
    }
}
