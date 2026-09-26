package com.example.videoplayer.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperLauncherSync {

    suspend fun apply(
        context: Context,
        wallpaperUri: String?,
        usePhotoAppIcon: Boolean = false,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            if (wallpaperUri.isNullOrBlank()) {
                WallpaperAppIconRenderer.deleteIconFile(context)
                WallpaperAppIconCache.clear()
                LauncherIconManager.applyDefault(context)
                WallpaperShortcutHelper.clear(context)
                return@withContext
            }
            val uri = Uri.parse(wallpaperUri)
            if (usePhotoAppIcon) {
                WallpaperAppIconRenderer.renderAndSave(context, uri)
                WallpaperAppIconCache.loadFromDisk(context)
                LauncherIconManager.applyWallpaperPhoto(context)
            } else {
                val accent = extractWallpaperAccentColor(context, uri)
                LauncherIconManager.applyColor(context, accent)
            }
            WallpaperShortcutHelper.updateFromWallpaper(context, uri)
        }
    }
}
