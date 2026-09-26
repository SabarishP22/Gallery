package com.example.videoplayer.domain.usecase

import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.repository.MediaRepository

class LoadGalleryMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(): Result<List<GalleryMedia>> = mediaRepository.loadAllMedia()
}
