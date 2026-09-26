package com.example.videoplayer.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperLauncherSync {

    suspend fun apply(context: Context, wallpaperUri: String?) = withContext(Dispatchers.IO) {
        runCatching {
            LauncherIconManager.applyDefault(context)
            if (wallpaperUri.isNullOrBlank()) {
                WallpaperShortcutHelper.clear(context)
            } else {
                WallpaperShortcutHelper.updateFromWallpaper(context, Uri.parse(wallpaperUri))
            }
        }
    }
}
