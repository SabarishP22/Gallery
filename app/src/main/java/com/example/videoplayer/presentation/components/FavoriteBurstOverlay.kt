package com.example.videoplayer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.videoplayer.R

@Composable
fun FavoriteBurstOverlay(
    playNonce: Long,
    modifier: Modifier = Modifier,
) {
    if (playNonce == 0L) return
    key(playNonce) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.favorite_like))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = 1,
            isPlaying = true,
        )
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(160.dp),
            )
        }
    }
}
