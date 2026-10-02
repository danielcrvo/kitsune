package com.kitsune.app.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.AudioQuality
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.VideoQuality
import com.kitsune.app.ui.screens.DownloadMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kitsune_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val VIDEO_QUALITY = stringPreferencesKey("pref_video_quality")
        val AUDIO_CODEC = stringPreferencesKey("pref_audio_codec")
        val AUDIO_QUALITY = stringPreferencesKey("pref_audio_quality")
        val DOWNLOAD_MODE = stringPreferencesKey("pref_download_mode")
        val EMBED_SUBTITLES = booleanPreferencesKey("pref_embed_subtitles")
        val AUTO_CLIPBOARD = booleanPreferencesKey("pref_auto_clipboard")
    }

    val userConfigFlow: Flow<DownloadConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val qualityName = preferences[PreferencesKeys.VIDEO_QUALITY] ?: VideoQuality.AUTO.name
            val codecName = preferences[PreferencesKeys.AUDIO_CODEC] ?: AudioCodec.MP3.name
            val audioQualityName = preferences[PreferencesKeys.AUDIO_QUALITY] ?: AudioQuality.BEST.name
            val embedSubs = preferences[PreferencesKeys.EMBED_SUBTITLES] ?: false

            val quality = runCatching { VideoQuality.valueOf(qualityName) }.getOrDefault(VideoQuality.AUTO)
            val codec = runCatching { AudioCodec.valueOf(codecName) }.getOrDefault(AudioCodec.MP3)
            val audioQuality = runCatching { AudioQuality.valueOf(audioQualityName) }.getOrDefault(AudioQuality.BEST)

            DownloadConfig(
                quality = quality,
                audioOnly = false,
                muteAudio = false,
                audioCodec = codec,
                audioQuality = audioQuality,
                embedSubtitles = embedSubs
            )
        }

    val downloadModeFlow: Flow<DownloadMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeName = preferences[PreferencesKeys.DOWNLOAD_MODE] ?: DownloadMode.AUTO.name
            runCatching { DownloadMode.valueOf(modeName) }.getOrDefault(DownloadMode.AUTO)
        }

    suspend fun saveDownloadConfig(config: DownloadConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIDEO_QUALITY] = config.quality.name
            preferences[PreferencesKeys.AUDIO_CODEC] = config.audioCodec.name
            preferences[PreferencesKeys.AUDIO_QUALITY] = config.audioQuality.name
            preferences[PreferencesKeys.EMBED_SUBTITLES] = config.embedSubtitles
        }
    }

    suspend fun saveDownloadMode(mode: DownloadMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOWNLOAD_MODE] = mode.name
        }
    }
}
