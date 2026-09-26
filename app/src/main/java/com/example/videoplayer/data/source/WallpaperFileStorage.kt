package com.example.videoplayer.data.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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

        val decoded = decodeBitmapFromUri(sourceUri)
            ?: error("Could not read or decode image")

        val scaled = scaleDownIfNeeded(decoded, MAX_SAVE_EDGE_PX)
        if (scaled != decoded && !decoded.isRecycled) decoded.recycle()

        FileOutputStream(outFile).use { output ->
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                error("Could not write wallpaper file")
            }
            output.flush()
            output.fd.sync()
        }
        if (!scaled.isRecycled) scaled.recycle()

        val version = System.currentTimeMillis()
        outFile.setLastModified(version)

        PersistedWallpaper(
            uriString = Uri.fromFile(outFile).toString(),
            contentVersion = version,
        )
    }

    private fun decodeBitmapFromUri(sourceUri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching {
                val source = ImageDecoder.createSource(context.contentResolver, sourceUri)
                return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = false
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }
        }

        context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(stream)?.let { return it }
        }

        context.contentResolver.openFileDescriptor(sourceUri, "r")?.use { pfd ->
            BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)?.let { return it }
        }

        return null
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
