package com.example.videoplayer.data.repository

import com.example.videoplayer.data.source.LauncherIconDataSource
import com.example.videoplayer.domain.repository.LauncherIconRepository

class LauncherIconRepositoryImpl(
    private val launcherIconDataSource: LauncherIconDataSource,
) : LauncherIconRepository {

    override suspend fun syncForWallpaper(wallpaperUri: String?, usePhotoAppIcon: Boolean) {
        launcherIconDataSource.syncForWallpaper(wallpaperUri, usePhotoAppIcon)
    }
}
