package com.example.videoplayer.presentation.gallery

import android.content.IntentSender
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.StorageStats
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.domain.model.ViewerListContext

data class GalleryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val allMedia: List<GalleryMedia> = emptyList(),
    val filter: MediaFilter = MediaFilter.ALL,
    val searchQuery: String = "",
    val isSearchFiltering: Boolean = false,
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val gridColumns: Int = 3,
    val gridScrollAllIndex: Int = 0,
    val gridScrollAllOffset: Int = 0,
    val gridScrollImagesIndex: Int = 0,
    val gridScrollImagesOffset: Int = 0,
    val gridScrollVideosIndex: Int = 0,
    val gridScrollVideosOffset: Int = 0,
    val gridScrollFavoritesIndex: Int = 0,
    val gridScrollFavoritesOffset: Int = 0,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
    val permissionGranted: Boolean = false,
    val activeMediaId: Long? = null,
    val viewerMediaId: Long? = null,
    val viewerListContext: ViewerListContext = ViewerListContext.Tab(MediaFilter.ALL),
    val pendingGridScrollRestore: GridScrollRestore? = null,
    val displayItems: List<GalleryMedia> = emptyList(),
    val gridRows: List<GalleryGridRow> = emptyList(),
    val displayItemsAll: List<GalleryMedia> = emptyList(),
    val displayItemsImages: List<GalleryMedia> = emptyList(),
    val displayItemsVideos: List<GalleryMedia> = emptyList(),
    val displayItemsFavorites: List<GalleryMedia> = emptyList(),
    val gridRowsAll: List<GalleryGridRow> = emptyList(),
    val gridRowsImages: List<GalleryGridRow> = emptyList(),
    val gridRowsVideos: List<GalleryGridRow> = emptyList(),
    val gridRowsFavorites: List<GalleryGridRow> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val favoritesVisible: Boolean = false,
    val filterBeforeFavorites: MediaFilter = MediaFilter.ALL,
    val favoriteBurstNonce: Long = 0L,
    val favoriteRemoveAnimMediaId: Long? = null,
    val wallpaperUri: String? = null,
    val wallpaperBlurDp: Float = 18f,
    val wallpaperContentVersion: Long = 0L,
    val wallpaperDialogMedia: GalleryMedia? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val pendingTrashIntentSender: IntentSender? = null,
    val showStorageDialog: Boolean = false,
    val storageStatsLoading: Boolean = false,
    val storageStats: StorageStats? = null,
    val storageStatsError: String? = null,
    val snackbarMessage: String? = null,
    val snackbarEventId: Long = 0L,
    val wallpaperSaveInProgress: Boolean = false,
    val galleryGridVisible: Boolean = true,
) {
    fun gridRowsFor(filter: MediaFilter): List<GalleryGridRow> = when (filter) {
        MediaFilter.ALL -> gridRowsAll
        MediaFilter.IMAGES -> gridRowsImages
        MediaFilter.VIDEOS -> gridRowsVideos
    }

    fun displayItemsFor(filter: MediaFilter): List<GalleryMedia> = when (filter) {
        MediaFilter.ALL -> displayItemsAll
        MediaFilter.IMAGES -> displayItemsImages
        MediaFilter.VIDEOS -> displayItemsVideos
    }

    fun gridScrollIndexFor(filter: MediaFilter): Int = when (filter) {
        MediaFilter.ALL -> gridScrollAllIndex
        MediaFilter.IMAGES -> gridScrollImagesIndex
        MediaFilter.VIDEOS -> gridScrollVideosIndex
    }

    fun gridScrollOffsetFor(filter: MediaFilter): Int = when (filter) {
        MediaFilter.ALL -> gridScrollAllOffset
        MediaFilter.IMAGES -> gridScrollImagesOffset
        MediaFilter.VIDEOS -> gridScrollVideosOffset
    }
}
