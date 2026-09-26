package com.example.videoplayer.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.videoplayer.domain.model.WallpaperSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wallpaperDataStore: DataStore<Preferences> by preferencesDataStore(name = "wallpaper")

class WallpaperPreferencesDataSource(
    private val context: Context,
) {

    val settings: Flow<WallpaperSettings> = context.wallpaperDataStore.data.map { prefs ->
        WallpaperSettings(
            imageUri = prefs[KEY_URI]?.takeIf { it.isNotBlank() },
            blurRadiusDp = prefs[KEY_BLUR] ?: 18f,
            contentRevision = prefs[KEY_REVISION] ?: 0L,
        )
    }

    suspend fun save(uri: String, blurRadiusDp: Float): Long {
        var revision = 0L
        context.wallpaperDataStore.edit { prefs ->
            revision = (prefs[KEY_REVISION] ?: 0L) + 1L
            prefs[KEY_REVISION] = revision
            prefs[KEY_URI] = uri
            prefs[KEY_BLUR] = blurRadiusDp.coerceIn(0f, 40f)
        }
        return revision
    }

    suspend fun clear(): Long {
        var revision = 0L
        context.wallpaperDataStore.edit { prefs ->
            revision = (prefs[KEY_REVISION] ?: 0L) + 1L
            prefs[KEY_REVISION] = revision
            prefs.remove(KEY_URI)
            prefs.remove(KEY_BLUR)
        }
        return revision
    }

    companion object {
        private val KEY_URI = stringPreferencesKey("wallpaper_uri")
        private val KEY_BLUR = floatPreferencesKey("wallpaper_blur_dp")
        private val KEY_REVISION = longPreferencesKey("wallpaper_content_revision")
    }
}
