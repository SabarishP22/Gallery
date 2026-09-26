package com.example.videoplayer.presentation.gallery

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary

@Composable
fun DeleteConfirmDialog(
    itemCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val label = if (itemCount == 1) "this item" else "$itemCount items"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Move to trash?",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        },
        text = {
            Text(
                text = "$label will be moved to trash. You can restore them from your device’s Photos or Files app.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Move to trash", color = androidx.compose.ui.graphics.Color(0xFFFF5252))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AuroraCyan)
            }
        },
    )
}
