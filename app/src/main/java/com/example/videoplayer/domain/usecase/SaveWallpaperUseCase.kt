package com.example.videoplayer.domain.usecase

import android.net.Uri
import com.example.videoplayer.domain.repository.WallpaperRepository

class SaveWallpaperUseCase(
    private val wallpaperRepository: WallpaperRepository,
    private val syncWallpaperLauncherUseCase: SyncWallpaperLauncherUseCase,
) {
    suspend operator fun invoke(
        sourceUri: Uri,
        blurRadiusDp: Float,
        usePhotoAppIcon: Boolean,
    ): Result<String> {
        val persistedUri = wallpaperRepository.persistFromSourceUri(sourceUri).getOrElse {
            return Result.failure(it)
        }
        wallpaperRepository.save(persistedUri, blurRadiusDp, usePhotoAppIcon)
        syncWallpaperLauncherUseCase(persistedUri, usePhotoAppIcon)
        return Result.success(persistedUri)
    }
}
