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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import kotlin.math.cos
import kotlin.math.sin

private val CardWidth = 248.dp
private val CardHeight = 332.dp
private val CardCorner = 22.dp
private const val CYLINDER_ANGLE_PER_PAGE = 42f
private const val CYLINDER_RADIUS_FRACTION = 0.38f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImmersiveMediaCarousel(
    items: List<GalleryMedia>,
    onOpenMedia: (GalleryMedia) -> Unit,
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
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF05060D).copy(alpha = 0.55f),
                        Color(0xFF0A1030).copy(alpha = 0.72f),
                        Color(0xFF0D1B4A).copy(alpha = 0.85f),
                    ),
                ),
            ),
    ) {
        val sidePadding = ((maxWidth - CardWidth) / 2).coerceAtLeast(0.dp)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.92f)
                .height(120.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AuroraCyan.copy(alpha = 0.35f),
                            AuroraViolet.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                        radius = 420f,
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 108.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AuroraCyan.copy(alpha = 0.65f),
                            AuroraViolet.copy(alpha = 0.55f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            pageSize = PageSize.Fixed(CardWidth),
            contentPadding = PaddingValues(horizontal = sidePadding),
            pageSpacing = (-72).dp,
            beyondViewportPageCount = 3,
            flingBehavior = fling,
            key = { page -> items.getOrNull(page)?.id ?: page },
        ) { page ->
            val media = items[page]
            val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            if (abs(offset) > 2.8f) {
                Box(modifier = Modifier.width(CardWidth).height(CardHeight))
                return@HorizontalPager
            }

            val density = LocalDensity.current
            val radiusPx = with(density) { maxWidth.toPx() * CYLINDER_RADIUS_FRACTION }
            val angleRad = Math.toRadians((offset * CYLINDER_ANGLE_PER_PAGE).toDouble())
            val sinA = sin(angleRad).toFloat()
            val cosA = cos(angleRad).toFloat()
            val proximity = (1f - abs(offset).coerceIn(0f, 1.25f) / 1.25f).coerceIn(0f, 1f)
            val scale = lerp(0.56f, 1f, proximity)
            val alpha = lerp(0.38f, 1f, proximity)
            val arcShiftX = sinA * radiusPx * 0.52f
            val arcShiftY = (1f - cosA) * with(density) { 36.dp.toPx() }

            Column(
                modifier = Modifier
                    .width(CardWidth)
                    .zIndex(20f - abs(offset))
                    .graphicsLayer {
                        translationX = arcShiftX
                        translationY = -arcShiftY
                        scaleX = scale
                        scaleY = scale
                        rotationY = offset * CYLINDER_ANGLE_PER_PAGE
                        this.alpha = alpha
                        cameraDistance = 6.5f * this.density
                        transformOrigin = TransformOrigin(0.5f, 0.62f)
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .clickable(
                        interactionSource = remember(page) { MutableInteractionSource() },
                        indication = null,
                    ) { onOpenMedia(media) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CylinderCarouselCard(
                    media = media,
                    pageIndex = page,
                    total = items.size,
                )
                CylinderCardReflection(media = media)
            }
        }
    }
}

@Composable
private fun CylinderCarouselCard(
    media: GalleryMedia,
    pageIndex: Int,
    total: Int,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(CardCorner)

    Box(
        modifier = Modifier
            .size(width = CardWidth, height = CardHeight)
            .clip(shape)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AuroraCyan.copy(alpha = 0.95f),
                        AuroraViolet.copy(alpha = 0.75f),
                        AuroraCyan.copy(alpha = 0.55f),
                    ),
                ),
                shape = shape,
            )
            .background(Color(0xFF0B1024)),
    ) {
        AsyncImage(
            model = mediaThumbnailRequest(context, media),
            contentDescription = media.displayName,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Medium,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                        ),
                    ),
                ),
        )
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
        if (media.isVideo) {
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
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(6.dp),
                tint = AuroraCyan,
            )
        }
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

@Composable
private fun CylinderCardReflection(media: GalleryMedia) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(CardCorner)
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .size(width = CardWidth, height = CardHeight * 0.42f)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.92f),
                        ),
                    ),
                )
            }
            .graphicsLayer {
                scaleY = -1f
                alpha = 0.42f
            }
            .clip(shape),
    ) {
        AsyncImage(
            model = mediaThumbnailRequest(context, media),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Low,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
