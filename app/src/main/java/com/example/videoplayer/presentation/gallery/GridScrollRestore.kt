package com.example.videoplayer.presentation.gallery

import com.example.videoplayer.domain.model.MediaFilter

data class GridScrollRestore(
    val rowIndex: Int,
    val scrollOffset: Int = 0,
    val filter: MediaFilter,
    val favoritesMode: Boolean,
)
