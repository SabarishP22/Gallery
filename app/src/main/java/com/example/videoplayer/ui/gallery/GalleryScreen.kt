package com.example.videoplayer.ui.gallery

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.DeepSpaceElevated
import com.example.videoplayer.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onOpenPhoto: (Long) -> Unit,
    onOpenVideo: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

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
        if (granted) viewModel.onPermissionResult(true)
        else permissionLauncher.launch(permissions)
    }

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
            onToggleSearch = { showSearch = !showSearch },
            onSearchChange = viewModel::setSearchQuery,
            onRefresh = viewModel::refreshMedia,
            onToggleGrid = viewModel::toggleGridColumns,
            onShowSort = { showSortMenu = true },
            onClearSelection = viewModel::clearSelection,
            onShareSelection = {
                shareMedia(context, state.allMedia.filter { it.id in state.selectedIds })
            },
        )

        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
            SortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(sortLabel(order)) },
                    onClick = {
                        viewModel.setSortOrder(order)
                        showSortMenu = false
                    },
                )
            }
        }

        FilterTabs(
            selected = state.filter,
            onSelected = viewModel::setFilter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AuroraCyan,
                    )
                }
                state.errorMessage != null -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        message = state.errorMessage ?: "Something went wrong.",
                    )
                }
                viewModel.filteredMedia(state).isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        message = if (state.searchQuery.isNotBlank()) "No matches for your search." else "No media found on this device.",
                    )
                }
                else -> {
                    val media = viewModel.filteredMedia(state)
                    AnimatedContent(
                        targetState = state.filter,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "filterContent",
                    ) {
                        MediaGrid(
                            items = media,
                            columns = state.gridColumns,
                            selectedIds = state.selectedIds,
                            selectionMode = state.selectionMode,
                            onClick = { item ->
                                if (state.selectionMode) {
                                    viewModel.toggleSelection(item.id)
                                } else {
                                    viewModel.openMedia(item.id)
                                    if (item.isVideo) onOpenVideo(item.id) else onOpenPhoto(item.id)
                                }
                            },
                            onLongClick = { item -> viewModel.toggleSelection(item.id) },
                        )
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
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleGrid: () -> Unit,
    onShowSort: () -> Unit,
    onClearSelection: () -> Unit,
    onShareSelection: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
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
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                if (selectionMode) {
                    IconButton(onClick = onShareSelection) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Close, contentDescription = "Clear selection")
                    }
                } else {
                    IconButton(onClick = onToggleSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onShowSort) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = onToggleGrid) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid size")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
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
                placeholder = { Text("Search by filename…") },
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
