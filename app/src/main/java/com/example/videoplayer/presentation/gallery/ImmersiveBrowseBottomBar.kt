package com.example.videoplayer.presentation.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary
import kotlin.math.roundToInt

private data class DockTab(
    val filter: MediaFilter,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun ImmersiveBrowseBottomBar(
    pagerPosition: Float,
    onSelected: (MediaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember {
        listOf(
            DockTab(MediaFilter.ALL, "All", Icons.Rounded.Collections),
            DockTab(MediaFilter.IMAGES, "Photos", Icons.Rounded.Image),
            DockTab(MediaFilter.VIDEOS, "Videos", Icons.Rounded.Videocam),
        )
    }
    var trackWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val bubbleOffsetPx = (pagerPosition * (trackWidthPx / tabs.size.coerceAtLeast(1))).roundToInt()
    val activeIndex = pagerPosition.roundToInt().coerceIn(0, tabs.lastIndex)
    val slotWidthPx = if (trackWidthPx > 0) trackWidthPx / tabs.size else 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .shadow(18.dp, RoundedCornerShape(40.dp), ambientColor = AuroraCyan.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF101428).copy(alpha = 0.94f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(58.dp)
                    .onSizeChanged { trackWidthPx = it.width },
            ) {
                if (slotWidthPx > 0) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(bubbleOffsetPx, 0) }
                            .width(with(density) { slotWidthPx.toDp() })
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            AuroraCyan.copy(alpha = 0.5f),
                                            AuroraViolet.copy(alpha = 0.28f),
                                            Color.Transparent,
                                        ),
                                    ),
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            AuroraCyan.copy(alpha = 0.34f),
                                            AuroraViolet.copy(alpha = 0.26f),
                                        ),
                                    ),
                                ),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val distance = kotlin.math.abs(pagerPosition - index)
                        val iconScale by animateFloatAsState(
                            targetValue = if (distance < 0.45f) 1.2f else 0.9f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                            label = "dockIconScale",
                        )
                        val isActive = index == activeIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { onSelected(tab.filter) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier
                                    .scale(iconScale)
                                    .size(26.dp),
                                tint = if (isActive) TextPrimary else TextSecondary.copy(alpha = 0.62f),
                            )
                        }
                    }
                }
            }
        }
    }
}
