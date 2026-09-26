package com.example.videoplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.ui.gallery.GalleryScreen
import com.example.videoplayer.ui.gallery.GalleryViewModel
import com.example.videoplayer.ui.viewer.PhotoViewerScreen
import com.example.videoplayer.ui.viewer.VideoPlayerScreen

object Routes {
    const val HOME = "home"
    const val PHOTO = "photo/{mediaId}"
    const val VIDEO = "video/{mediaId}"

    fun photo(id: Long) = "photo/$id"
    fun video(id: Long) = "video/$id"
}

@Composable
fun GalleryNavHost(viewModel: GalleryViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            GalleryScreen(
                viewModel = viewModel,
                onOpenPhoto = { id -> navController.navigate(Routes.photo(id)) },
                onOpenVideo = { id -> navController.navigate(Routes.video(id)) },
            )
        }
        composable(
            route = Routes.PHOTO,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType }),
        ) { entry ->
            val mediaId = entry.arguments?.getLong("mediaId") ?: return@composable
            val (images, index) = viewModel.imageMediaForPager(mediaId)
            PhotoViewerScreen(
                images = images,
                startIndex = index,
                onBack = {
                    viewModel.clearActiveMedia()
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = Routes.VIDEO,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType }),
        ) { entry ->
            val mediaId = entry.arguments?.getLong("mediaId") ?: return@composable
            val media = viewModel.mediaById(mediaId) ?: state.allMedia.firstOrNull { it.id == mediaId }
            if (media == null) {
                navController.popBackStack()
            } else {
                VideoPlayerScreen(
                    media = media,
                    onBack = {
                        viewModel.clearActiveMedia()
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
