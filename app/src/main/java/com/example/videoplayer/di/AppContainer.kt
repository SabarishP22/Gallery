package com.example.videoplayer.di

import android.app.Application
import com.example.videoplayer.data.repository.LauncherIconRepositoryImpl
import com.example.videoplayer.data.repository.MediaRepositoryImpl
import com.example.videoplayer.data.repository.WallpaperRepositoryImpl
import com.example.videoplayer.data.source.LauncherIconDataSource
import com.example.videoplayer.data.source.MediaStoreDataSource
import com.example.videoplayer.data.source.WallpaperFileStorage
import com.example.videoplayer.data.source.WallpaperPreferencesDataSource
import com.example.videoplayer.domain.repository.LauncherIconRepository
import com.example.videoplayer.domain.repository.MediaRepository
import com.example.videoplayer.domain.repository.WallpaperRepository
import com.example.videoplayer.domain.usecase.ClearWallpaperUseCase
import com.example.videoplayer.domain.usecase.FilterAndSortMediaUseCase
import com.example.videoplayer.domain.usecase.GetViewerPagerMediaUseCase
import com.example.videoplayer.domain.usecase.LoadGalleryMediaUseCase
import com.example.videoplayer.domain.usecase.MigrateWallpaperUseCase
import com.example.videoplayer.domain.usecase.ObserveWallpaperSettingsUseCase
import com.example.videoplayer.domain.usecase.SaveWallpaperUseCase
import com.example.videoplayer.domain.usecase.SyncWallpaperLauncherUseCase

class AppContainer(application: Application) {

    private val mediaStoreDataSource = MediaStoreDataSource(application)
    private val wallpaperPreferencesDataSource = WallpaperPreferencesDataSource(application)
    private val wallpaperFileStorage = WallpaperFileStorage(application)
    private val launcherIconDataSource = LauncherIconDataSource(application)

    val mediaRepository: MediaRepository = MediaRepositoryImpl(mediaStoreDataSource)
    val wallpaperRepository: WallpaperRepository = WallpaperRepositoryImpl(
        wallpaperPreferencesDataSource,
        wallpaperFileStorage,
    )
    val launcherIconRepository: LauncherIconRepository = LauncherIconRepositoryImpl(launcherIconDataSource)

    private val filterAndSortMediaUseCase = FilterAndSortMediaUseCase()
    val syncWallpaperLauncherUseCase = SyncWallpaperLauncherUseCase(launcherIconRepository)

    val loadGalleryMediaUseCase = LoadGalleryMediaUseCase(mediaRepository)
    val observeWallpaperSettingsUseCase = ObserveWallpaperSettingsUseCase(wallpaperRepository)
    val migrateWallpaperUseCase = MigrateWallpaperUseCase(wallpaperRepository)
    val saveWallpaperUseCase = SaveWallpaperUseCase(wallpaperRepository, syncWallpaperLauncherUseCase)
    val clearWallpaperUseCase = ClearWallpaperUseCase(wallpaperRepository, syncWallpaperLauncherUseCase)
    val getViewerPagerMediaUseCase = GetViewerPagerMediaUseCase(filterAndSortMediaUseCase)

    fun filterAndSortMediaUseCase(): FilterAndSortMediaUseCase = filterAndSortMediaUseCase
}
