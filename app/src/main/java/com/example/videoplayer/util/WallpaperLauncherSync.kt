package com.example.videoplayer.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperLauncherSync {

    suspend fun apply(context: Context, wallpaperUri: String?) = withContext(Dispatchers.IO) {
        runCatching {
            if (wallpaperUri.isNullOrBlank()) {
                LauncherIconManager.applyDefault(context)
                WallpaperShortcutHelper.clear(context)
                return@withContext
            }
            val uri = Uri.parse(wallpaperUri)
            val accent = extractWallpaperAccentColor(context, uri)
            LauncherIconManager.applyColor(context, accent)
            WallpaperShortcutHelper.updateFromWallpaper(context, uri)
        }
    }
}
