package com.example.videoplayer.data

import android.net.Uri

data class GalleryMedia(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAddedSec: Long,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
) {
    val isVideo: Boolean get() = mimeType.startsWith("video/")
}

enum class MediaFilter { ALL, IMAGES, VIDEOS }

enum class SortOrder {
    DATE_NEWEST,
    DATE_OLDEST,
    NAME_ASC,
    NAME_DESC,
    SIZE_LARGEST,
}
