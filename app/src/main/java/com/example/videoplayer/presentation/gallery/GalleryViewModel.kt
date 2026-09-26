package com.example.videoplayer.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.videoplayer.di.AppContainer
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.domain.usecase.FilterAndSortMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val appContainer: AppContainer,
) : ViewModel() {

    private val loadGalleryMediaUseCase = appContainer.loadGalleryMediaUseCase
    private val observeWallpaperSettingsUseCase = appContainer.observeWallpaperSettingsUseCase
    private val migrateWallpaperUseCase = appContainer.migrateWallpaperUseCase
    private val saveWallpaperUseCase = appContainer.saveWallpaperUseCase
    private val clearWallpaperUseCase = appContainer.clearWallpaperUseCase
    private val getViewerPagerMediaUseCase = appContainer.getViewerPagerMediaUseCase
    private val filterAndSortMediaUseCase: FilterAndSortMediaUseCase =
        appContainer.filterAndSortMediaUseCase()

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private var permissionCheckHandled = false
    private var lastSyncedWallpaperUri: String? = null

    init {
        viewModelScope.launch {
            runCatching { migrateWallpaperUseCase() }
            observeWallpaperSettingsUseCase().collect { settings ->
                _uiState.update {
                    it.copy(
                        wallpaperUri = settings.imageUri,
                        wallpaperBlurDp = settings.blurRadiusDp,
                    )
                }
                if (settings.imageUri != lastSyncedWallpaperUri) {
                    lastSyncedWallpaperUri = settings.imageUri
                    appContainer.syncWallpaperLauncherUseCase(settings.imageUri)
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
            loadGalleryMediaUseCase()
                .onSuccess { media -> applyMediaLoaded(media) }
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

    private fun applyMediaLoaded(media: List<GalleryMedia>) {
        _uiState.update { state ->
            val filtered = filterAndSortMediaUseCase(
                allMedia = media,
                filter = state.filter,
                searchQuery = state.searchQuery,
                sortOrder = state.sortOrder,
            )
            state.copy(
                isLoading = false,
                isRefreshing = false,
                allMedia = media,
                displayItems = filtered,
                gridRows = buildGalleryGridRows(filtered, state.sortOrder),
            )
        }
    }

    private fun refreshFilteredLists() {
        _uiState.update { state ->
            val filtered = filterAndSortMediaUseCase(
                allMedia = state.allMedia,
                filter = state.filter,
                searchQuery = state.searchQuery,
                sortOrder = state.sortOrder,
            )
            state.copy(
                displayItems = filtered,
                gridRows = buildGalleryGridRows(filtered, state.sortOrder),
            )
        }
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.update { it.copy(filter = filter) }
        refreshFilteredLists()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshFilteredLists()
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        refreshFilteredLists()
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
            saveWallpaperUseCase(uri, blurDp)
                .onSuccess { persistedUri ->
                    lastSyncedWallpaperUri = persistedUri
                    _uiState.update {
                        it.copy(
                            wallpaperUri = persistedUri,
                            wallpaperBlurDp = blurDp,
                            wallpaperDialogMedia = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            errorMessage = "Could not save background. Try another image.",
                            wallpaperDialogMedia = null,
                        )
                    }
                }
        }
    }

    fun clearWallpaper() {
        viewModelScope.launch {
            clearWallpaperUseCase()
            lastSyncedWallpaperUri = null
            _uiState.update {
                it.copy(
                    wallpaperUri = null,
                    wallpaperBlurDp = 18f,
                    wallpaperDialogMedia = null,
                )
            }
        }
    }

    fun gridRowsForFilter(filter: MediaFilter): List<GalleryGridRow> {
        val state = _uiState.value
        val filtered = filterAndSortMediaUseCase(
            allMedia = state.allMedia,
            filter = filter,
            searchQuery = state.searchQuery,
            sortOrder = state.sortOrder,
        )
        return buildGalleryGridRows(filtered, state.sortOrder)
    }

    fun pagerMediaFor(currentId: Long): Pair<List<GalleryMedia>, Int> {
        val state = _uiState.value
        return getViewerPagerMediaUseCase(
            allMedia = state.allMedia,
            filter = state.filter,
            searchQuery = state.searchQuery,
            sortOrder = state.sortOrder,
            currentMediaId = currentId,
        )
    }
}
