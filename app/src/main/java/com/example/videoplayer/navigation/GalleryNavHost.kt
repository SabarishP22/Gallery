package com.example.videoplayer.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.videoplayer.ui.components.GalleryBackground
import com.example.videoplayer.ui.gallery.GalleryScreen
import com.example.videoplayer.ui.gallery.GalleryViewModel
import com.example.videoplayer.ui.viewer.MediaViewerScreen
import com.example.videoplayer.ui.wallpaper.WallpaperSetupDialog

@Composable
fun GalleryNavHost(viewModel: GalleryViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val viewerId = state.viewerMediaId

    GalleryBackground(
        wallpaperUri = state.wallpaperUri,
        wallpaperBlurDp = state.wallpaperBlurDp,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GalleryScreen(
                viewModel = viewModel,
                onOpenMedia = { id -> viewModel.openMedia(id) },
            )

            AnimatedVisibility(
                visible = viewerId != null,
                enter = scaleIn(
                    initialScale = 0.78f,
                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                    animationSpec = tween(340),
                ) + fadeIn(tween(280)),
                exit = scaleOut(
                    targetScale = 0.78f,
                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                    animationSpec = tween(280),
                ) + fadeOut(tween(240)),
                modifier = Modifier.fillMaxSize(),
            ) {
                val mediaId = viewerId ?: return@AnimatedVisibility
                val (items, index) = viewModel.pagerMediaFor(mediaId)
                MediaViewerScreen(
                    items = items,
                    startIndex = index,
                    onBack = { viewModel.closeViewer() },
                    onWallpaperRequest = { media -> viewModel.openWallpaperDialog(media) },
                )
            }
        }
    }

    state.wallpaperDialogMedia?.let { media ->
        WallpaperSetupDialog(
            media = media,
            initialBlurDp = state.wallpaperBlurDp,
            onDismiss = viewModel::closeWallpaperDialog,
            onSave = { blur -> viewModel.saveWallpaper(media.uri, blur) },
            onClear = viewModel::clearWallpaper,
        )
    }
}
