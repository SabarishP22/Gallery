package com.example.videoplayer.di

import android.app.Application
import com.example.videoplayer.data.repository.LauncherIconRepositoryImpl
import com.example.videoplayer.data.repository.MediaRepositoryImpl
import com.example.videoplayer.data.repository.StorageStatsRepositoryImpl
import com.example.videoplayer.data.repository.WallpaperRepositoryImpl
import com.example.videoplayer.data.source.LauncherIconDataSource
import com.example.videoplayer.data.source.MediaStoreDataSource
import com.example.videoplayer.data.source.StorageStatsDataSource
import com.example.videoplayer.data.source.WallpaperFileStorage
import com.example.videoplayer.data.source.WallpaperPreferencesDataSource
import com.example.videoplayer.domain.repository.LauncherIconRepository
import com.example.videoplayer.domain.repository.MediaRepository
import com.example.videoplayer.domain.repository.WallpaperRepository
import com.example.videoplayer.domain.usecase.ClearWallpaperUseCase
import com.example.videoplayer.domain.usecase.FilterAndSortMediaUseCase
import com.example.videoplayer.domain.repository.StorageStatsRepository
import com.example.videoplayer.domain.usecase.GetStorageStatsUseCase
import com.example.videoplayer.domain.usecase.GetViewerPagerMediaUseCase
import com.example.videoplayer.domain.usecase.LoadGalleryMediaUseCase
import com.example.videoplayer.domain.usecase.MigrateWallpaperUseCase
import com.example.videoplayer.domain.usecase.ObserveWallpaperSettingsUseCase
import com.example.videoplayer.domain.usecase.SaveWallpaperUseCase
import com.example.videoplayer.domain.usecase.SyncWallpaperLauncherUseCase
import com.example.videoplayer.domain.usecase.TrashMediaUseCase

class AppContainer(
    val application: Application,
) {

    private val mediaStoreDataSource = MediaStoreDataSource(application)
    private val storageStatsDataSource = StorageStatsDataSource(application)
    private val wallpaperPreferencesDataSource = WallpaperPreferencesDataSource(application)
    private val wallpaperFileStorage = WallpaperFileStorage(application)
    private val launcherIconDataSource = LauncherIconDataSource(application)

    val mediaRepository: MediaRepository = MediaRepositoryImpl(mediaStoreDataSource)
    val storageStatsRepository: StorageStatsRepository = StorageStatsRepositoryImpl(storageStatsDataSource)
    val wallpaperRepository: WallpaperRepository = WallpaperRepositoryImpl(
        wallpaperPreferencesDataSource,
        wallpaperFileStorage,
    )
    val launcherIconRepository: LauncherIconRepository = LauncherIconRepositoryImpl(launcherIconDataSource)

    private val filterAndSortMediaUseCase = FilterAndSortMediaUseCase()
    val syncWallpaperLauncherUseCase = SyncWallpaperLauncherUseCase(launcherIconRepository)

    val loadGalleryMediaUseCase = LoadGalleryMediaUseCase(mediaRepository)
    val trashMediaUseCase = TrashMediaUseCase(mediaRepository)
    val observeWallpaperSettingsUseCase = ObserveWallpaperSettingsUseCase(wallpaperRepository)
    val migrateWallpaperUseCase = MigrateWallpaperUseCase(wallpaperRepository)
    val saveWallpaperUseCase = SaveWallpaperUseCase(wallpaperRepository)
    val clearWallpaperUseCase = ClearWallpaperUseCase(wallpaperRepository)
    val getViewerPagerMediaUseCase = GetViewerPagerMediaUseCase(filterAndSortMediaUseCase)
    val getStorageStatsUseCase = GetStorageStatsUseCase(storageStatsRepository)

    fun filterAndSortMediaUseCase(): FilterAndSortMediaUseCase = filterAndSortMediaUseCase
}
