package com.example.videoplayer.util

import android.content.Context
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.memory.MemoryCache
import kotlin.math.roundToInt

object WallpaperImageCache {

    fun cacheKey(wallpaperUri: String?, contentRevision: Long, blurDp: Float): String {
        return "wallpaper-${wallpaperUri.orEmpty()}-r$contentRevision-b${blurDp.roundToInt()}"
    }

    @OptIn(ExperimentalCoilApi::class)
    fun invalidate(context: Context, wallpaperUri: String?, contentRevision: Long, blurDp: Float) {
        val loader = context.imageLoader
        val key = cacheKey(wallpaperUri, contentRevision, blurDp)
        loader.memoryCache?.remove(MemoryCache.Key(key))
        loader.diskCache?.remove(key)
    }

    @OptIn(ExperimentalCoilApi::class)
    fun invalidateAllWallpaperEntries(context: Context) {
        val loader = context.imageLoader
        val memoryKeys = loader.memoryCache?.keys?.toList().orEmpty()
        memoryKeys.forEach { memoryKey ->
            if (memoryKey.key.startsWith("wallpaper-")) {
                loader.memoryCache?.remove(memoryKey)
            }
        }
    }
}
