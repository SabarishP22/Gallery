package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.repository.WallpaperRepository

class ClearWallpaperUseCase(
    private val wallpaperRepository: WallpaperRepository,
    private val syncWallpaperLauncherUseCase: SyncWallpaperLauncherUseCase,
) {
    suspend operator fun invoke() {
        wallpaperRepository.deleteStoredFile()
        wallpaperRepository.clear()
        syncWallpaperLauncherUseCase(null)
    }
}
