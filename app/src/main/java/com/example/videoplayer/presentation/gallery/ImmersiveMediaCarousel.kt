package com.example.videoplayer.presentation.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary
import com.example.videoplayer.util.formatDuration
import com.example.videoplayer.util.mediaThumbnailRequest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

private val CardCorner = 28.dp
private const val COVER_FLOW_ANGLE = 58f
private const val MAX_VISIBLE_OFFSET = 4.25f

private fun coverFlowScale(absOffset: Float): Float {
    val d = absOffset.coerceIn(0f, MAX_VISIBLE_OFFSET)
    return when {
        d <= 1f -> lerp(1f, 0.78f, d)
        d <= 2f -> lerp(0.78f, 0.67f, d - 1f)
        d <= 3f -> lerp(0.67f, 0.58f, d - 2f)
        else -> lerp(0.58f, 0.5f, (d - 3f).coerceIn(0f, 1f))
    }
}

private fun coverFlowAlpha(absOffset: Float): Float {
    val d = absOffset.coerceIn(0f, MAX_VISIBLE_OFFSET)
    return when {
        d <= 1f -> lerp(1f, 0.88f, d)
        d <= 2f -> lerp(0.88f, 0.72f, d - 1f)
        d <= 3f -> lerp(0.72f, 0.58f, d - 2f)
        else -> lerp(0.58f, 0.45f, (d - 3f).coerceIn(0f, 1f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImmersiveMediaCarousel(
    items: List<GalleryMedia>,
    onOpenMedia: (GalleryMedia, ViewerTransitionOrigin) -> Unit,
    modifier: Modifier = Modifier,
    filterKey: Any = Unit,
) {
    if (items.isEmpty()) {
        EmptyState(
            modifier = modifier.fillMaxSize(),
            message = "No media in this category.",
        )
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size },
    )

    LaunchedEffect(filterKey) {
        if (pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }

    LaunchedEffect(pagerState, items.size) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page >= items.size && items.isNotEmpty()) {
                    pagerState.scrollToPage(items.lastIndex)
                }
            }
    }

    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )

    var rootWidthPx by remember { mutableFloatStateOf(1f) }
    var rootHeightPx by remember { mutableFloatStateOf(1f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { clip = false }
            .onGloballyPositioned { coordinates ->
                val root = coordinates.findRootCoordinates().size
                rootWidthPx = root.width.toFloat().coerceAtLeast(1f)
                rootHeightPx = root.height.toFloat().coerceAtLeast(1f)
            },
    ) {
        val isLandscape = maxWidth > maxHeight
        val cardHeight = if (isLandscape) {
            minOf(maxHeight * 0.88f, 372.dp)
        } else {
            minOf(maxHeight * 0.62f, 384.dp)
        }.coerceAtLeast(248.dp)
        val cardWidth = (cardHeight * 0.76f).coerceIn(210.dp, 318.dp)
        val sidePadding = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)
        val stackOverlap = (cardWidth * 0.76f).coerceAtLeast(140.dp)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false },
            pageSize = PageSize.Fixed(cardWidth),
            contentPadding = PaddingValues(horizontal = sidePadding),
            pageSpacing = -stackOverlap,
            beyondViewportPageCount = 6,
            flingBehavior = fling,
            key = { page -> items.getOrNull(page)?.id ?: page },
        ) { page ->
            val media = items[page]
            val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val absOffset = abs(offset)

            if (absOffset > MAX_VISIBLE_OFFSET) {
                Box(
                    modifier = Modifier
                        .width(cardWidth)
                        .fillMaxHeight(),
                )
                return@HorizontalPager
            }

            val scale = coverFlowScale(absOffset)
            val alpha = coverFlowAlpha(absOffset)
            val density = LocalDensity.current
            val depthPullPx = with(density) { (absOffset * 6).dp.toPx() }
            val tuckXPx = with(density) { (absOffset * 4).dp.toPx() * -offset.sign }

            var cardBounds by remember(page) { mutableStateOf<Rect?>(null) }

            Box(
                modifier = Modifier
                    .width(cardWidth)
                    .fillMaxHeight()
                    .zIndex(100f - absOffset)
                    .graphicsLayer { clip = false },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .zIndex(100f - absOffset)
                        .graphicsLayer {
                            translationX = tuckXPx
                            translationY = depthPullPx * 0.35f
                            scaleX = scale
                            scaleY = scale
                            rotationY = -offset * COVER_FLOW_ANGLE
                            this.alpha = alpha
                            cameraDistance = 8f * this.density
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .onGloballyPositioned { coordinates ->
                            cardBounds = coordinates.boundsInRoot()
                        }
                        .clickable(
                            interactionSource = remember(page) { MutableInteractionSource() },
                            indication = null,
                        ) {
                            val bounds = cardBounds ?: return@clickable
                            onOpenMedia(
                                media,
                                ViewerTransitionOrigin(
                                    centerXFraction = (bounds.left + bounds.width / 2f) / rootWidthPx,
                                    centerYFraction = (bounds.top + bounds.height / 2f) / rootHeightPx,
                                    widthFraction = bounds.width / rootWidthPx,
                                    heightFraction = bounds.height / rootHeightPx,
                                ),
                            )
                        },
                ) {
                    CoverFlowCard(
                        media = media,
                        pageIndex = page,
                        total = items.size,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        isCenter = absOffset < 0.28f,
                    )
                }
            }
        }
    }
}

private val Float.sign: Float
    get() = when {
        this > 0f -> 1f
        this < 0f -> -1f
        else -> 0f
    }

@Composable
private fun CoverFlowCard(
    media: GalleryMedia,
    pageIndex: Int,
    total: Int,
    cardWidth: Dp,
    cardHeight: Dp,
    isCenter: Boolean,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(CardCorner)

    Box(
        modifier = Modifier
            .size(width = cardWidth, height = cardHeight)
            .clip(shape)
            .border(
                width = if (isCenter) 2.5.dp else 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AuroraCyan.copy(alpha = if (isCenter) 1f else 0.82f),
                        AuroraViolet.copy(alpha = if (isCenter) 0.85f else 0.65f),
                        AuroraCyan.copy(alpha = if (isCenter) 0.7f else 0.5f),
                    ),
                ),
                shape = shape,
            )
            .background(Color(0xFF0A0E1C)),
    ) {
        AsyncImage(
            model = mediaThumbnailRequest(context, media),
            contentDescription = media.displayName,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Medium,
            modifier = Modifier.fillMaxSize(),
        )
        if (!isCenter) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.12f)),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.04f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (isCenter) 0.45f else 0.35f),
                        ),
                    ),
                ),
        )
        if (isCenter) {
            Text(
                text = "${pageIndex + 1} / $total",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        if (media.isVideo && isCenter) {
            Text(
                text = formatDuration(media.durationMs),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(6.dp),
                tint = AuroraCyan,
            )
        }
        if (isCenter) {
            Text(
                text = media.displayName,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
