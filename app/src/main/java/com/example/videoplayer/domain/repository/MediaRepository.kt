package com.example.videoplayer.domain.repository

import android.net.Uri
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.TrashDeleteRequest

interface MediaRepository {
    suspend fun loadAllMedia(): Result<List<GalleryMedia>>
    suspend fun requestTrashDelete(uris: List<Uri>): Result<TrashDeleteRequest>
}
