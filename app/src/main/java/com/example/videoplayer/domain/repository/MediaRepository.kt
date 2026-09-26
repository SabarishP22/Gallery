package com.example.videoplayer.domain.repository

import com.example.videoplayer.domain.model.GalleryMedia

interface MediaRepository {
    suspend fun loadAllMedia(): Result<List<GalleryMedia>>
}
