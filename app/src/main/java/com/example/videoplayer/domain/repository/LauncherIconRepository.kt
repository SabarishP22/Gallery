package com.example.videoplayer.domain.repository

interface LauncherIconRepository {
    suspend fun syncForWallpaper(wallpaperUri: String?)
}
