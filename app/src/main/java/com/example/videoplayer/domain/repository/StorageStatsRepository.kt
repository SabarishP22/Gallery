package com.example.videoplayer.domain.repository

import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.StorageStats

interface StorageStatsRepository {
    suspend fun load(libraryMedia: List<GalleryMedia>): StorageStats
}
