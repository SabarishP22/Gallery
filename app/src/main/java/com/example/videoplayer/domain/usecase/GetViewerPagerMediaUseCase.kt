package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder

class GetViewerPagerMediaUseCase(
    private val filterAndSortMediaUseCase: FilterAndSortMediaUseCase,
) {
    operator fun invoke(
        allMedia: List<GalleryMedia>,
        filter: MediaFilter,
        searchQuery: String,
        sortOrder: SortOrder,
        currentMediaId: Long,
    ): Pair<List<GalleryMedia>, Int> {
        val list = filterAndSortMediaUseCase(allMedia, filter, searchQuery, sortOrder)
        val index = list.indexOfFirst { it.id == currentMediaId }.coerceAtLeast(0)
        return list to index
    }
}
