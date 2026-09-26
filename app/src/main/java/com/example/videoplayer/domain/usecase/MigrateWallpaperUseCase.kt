package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.repository.WallpaperRepository

class MigrateWallpaperUseCase(
    private val wallpaperRepository: WallpaperRepository,
) {
    suspend operator fun invoke() {
        wallpaperRepository.migrateLegacyUriIfNeeded()
    }
}
