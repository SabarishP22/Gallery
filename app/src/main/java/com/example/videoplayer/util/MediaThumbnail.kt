package com.example.videoplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.example.videoplayer.data.GalleryMedia
import coil.request.ImageRequest
import coil.request.videoFrameMillis

fun mediaThumbnailRequest(
    context: Context,
    media: GalleryMedia,
    pixelSize: Int = 400,
): ImageRequest {
    val builder = ImageRequest.Builder(context)
        .data(media.uri)
        .size(pixelSize)
        .crossfade(pixelSize >= 800)
        .allowHardware(true)
        .memoryCacheKey("${media.uri}-thumb-$pixelSize")
        .diskCacheKey("${media.uri}-thumb-$pixelSize")

    if (media.isVideo) {
        builder
            .videoFrameMillis(500L)
            .allowHardware(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.placeholderMemoryCacheKey("video-ph-${media.id}")
        }
    }
    return builder.build()
}

fun loadVideoThumbnailBitmap(context: Context, media: GalleryMedia): Bitmap? {
    if (!media.isVideo) return null
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(
                media.uri,
                Size(512, 512),
                null,
            )
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Video.Thumbnails.getThumbnail(
                context.contentResolver,
                media.id,
                MediaStore.Video.Thumbnails.MINI_KIND,
                null,
            )
        }
    } catch (_: Exception) {
        null
    }
}
