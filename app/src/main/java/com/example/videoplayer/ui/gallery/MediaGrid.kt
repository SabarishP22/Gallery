package com.example.videoplayer.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.DeepSpace
import com.example.videoplayer.ui.theme.TextPrimary
import com.example.videoplayer.util.formatDuration
import com.example.videoplayer.util.mediaThumbnailRequest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    rows: List<GalleryGridRow>,
    columns: Int,
    gridState: LazyGridState,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onClick: (GalleryMedia) -> Unit,
    onLongClick: (GalleryMedia) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        state = gridState,
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = rows,
            key = { it.stableKey },
            contentType = { row ->
                when (row) {
                    is GalleryGridRow.Header -> 0
                    is GalleryGridRow.Cell -> 1
                }
            },
            span = { row ->
                if (row is GalleryGridRow.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1)
            },
        ) { row ->
            when (row) {
                is GalleryGridRow.Header -> {
                    Text(
                        text = row.title,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }
                is GalleryGridRow.Cell -> {
                    MediaGridItem(
                        media = row.media,
                        selected = row.media.id in selectedIds,
                        selectionMode = selectionMode,
                        gridColumns = columns,
                        onClick = { onClick(row.media) },
                        onLongClick = { onLongClick(row.media) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaGridItem(
    media: GalleryMedia,
    selected: Boolean,
    selectionMode: Boolean,
    gridColumns: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val thumbSize = remember(gridColumns) {
        when {
            gridColumns <= 2 -> 400
            gridColumns <= 4 -> 280
            else -> 180
        }
    }
    val request = remember(media.id, thumbSize) {
        mediaThumbnailRequest(context, media, thumbSize)
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(DeepSpace)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        AsyncImage(
            model = request,
            contentDescription = media.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (media.isVideo) {
            Text(
                text = formatDuration(media.durationMs),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(AuroraCyan.copy(alpha = 0.32f), RoundedCornerShape(50))
                    .padding(5.dp),
            )
        }
        if (selectionMode) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (selected) AuroraCyan else TextPrimary.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp),
            )
        }
    }
}
