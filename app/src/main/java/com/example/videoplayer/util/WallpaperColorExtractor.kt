package com.example.videoplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

suspend fun extractWallpaperAccentColor(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
    val bitmap = loadSampleBitmap(context, uri) ?: return@withContext 0xFF12121A.toInt()
    val palette = Palette.from(bitmap).generate()
    val candidate = palette.vibrantSwatch?.rgb
        ?: palette.lightVibrantSwatch?.rgb
        ?: palette.dominantSwatch?.rgb
        ?: palette.mutedSwatch?.rgb
        ?: 0xFF12121A.toInt()
    bitmap.recycle()
    candidate
}

private fun loadSampleBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply { inSampleSize = 8 }
            BitmapFactory.decodeStream(stream, null, options)
        }
    } catch (_: Exception) {
        null
    }
}

fun nearestLauncherBucket(color: Int): Int {
    val buckets = LauncherIconManager.BUCKET_COLORS
    var bestIndex = 0
    var bestDistance = Float.MAX_VALUE
    buckets.forEachIndexed { index, bucketColor ->
        val distance = colorDistance(color, bucketColor)
        if (distance < bestDistance) {
            bestDistance = distance
            bestIndex = index
        }
    }
    return bestIndex
}

private fun colorDistance(a: Int, b: Int): Float {
    val ar = (a shr 16) and 0xFF
    val ag = (a shr 8) and 0xFF
    val ab = a and 0xFF
    val br = (b shr 16) and 0xFF
    val bg = (b shr 8) and 0xFF
    val bb = b and 0xFF
    val dr = (ar - br).toFloat()
    val dg = (ag - bg).toFloat()
    val db = (ab - bb).toFloat()
    return sqrt(dr * dr + dg * dg + db * db)
}
