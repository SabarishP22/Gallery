package com.example.videoplayer.domain.model

sealed class ViewerListContext {
    data class Tab(val filter: MediaFilter) : ViewerListContext()
    data object Favorites : ViewerListContext()
}
