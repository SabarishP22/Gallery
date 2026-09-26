package com.example.videoplayer.data.repository

import android.net.Uri
import android.os.Build
import com.example.videoplayer.data.source.MediaStoreDataSource
import com.example.videoplayer.domain.model.GalleryMedia
import com.example.videoplayer.domain.model.TrashDeleteRequest
import com.example.videoplayer.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepositoryImpl(
    private val mediaStoreDataSource: MediaStoreDataSource,
) : MediaRepository {

    override suspend fun loadAllMedia(): Result<List<GalleryMedia>> = withContext(Dispatchers.IO) {
        runCatching { mediaStoreDataSource.loadAllMedia() }
    }

    override suspend fun requestTrashDelete(uris: List<Uri>): Result<TrashDeleteRequest> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val pending = mediaStoreDataSource.createTrashPendingIntent(uris)
                        ?: error("Could not open system trash dialog.")
                    TrashDeleteRequest.SystemConfirmation(pending.intentSender)
                } else {
                    mediaStoreDataSource.moveToTrash(uris).getOrThrow()
                    TrashDeleteRequest.Completed
                }
            }
        }
}