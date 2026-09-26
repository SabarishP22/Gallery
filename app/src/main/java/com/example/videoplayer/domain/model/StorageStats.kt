package com.example.videoplayer.domain.model

data class StorageStats(
    val photoBytes: Long,
    val photoCount: Int,
    val videoBytes: Long,
    val videoCount: Int,
    val trashBytes: Long,
    val trashCount: Int,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val libraryPhotoCount: Int,
    val libraryVideoCount: Int,
) {
    val otherBytes: Long
        get() = (usedBytes - photoBytes - videoBytes - trashBytes).coerceAtLeast(0L)

    val usedFraction: Float
        get() = if (totalBytes > 0L) usedBytes.toFloat() / totalBytes else 0f

    val freeFraction: Float
        get() = if (totalBytes > 0L) freeBytes.toFloat() / totalBytes else 0f
}
