package com.example.videoplayer.ui.gallery

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.data.SortOrder
import com.example.videoplayer.ui.theme.AuraIconDefaults
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.DeepSpaceElevated
import com.example.videoplayer.ui.theme.TextPrimary
import com.example.videoplayer.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onOpenMedia: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val mediaItems = state.displayItems
    val context = LocalContext.current
    var showSearch by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = state.gridScrollIndex,
        initialFirstVisibleItemScrollOffset = state.gridScrollOffset,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        GalleryTopBar(
            selectionMode = state.selectionMode,
            selectedCount = state.selectedIds.size,
            showSearch = showSearch,
            searchQuery = state.searchQuery,
            sortOrder = state.sortOrder,
            onToggleSearch = { showSearch = !showSearch },
            onSearchChange = viewModel::setSearchQuery,
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshMedia,
            onToggleGrid = viewModel::toggleGridColumns,
            onSortSelected = viewModel::setSortOrder,
            onClearSelection = viewModel::clearSelection,
            onShareSelection = {
                shareMedia(context, state.allMedia.filter { it.id in state.selectedIds })
            },
        )

        FilterTabs(
            selected = state.filter,
            onSelected = viewModel::setFilter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

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
                mediaItems.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        message = if (state.searchQuery.isNotBlank()) "No matches for your search." else "No media found on this device.",
                    )
                }
                else -> {
                    MediaGrid(
                        rows = state.gridRows,
                        columns = state.gridColumns,
                        gridState = gridState,
                        selectedIds = state.selectedIds,
                        selectionMode = state.selectionMode,
                        onClick = { item ->
                            if (state.selectionMode) {
                                viewModel.toggleSelection(item.id)
                            } else {
                                viewModel.saveGridScroll(
                                    gridState.firstVisibleItemIndex,
                                    gridState.firstVisibleItemScrollOffset,
                                )
                                onOpenMedia(item.id)
                            }
                        },
                        onLongClick = { item ->
                            if (item.isVideo) {
                                viewModel.toggleSelection(item.id)
                            } else {
                                viewModel.openWallpaperDialog(item)
                            }
                        },
                    )
                }
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
                                gridState.animateScrollToItem(0)
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
                                gridState.animateScrollToItem(state.gridRows.lastIndex)
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

@Composable
private fun GalleryTopBar(
    selectionMode: Boolean,
    selectedCount: Int,
    showSearch: Boolean,
    searchQuery: String,
    sortOrder: SortOrder,
    isRefreshing: Boolean,
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleGrid: () -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onClearSelection: () -> Unit,
    onShareSelection: () -> Unit,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectionMode) "$selectedCount selected" else "Aura Gallery",
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
                IconButton(onClick = onShareSelection) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
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
                IconButton(onClick = onToggleSearch, colors = AuraIconDefaults.iconButtonColors()) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                }
                Box {
                    IconButton(onClick = { showSortMenu = true }, colors = AuraIconDefaults.iconButtonColors()) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = TextPrimary)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sortLabel(order),
                                        color = if (order == sortOrder) AuroraCyan else TextPrimary,
                                    )
                                },
                                onClick = {
                                    onSortSelected(order)
                                    showSortMenu = false
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = onToggleGrid, colors = AuraIconDefaults.iconButtonColors()) {
                    Icon(Icons.Default.GridView, contentDescription = "Grid size", tint = TextPrimary)
                }
            }
        }
        if (showSearch) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Name, date, month, year…") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DeepSpaceElevated,
                    unfocusedContainerColor = DeepSpaceElevated,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, message: String) {
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

private fun sortLabel(order: SortOrder): String = when (order) {
    SortOrder.DATE_NEWEST -> "Newest first"
    SortOrder.DATE_OLDEST -> "Oldest first"
    SortOrder.NAME_ASC -> "Name (A–Z)"
    SortOrder.NAME_DESC -> "Name (Z–A)"
    SortOrder.SIZE_LARGEST -> "Largest first"
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
