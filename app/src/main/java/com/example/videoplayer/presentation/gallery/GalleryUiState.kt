package com.example.videoplayer.presentation.gallery

import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder

data class GalleryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val allMedia: List<GalleryMedia> = emptyList(),
    val filter: MediaFilter = MediaFilter.ALL,
    val searchQuery: String = "",
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
    val wallpaperUri: String? = null,
    val wallpaperBlurDp: Float = 18f,
    val wallpaperDialogMedia: GalleryMedia? = null,
)
