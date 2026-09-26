package com.example.videoplayer.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.videoplayer.MainActivity
import com.example.videoplayer.R

object WallpaperShortcutHelper {

    private const val SHORTCUT_ID = "aura_wallpaper_launcher"

    fun clear(context: Context) {
        ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(SHORTCUT_ID))
    }

    fun updateFromWallpaper(context: Context, uri: Uri) {
        val bitmap = loadAdaptiveWallpaperBitmap(context, uri) ?: return
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IconCompat.createWithAdaptiveBitmap(bitmap)
        } else {
            IconCompat.createWithBitmap(bitmap)
        }
        if (bitmap.isRecycled.not()) {
            bitmap.recycle()
        }
        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_name))
            .setLongLabel(context.getString(R.string.app_name))
            .setIcon(icon)
            .setIntent(launchIntent)
            .build()
        ShortcutManagerCompat.setDynamicShortcuts(context, listOf(shortcut))
    }

    private fun loadAdaptiveWallpaperBitmap(context: Context, uri: Uri): Bitmap? {
        val decoded = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (_: Exception) {
            null
        } ?: return null

        val size = 512
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val srcW = decoded.width.toFloat()
        val srcH = decoded.height.toFloat()
        val scale = maxOf(size / srcW, size / srcH)
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(
                (size - srcW * scale) / 2f,
                (size - srcH * scale) / 2f,
            )
        }
        canvas.drawBitmap(decoded, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        if (decoded != output && decoded.isRecycled.not()) {
            decoded.recycle()
        }
        val inset = size * 0.08f
        canvas.drawRoundRect(
            RectF(inset, inset, size - inset, size - inset),
            size * 0.22f,
            size * 0.22f,
            Paint().apply {
                color = 0x33000000
                style = Paint.Style.STROKE
                strokeWidth = size * 0.012f
                isAntiAlias = true
            },
        )
        return output
    }
}
