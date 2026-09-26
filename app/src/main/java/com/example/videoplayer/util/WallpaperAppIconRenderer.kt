package com.example.videoplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

object WallpaperAppIconRenderer {

    suspend fun renderAndSave(context: Context, imageUri: Uri) = withContext(Dispatchers.IO) {
        val decoded = context.contentResolver.openInputStream(imageUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 2 })
        } ?: return@withContext false

        val size = 512
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val srcW = decoded.width.toFloat()
        val srcH = decoded.height.toFloat()
        val scale = maxOf(size / srcW, size / srcH)
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate((size - srcW * scale) / 2f, (size - srcH * scale) / 2f)
        }
        canvas.drawBitmap(decoded, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        if (decoded != output && !decoded.isRecycled) decoded.recycle()

        val outFile = WallpaperAppIconDrawable.iconFile(context)
        outFile.parentFile?.mkdirs()
        FileOutputStream(outFile).use { stream ->
            output.compress(Bitmap.CompressFormat.PNG, 95, stream)
        }
        WallpaperAppIconCache.setFromBitmap(output.copy(output.config ?: Bitmap.Config.ARGB_8888, false))
        if (!output.isRecycled) output.recycle()
        true
    }

    fun deleteIconFile(context: Context) {
        WallpaperAppIconDrawable.iconFile(context).delete()
    }
}
