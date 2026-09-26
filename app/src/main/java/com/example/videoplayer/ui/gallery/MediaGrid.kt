package com.example.videoplayer.ui.gallery

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.ui.theme.AuroraCyan
import com.example.videoplayer.ui.theme.DeepSpace
import com.example.videoplayer.ui.theme.TextPrimary
import com.example.videoplayer.util.formatDateHeader
import com.example.videoplayer.util.formatDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    items: List<GalleryMedia>,
    columns: Int,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onClick: (GalleryMedia) -> Unit,
    onLongClick: (GalleryMedia) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(items) {
        items.groupBy { formatDateHeader(it.dateAddedSec) }
            .toList()
            .sortedByDescending { (_, group) -> group.maxOf { it.dateAddedSec } }
    }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        grouped.forEach { (header, groupItems) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = header,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }
            itemsIndexed(groupItems, key = { _, media -> media.id }) { index, media ->
                var visible by remember(media.id) { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.88f,
                    animationSpec = tween(durationMillis = 350, delayMillis = (index % 12) * 25),
                    label = "gridItemScale",
                )
                androidx.compose.runtime.LaunchedEffect(media.id) { visible = true }

                MediaGridItem(
                    media = media,
                    selected = media.id in selectedIds,
                    selectionMode = selectionMode,
                    modifier = Modifier
                        .animateItem()
                        .scale(scale),
                    onClick = { onClick(media) },
                    onLongClick = { onLongClick(media) },
                )
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(DeepSpace)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        val request = remember(media.uri, media.isVideo) {
            ImageRequest.Builder(context)
                .data(media.uri)
                .crossfade(280)
                .apply {
                    if (media.isVideo) videoFrameMillis(1L)
                }
                .build()
        }
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
                            listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
            )
            RowBadge(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                duration = formatDuration(media.durationMs),
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

@Composable
private fun RowBadge(modifier: Modifier = Modifier, duration: String) {
    Text(
        text = duration,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = TextPrimary,
    )
}
