package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.repository.WallpaperRepository

class ClearWallpaperUseCase(
    private val wallpaperRepository: WallpaperRepository,
) {
    suspend operator fun invoke(): Long {
        wallpaperRepository.deleteStoredFile()
        return wallpaperRepository.clear()
    }
}
