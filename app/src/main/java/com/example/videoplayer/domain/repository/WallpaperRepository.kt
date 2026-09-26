package com.example.videoplayer.domain.repository

import android.net.Uri
import com.example.videoplayer.domain.model.SavedWallpaper
import com.example.videoplayer.domain.model.WallpaperSettings
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {
    val settings: Flow<WallpaperSettings>
    suspend fun save(imageUri: String, blurRadiusDp: Float): Long
    suspend fun clear(): Long
    suspend fun persistFromSourceUri(sourceUri: Uri): Result<SavedWallpaper>
    fun storedFileUri(): String?
    fun storedContentVersion(): Long
    fun deleteStoredFile()
    suspend fun migrateLegacyUriIfNeeded()
}
