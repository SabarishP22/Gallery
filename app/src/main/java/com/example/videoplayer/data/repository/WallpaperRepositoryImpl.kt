package com.example.videoplayer.data.repository

import android.net.Uri
import com.example.videoplayer.data.source.WallpaperFileStorage
import com.example.videoplayer.data.source.WallpaperPreferencesDataSource
import com.example.videoplayer.domain.model.SavedWallpaper
import com.example.videoplayer.domain.model.WallpaperSettings
import com.example.videoplayer.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WallpaperRepositoryImpl(
    private val preferencesDataSource: WallpaperPreferencesDataSource,
    private val fileStorage: WallpaperFileStorage,
) : WallpaperRepository {

    override val settings: Flow<WallpaperSettings> = preferencesDataSource.settings

    override suspend fun save(imageUri: String, blurRadiusDp: Float): Long {
        return preferencesDataSource.save(imageUri, blurRadiusDp)
    }

    override suspend fun clear(): Long {
        return preferencesDataSource.clear()
    }

    override suspend fun persistFromSourceUri(sourceUri: Uri): Result<SavedWallpaper> {
        return runCatching {
            val persisted = fileStorage.persistFromSourceUri(sourceUri)
            SavedWallpaper(
                uri = persisted.uriString,
                contentVersion = persisted.contentVersion,
                contentRevision = 0L,
            )
        }
    }

    override fun storedFileUri(): String? = fileStorage.storedFileUri()

    override fun storedContentVersion(): Long = fileStorage.storedContentVersion()

    override fun deleteStoredFile() {
        fileStorage.deleteStoredWallpaper()
    }

    override suspend fun migrateLegacyUriIfNeeded() {
        val settings = preferencesDataSource.settings.first()
        val stored = fileStorage.storedFileUri()
        if (stored != null) {
            if (settings.imageUri != stored || settings.contentRevision == 0L) {
                preferencesDataSource.save(stored, settings.blurRadiusDp)
            }
            return
        }
        val legacy = settings.imageUri ?: return
        if (legacy.startsWith("file://")) return
        runCatching {
            val persisted = fileStorage.persistFromSourceUri(Uri.parse(legacy))
            preferencesDataSource.save(persisted.uriString, settings.blurRadiusDp)
        }.onFailure {
            preferencesDataSource.clear()
            fileStorage.deleteStoredWallpaper()
        }
    }
}
