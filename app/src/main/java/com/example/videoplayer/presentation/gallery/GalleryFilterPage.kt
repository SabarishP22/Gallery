package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    swipeSelectScope: CoroutineScope,
    onSwipeSelectMedia: (Long, Boolean) -> Unit,
    onSwipeSelectFinished: () -> Unit,
    onOpenMedia: (Long) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onBeginSelection: (Long) -> Unit,
    onSaveGridScroll: (Int, Int) -> Unit,
) {
    if (rows.isEmpty()) {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            message = when (pageFilter) {
                MediaFilter.IMAGES -> "No images in this view."
                MediaFilter.VIDEOS -> "No videos in this view."
                else -> if (searchQuery.isNotBlank()) {
                    "No matches for your search."
                } else {
                    "No media found on this device."
                }
            },
        )
    } else {
        MediaGrid(
            rows = rows,
            columns = gridColumns,
            gridState = gridState,
            selectedIds = selectedIds,
            selectionMode = selectionMode,
            swipeSelectScope = swipeSelectScope,
            onSwipeSelectMedia = onSwipeSelectMedia,
            onSwipeSelectFinished = onSwipeSelectFinished,
            onClick = { item ->
                if (selectionMode) {
                    onToggleSelection(item.id)
                } else {
                    onSaveGridScroll(
                        gridState.firstVisibleItemIndex,
                        gridState.firstVisibleItemScrollOffset,
                    )
                    onOpenMedia(item.id)
                }
            },
            onLongClick = { item -> onBeginSelection(item.id) },
        )
    }
}
