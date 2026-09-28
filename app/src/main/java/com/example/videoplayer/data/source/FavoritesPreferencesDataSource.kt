package com.example.videoplayer.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class FavoritesPreferencesDataSource(
    private val context: Context,
) {
    val favoriteIds: Flow<Set<Long>> = context.favoritesDataStore.data.map { prefs ->
        prefs[KEY_IDS].orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    }

    suspend fun add(id: Long) {
        context.favoritesDataStore.edit { prefs ->
            val next = prefs[KEY_IDS].orEmpty().toMutableSet()
            next.add(id.toString())
            prefs[KEY_IDS] = next
        }
    }

    suspend fun remove(id: Long) {
        context.favoritesDataStore.edit { prefs ->
            val next = prefs[KEY_IDS].orEmpty().toMutableSet()
            next.remove(id.toString())
            prefs[KEY_IDS] = next
        }
    }

    suspend fun toggle(id: Long): Boolean {
        var nowFavorite = false
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[KEY_IDS].orEmpty().toMutableSet()
            val key = id.toString()
            nowFavorite = if (key in current) {
                current.remove(key)
                false
            } else {
                current.add(key)
                true
            }
            prefs[KEY_IDS] = current
        }
        return nowFavorite
    }

    companion object {
        private val KEY_IDS = stringSetPreferencesKey("favorite_media_ids")
    }
}
