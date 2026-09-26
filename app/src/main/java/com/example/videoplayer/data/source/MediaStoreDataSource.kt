package com.example.videoplayer.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.videoplayer.domain.model.GalleryMedia

class MediaStoreDataSource(
    private val context: Context,
) {

    fun loadAllMedia(): List<GalleryMedia> {
        val images = queryMedia(
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mimeColumn = MediaStore.Images.Media.MIME_TYPE,
        )
        val videos = queryMedia(
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            mimeColumn = MediaStore.Video.Media.MIME_TYPE,
        )
        return (images + videos).distinctBy { it.id to it.uri }
    }

    private fun queryMedia(collection: Uri, mimeColumn: String): List<GalleryMedia> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            mimeColumn,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.Video.Media.DURATION,
        )

        val selection: String?
        val selectionArgs: Array<String>?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.MediaColumns.IS_TRASHED} = ?"
            selectionArgs = arrayOf("0")
        } else {
            selection = null
            selectionArgs = null
        }

        val items = mutableListOf<GalleryMedia>()
        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val mimeCol = cursor.getColumnIndexOrThrow(mimeColumn)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items += GalleryMedia(
                    id = id,
                    uri = uri,
                    displayName = cursor.getString(nameCol) ?: "Untitled",
                    dateAddedSec = cursor.getLong(dateCol),
                    mimeType = cursor.getString(mimeCol) ?: "application/octet-stream",
                    sizeBytes = cursor.getLong(sizeCol).coerceAtLeast(0L),
                    durationMs = cursor.getLong(durationCol).coerceAtLeast(0L),
                    width = cursor.getInt(widthCol).coerceAtLeast(0),
                    height = cursor.getInt(heightCol).coerceAtLeast(0),
                )
            }
        }
        return items
    }

    fun moveToTrash(uris: List<Uri>): Result<Unit> {
        if (uris.isEmpty()) return Result.success(Unit)
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                error("Use createTrashRequest on API 30+")
            } else {
                uris.forEach { uri ->
                    context.contentResolver.delete(uri, null, null)
                }
            }
        }
    }

    fun createTrashPendingIntent(uris: List<Uri>): android.app.PendingIntent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || uris.isEmpty()) return null
        return MediaStore.createTrashRequest(context.contentResolver, uris, true)
    }
}
