package com.example.videoplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import coil.size.Precision
import coil.size.Scale
import com.example.videoplayer.domain.model.GalleryMedia

fun mediaThumbnailRequest(
    context: Context,
    media: GalleryMedia,
    pixelSize: Int = 400,
): ImageRequest {
    val targetSize = pixelSize.coerceIn(128, 512)
    val builder = ImageRequest.Builder(context)
        .data(media.uri)
        .size(targetSize)
        .scale(Scale.FILL)
        .precision(Precision.INEXACT)
        .crossfade(false)
        .allowHardware(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCacheKey("${media.id}-thumb-$targetSize")
        .diskCacheKey("${media.id}-thumb-$targetSize")

    if (media.isVideo) {
        builder
            .videoFrameMillis(0L)
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
