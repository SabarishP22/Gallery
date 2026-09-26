package com.example.videoplayer.presentation.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.util.formatFileSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PhotoViewerScreen(
    images: List<GalleryMedia>,
    startIndex: Int,
    onBack: () -> Unit,
) {
    if (images.isEmpty()) {
        onBack()
        return
    }
    val pagerState = rememberPagerState(initialPage = startIndex.coerceIn(0, images.lastIndex)) { images.size }
    var chromeVisible by remember { mutableStateOf(true) }
    var infoVisible by remember { mutableStateOf(false) }
    var photoZoomed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val current = images[pagerState.currentPage]

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect {
                chromeVisible = true
                infoVisible = false
                photoZoomed = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !photoZoomed,
            key = { page -> images[page].id },
        ) { page ->
            val isActive = pagerState.currentPage == page
            ZoomablePhoto(
                media = images[page],
                onToggleChrome = { chromeVisible = !chromeVisible },
                onZoomChanged = { zoomed -> if (isActive) photoZoomed = zoomed },
                onLongPress = { },
            )
        }

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                cornerRadius = 16.dp,
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = current.displayName,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                    )
                    IconButton(onClick = { infoVisible = !infoVisible }, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = infoVisible && chromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                val date = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
                    .format(Date(current.dateAddedSec * 1000))
                Text(
                    text = buildString {
                        appendLine(current.displayName)
                        appendLine(date)
                        append("${current.width} × ${current.height} · ${formatFileSize(current.sizeBytes)}")
                    },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            IconButton(
                onClick = {
                    val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = current.mimeType
                        putExtra(android.content.Intent.EXTRA_STREAM, current.uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(share, "Share photo"))
                },
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
            }
        }
    }
}
