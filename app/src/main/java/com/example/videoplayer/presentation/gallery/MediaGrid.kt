package com.example.videoplayer.presentation.gallery

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.util.formatDuration
import com.example.videoplayer.util.mediaThumbnailRequest
import kotlinx.coroutines.CoroutineScope

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
    swipeSelectScope: CoroutineScope,
    onSwipeSelectMedia: (Long, Boolean) -> Unit,
    onSwipeSelectFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .gridSwipeSelection(
                enabled = selectionMode,
                gridState = gridState,
                scope = swipeSelectScope,
                onMediaAtPosition = onSwipeSelectMedia,
                onDragFinished = onSwipeSelectFinished,
            ),
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
    val density = LocalDensity.current
    val thumbSize = remember(gridColumns, density.density) {
        val cellPx = when {
            gridColumns <= 2 -> 320
            gridColumns <= 4 -> 240
            else -> 160
        }
        cellPx
    }
    val request = remember(media.id, thumbSize) {
        mediaThumbnailRequest(context, media, thumbSize)
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer { clip = true }
            .background(DeepSpace)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
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
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AuroraCyan.copy(alpha = 0.28f)),
                )
            }
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
