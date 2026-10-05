package com.example.videoplayer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Heart pop for full-screen viewer (anchored near the favorite control). */
@Composable
fun FavoriteBurstOverlay(
    playNonce: Long,
    modifier: Modifier = Modifier,
) {
    if (playNonce == 0L) return
    Box(
        modifier = modifier.size(72.dp),
        contentAlignment = Alignment.Center,
    ) {
        FavoriteHeartPop(playKey = playNonce)
    }
}
