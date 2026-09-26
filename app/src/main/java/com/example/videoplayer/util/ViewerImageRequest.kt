package com.example.videoplayer.util

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.example.videoplayer.domain.model.GalleryMedia

/** Full-resolution (bounded) load for pinch-zoom in the photo viewer. */
fun viewerPhotoRequest(context: Context, media: GalleryMedia): ImageRequest {
    return ImageRequest.Builder(context)
        .data(media.uri)
        .size(VIEWER_MAX_EDGE_PX)
        .scale(Scale.FIT)
        .precision(Precision.INEXACT)
        .crossfade(150)
        .allowHardware(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCacheKey("${media.id}-viewer-full")
        .diskCacheKey("${media.id}-viewer-full")
        .build()
}

private const val VIEWER_MAX_EDGE_PX = 4096
