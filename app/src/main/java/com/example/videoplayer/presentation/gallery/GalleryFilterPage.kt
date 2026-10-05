package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun GalleryFilterPage(
    pageFilter: MediaFilter,
    rows: List<GalleryGridRow>,
    gridState: LazyGridState,
    gridColumns: Int,
    searchQuery: String,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    favoriteIds: Set<Long>,
    removingFavoriteMediaId: Long?,
    favoriteBurstMediaId: Long?,
    favoriteBurstNonce: Long,
    favoritesMode: Boolean,
    swipeSelectScope: CoroutineScope,
    onSwipeSelectMedia: (Long, Boolean) -> Unit,
    onSwipeSelectFinished: () -> Unit,
    onOpenMedia: (Long) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onBeginSelection: (Long) -> Unit,
    onSaveGridScroll: (MediaFilter, Int, Int) -> Unit,
    onSaveFavoritesGridScroll: (Int, Int) -> Unit,
    onAddFavorite: (Long) -> Unit,
    onRemoveFavorite: (Long) -> Unit,
) {
    if (rows.isEmpty()) {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            message = when {
                favoritesMode -> "No favorites yet. Double-tap items in your library to save them here."
                pageFilter == MediaFilter.IMAGES -> "No images in this view."
                pageFilter == MediaFilter.VIDEOS -> "No videos in this view."
                searchQuery.isNotBlank() -> "No matches for your search."
                else -> "No media found on this device."
            },
        )
    } else {
        MediaGrid(
            rows = rows,
            columns = gridColumns,
            gridState = gridState,
            selectedIds = selectedIds,
            selectionMode = selectionMode,
            favoriteIds = favoriteIds,
            removingFavoriteMediaId = removingFavoriteMediaId,
            onClick = { item ->
                if (selectionMode) {
                    onToggleSelection(item.id)
                } else {
                    if (favoritesMode) {
                        onSaveFavoritesGridScroll(
                            gridState.firstVisibleItemIndex,
                            gridState.firstVisibleItemScrollOffset,
                        )
                    } else {
                        onSaveGridScroll(
                            pageFilter,
                            gridState.firstVisibleItemIndex,
                            gridState.firstVisibleItemScrollOffset,
                        )
                    }
                    onOpenMedia(item.id)
                }
            },
            onLongClick = { item -> onBeginSelection(item.id) },
            onDoubleClick = { item ->
                if (selectionMode) return@MediaGrid
                if (item.id in favoriteIds) {
                    onRemoveFavorite(item.id)
                } else {
                    onAddFavorite(item.id)
                }
            },
            favoriteBurstMediaId = favoriteBurstMediaId,
            favoriteBurstNonce = favoriteBurstNonce,
            swipeSelectScope = swipeSelectScope,
            onSwipeSelectMedia = onSwipeSelectMedia,
            onSwipeSelectFinished = onSwipeSelectFinished,
        )
    }
}
