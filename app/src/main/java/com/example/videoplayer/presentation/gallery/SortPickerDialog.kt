package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.videoplayer.domain.model.SortOrder
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.components.GlassSurfaceStyle
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.AuroraViolet
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary

private data class SortOption(
    val order: SortOrder,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

@Composable
fun SortPickerDialog(
    selected: SortOrder,
    onDismiss: () -> Unit,
    onSelected: (SortOrder) -> Unit,
) {
    val options = remember {
        listOf(
            SortOption(SortOrder.DATE_NEWEST, "Newest first", "Recent photos & videos on top", Icons.Default.CalendarMonth),
            SortOption(SortOrder.DATE_OLDEST, "Oldest first", "Earliest items on top", Icons.Default.CalendarMonth),
            SortOption(SortOrder.NAME_ASC, "Name A → Z", "Alphabetical order", Icons.Default.SortByAlpha),
            SortOption(SortOrder.NAME_DESC, "Name Z → A", "Reverse alphabetical", Icons.Default.SortByAlpha),
            SortOption(SortOrder.SIZE_LARGEST, "Largest first", "Biggest files on top", Icons.Default.SdStorage),
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        GlassSurface(
            style = GlassSurfaceStyle.Dialog,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 8.dp),
            cornerRadius = 24.dp,
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = AuroraCyan,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(22.dp),
                    )
                    Text(
                        text = "Sort gallery",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                options.forEach { option ->
                    val isSelected = option.order == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(AuroraCyan.copy(alpha = 0.18f))
                                        .border(1.dp, AuroraCyan.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                } else {
                                    Modifier.background(AuroraViolet.copy(alpha = 0.06f))
                                },
                            )
                            .clickable(
                                interactionSource = remember(option.order) { MutableInteractionSource() },
                                indication = null,
                            ) {
                                onSelected(option.order)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (isSelected) AuroraCyan else TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                        ) {
                            Text(
                                text = option.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = TextPrimary,
                            )
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = AuroraViolet,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
