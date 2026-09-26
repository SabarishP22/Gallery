package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.model.WallpaperSettings
import com.example.videoplayer.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow

class ObserveWallpaperSettingsUseCase(
    private val wallpaperRepository: WallpaperRepository,
) {
    operator fun invoke(): Flow<WallpaperSettings> = wallpaperRepository.settings
}
