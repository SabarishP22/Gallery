package com.example.videoplayer.domain.repository

import android.net.Uri
import com.example.videoplayer.domain.model.WallpaperSettings
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {
    val settings: Flow<WallpaperSettings>
    suspend fun save(imageUri: String, blurRadiusDp: Float, usePhotoAppIcon: Boolean)
    suspend fun clear()
    suspend fun persistFromSourceUri(sourceUri: Uri): Result<String>
    fun storedFileUri(): String?
    fun deleteStoredFile()
    suspend fun migrateLegacyUriIfNeeded()
}
