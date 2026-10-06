package com.example.videoplayer.presentation.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary
import kotlin.math.roundToInt

private data class SideRailTab(
    val filter: MediaFilter,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun ImmersiveBrowseSideRail(
    pagerPosition: Float,
    onSelected: (MediaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember {
        listOf(
            SideRailTab(MediaFilter.ALL, "All", Icons.Rounded.Collections),
            SideRailTab(MediaFilter.IMAGES, "Photos", Icons.Rounded.Image),
            SideRailTab(MediaFilter.VIDEOS, "Videos", Icons.Rounded.Videocam),
        )
    }
    val activeIndex = pagerPosition.roundToInt().coerceIn(0, tabs.lastIndex)
    var trackHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val slotHeightPx = if (trackHeightPx > 0) trackHeightPx / tabs.size else 0
    val bubbleOffsetPx = (pagerPosition * slotHeightPx).roundToInt()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(96.dp)
            .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
            .background(Color(0xFF101428).copy(alpha = 0.92f))
            .padding(vertical = 20.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .width(76.dp)
                .onSizeChanged { trackHeightPx = it.height },
        ) {
            if (slotHeightPx > 0) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, bubbleOffsetPx) }
                        .width(76.dp)
                        .height(with(density) { slotHeightPx.toDp() }),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        AuroraCyan.copy(alpha = 0.45f),
                                        AuroraViolet.copy(alpha = 0.25f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val distance = kotlin.math.abs(pagerPosition - index)
                    val scale by animateFloatAsState(
                        targetValue = if (distance < 0.4f) 1.08f else 0.94f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "sideRailScale",
                    )
                    val isActive = index == activeIndex
                    Column(
                        modifier = Modifier
                            .scale(scale)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isActive) {
                                    Brush.verticalGradient(
                                        listOf(
                                            AuroraCyan.copy(alpha = 0.32f),
                                            AuroraViolet.copy(alpha = 0.22f),
                                        ),
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Transparent),
                                    )
                                },
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSelected(tab.filter) }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                            .graphicsLayer { rotationZ = -90f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(22.dp),
                            tint = if (isActive) TextPrimary else TextSecondary,
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isActive) TextPrimary else TextSecondary,
                        )
                    }
                }
            }
        }
    }
}
