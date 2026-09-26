package com.example.videoplayer.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.StorageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageStatsDataSource(
    private val context: Context,
) {

    companion object {
        private const val MATCH_EXCLUDE_TRASHED = 0
        private const val MATCH_ONLY_TRASHED = 2
    }

    suspend fun load(libraryMedia: List<GalleryMedia>): StorageStats = withContext(Dispatchers.IO) {
        val photos = sumCollection(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            trashed = false,
        )
        val videos = sumCollection(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            trashed = false,
        )
        val trash = sumAllTrashedOnDevice()
        val (totalBytes, freeBytes) = readPrimaryStorage()
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
        val libraryPhotos = libraryMedia.count { !it.isVideo }
        val libraryVideos = libraryMedia.count { it.isVideo }

        StorageStats(
            photoBytes = photos.first,
            photoCount = photos.second,
            videoBytes = videos.first,
            videoCount = videos.second,
            trashBytes = trash.first,
            trashCount = trash.second,
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = usedBytes,
            libraryPhotoCount = libraryPhotos,
            libraryVideoCount = libraryVideos,
        )
    }

    /**
     * All trashed items visible through MediaStore (matches system gallery / Files trash for media).
     */
    private fun sumAllTrashedOnDevice(): Pair<Long, Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return 0L to 0
        }

        val volumes = externalVolumeNames()
        var bytes = 0L
        var count = 0
        for (volume in volumes) {
            val part = sumCollection(MediaStore.Files.getContentUri(volume), trashed = true)
            bytes += part.first
            count += part.second
        }

        if (count == 0) {
            for (volume in volumes) {
                val imagesUri = MediaStore.Images.Media.getContentUri(volume)
                val videosUri = MediaStore.Video.Media.getContentUri(volume)
                val photos = sumCollection(imagesUri, trashed = true)
                val videos = sumCollection(videosUri, trashed = true)
                bytes += photos.first + videos.first
                count += photos.second + videos.second
            }
        }

        return bytes to count
    }

    private fun externalVolumeNames(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.getExternalVolumeNames(context).toList().ifEmpty {
                listOf(MediaStore.VOLUME_EXTERNAL)
            }
        } else {
            listOf(MediaStore.VOLUME_EXTERNAL)
        }
    }

    private fun sumCollection(uri: Uri, trashed: Boolean): Pair<Long, Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && trashed) {
            return 0L to 0
        }

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.SIZE,
        )

        val cursor = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val queryArgs = Bundle().apply {
                    putInt(
                        MediaStore.QUERY_ARG_MATCH_TRASHED,
                        if (trashed) MATCH_ONLY_TRASHED else MATCH_EXCLUDE_TRASHED,
                    )
                }
                context.contentResolver.query(uri, projection, queryArgs, null)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val selection = "${MediaStore.MediaColumns.IS_TRASHED} = ?"
                val selectionArgs = arrayOf(if (trashed) "1" else "0")
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            }
            else -> {
                if (trashed) return 0L to 0
                context.contentResolver.query(uri, projection, null, null, null)
            }
        }

        var bytes = 0L
        var count = 0
        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (c.moveToNext()) {
                count++
                val id = c.getLong(idCol)
                val reportedSize = c.getLong(sizeCol)
                bytes += resolveItemSize(
                    collection = uri,
                    id = id,
                    reportedSize = reportedSize,
                )
            }
        }
        return bytes to count
    }

    private fun resolveItemSize(collection: Uri, id: Long, reportedSize: Long): Long {
        if (reportedSize > 0L) return reportedSize
        val itemUri = ContentUris.withAppendedId(collection, id)
        return readSizeFromUri(itemUri)
    }

    private fun readSizeFromUri(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.statSize.coerceAtLeast(0L)
            } ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun readPrimaryStorage(): Pair<Long, Long> {
        val path = Environment.getExternalStorageDirectory()?.absolutePath ?: return 0L to 0L
        return try {
            val stat = StatFs(path)
            stat.totalBytes to stat.availableBytes
        } catch (_: Exception) {
            0L to 0L
        }
    }
}
