package com.example.videoplayer.util

import android.content.Context
import android.view.MotionEvent
import androidx.media3.ui.PlayerView

/** Lets Compose overlays receive every tap; ExoPlayer [PlayerView] otherwise consumes touches. */
class NonTouchPlayerView(context: Context) : PlayerView(context) {
    override fun onTouchEvent(event: MotionEvent): Boolean = false
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = false
}
