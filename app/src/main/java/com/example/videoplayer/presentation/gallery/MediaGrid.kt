package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.presentation.components.FavoriteHeartPop
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.util.formatDuration
import com.example.videoplayer.util.mediaThumbnailRequest
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    rows: List<GalleryGridRow>,
    columns: Int,
    gridState: LazyGridState,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    favoriteIds: Set<Long>,
    removingFavoriteMediaId: Long?,
    favoriteBurstMediaId: Long?,
    favoriteBurstNonce: Long,
    onClick: (GalleryMedia) -> Unit,
    onLongClick: (GalleryMedia) -> Unit,
    onDoubleClick: (GalleryMedia) -> Unit,
    swipeSelectScope: CoroutineScope,
    onSwipeSelectMedia: (Long, Boolean) -> Unit,
    onSwipeSelectFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
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
                        isFavorite = row.media.id in favoriteIds,
                        isRemovingFavorite = row.media.id == removingFavoriteMediaId,
                        showFavoriteBurst = row.media.id == favoriteBurstMediaId,
                        favoriteBurstNonce = favoriteBurstNonce,
                        gridColumns = columns,
                        onClick = { onClick(row.media) },
                        onLongClick = { onLongClick(row.media) },
                        onDoubleClick = { onDoubleClick(row.media) },
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
    isFavorite: Boolean,
    isRemovingFavorite: Boolean,
    showFavoriteBurst: Boolean,
    favoriteBurstNonce: Long,
    gridColumns: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDoubleClick: () -> Unit,
) {
    val cellAlpha by animateFloatAsState(
        targetValue = if (isRemovingFavorite) 0f else 1f,
        animationSpec = tween(280),
        label = "favRemoveAlpha",
    )
    val cellScale by animateFloatAsState(
        targetValue = if (isRemovingFavorite) 0.82f else 1f,
        animationSpec = tween(280),
        label = "favRemoveScale",
    )
    val appContext = LocalContext.current.applicationContext
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val thumbSizePx = remember(gridColumns, screenWidthDp, density) {
        val gridWidthPx = with(density) { screenWidthDp.dp.toPx() }
        val paddingPx = with(density) { (12.dp * 2 + 6.dp * (gridColumns - 1).coerceAtLeast(0)).toPx() }
        val cellPx = ((gridWidthPx - paddingPx) / gridColumns.coerceAtLeast(1)).roundToInt()
        cellPx.coerceIn(96, 384)
    }
    val request = remember(media.id, thumbSizePx) {
        mediaThumbnailRequest(appContext, media, thumbSizePx)
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                alpha = cellAlpha
                scaleX = cellScale
                scaleY = cellScale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(DeepSpace)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
            ),
    ) {
        GridCellThumbnail(
            request = request,
            modifier = Modifier.fillMaxSize(),
        )
        if (media.isVideo) {
            MediaGridVideoOverlay(media = media)
        }
        if (showFavoriteBurst) {
            FavoriteHeartPop(
                playKey = favoriteBurstNonce,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (!selectionMode && isFavorite) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp),
            )
        }
        if (selectionMode) {
            MediaGridSelectionOverlay(selected = selected)
        }
    }
}

@Composable
private fun GridCellThumbnail(
    request: ImageRequest,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = request,
        contentDescription = null,
        modifier = modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        },
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Low,
        loading = {
            Box(modifier = Modifier.fillMaxSize().background(DeepSpace))
        },
        error = {
            Box(modifier = Modifier.fillMaxSize().background(DeepSpace))
        },
        success = {
            SubcomposeAsyncImageContent(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        },
    )
}

@Composable
private fun BoxScope.MediaGridVideoOverlay(media: GalleryMedia) {
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

@Composable
private fun BoxScope.MediaGridSelectionOverlay(selected: Boolean) {
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
