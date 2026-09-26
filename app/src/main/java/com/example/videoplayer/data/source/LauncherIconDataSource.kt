package com.example.videoplayer.data.source

import android.content.Context
import com.example.videoplayer.util.WallpaperLauncherSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LauncherIconDataSource(
    private val context: Context,
) {

    suspend fun syncForWallpaper(wallpaperUri: String?, usePhotoAppIcon: Boolean) = withContext(Dispatchers.IO) {
        WallpaperLauncherSync.apply(context, wallpaperUri, usePhotoAppIcon)
    }
}
