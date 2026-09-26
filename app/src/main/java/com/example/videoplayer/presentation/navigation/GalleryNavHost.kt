package com.example.videoplayer.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.videoplayer.AuraGalleryApplication
import com.example.videoplayer.di.GalleryViewModelFactory
import com.example.videoplayer.presentation.components.GalleryBackground
import com.example.videoplayer.presentation.gallery.GalleryScreen
import com.example.videoplayer.presentation.gallery.GalleryViewModel
import com.example.videoplayer.presentation.viewer.MediaViewerScreen
import com.example.videoplayer.presentation.wallpaper.WallpaperSetupDialog

@Composable
fun GalleryNavHost(
    viewModel: GalleryViewModel = viewModel(
        factory = GalleryViewModelFactory(
            (LocalContext.current.applicationContext as AuraGalleryApplication).appContainer,
        ),
    ),
) {
    val state by viewModel.uiState.collectAsState()
    val viewerId = state.viewerMediaId

    BackHandler(enabled = viewerId != null) {
        viewModel.closeViewer()
    }

    GalleryBackground(
        wallpaperUri = state.wallpaperUri,
        wallpaperBlurDp = state.wallpaperBlurDp,
        wallpaperContentVersion = state.wallpaperContentVersion,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewerId == null) {
                GalleryScreen(
                    viewModel = viewModel,
                    onOpenMedia = { id -> viewModel.openMedia(id) },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                ) {
                    val (items, index) = viewModel.pagerMediaFor(viewerId)
                    MediaViewerScreen(
                        items = items,
                        startIndex = index,
                        onBack = { viewModel.closeViewer() },
                        onWallpaperRequest = { media -> viewModel.openWallpaperDialog(media) },
                    )
                }
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
