package com.example.videoplayer.presentation.gallery

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.videoplayer.domain.model.MediaFilter
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.DeepSpaceElevated
import com.example.videoplayer.presentation.theme.TextSecondary

@Composable
fun FilterTabs(
    selected: MediaFilter,
    onSelected: (MediaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        MediaFilter.ALL to "All",
        MediaFilter.IMAGES to "Images",
        MediaFilter.VIDEOS to "Videos",
    )
    val selectedIndex = tabs.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    var tabWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val indicatorOffset by animateDpAsState(
        targetValue = with(density) { (selectedIndex * tabWidthPx).toDp() },
        animationSpec = tween(durationMillis = 180),
        label = "tabIndicator",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(DeepSpaceElevated)
            .padding(4.dp),
    ) {
        if (tabWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(with(density) { indicatorOffset.roundToPx() }, 0) }
                    .width(with(density) { tabWidthPx.toDp() })
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AuroraCyan.copy(alpha = 0.22f)),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            tabs.forEach { (filter, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onSizeChanged { size ->
                            if (tabWidthPx != size.width) tabWidthPx = size.width
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelected(filter) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (filter == selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (filter == selected) AuroraCyan else TextSecondary,
                    )
                }
            }
        }
    }
}
