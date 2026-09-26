package com.example.videoplayer.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary

private val TrashRed = Color(0xFFFF5252)
private val TrashRedSoft = Color(0xFFFF5252).copy(alpha = 0.14f)

@Composable
fun DeleteConfirmDialog(
    itemCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val title = if (itemCount == 1) "Move item to trash?" else "Move $itemCount items to trash?"
    val summary = if (itemCount == 1) {
        "This photo or video will leave your gallery but stay in trash until you empty it."
    } else {
        "These items will leave your gallery but stay in trash until you empty it."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
        ),
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 8.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            TrashRed.copy(alpha = 0.35f),
                            AuroraCyan.copy(alpha = 0.2f),
                        ),
                    ),
                    shape = RoundedCornerShape(28.dp),
                ),
            cornerRadius = 28.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(TrashRedSoft),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = TrashRed,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepSpace.copy(alpha = 0.45f))
                        .border(1.dp, AuroraCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Default.RestoreFromTrash,
                        contentDescription = null,
                        tint = AuroraCyan,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp),
                    )
                    Text(
                        text = "You can restore from your device’s Photos or Files trash anytime before it’s permanently deleted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary.copy(alpha = 0.88f),
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepSpace.copy(alpha = 0.85f),
                            contentColor = TextPrimary,
                        ),
                    ) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier.padding(vertical = 2.dp),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TrashRed,
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Move to trash",
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .padding(vertical = 2.dp),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
