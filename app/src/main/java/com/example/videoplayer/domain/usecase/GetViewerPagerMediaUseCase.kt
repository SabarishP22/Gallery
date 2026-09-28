package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.domain.model.ViewerListContext

class GetViewerPagerMediaUseCase(
    private val filterAndSortMediaUseCase: FilterAndSortMediaUseCase,
) {
    operator fun invoke(
        allMedia: List<GalleryMedia>,
        listContext: ViewerListContext,
        searchQuery: String,
        sortOrder: SortOrder,
        currentMediaId: Long,
        favoriteIds: Set<Long>,
    ): Pair<List<GalleryMedia>, Int> {
        val list = when (listContext) {
            ViewerListContext.Favorites -> {
                val fav = allMedia.filter { it.id in favoriteIds }
                filterAndSortMediaUseCase(fav, MediaFilter.ALL, searchQuery, sortOrder)
            }
            is ViewerListContext.Tab -> filterAndSortMediaUseCase(
                allMedia,
                listContext.filter,
                searchQuery,
                sortOrder,
            )
        }
        val index = list.indexOfFirst { it.id == currentMediaId }.coerceAtLeast(0)
        return list to index
    }
}
