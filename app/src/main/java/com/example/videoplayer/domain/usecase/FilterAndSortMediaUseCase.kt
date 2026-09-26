package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.domain.service.MediaSearchMatcher

class FilterAndSortMediaUseCase {

    operator fun invoke(
        allMedia: List<GalleryMedia>,
        filter: MediaFilter,
        searchQuery: String,
        sortOrder: SortOrder,
    ): List<GalleryMedia> {
        var list = allMedia
        list = when (filter) {
            MediaFilter.ALL -> list
            MediaFilter.IMAGES -> list.filter { !it.isVideo }
            MediaFilter.VIDEOS -> list.filter { it.isVideo }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter { MediaSearchMatcher.matches(it, searchQuery) }
        }
        return when (sortOrder) {
            SortOrder.DATE_NEWEST -> list.sortedByDescending { it.dateAddedSec }
            SortOrder.DATE_OLDEST -> list.sortedBy { it.dateAddedSec }
            SortOrder.NAME_ASC -> list.sortedBy { it.displayName.lowercase() }
            SortOrder.NAME_DESC -> list.sortedByDescending { it.displayName.lowercase() }
            SortOrder.SIZE_LARGEST -> list.sortedByDescending { it.sizeBytes }
        }
    }
}
