package com.example.videoplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import java.io.File

class WallpaperAppIconDrawable : Drawable() {

    override fun draw(canvas: Canvas) {
        val bmp = WallpaperAppIconCache.bitmap ?: return
        val bounds = bounds
        if (bounds.isEmpty) return
        val scale = maxOf(
            bounds.width().toFloat() / bmp.width,
            bounds.height().toFloat() / bmp.height,
        )
        val dx = (bounds.width() - bmp.width * scale) / 2f
        val dy = (bounds.height() - bmp.height * scale) / 2f
        canvas.drawBitmap(
            bmp,
            null,
            Rect(
                (bounds.left + dx).toInt(),
                (bounds.top + dy).toInt(),
                (bounds.left + dx + bmp.width * scale).toInt(),
                (bounds.top + dy + bmp.height * scale).toInt(),
            ),
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG),
        )
    }

    override fun setAlpha(alpha: Int) = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    companion object {
        fun iconFile(context: Context): File {
            return File(File(context.filesDir, "wallpaper"), "app_icon.png")
        }
    }
}

object WallpaperAppIconCache {
    @Volatile
    var bitmap: Bitmap? = null

    fun loadFromDisk(context: Context) {
        val file = WallpaperAppIconDrawable.iconFile(context)
        bitmap = if (file.exists() && file.length() > 0L) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun setFromBitmap(source: Bitmap) {
        bitmap?.takeIf { it != source && !it.isRecycled }?.recycle()
        bitmap = source
    }

    fun clear() {
        bitmap?.takeIf { !it.isRecycled }?.recycle()
        bitmap = null
    }
}
