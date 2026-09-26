package com.example.videoplayer.domain.model

data class WallpaperSettings(
    val imageUri: String? = null,
    val blurRadiusDp: Float = 18f,
    val usePhotoAppIcon: Boolean = false,
)
