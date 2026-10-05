package com.example.videoplayer.presentation.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.presentation.theme.AuraIconDefaults
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.DeepSpaceElevated
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onOpenMedia: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val tabFilters = remember { listOf(MediaFilter.ALL, MediaFilter.IMAGES, MediaFilter.VIDEOS) }
    val filterPagerState = rememberPagerState(
        initialPage = tabFilters.indexOf(state.filter).coerceAtLeast(0),
        pageCount = { tabFilters.size },
    )

    val pagerPosition by remember {
        derivedStateOf {
            filterPagerState.currentPage + filterPagerState.currentPageOffsetFraction
        }
    }

    LaunchedEffect(filterPagerState) {
        snapshotFlow { filterPagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                viewModel.setFilter(tabFilters[page.coerceIn(0, tabFilters.lastIndex)])
            }
    }

    LaunchedEffect(state.filter) {
        val target = tabFilters.indexOf(state.filter).coerceAtLeast(0)
        if (filterPagerState.currentPage != target && filterPagerState.isScrollInProgress.not()) {
            filterPagerState.scrollToPage(target)
        }
    }

    val pagerFling = PagerDefaults.flingBehavior(
        state = filterPagerState,
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )

    val rowsAll by remember { derivedStateOf { state.gridRowsAll } }
    val rowsImages by remember { derivedStateOf { state.gridRowsImages } }
    val rowsVideos by remember { derivedStateOf { state.gridRowsVideos } }
    val rowsFavorites by remember { derivedStateOf { state.gridRowsFavorites } }
    val context = LocalContext.current
    var showSearch by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = state.selectionMode) {
        viewModel.clearSelection()
    }

    BackHandler(enabled = state.favoritesVisible && !state.selectionMode && state.viewerMediaId == null) {
        viewModel.toggleFavoritesScreen()
    }

    BackHandler(enabled = state.immersiveBrowseMode && !state.selectionMode && state.viewerMediaId == null) {
        viewModel.toggleImmersiveBrowseMode()
    }

    val trashLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        viewModel.onSystemTrashResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(state.pendingTrashIntentSender) {
        val sender = state.pendingTrashIntentSender ?: return@LaunchedEffect
        trashLauncher.launch(IntentSenderRequest.Builder(sender).build())
    }

    if (state.showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            itemCount = state.selectedIds.size,
            onDismiss = viewModel::dismissDeleteConfirm,
            onConfirm = viewModel::confirmMoveToTrash,
        )
    }

    StorageDetailsDialog(
        visible = state.showStorageDialog,
        loading = state.storageStatsLoading,
        stats = state.storageStats,
        errorMessage = state.storageStatsError,
        onDismiss = viewModel::dismissStorageDialog,
    )

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result.values.all { it }
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val granted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        viewModel.handleInitialPermissionCheck(alreadyGranted = granted) {
            permissionLauncher.launch(permissions)
        }
    }

    val gridStateAll = rememberLazyGridState(
        initialFirstVisibleItemIndex = state.gridScrollIndexFor(MediaFilter.ALL),
        initialFirstVisibleItemScrollOffset = state.gridScrollOffsetFor(MediaFilter.ALL),
    )
    val gridStateImages = rememberLazyGridState(
        initialFirstVisibleItemIndex = state.gridScrollIndexFor(MediaFilter.IMAGES),
        initialFirstVisibleItemScrollOffset = state.gridScrollOffsetFor(MediaFilter.IMAGES),
    )
    val gridStateVideos = rememberLazyGridState(
        initialFirstVisibleItemIndex = state.gridScrollIndexFor(MediaFilter.VIDEOS),
        initialFirstVisibleItemScrollOffset = state.gridScrollOffsetFor(MediaFilter.VIDEOS),
    )
    val gridStateFavorites = rememberLazyGridState(
        initialFirstVisibleItemIndex = state.gridScrollFavoritesIndex,
        initialFirstVisibleItemScrollOffset = state.gridScrollFavoritesOffset,
    )

    LaunchedEffect(state.pendingGridScrollRestore) {
        val target = state.pendingGridScrollRestore ?: return@LaunchedEffect
        val gridState = when {
            target.favoritesMode -> gridStateFavorites
            target.filter == MediaFilter.ALL -> gridStateAll
            target.filter == MediaFilter.IMAGES -> gridStateImages
            else -> gridStateVideos
        }
        val closingViewer = state.viewerMediaId != null
        if (closingViewer) {
            gridState.scrollToItem(target.rowIndex, target.scrollOffset)
            viewModel.completeViewerDismissAfterScroll()
        } else {
            val distance = kotlin.math.abs(
                gridState.firstVisibleItemIndex - target.rowIndex,
            )
            if (distance <= 12) {
                gridState.animateScrollToItem(target.rowIndex, target.scrollOffset)
            } else {
                val leadIndex = (target.rowIndex - 4).coerceAtLeast(0)
                gridState.scrollToItem(leadIndex, 0)
                gridState.animateScrollToItem(target.rowIndex, target.scrollOffset)
            }
            viewModel.clearPendingGridScrollRestore()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        GalleryTopBar(
            selectionMode = state.selectionMode,
            selectedCount = state.selectedIds.size,
            searchQuery = state.searchQuery,
            sortOrder = state.sortOrder,
            onOpenSearch = { showSearch = true },
            searchActive = showSearch,
            onSearchChange = viewModel::setSearchQuery,
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshMedia,
            onToggleGrid = viewModel::toggleGridColumns,
            onSortSelected = viewModel::setSortOrder,
            onClearSelection = viewModel::clearSelection,
            onDeleteSelection = viewModel::openDeleteConfirm,
            onShareSelection = {
                shareMedia(context, state.allMedia.filter { it.id in state.selectedIds })
            },
            onOpenStorage = viewModel::openStorageDialog,
            galleryGridVisible = state.galleryGridVisible,
            onToggleGalleryGridVisible = viewModel::toggleGalleryGridVisible,
            favoritesVisible = state.favoritesVisible,
            onToggleFavorites = viewModel::toggleFavoritesScreen,
            immersiveBrowseMode = state.immersiveBrowseMode,
            onToggleImmersiveBrowse = viewModel::toggleImmersiveBrowseMode,
        )

        GallerySearchBar(
            visible = showSearch,
            query = state.searchQuery,
            isSearchFiltering = state.isSearchFiltering,
            onQueryChange = viewModel::setSearchQuery,
            onClose = { showSearch = false },
        )

        if (!state.immersiveBrowseMode) {
            AnimatedContent(
                targetState = state.favoritesVisible,
                transitionSpec = {
                    val enterOffset = if (targetState) 1 else -1
                    val exitOffset = if (targetState) -1 else 1
                    (fadeIn(tween(280)) + slideInHorizontally(tween(280)) { enterOffset * it / 3 })
                        .togetherWith(fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { exitOffset * it / 3 })
                },
                label = "galleryHomeFavoritesNav",
            ) { favoritesVisible ->
                if (favoritesVisible) {
                    Text(
                        text = "Favorites",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                } else {
                    FilterTabs(
                        pagerPosition = pagerPosition,
                        onSelected = { filter ->
                            viewModel.setFilter(filter)
                            scope.launch {
                                filterPagerState.animateScrollToPage(
                                    page = tabFilters.indexOf(filter).coerceAtLeast(0),
                                    animationSpec = tween(durationMillis = 180),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state.immersiveBrowseMode,
                transitionSpec = {
                    (fadeIn(tween(320)) + slideInHorizontally(tween(320)) { if (targetState) it / 4 else -it / 4 })
                        .togetherWith(fadeOut(tween(240)) + slideOutHorizontally(tween(240)) { if (targetState) -it / 4 else it / 4 })
                },
                label = "galleryBrowseMode",
                modifier = Modifier.fillMaxSize(),
            ) { immersiveBrowse ->
                if (immersiveBrowse) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            when {
                                state.isLoading && state.allMedia.isEmpty() -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = AuroraCyan,
                                    )
                                }
                                state.allMedia.isEmpty() -> {
                                    EmptyState(
                                        modifier = Modifier.align(Alignment.Center),
                                        message = "No media found on this device.",
                                    )
                                }
                                else -> {
                                    HorizontalPager(
                                        state = filterPagerState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { clip = false },
                                        beyondViewportPageCount = 1,
                                        userScrollEnabled = false,
                                        flingBehavior = pagerFling,
                                        key = { page -> "immersive_${tabFilters[page].name}" },
                                    ) { page ->
                                        val filter = tabFilters[page]
                                        ImmersiveMediaCarousel(
                                            items = state.displayItemsFor(filter),
                                            filterKey = filter,
                                            onOpenMedia = { media -> onOpenMedia(media.id) },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                }
                            }
                        }
                        ImmersiveBrowseBottomBar(
                            pagerPosition = pagerPosition,
                            onSelected = { filter ->
                                viewModel.setFilter(filter)
                                scope.launch {
                                    filterPagerState.animateScrollToPage(
                                        page = tabFilters.indexOf(filter).coerceAtLeast(0),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                } else {
                    ImmersiveGridPlaceholder(
                        state = state,
                        tabFilters = tabFilters,
                        filterPagerState = filterPagerState,
                        rowsAll = rowsAll,
                        rowsImages = rowsImages,
                        rowsVideos = rowsVideos,
                        rowsFavorites = rowsFavorites,
                        gridStateAll = gridStateAll,
                        gridStateImages = gridStateImages,
                        gridStateVideos = gridStateVideos,
                        gridStateFavorites = gridStateFavorites,
                        scope = scope,
                        viewModel = viewModel,
                        onOpenMedia = onOpenMedia,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImmersiveGridPlaceholder(
    state: GalleryUiState,
    tabFilters: List<MediaFilter>,
    filterPagerState: androidx.compose.foundation.pager.PagerState,
    rowsAll: List<GalleryGridRow>,
    rowsImages: List<GalleryGridRow>,
    rowsVideos: List<GalleryGridRow>,
    rowsFavorites: List<GalleryGridRow>,
    gridStateAll: androidx.compose.foundation.lazy.grid.LazyGridState,
    gridStateImages: androidx.compose.foundation.lazy.grid.LazyGridState,
    gridStateVideos: androidx.compose.foundation.lazy.grid.LazyGridState,
    gridStateFavorites: androidx.compose.foundation.lazy.grid.LazyGridState,
    scope: kotlinx.coroutines.CoroutineScope,
    viewModel: GalleryViewModel,
    onOpenMedia: (Long) -> Unit,
) {
    val pagerFling = PagerDefaults.flingBehavior(
        state = filterPagerState,
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
        Box(modifier = Modifier.fillMaxSize()) {
            val gridFadeMillis = 2_500
            androidx.compose.animation.AnimatedVisibility(
                visible = state.galleryGridVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = gridFadeMillis)),
                exit = fadeOut(animationSpec = tween(durationMillis = gridFadeMillis)),
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        state.isLoading && state.allMedia.isEmpty() -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = AuroraCyan,
                            )
                        }
                        state.errorMessage != null && state.allMedia.isEmpty() -> {
                            EmptyState(
                                modifier = Modifier.align(Alignment.Center),
                                message = state.errorMessage ?: "Something went wrong.",
                            )
                        }
                        state.allMedia.isEmpty() -> {
                            EmptyState(
                                modifier = Modifier.align(Alignment.Center),
                                message = if (state.searchQuery.isNotBlank()) {
                                    "No matches for your search."
                                } else {
                                    "No media found on this device."
                                },
                            )
                        }
                        state.favoritesVisible -> {
                            GalleryFilterPage(
                                pageFilter = MediaFilter.ALL,
                                rows = rowsFavorites,
                                gridState = gridStateFavorites,
                                gridColumns = state.gridColumns,
                                searchQuery = state.searchQuery,
                                selectedIds = state.selectedIds,
                                selectionMode = state.selectionMode,
                                favoriteIds = state.favoriteIds,
                                removingFavoriteMediaId = state.favoriteRemoveAnimMediaId,
                                favoriteBurstMediaId = state.favoriteBurstMediaId,
                                favoriteBurstNonce = state.favoriteBurstNonce,
                                favoritesMode = true,
                                swipeSelectScope = scope,
                                onSwipeSelectMedia = viewModel::onSwipeSelectMedia,
                                onSwipeSelectFinished = viewModel::onSwipeSelectFinished,
                                onOpenMedia = onOpenMedia,
                                onToggleSelection = viewModel::toggleSelection,
                                onBeginSelection = viewModel::beginSelection,
                                onSaveGridScroll = viewModel::saveGridScroll,
                                onSaveFavoritesGridScroll = viewModel::saveFavoritesGridScroll,
                                onAddFavorite = viewModel::addFavorite,
                                onRemoveFavorite = {
                                    viewModel.removeFavorite(it, animateGridRemoval = true)
                                },
                            )
                        }
                        else -> {
                            HorizontalPager(
                                state = filterPagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { clip = false },
                                beyondViewportPageCount = 0,
                                userScrollEnabled = !state.selectionMode,
                                flingBehavior = pagerFling,
                                key = { page -> tabFilters[page].name },
                            ) { page ->
                                GalleryFilterPage(
                                    pageFilter = tabFilters[page],
                                    rows = when (tabFilters[page]) {
                                        MediaFilter.ALL -> rowsAll
                                        MediaFilter.IMAGES -> rowsImages
                                        MediaFilter.VIDEOS -> rowsVideos
                                    },
                                    gridState = when (tabFilters[page]) {
                                        MediaFilter.ALL -> gridStateAll
                                        MediaFilter.IMAGES -> gridStateImages
                                        MediaFilter.VIDEOS -> gridStateVideos
                                    },
                                    gridColumns = state.gridColumns,
                                    searchQuery = state.searchQuery,
                                    selectedIds = state.selectedIds,
                                    selectionMode = state.selectionMode,
                                    favoriteIds = state.favoriteIds,
                                    removingFavoriteMediaId = state.favoriteRemoveAnimMediaId,
                                    favoriteBurstMediaId = state.favoriteBurstMediaId,
                                    favoriteBurstNonce = state.favoriteBurstNonce,
                                    favoritesMode = false,
                                    swipeSelectScope = scope,
                                    onSwipeSelectMedia = viewModel::onSwipeSelectMedia,
                                    onSwipeSelectFinished = viewModel::onSwipeSelectFinished,
                                    onOpenMedia = onOpenMedia,
                                    onToggleSelection = viewModel::toggleSelection,
                                    onBeginSelection = viewModel::beginSelection,
                                    onSaveGridScroll = viewModel::saveGridScroll,
                                    onSaveFavoritesGridScroll = viewModel::saveFavoritesGridScroll,
                                    onAddFavorite = viewModel::addFavorite,
                                    onRemoveFavorite = {
                                        viewModel.removeFavorite(it, animateGridRemoval = false)
                                    },
                                )
                            }
                        }
                    }

                    val activeGridState = when {
                        state.favoritesVisible -> gridStateFavorites
                        state.filter == MediaFilter.ALL -> gridStateAll
                        state.filter == MediaFilter.IMAGES -> gridStateImages
                        else -> gridStateVideos
                    }
                    if (state.gridRows.isNotEmpty() && state.viewerMediaId == null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        activeGridState.animateScrollToItem(0)
                                    }
                                },
                                containerColor = DeepSpaceElevated,
                                contentColor = TextPrimary,
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        activeGridState.animateScrollToItem(state.gridRows.lastIndex)
                                    }
                                },
                                containerColor = DeepSpaceElevated,
                                contentColor = TextPrimary,
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to bottom")
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun GalleryTopBar(
    selectionMode: Boolean,
    selectedCount: Int,
    searchActive: Boolean,
    searchQuery: String,
    sortOrder: SortOrder,
    isRefreshing: Boolean,
    onOpenSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleGrid: () -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
    onShareSelection: () -> Unit,
    onOpenStorage: () -> Unit,
    galleryGridVisible: Boolean,
    onToggleGalleryGridVisible: () -> Unit,
    favoritesVisible: Boolean,
    onToggleFavorites: () -> Unit,
    immersiveBrowseMode: Boolean,
    onToggleImmersiveBrowse: () -> Unit,
) {
    var showSortPicker by remember { mutableStateOf(false) }

    if (showSortPicker) {
        SortPickerDialog(
            selected = sortOrder,
            onDismiss = { showSortPicker = false },
            onSelected = onSortSelected,
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectionMode) "$selectedCount selected" else "PixLab",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (!selectionMode) {
                    Text(
                        text = "Your memories, reimagined",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
            if (selectionMode) {
                IconButton(onClick = onDeleteSelection) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Move to trash",
                        tint = Color(0xFFFF5252),
                    )
                }
                IconButton(onClick = onShareSelection) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Close selection")
                }
            } else {
                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    colors = AuraIconDefaults.iconButtonColors(),
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = AuroraCyan,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh library")
                    }
                }
            }
        }
        if (!selectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onToggleImmersiveBrowse,
                    colors = AuraIconDefaults.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ViewCarousel,
                        contentDescription = if (immersiveBrowseMode) {
                            "Exit carousel browse"
                        } else {
                            "Carousel browse"
                        },
                        tint = if (immersiveBrowseMode) AuroraCyan else TextPrimary,
                    )
                }
                IconButton(
                    onClick = onToggleFavorites,
                    enabled = !immersiveBrowseMode,
                    colors = AuraIconDefaults.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = if (favoritesVisible) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (favoritesVisible) "Close favorites" else "Favorites",
                        tint = if (favoritesVisible) Color(0xFFFF5252) else TextPrimary,
                    )
                }
                IconButton(
                    onClick = onToggleGalleryGridVisible,
                    colors = AuraIconDefaults.iconButtonColors(),
                ) {
                    Icon(
                        Icons.Outlined.Wallpaper,
                        contentDescription = if (galleryGridVisible) {
                            "Hide gallery to view background"
                        } else {
                            "Show gallery"
                        },
                        tint = if (galleryGridVisible) TextPrimary else AuroraCyan,
                    )
                }
                IconButton(
                    onClick = onOpenStorage,
                    colors = AuraIconDefaults.iconButtonColors(),
                ) {
                    Icon(
                        Icons.Default.SdStorage,
                        contentDescription = "Storage details",
                        tint = TextPrimary,
                    )
                }
                IconButton(
                    onClick = onOpenSearch,
                    colors = AuraIconDefaults.iconButtonColors(),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (searchActive || searchQuery.isNotEmpty()) AuroraCyan else TextPrimary,
                    )
                }
                IconButton(
                    onClick = { showSortPicker = true },
                    colors = AuraIconDefaults.iconButtonColors(),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        tint = AuroraCyan,
                    )
                }
                IconButton(onClick = onToggleGrid, colors = AuraIconDefaults.iconButtonColors()) {
                    Icon(Icons.Default.GridView, contentDescription = "Grid size", tint = TextPrimary)
                }
            }
        }
    }
}

@Composable
internal fun EmptyState(modifier: Modifier = Modifier, message: String) {
    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Outlined.PhotoLibrary,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(text = message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun shareMedia(context: android.content.Context, items: List<GalleryMedia>) {
    if (items.isEmpty()) return
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "*/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(items.map { it.uri }))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share media"))
}
