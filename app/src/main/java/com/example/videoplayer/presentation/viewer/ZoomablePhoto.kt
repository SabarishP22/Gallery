package com.example.videoplayer.presentation.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.util.viewerPhotoRequest

@Composable
fun ZoomablePhoto(
    media: GalleryMedia,
    onToggleChrome: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val zoomState = rememberZoomTransformState(media.id)

    LaunchedEffect(zoomState.isZoomed) {
        onZoomChanged(zoomState.isZoomed)
    }

    ZoomableBox(
        state = zoomState,
        modifier = modifier,
        onSingleTap = onToggleChrome,
        onLongPress = onLongPress,
    ) {
        AsyncImage(
            model = viewerPhotoRequest(context, media),
            contentDescription = media.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
