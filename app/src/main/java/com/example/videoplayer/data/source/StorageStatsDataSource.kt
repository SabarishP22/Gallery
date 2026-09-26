package com.example.videoplayer.data.source

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.example.videoplayer.domain.model.StorageStats
import com.example.videoplayer.domain.model.GalleryMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageStatsDataSource(
    private val context: Context,
) {

    suspend fun load(libraryMedia: List<GalleryMedia>): StorageStats = withContext(Dispatchers.IO) {
        val photos = sumCollection(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            trashed = false,
        )
        val videos = sumCollection(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            trashed = false,
        )
        val trashPhotos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            sumCollection(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, trashed = true)
        } else {
            0L to 0
        }
        val trashVideos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            sumCollection(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, trashed = true)
        } else {
            0L to 0
        }
        val (totalBytes, freeBytes) = readPrimaryStorage()
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
        val libraryPhotos = libraryMedia.count { !it.isVideo }
        val libraryVideos = libraryMedia.count { it.isVideo }

        StorageStats(
            photoBytes = photos.first,
            photoCount = photos.second,
            videoBytes = videos.first,
            videoCount = videos.second,
            trashBytes = trashPhotos.first + trashVideos.first,
            trashCount = trashPhotos.second + trashVideos.second,
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = usedBytes,
            libraryPhotoCount = libraryPhotos,
            libraryVideoCount = libraryVideos,
        )
    }

    private fun sumCollection(uri: Uri, trashed: Boolean): Pair<Long, Int> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.SIZE,
        )
        val selection: String?
        val selectionArgs: Array<String>?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.MediaColumns.IS_TRASHED} = ?"
            selectionArgs = arrayOf(if (trashed) "1" else "0")
        } else if (trashed) {
            return 0L to 0
        } else {
            selection = null
            selectionArgs = null
        }

        var bytes = 0L
        var count = 0
        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) {
                count++
                bytes += cursor.getLong(sizeCol).coerceAtLeast(0L)
            }
        }
        return bytes to count
    }

    private fun readPrimaryStorage(): Pair<Long, Long> {
        val path = Environment.getExternalStorageDirectory()?.absolutePath ?: return 0L to 0L
        return try {
            val stat = StatFs(path)
            val total = stat.totalBytes
            val free = stat.availableBytes
            total to free
        } catch (_: Exception) {
            0L to 0L
        }
    }
}
