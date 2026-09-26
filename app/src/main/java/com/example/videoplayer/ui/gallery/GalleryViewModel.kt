package com.example.videoplayer.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.data.MediaFilter
import com.example.videoplayer.data.MediaRepository
import com.example.videoplayer.data.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryUiState(
    val isLoading: Boolean = true,
    val allMedia: List<GalleryMedia> = emptyList(),
    val filter: MediaFilter = MediaFilter.ALL,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val gridColumns: Int = 3,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
    val permissionGranted: Boolean = false,
    val activeMediaId: Long? = null,
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) refreshMedia()
        else {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Storage permission is required to browse your gallery.")
            }
        }
    }

    fun refreshMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.loadAllMedia() }
                .onSuccess { media ->
                    _uiState.update { it.copy(isLoading = false, allMedia = media) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not load media.",
                        )
                    }
                }
        }
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun toggleGridColumns() {
        _uiState.update {
            val next = when (it.gridColumns) {
                2 -> 3
                3 -> 4
                else -> 2
            }
            it.copy(gridColumns = next)
        }
    }

    fun openMedia(id: Long) {
        _uiState.update { it.copy(activeMediaId = id) }
    }

    fun clearActiveMedia() {
        _uiState.update { it.copy(activeMediaId = null) }
    }

    fun toggleSelection(id: Long) {
        _uiState.update { state ->
            val selected = state.selectedIds.toMutableSet()
            if (id in selected) selected.remove(id) else selected.add(id)
            state.copy(
                selectedIds = selected,
                selectionMode = selected.isNotEmpty(),
            )
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectionMode = false, selectedIds = emptySet()) }
    }

    fun filteredMedia(state: GalleryUiState = _uiState.value): List<GalleryMedia> {
        var list = state.allMedia
        list = when (state.filter) {
            MediaFilter.ALL -> list
            MediaFilter.IMAGES -> list.filter { !it.isVideo }
            MediaFilter.VIDEOS -> list.filter { it.isVideo }
        }
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            list = list.filter { it.displayName.lowercase().contains(q) }
        }
        list = when (state.sortOrder) {
            SortOrder.DATE_NEWEST -> list.sortedByDescending { it.dateAddedSec }
            SortOrder.DATE_OLDEST -> list.sortedBy { it.dateAddedSec }
            SortOrder.NAME_ASC -> list.sortedBy { it.displayName.lowercase() }
            SortOrder.NAME_DESC -> list.sortedByDescending { it.displayName.lowercase() }
            SortOrder.SIZE_LARGEST -> list.sortedByDescending { it.sizeBytes }
        }
        return list
    }

    fun mediaById(id: Long): GalleryMedia? = _uiState.value.allMedia.find { it.id == id }

    fun imageMediaForPager(currentId: Long): Pair<List<GalleryMedia>, Int> {
        val images = filteredMedia().filter { !it.isVideo }
        val index = images.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        return images to index
    }
}
