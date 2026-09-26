package com.example.videoplayer.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.ui.gallery.GalleryScreen
import com.example.videoplayer.ui.gallery.GalleryViewModel
import com.example.videoplayer.ui.viewer.MediaViewerScreen

object Routes {
    const val HOME = "home"
    const val VIEWER = "viewer/{mediaId}"

    fun viewer(id: Long) = "viewer/$id"
}

@Composable
fun GalleryNavHost(viewModel: GalleryViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            GalleryScreen(
                viewModel = viewModel,
                onOpenMedia = { id ->
                    navController.navigate(Routes.viewer(id)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable(
            route = Routes.VIEWER,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType }),
        ) { entry ->
            val mediaId = entry.arguments?.getLong("mediaId") ?: return@composable
            val (items, index) = viewModel.pagerMediaFor(mediaId)
            MediaViewerScreen(
                items = items,
                startIndex = index,
                onBack = {
                    viewModel.clearActiveMedia()
                    navController.popBackStack(Routes.HOME, inclusive = false, saveState = true)
                },
            )
        }
    }
}
