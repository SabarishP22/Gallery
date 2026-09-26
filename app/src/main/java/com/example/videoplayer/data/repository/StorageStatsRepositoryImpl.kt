package com.example.videoplayer.data.repository

import com.example.videoplayer.data.source.StorageStatsDataSource
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.StorageStats
import com.example.videoplayer.domain.repository.StorageStatsRepository

class StorageStatsRepositoryImpl(
    private val dataSource: StorageStatsDataSource,
) : StorageStatsRepository {

    override suspend fun load(libraryMedia: List<GalleryMedia>): StorageStats {
        return dataSource.load(libraryMedia)
    }
}
