package com.example.videoplayer.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.videoplayer.di.AppContainer
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.domain.model.TrashDeleteRequest
import com.example.videoplayer.domain.usecase.FilterAndSortMediaUseCase
import com.example.videoplayer.domain.usecase.GetStorageStatsUseCase
import com.example.videoplayer.domain.usecase.TrashMediaUseCase
import com.example.videoplayer.util.WallpaperImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val trashMediaUseCase: TrashMediaUseCase = appContainer.trashMediaUseCase
    private val getStorageStatsUseCase: GetStorageStatsUseCase = appContainer.getStorageStatsUseCase
    private val filterAndSortMediaUseCase: FilterAndSortMediaUseCase =
        appContainer.filterAndSortMediaUseCase()

    private var swipeSelectAdding: Boolean? = null
    private val swipeVisitedIds = mutableSetOf<Long>()

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private var permissionCheckHandled = false
    private var lastSyncedWallpaperUri: String? = null
    private var recomputeJob: Job? = null
    private var searchDebounceJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching { migrateWallpaperUseCase() }
            observeWallpaperSettingsUseCase().collect { settings ->
                _uiState.update {
                    it.copy(
                        wallpaperUri = settings.imageUri,
                        wallpaperBlurDp = settings.blurRadiusDp,
                        wallpaperContentVersion = settings.contentRevision,
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
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                allMedia = media,
            )
        }
        scheduleRecomputeFilters()
    }

    private fun scheduleRecomputeFilters() {
        recomputeJob?.cancel()
        recomputeJob = viewModelScope.launch {
            val snapshot = _uiState.value
            if (snapshot.allMedia.isEmpty()) {
                _uiState.update {
                    it.copy(
                        displayItems = emptyList(),
                        displayItemsAll = emptyList(),
                        displayItemsImages = emptyList(),
                        displayItemsVideos = emptyList(),
                        gridRows = emptyList(),
                        gridRowsAll = emptyList(),
                        gridRowsImages = emptyList(),
                        gridRowsVideos = emptyList(),
                        isSearchFiltering = false,
                    )
                }
                return@launch
            }
            if (snapshot.searchQuery.isNotBlank()) {
                _uiState.update { it.copy(isSearchFiltering = true) }
            }
            val updated = withContext(Dispatchers.Default) {
                computeAllTabs(_uiState.value)
            }
            _uiState.value = updated.copy(isSearchFiltering = false)
        }
    }

    private fun computeAllTabs(state: GalleryUiState): GalleryUiState {
        val media = state.allMedia
        val query = state.searchQuery
        val sort = state.sortOrder
        val itemsAll = filterAndSortMediaUseCase(media, MediaFilter.ALL, query, sort)
        val itemsImages = filterAndSortMediaUseCase(media, MediaFilter.IMAGES, query, sort)
        val itemsVideos = filterAndSortMediaUseCase(media, MediaFilter.VIDEOS, query, sort)
        val rowsAll = buildGalleryGridRows(itemsAll, sort)
        val rowsImages = buildGalleryGridRows(itemsImages, sort)
        val rowsVideos = buildGalleryGridRows(itemsVideos, sort)
        val activeRows = when (state.filter) {
            MediaFilter.ALL -> rowsAll
            MediaFilter.IMAGES -> rowsImages
            MediaFilter.VIDEOS -> rowsVideos
        }
        val activeItems = when (state.filter) {
            MediaFilter.ALL -> itemsAll
            MediaFilter.IMAGES -> itemsImages
            MediaFilter.VIDEOS -> itemsVideos
        }
        return state.copy(
            displayItemsAll = itemsAll,
            displayItemsImages = itemsImages,
            displayItemsVideos = itemsVideos,
            gridRowsAll = rowsAll,
            gridRowsImages = rowsImages,
            gridRowsVideos = rowsVideos,
            displayItems = activeItems,
            gridRows = activeRows,
        )
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.update { state ->
            if (state.filter == filter) return@update state
            state.copy(
                filter = filter,
                displayItems = state.displayItemsFor(filter),
                gridRows = state.gridRowsFor(filter),
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                isSearchFiltering = query.isNotBlank(),
            )
        }
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(250)
            scheduleRecomputeFilters()
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        scheduleRecomputeFilters()
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

    fun beginSelection(id: Long) {
        _uiState.update { state ->
            val selected = state.selectedIds.toMutableSet()
            selected.add(id)
            state.copy(
                selectedIds = selected,
                selectionMode = true,
            )
        }
    }

    fun onSwipeSelectMedia(id: Long, isStart: Boolean) {
        if (!_uiState.value.selectionMode) return
        if (isStart) {
            swipeSelectAdding = id !in _uiState.value.selectedIds
            swipeVisitedIds.clear()
        }
        if (id in swipeVisitedIds) return
        swipeVisitedIds.add(id)
        val adding = swipeSelectAdding ?: return
        _uiState.update { state ->
            val selected = state.selectedIds.toMutableSet()
            if (adding) selected.add(id) else selected.remove(id)
            state.copy(
                selectedIds = selected,
                selectionMode = selected.isNotEmpty(),
            )
        }
    }

    fun onSwipeSelectFinished() {
        swipeSelectAdding = null
        swipeVisitedIds.clear()
    }

    fun clearSelection() {
        onSwipeSelectFinished()
        _uiState.update { it.copy(selectionMode = false, selectedIds = emptySet()) }
    }

    fun openStorageDialog() {
        _uiState.update {
            it.copy(
                showStorageDialog = true,
                storageStatsLoading = true,
                storageStats = null,
                storageStatsError = null,
            )
        }
        viewModelScope.launch {
            val media = _uiState.value.allMedia
            val result = withContext(Dispatchers.IO) {
                getStorageStatsUseCase(media)
            }
            _uiState.update { state ->
                if (!state.showStorageDialog) return@update state
                result.fold(
                    onSuccess = { stats ->
                        state.copy(
                            storageStatsLoading = false,
                            storageStats = stats,
                            storageStatsError = null,
                        )
                    },
                    onFailure = {
                        state.copy(
                            storageStatsLoading = false,
                            storageStatsError = it.message ?: "Could not load storage info.",
                        )
                    },
                )
            }
        }
    }

    fun dismissStorageDialog() {
        _uiState.update {
            it.copy(
                showStorageDialog = false,
                storageStatsLoading = false,
                storageStats = null,
                storageStatsError = null,
            )
        }
    }

    fun openDeleteConfirm() {
        if (_uiState.value.selectedIds.isEmpty()) return
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun confirmMoveToTrash() {
        viewModelScope.launch {
            val state = _uiState.value
            val uris = state.allMedia.filter { it.id in state.selectedIds }.map { it.uri }
            if (uris.isEmpty()) {
                dismissDeleteConfirm()
                return@launch
            }
            trashMediaUseCase(uris)
                .onSuccess { request ->
                    when (request) {
                        is TrashDeleteRequest.SystemConfirmation -> {
                            _uiState.update {
                                it.copy(
                                    showDeleteConfirmDialog = false,
                                    pendingTrashIntentSender = request.intentSender,
                                )
                            }
                        }
                        TrashDeleteRequest.Completed -> {
                            finishTrashDelete()
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            showDeleteConfirmDialog = false,
                            errorMessage = "Could not move items to trash.",
                        )
                    }
                }
        }
    }

    fun onSystemTrashResult(confirmed: Boolean) {
        _uiState.update { it.copy(pendingTrashIntentSender = null) }
        if (confirmed) {
            finishTrashDelete()
        }
    }

    fun clearPendingTrashIntent() {
        _uiState.update { it.copy(pendingTrashIntentSender = null) }
    }

    private fun finishTrashDelete() {
        val deletedIds = _uiState.value.selectedIds
        _uiState.update { state ->
            state.copy(
                selectedIds = emptySet(),
                selectionMode = false,
                showDeleteConfirmDialog = false,
                viewerMediaId = state.viewerMediaId?.takeUnless { it in deletedIds },
                activeMediaId = state.activeMediaId?.takeUnless { it in deletedIds },
            )
        }
        onSwipeSelectFinished()
        loadMedia(showFullScreenLoading = false)
    }

    fun openWallpaperDialog(media: GalleryMedia) {
        if (media.isVideo) return
        _uiState.update { it.copy(wallpaperDialogMedia = media) }
    }

    fun closeWallpaperDialog() {
        _uiState.update { it.copy(wallpaperDialogMedia = null) }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun saveWallpaper(uri: Uri, blurDp: Float) {
        viewModelScope.launch {
            val previous = _uiState.value
            saveWallpaperUseCase(uri, blurDp)
                .onSuccess { saved ->
                    val context = appContainer.application
                    WallpaperImageCache.invalidate(
                        context,
                        previous.wallpaperUri,
                        previous.wallpaperContentVersion,
                        previous.wallpaperBlurDp,
                    )
                    WallpaperImageCache.invalidate(
                        context,
                        saved.uri,
                        saved.contentRevision,
                        blurDp,
                    )
                    lastSyncedWallpaperUri = saved.uri
                    _uiState.update {
                        it.copy(
                            wallpaperUri = saved.uri,
                            wallpaperBlurDp = blurDp,
                            wallpaperContentVersion = saved.contentRevision,
                            wallpaperDialogMedia = null,
                            snackbarMessage = "Background updated",
                        )
                    }
                    syncLauncherInBackground(saved.uri)
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
            val previous = _uiState.value
            val revision = clearWallpaperUseCase()
            WallpaperImageCache.invalidate(
                appContainer.application,
                previous.wallpaperUri,
                previous.wallpaperContentVersion,
                previous.wallpaperBlurDp,
            )
            lastSyncedWallpaperUri = null
            _uiState.update {
                it.copy(
                    wallpaperUri = null,
                    wallpaperBlurDp = 18f,
                    wallpaperContentVersion = revision,
                    wallpaperDialogMedia = null,
                    snackbarMessage = "Background reset to default",
                )
            }
            syncLauncherInBackground(null)
        }
    }

    private fun syncLauncherInBackground(wallpaperUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(NonCancellable) {
                runCatching { appContainer.syncWallpaperLauncherUseCase(wallpaperUri) }
            }
        }
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
