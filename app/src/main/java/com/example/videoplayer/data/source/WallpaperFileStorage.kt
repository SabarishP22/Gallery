package com.example.videoplayer.data.source

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WallpaperFileStorage(
    private val context: Context,
) {

    suspend fun persistFromSourceUri(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
        val outFile = File(dir, FILE_NAME)
        val input = context.contentResolver.openInputStream(sourceUri)
            ?: error("Could not read image")
        input.use { stream ->
            outFile.outputStream().use { output ->
                stream.copyTo(output)
            }
        }
        Uri.fromFile(outFile).toString()
    }

    fun deleteStoredWallpaper() {
        File(context.filesDir, DIR_NAME).deleteRecursively()
    }

    fun storedFileUri(): String? {
        val file = File(File(context.filesDir, DIR_NAME), FILE_NAME)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else null
    }

    companion object {
        private const val DIR_NAME = "wallpaper"
        private const val FILE_NAME = "background.jpg"
    }
}
