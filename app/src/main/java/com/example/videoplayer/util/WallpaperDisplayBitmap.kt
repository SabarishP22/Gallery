package com.example.videoplayer.util

import android.graphics.Bitmap

/**
 * Softens a wallpaper once at decode time so the gallery background does not need a live
 * [androidx.compose.ui.graphics.BlurEffect] while the grid scrolls.
 */
internal fun softenWallpaperForDisplay(source: Bitmap, blurRadiusPx: Float): Bitmap {
    if (blurRadiusPx <= 0.5f) return source
    val scale = when {
        blurRadiusPx >= 24f -> 0.1f
        blurRadiusPx >= 16f -> 0.14f
        blurRadiusPx >= 8f -> 0.2f
        else -> 0.28f
    }
    val smallW = (source.width * scale).toInt().coerceAtLeast(1)
    val smallH = (source.height * scale).toInt().coerceAtLeast(1)
    val small = Bitmap.createScaledBitmap(source, smallW, smallH, true)
    val upscaled = Bitmap.createScaledBitmap(small, source.width, source.height, true)
    if (small !== source && small !== upscaled) {
        small.recycle()
    }
    return upscaled
}
