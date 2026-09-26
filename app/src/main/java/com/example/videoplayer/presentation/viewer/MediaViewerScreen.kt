package com.example.videoplayer.presentation.viewer

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.util.ForcedOrientationController
import com.example.videoplayer.util.formatDuration
import com.example.videoplayer.util.formatFileSize
import com.example.videoplayer.util.mediaThumbnailRequest
import android.app.Activity
import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MediaViewerScreen(
    items: List<GalleryMedia>,
    startIndex: Int,
    onBack: () -> Unit,
    onWallpaperRequest: (GalleryMedia) -> Unit = {},
) {
    if (items.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val safeStart = startIndex.coerceIn(0, items.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeStart) { items.size }
    var chromeVisible by remember { mutableStateOf(false) }
    var infoVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val current = items[pagerState.currentPage]
    var photoZoomed by remember { mutableStateOf(false) }

    val activity = context as? Activity
    ImmersiveViewerSystemBars(enabled = true)
    DisposableEffect(activity) {
        val controller = activity?.let { ForcedOrientationController(it) }
        controller?.start()
        onDispose { controller?.stop() }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect {
                chromeVisible = false
                infoVisible = false
                photoZoomed = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            userScrollEnabled = !photoZoomed,
            key = { page -> items[page].id },
        ) { page ->
            val media = items[page]
            val isActive = pagerState.currentPage == page
            if (media.isVideo) {
                VideoPage(
                    media = media,
                    isActive = isActive,
                    onBack = onBack,
                    onChromeVisibleChange = { chromeVisible = it },
                )
            } else {
                ZoomablePhotoPage(
                    media = media,
                    onToggleChrome = { chromeVisible = !chromeVisible },
                    onZoomChanged = { zoomed -> if (isActive) photoZoomed = zoomed },
                    onLongPress = { onWallpaperRequest(media) },
                )
            }
        }

        AnimatedVisibility(
            visible = chromeVisible && !current.isVideo,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .zIndex(50f),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
                    .padding(4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        AnimatedVisibility(
            visible = chromeVisible && !current.isVideo,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        ) {
            Row {
                IconButton(
                    onClick = { infoVisible = !infoVisible },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.35f), MaterialTheme.shapes.medium),
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = current.mimeType
                            putExtra(android.content.Intent.EXTRA_STREAM, current.uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(share, "Share"))
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.35f), MaterialTheme.shapes.medium),
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }
        }

        AnimatedVisibility(
            visible = infoVisible && chromeVisible && !current.isVideo,
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

        if (!chromeVisible) {
            Text(
                text = "${pagerState.currentPage + 1} / ${items.size}",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
                    .background(Color.Black.copy(alpha = 0.25f), MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun ZoomablePhotoPage(
    media: GalleryMedia,
    onToggleChrome: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    onLongPress: () -> Unit,
) {
    var scale by remember(media.id) { mutableFloatStateOf(1f) }
    var offset by remember(media.id) { mutableStateOf(Offset.Zero) }
    val context = LocalContext.current

    LaunchedEffect(scale) {
        onZoomChanged(scale > 1.05f)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = mediaThumbnailRequest(context, media, pixelSize = 2048),
            contentDescription = media.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(media.id) {
                    detectTapGestures(
                        onTap = { onToggleChrome() },
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                            }
                        },
                        onLongPress = { onLongPress() },
                    )
                }
                .then(
                    if (scale > 1f) {
                        Modifier.pointerInput(media.id, scale) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                if (zoom != 1f) {
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                }
                                offset += pan
                                if (scale <= 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPage(
    media: GalleryMedia,
    isActive: Boolean,
    onBack: () -> Unit,
    onChromeVisibleChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var controlsVisible by remember(media.id) { mutableStateOf(false) }
    var isPlaying by remember(media.id) { mutableStateOf(false) }
    var durationMs by remember(media.id) { mutableLongStateOf(media.durationMs) }
    var positionMs by remember(media.id) { mutableLongStateOf(0L) }
    var playbackSpeed by remember(media.id) { mutableFloatStateOf(1f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0f) }

    val player = remember(media.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(media.uri))
            prepare()
        }
    }

    DisposableEffect(media.id) {
        onDispose {
            player.release()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    durationMs = player.duration.coerceAtLeast(0L)
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            player.playWhenReady = true
            player.play()
        } else {
            player.pause()
            player.playWhenReady = false
        }
    }

    LaunchedEffect(player, isScrubbing, isActive) {
        while (isActive) {
            if (!isScrubbing) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
                if (durationMs <= 0L && player.duration > 0) durationMs = player.duration
            }
            delay(200)
        }
    }

    LaunchedEffect(controlsVisible, isPlaying, isActive) {
        if (isActive && isPlaying && controlsVisible) {
            delay(4500)
            if (isActive && isPlaying && controlsVisible) {
                controlsVisible = false
                onChromeVisibleChange(false)
            }
        }
    }

    fun toggleControls() {
        controlsVisible = !controlsVisible
        onChromeVisibleChange(controlsVisible)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { it.player = if (isActive) player else null },
        )

        if (isActive) {
            var lastQuickTapUptime by remember(media.id) { mutableLongStateOf(0L) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(45f)
                    .pointerInput(media.id) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            val up = waitForUpOrCancellation() ?: return@awaitEachGesture
                            val now = SystemClock.uptimeMillis()
                            val width = size.width.toFloat()
                            val x = up.position.x
                            if (controlsVisible) return@awaitEachGesture
                            if (lastQuickTapUptime > 0L && now - lastQuickTapUptime in 1..320L) {
                                lastQuickTapUptime = 0L
                                when {
                                    x < width * 0.38f -> {
                                        player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0L))
                                    }
                                    x > width * 0.62f -> {
                                        player.seekTo(
                                            (player.currentPosition + 10_000).coerceAtMost(
                                                if (player.duration > 0) player.duration else Long.MAX_VALUE,
                                            ),
                                        )
                                    }
                                }
                                controlsVisible = true
                                onChromeVisibleChange(true)
                            } else {
                                lastQuickTapUptime = now
                                toggleControls()
                            }
                        }
                    },
            )
        }

        if (controlsVisible && isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(48f)
                    .background(Color.Black.copy(alpha = 0.18f))
                    .clickable(
                        interactionSource = remember(media.id) { MutableInteractionSource() },
                        indication = null,
                    ) { toggleControls() },
            )
            GlassSurface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(50f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    cornerRadius = 16.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = media.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            Text(
                                text = "${formatDuration(positionMs)} / ${formatDuration(durationMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                        Box {
                            IconButton(onClick = { speedMenuExpanded = true }) {
                                Icon(Icons.Default.Speed, contentDescription = "Speed", tint = TextPrimary)
                            }
                            DropdownMenu(expanded = speedMenuExpanded, onDismissRequest = { speedMenuExpanded = false }) {
                                listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = { Text("${speed}x") },
                                        onClick = {
                                            playbackSpeed = speed
                                            player.setPlaybackSpeed(speed)
                                            speedMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        IconButton(onClick = {
                            val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = media.mimeType
                                putExtra(android.content.Intent.EXTRA_STREAM, media.uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(share, "Share video"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
                        }
                    }
                }

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(50f),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                    IconButton(onClick = {
                        player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0L))
                    }) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = "Back 10s",
                            modifier = Modifier.size(44.dp),
                            tint = TextPrimary,
                        )
                    }
                    IconButton(onClick = {
                        when {
                            player.playbackState == Player.STATE_ENDED -> {
                                player.seekTo(0)
                                player.playWhenReady = true
                                player.prepare()
                                player.play()
                            }
                            player.isPlaying -> player.pause()
                            else -> {
                                player.playWhenReady = true
                                player.play()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(64.dp),
                            tint = AuroraCyan,
                        )
                    }
                    IconButton(onClick = {
                        player.seekTo((player.currentPosition + 10_000).coerceAtMost(player.duration))
                    }) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            modifier = Modifier.size(44.dp),
                            tint = TextPrimary,
                        )
                    }
            }

            GlassSurface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(50f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
                cornerRadius = 16.dp,
            ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
                        Slider(
                            value = if (isScrubbing) scrubPosition else progress,
                            onValueChange = {
                                isScrubbing = true
                                scrubPosition = it
                            },
                            onValueChangeFinished = {
                                val seekTo = (scrubPosition * durationMs).toLong()
                                player.seekTo(seekTo)
                                positionMs = seekTo
                                isScrubbing = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = AuroraCyan,
                                activeTrackColor = AuroraCyan,
                            ),
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatDuration(positionMs), style = MaterialTheme.typography.labelSmall)
                            Text("${playbackSpeed}x · ${formatDuration(durationMs)}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
        }

        BrightnessVolumeGestureLayer(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(30f),
            enabled = isActive && !controlsVisible,
        )
    }
}

