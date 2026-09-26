package com.example.videoplayer.domain.usecase

import android.net.Uri
import com.example.videoplayer.domain.model.SavedWallpaper
import com.example.videoplayer.domain.repository.WallpaperRepository

class SaveWallpaperUseCase(
    private val wallpaperRepository: WallpaperRepository,
) {
    suspend operator fun invoke(
        sourceUri: Uri,
        blurRadiusDp: Float,
    ): Result<SavedWallpaper> {
        val saved = wallpaperRepository.persistFromSourceUri(sourceUri).getOrElse {
            return Result.failure(it)
        }
        val revision = wallpaperRepository.save(saved.uri, blurRadiusDp)
        return Result.success(
            saved.copy(contentRevision = revision),
        )
    }
}
