package com.example.videoplayer.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(): Flow<Set<Long>>
    suspend fun addFavorite(mediaId: Long)
    suspend fun removeFavorite(mediaId: Long)
    suspend fun toggleFavorite(mediaId: Long): Boolean
}
