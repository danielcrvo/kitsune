package com.kitsune.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kitsune.app.domain.model.AudioCodec
import com.kitsune.app.domain.model.AudioQuality
import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import com.kitsune.app.domain.model.VideoQuality
import com.kitsune.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorePreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private object Keys {
        val VIDEO_QUALITY = stringPreferencesKey("pref_video_quality")
        val AUDIO_CODEC = stringPreferencesKey("pref_audio_codec")
        val AUDIO_QUALITY = stringPreferencesKey("pref_audio_quality")
        val DOWNLOAD_MODE = stringPreferencesKey("pref_download_mode")
        val EMBED_SUBTITLES = booleanPreferencesKey("pref_embed_subtitles")
        val AMOLED_THEME = booleanPreferencesKey("pref_amoled_theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("pref_dynamic_color")
        val WIFI_ONLY = booleanPreferencesKey("pref_wifi_only")
        val AUTO_CHECK_UPDATES = booleanPreferencesKey("pref_auto_check_updates")
    }

    private val preferences: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    private fun <T> preference(transform: (Preferences) -> T): Flow<T> =
        preferences.map(transform).distinctUntilChanged()

    override val downloadConfig: Flow<DownloadConfig> = preference { prefs ->
        DownloadConfig(
            quality = enumValueOrDefault(prefs[Keys.VIDEO_QUALITY], VideoQuality.AUTO),
            audioCodec = enumValueOrDefault(prefs[Keys.AUDIO_CODEC], AudioCodec.MP3),
            audioQuality = enumValueOrDefault(prefs[Keys.AUDIO_QUALITY], AudioQuality.BEST),
            embedSubtitles = prefs[Keys.EMBED_SUBTITLES] ?: false
        )
    }

    override val downloadMode: Flow<DownloadMode> = preference { prefs ->
        enumValueOrDefault(prefs[Keys.DOWNLOAD_MODE], DownloadMode.AUTO)
    }

    override val isAmoledTheme: Flow<Boolean> = preference { it[Keys.AMOLED_THEME] ?: false }

    override val isDynamicColor: Flow<Boolean> = preference { it[Keys.DYNAMIC_COLOR] ?: false }

    override val isWifiOnly: Flow<Boolean> = preference { it[Keys.WIFI_ONLY] ?: false }

    override val isAutoCheckUpdates: Flow<Boolean> = preference { it[Keys.AUTO_CHECK_UPDATES] ?: true }

    override suspend fun saveDownloadConfig(config: DownloadConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.VIDEO_QUALITY] = config.quality.name
            prefs[Keys.AUDIO_CODEC] = config.audioCodec.name
            prefs[Keys.AUDIO_QUALITY] = config.audioQuality.name
            prefs[Keys.EMBED_SUBTITLES] = config.embedSubtitles
        }
    }

    override suspend fun saveDownloadMode(mode: DownloadMode) {
        dataStore.edit { it[Keys.DOWNLOAD_MODE] = mode.name }
    }

    override suspend fun setAmoledTheme(enabled: Boolean) {
        dataStore.edit { it[Keys.AMOLED_THEME] = enabled }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setWifiOnly(enabled: Boolean) {
        dataStore.edit { it[Keys.WIFI_ONLY] = enabled }
    }

    override suspend fun setAutoCheckUpdates(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_CHECK_UPDATES] = enabled }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
