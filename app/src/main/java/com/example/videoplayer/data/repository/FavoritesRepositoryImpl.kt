package com.example.videoplayer.data.repository

import com.example.videoplayer.data.source.FavoritesPreferencesDataSource
import com.example.videoplayer.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class FavoritesRepositoryImpl(
    private val dataSource: FavoritesPreferencesDataSource,
) : FavoritesRepository {

    override fun observeFavoriteIds(): Flow<Set<Long>> = dataSource.favoriteIds

    override suspend fun addFavorite(mediaId: Long) {
        dataSource.add(mediaId)
    }

    override suspend fun removeFavorite(mediaId: Long) {
        dataSource.remove(mediaId)
    }

    override suspend fun toggleFavorite(mediaId: Long): Boolean = dataSource.toggle(mediaId)
}
