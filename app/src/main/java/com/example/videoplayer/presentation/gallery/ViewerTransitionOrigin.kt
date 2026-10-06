package com.example.videoplayer.presentation.gallery

data class ViewerTransitionOrigin(
    val centerXFraction: Float,
    val centerYFraction: Float,
    val widthFraction: Float,
    val heightFraction: Float,
) {
    val startScale: Float
        get() = widthFraction.coerceIn(0.08f, 0.95f)
}
