package com.example.videoplayer.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.videoplayer.AuraGalleryApplication
import com.example.videoplayer.di.GalleryViewModelFactory
import com.example.videoplayer.presentation.components.GalleryBackground
import com.example.videoplayer.presentation.components.PixLabSnackbarHost
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
    val gallerySnackbarHostState = remember { SnackbarHostState() }
    val overlaySnackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarEventId) {
        if (state.snackbarEventId == 0L) return@LaunchedEffect
        val message = state.snackbarMessage ?: return@LaunchedEffect
        val host = if (viewerId != null) overlaySnackbarHostState else gallerySnackbarHostState
        host.showSnackbar(
            message = message,
            duration = if (state.snackbarLongDuration) {
                SnackbarDuration.Long
            } else {
                SnackbarDuration.Short
            },
        )
        viewModel.clearSnackbarMessage()
    }

    BackHandler(enabled = viewerId != null && !state.immersiveBrowseMode) {
        viewModel.closeViewer()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                if (viewerId == null) {
                    PixLabSnackbarHost(hostState = gallerySnackbarHostState)
                }
            },
        ) {
            GalleryBackground(
                wallpaperUri = state.wallpaperUri,
                wallpaperBlurDp = state.wallpaperBlurDp,
                wallpaperContentVersion = state.wallpaperContentVersion,
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GalleryScreen(
                        viewModel = viewModel,
                        onOpenMedia = { id -> viewModel.openMedia(id) },
                    )
                    if (viewerId != null) {
                        val (items, index) = viewModel.pagerMediaFor(viewerId)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                        ) {
                            if (state.immersiveBrowseMode) {
                                ViewerTransitionOverlay(
                                    viewerMediaId = viewerId,
                                    items = items,
                                    startIndex = index,
                                    favoriteIds = state.favoriteIds,
                                    viewerFavoriteBurstNonce = state.viewerFavoriteBurstNonce,
                                    transitionOrigin = state.viewerTransitionOrigin,
                                    onDismissComplete = viewModel::finishCloseViewer,
                                    onCurrentMediaChanged = viewModel::onViewerPageChanged,
                                    onToggleFavorite = { media ->
                                        viewModel.toggleFavoriteFromViewer(media.id)
                                    },
                                    onWallpaperRequest = { media ->
                                        viewModel.openWallpaperDialog(media)
                                    },
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black),
                                ) {
                                    MediaViewerScreen(
                                        items = items,
                                        startIndex = index,
                                        favoriteIds = state.favoriteIds,
                                        viewerFavoriteBurstNonce = state.viewerFavoriteBurstNonce,
                                        onBack = { viewModel.closeViewer() },
                                        onCurrentMediaChanged = viewModel::onViewerPageChanged,
                                        onToggleFavorite = { media ->
                                            viewModel.toggleFavoriteFromViewer(media.id)
                                        },
                                        onWallpaperRequest = { media ->
                                            viewModel.openWallpaperDialog(media)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        state.wallpaperDialogMedia?.let { media ->
            WallpaperSetupDialog(
                media = media,
                initialBlurDp = state.wallpaperBlurDp,
                isSaving = state.wallpaperSaveInProgress,
                onDismiss = viewModel::closeWallpaperDialog,
                onSave = { blur -> viewModel.saveWallpaper(media.uri, blur) },
                onClear = viewModel::clearWallpaper,
            )
        }

        if (viewerId != null) {
            PixLabSnackbarHost(
                hostState = overlaySnackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 24.dp)
                    .zIndex(20_000f),
            )
        }
    }
}
