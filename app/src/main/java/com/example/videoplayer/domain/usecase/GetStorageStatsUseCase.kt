package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.StorageStats
import com.example.videoplayer.domain.repository.StorageStatsRepository

class GetStorageStatsUseCase(
    private val storageStatsRepository: StorageStatsRepository,
) {
    suspend operator fun invoke(libraryMedia: List<GalleryMedia>): Result<StorageStats> {
        return runCatching { storageStatsRepository.load(libraryMedia) }
    }
}
