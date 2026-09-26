package com.example.videoplayer.data.repository

import com.example.videoplayer.data.source.MediaStoreDataSource
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepositoryImpl(
    private val mediaStoreDataSource: MediaStoreDataSource,
) : MediaRepository {

    override suspend fun loadAllMedia(): Result<List<GalleryMedia>> = withContext(Dispatchers.IO) {
        runCatching { mediaStoreDataSource.loadAllMedia() }
    }
}
