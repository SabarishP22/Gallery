package com.example.videoplayer.presentation.gallery

import android.content.IntentSender
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.StorageStats
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder

data class GalleryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val allMedia: List<GalleryMedia> = emptyList(),
    val filter: MediaFilter = MediaFilter.ALL,
    val searchQuery: String = "",
    val isSearchFiltering: Boolean = false,
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val gridColumns: Int = 3,
    val gridScrollIndex: Int = 0,
    val gridScrollOffset: Int = 0,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
    val permissionGranted: Boolean = false,
    val activeMediaId: Long? = null,
    val viewerMediaId: Long? = null,
    val displayItems: List<GalleryMedia> = emptyList(),
    val gridRows: List<GalleryGridRow> = emptyList(),
    val displayItemsAll: List<GalleryMedia> = emptyList(),
    val displayItemsImages: List<GalleryMedia> = emptyList(),
    val displayItemsVideos: List<GalleryMedia> = emptyList(),
    val gridRowsAll: List<GalleryGridRow> = emptyList(),
    val gridRowsImages: List<GalleryGridRow> = emptyList(),
    val gridRowsVideos: List<GalleryGridRow> = emptyList(),
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
}
