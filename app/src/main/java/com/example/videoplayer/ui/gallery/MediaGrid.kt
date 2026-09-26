package com.example.videoplayer.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.DeepSpace
import com.example.videoplayer.ui.theme.TextPrimary
import com.example.videoplayer.util.formatDateHeader
import com.example.videoplayer.util.formatDuration
import com.example.videoplayer.util.mediaThumbnailRequest

private sealed interface GridEntry {
    val key: String

    data class Header(val title: String) : GridEntry {
        override val key: String = "header-$title"
    }

    data class Item(val media: GalleryMedia) : GridEntry {
        override val key: String = "media-${media.id}-${media.uri}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    items: List<GalleryMedia>,
    columns: Int,
    gridState: LazyGridState,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onClick: (GalleryMedia) -> Unit,
    onLongClick: (GalleryMedia) -> Unit,
    onPinchColumnChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(items) {
        val grouped = items.groupBy { formatDateHeader(it.dateAddedSec) }
            .toList()
            .sortedByDescending { (_, group) -> group.maxOf { it.dateAddedSec } }
        buildList {
            grouped.forEach { (header, groupItems) ->
                add(GridEntry.Header(header))
                groupItems.forEach { add(GridEntry.Item(it)) }
            }
        }
    }

    var pinchAccumulator by remember { mutableFloatStateOf(1f) }

    LazyVerticalGrid(
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(columns) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom == 1f) return@detectTransformGestures
                    pinchAccumulator *= zoom
                    when {
                        pinchAccumulator > 1.22f -> {
                            onPinchColumnChange((columns + 1).coerceAtMost(7))
                            pinchAccumulator = 1f
                        }
                        pinchAccumulator < 0.78f -> {
                            onPinchColumnChange((columns - 1).coerceAtLeast(2))
                            pinchAccumulator = 1f
                        }
                    }
                }
            },
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = entries,
            key = { it.key },
            span = { entry ->
                if (entry is GridEntry.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1)
            },
        ) { entry ->
            when (entry) {
                is GridEntry.Header -> {
                    Text(
                        text = entry.title,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }
                is GridEntry.Item -> {
                    MediaGridItem(
                        media = entry.media,
                        selected = entry.media.id in selectedIds,
                        selectionMode = selectionMode,
                        gridColumns = columns,
                        onClick = { onClick(entry.media) },
                        onLongClick = { onLongClick(entry.media) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
            gridColumns <= 2 -> 720
            gridColumns <= 4 -> 480
            else -> 320
        }
    }
    val request = remember(media.uri, media.isVideo, thumbSize) {
        mediaThumbnailRequest(context, media, thumbSize)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        ),
                    ),
            )
            Text(
                text = formatDuration(media.durationMs),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(AuroraCyan.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(6.dp),
            )
        }
        if (selectionMode) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (selected) AuroraCyan else TextPrimary.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            )
        }
    }
}
