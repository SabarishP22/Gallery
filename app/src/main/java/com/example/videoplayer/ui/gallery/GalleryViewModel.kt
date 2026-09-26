package com.example.videoplayer.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.data.MediaFilter
import com.example.videoplayer.data.MediaRepository
import com.example.videoplayer.data.SortOrder
import com.example.videoplayer.data.WallpaperPreferences
import com.example.videoplayer.util.mediaMatchesQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.net.Uri

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

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private val wallpaperPreferences = WallpaperPreferences(application)

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private var permissionCheckHandled = false

    init {
        viewModelScope.launch {
            wallpaperPreferences.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        wallpaperUri = settings.imageUri,
                        wallpaperBlurDp = settings.blurRadiusDp,
                    )
                }
            }
        }
    }

    fun handleInitialPermissionCheck(alreadyGranted: Boolean, requestPermissions: () -> Unit) {
        if (permissionCheckHandled) return
        permissionCheckHandled = true
        if (alreadyGranted) {
            onPermissionGranted(loadIfEmpty = true)
        } else {
            requestPermissions()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            onPermissionGranted(loadIfEmpty = true)
        } else {
            _uiState.update {
                it.copy(
                    permissionGranted = false,
                    isLoading = false,
                    errorMessage = "Storage permission is required to browse your gallery.",
                )
            }
        }
    }

    private fun onPermissionGranted(loadIfEmpty: Boolean) {
        _uiState.update { it.copy(permissionGranted = true, errorMessage = null) }
        if (loadIfEmpty && _uiState.value.allMedia.isEmpty()) {
            loadMedia(showFullScreenLoading = true)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refreshMedia() {
        loadMedia(showFullScreenLoading = false)
    }

    fun saveGridScroll(index: Int, offset: Int) {
        _uiState.update { it.copy(gridScrollIndex = index, gridScrollOffset = offset) }
    }

    private fun loadMedia(showFullScreenLoading: Boolean) {
        viewModelScope.launch {
            val showBlockingLoader = showFullScreenLoading && _uiState.value.allMedia.isEmpty()
            _uiState.update {
                it.copy(
                    isLoading = showBlockingLoader,
                    isRefreshing = !showBlockingLoader,
                    errorMessage = null,
                )
            }
            runCatching { repository.loadAllMedia() }
                .onSuccess { media ->
                    _uiState.update { state ->
                        val filtered = computeFiltered(state.copy(allMedia = media))
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            allMedia = media,
                            displayItems = filtered,
                            gridRows = buildGalleryGridRows(filtered, state.sortOrder),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message ?: "Could not load media.",
                        )
                    }
                }
        }
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.update { state ->
            val next = state.copy(filter = filter)
            val filtered = computeFiltered(next)
            next.copy(displayItems = filtered, gridRows = buildGalleryGridRows(filtered, next.sortOrder))
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val next = state.copy(searchQuery = query)
            val filtered = computeFiltered(next)
            next.copy(displayItems = filtered, gridRows = buildGalleryGridRows(filtered, next.sortOrder))
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { state ->
            val next = state.copy(sortOrder = order)
            val filtered = computeFiltered(next)
            next.copy(displayItems = filtered, gridRows = buildGalleryGridRows(filtered, order))
        }
    }

    fun toggleGridColumns() {
        _uiState.update {
            val next = when (it.gridColumns) {
                2 -> 3
                3 -> 4
                4 -> 5
                else -> 2
            }
            it.copy(gridColumns = next)
        }
    }

    fun setGridColumns(columns: Int) {
        _uiState.update { it.copy(gridColumns = columns.coerceIn(2, 7)) }
    }

    fun openMedia(id: Long) {
        _uiState.update { it.copy(activeMediaId = id, viewerMediaId = id) }
    }

    fun closeViewer() {
        _uiState.update { it.copy(activeMediaId = null, viewerMediaId = null) }
    }

    fun clearActiveMedia() {
        closeViewer()
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

    fun openWallpaperDialog(media: GalleryMedia) {
        if (media.isVideo) return
        _uiState.update { it.copy(wallpaperDialogMedia = media) }
    }

    fun closeWallpaperDialog() {
        _uiState.update { it.copy(wallpaperDialogMedia = null) }
    }

    fun saveWallpaper(uri: Uri, blurDp: Float) {
        viewModelScope.launch {
            wallpaperPreferences.save(uri.toString(), blurDp)
            _uiState.update {
                it.copy(
                    wallpaperUri = uri.toString(),
                    wallpaperBlurDp = blurDp,
                    wallpaperDialogMedia = null,
                )
            }
        }
    }

    fun clearWallpaper() {
        viewModelScope.launch {
            wallpaperPreferences.clear()
            _uiState.update {
                it.copy(
                    wallpaperUri = null,
                    wallpaperBlurDp = 18f,
                    wallpaperDialogMedia = null,
                )
            }
        }
    }

    fun filteredMedia(state: GalleryUiState = _uiState.value): List<GalleryMedia> {
        return state.displayItems.ifEmpty { computeFiltered(state) }
    }

    private fun computeFiltered(state: GalleryUiState): List<GalleryMedia> {
        var list = state.allMedia
        list = when (state.filter) {
            MediaFilter.ALL -> list
            MediaFilter.IMAGES -> list.filter { !it.isVideo }
            MediaFilter.VIDEOS -> list.filter { it.isVideo }
        }
        if (state.searchQuery.isNotBlank()) {
            list = list.filter { mediaMatchesQuery(it, state.searchQuery) }
        }
        return when (state.sortOrder) {
            SortOrder.DATE_NEWEST -> list.sortedByDescending { it.dateAddedSec }
            SortOrder.DATE_OLDEST -> list.sortedBy { it.dateAddedSec }
            SortOrder.NAME_ASC -> list.sortedBy { it.displayName.lowercase() }
            SortOrder.NAME_DESC -> list.sortedByDescending { it.displayName.lowercase() }
            SortOrder.SIZE_LARGEST -> list.sortedByDescending { it.sizeBytes }
        }
    }

    fun mediaById(id: Long): GalleryMedia? = _uiState.value.allMedia.find { it.id == id }

    fun pagerMediaFor(currentId: Long): Pair<List<GalleryMedia>, Int> {
        val list = filteredMedia()
        val index = list.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        return list to index
    }
}
