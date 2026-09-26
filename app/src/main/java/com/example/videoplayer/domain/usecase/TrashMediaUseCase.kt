package com.example.videoplayer.domain.usecase

import android.net.Uri
import com.example.videoplayer.domain.model.TrashDeleteRequest
import com.example.videoplayer.domain.repository.MediaRepository

class TrashMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(uris: List<Uri>): Result<TrashDeleteRequest> {
        return mediaRepository.requestTrashDelete(uris)
    }
}
