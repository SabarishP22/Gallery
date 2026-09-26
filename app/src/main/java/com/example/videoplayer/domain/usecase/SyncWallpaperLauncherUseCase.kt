package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.repository.LauncherIconRepository

class SyncWallpaperLauncherUseCase(
    private val launcherIconRepository: LauncherIconRepository,
) {
    suspend operator fun invoke(wallpaperUri: String?) {
        launcherIconRepository.syncForWallpaper(wallpaperUri)
    }
}
