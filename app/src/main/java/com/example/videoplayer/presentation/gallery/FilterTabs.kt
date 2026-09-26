package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary
import kotlin.math.roundToInt

private data class FilterTabSpec(
    val filter: MediaFilter,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun FilterTabs(
    pagerPosition: Float,
    onSelected: (MediaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember {
        listOf(
            FilterTabSpec(MediaFilter.ALL, "All", Icons.Rounded.Collections),
            FilterTabSpec(MediaFilter.IMAGES, "Photos", Icons.Rounded.Image),
            FilterTabSpec(MediaFilter.VIDEOS, "Videos", Icons.Rounded.Videocam),
        )
    }
    var tabWidthPx by remember { mutableIntStateOf(0) }
    var tapHighlightFilter by remember { mutableStateOf<MediaFilter?>(null) }
    val density = LocalDensity.current
    val indicatorOffsetPx = (pagerPosition * tabWidthPx).roundToInt()
    val pagerHighlightFilter = tabs[
        (pagerPosition + 0.5f).roundToInt().coerceIn(0, tabs.lastIndex)
    ].filter
    val activeFilter = tapHighlightFilter ?: pagerHighlightFilter

    LaunchedEffect(pagerPosition, tapHighlightFilter) {
        val pending = tapHighlightFilter ?: return@LaunchedEffect
        val targetIndex = tabs.indexOfFirst { it.filter == pending }
        if (targetIndex >= 0 && kotlin.math.abs(pagerPosition - targetIndex) < 0.02f) {
            tapHighlightFilter = null
        }
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(5.dp),
        ) {
            if (tabWidthPx > 0) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffsetPx, 0) }
                        .width(with(density) { tabWidthPx.toDp() })
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    AuroraCyan.copy(alpha = 0.28f),
                                    AuroraViolet.copy(alpha = 0.22f),
                                ),
                            ),
                        ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                tabs.forEach { spec ->
                    val isHighlighted = spec.filter == activeFilter
                    val labelColor = if (isHighlighted) TextPrimary else TextSecondary
                    val iconTint = if (isHighlighted) AuroraCyan else TextSecondary.copy(alpha = 0.85f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .onSizeChanged { size ->
                                if (tabWidthPx != size.width) tabWidthPx = size.width
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                tapHighlightFilter = spec.filter
                                onSelected(spec.filter)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = spec.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = iconTint,
                            )
                            Text(
                                text = spec.label,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Medium,
                                color = labelColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
