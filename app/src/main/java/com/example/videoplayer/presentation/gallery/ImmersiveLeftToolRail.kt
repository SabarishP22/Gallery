package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.presentation.theme.AuraIconDefaults
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary

@Composable
fun ImmersiveLeftToolRail(
    searchActive: Boolean,
    searchQuery: String,
    sortOrder: SortOrder,
    isRefreshing: Boolean,
    galleryGridVisible: Boolean,
    onOpenSearch: () -> Unit,
    onRefresh: () -> Unit,
    onToggleGrid: () -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onOpenStorage: () -> Unit,
    onToggleGalleryGridVisible: () -> Unit,
    onToggleImmersiveBrowse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortPicker by remember { mutableStateOf(false) }

    if (showSortPicker) {
        SortPickerDialog(
            selected = sortOrder,
            onDismiss = { showSortPicker = false },
            onSelected = {
                onSortSelected(it)
                showSortPicker = false
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp)
            .padding(start = 8.dp, top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "PixLab",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Your memories, reimagined",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 2,
            )
        }

        IconButton(onClick = onToggleImmersiveBrowse, colors = AuraIconDefaults.iconButtonColors()) {
            Icon(
                imageVector = Icons.Rounded.ViewCarousel,
                contentDescription = "Exit carousel browse",
                tint = AuroraCyan,
            )
        }
        IconButton(onClick = onRefresh, enabled = !isRefreshing, colors = AuraIconDefaults.iconButtonColors()) {
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
        IconButton(onClick = onToggleGalleryGridVisible, colors = AuraIconDefaults.iconButtonColors()) {
            Icon(
                imageVector = Icons.Outlined.Wallpaper,
                contentDescription = if (galleryGridVisible) {
                    "Hide gallery to view background"
                } else {
                    "Show gallery"
                },
                tint = if (galleryGridVisible) TextPrimary else AuroraCyan,
            )
        }
        IconButton(onClick = onOpenStorage, colors = AuraIconDefaults.iconButtonColors()) {
            Icon(Icons.Default.SdStorage, contentDescription = "Storage details", tint = TextPrimary)
        }
        IconButton(onClick = onOpenSearch, colors = AuraIconDefaults.iconButtonColors()) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = if (searchActive || searchQuery.isNotEmpty()) AuroraCyan else TextPrimary,
            )
        }
        IconButton(onClick = { showSortPicker = true }, colors = AuraIconDefaults.iconButtonColors()) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = AuroraCyan)
        }
        IconButton(onClick = onToggleGrid, colors = AuraIconDefaults.iconButtonColors()) {
            Icon(Icons.Default.GridView, contentDescription = "Grid size", tint = TextPrimary)
        }
    }
}
