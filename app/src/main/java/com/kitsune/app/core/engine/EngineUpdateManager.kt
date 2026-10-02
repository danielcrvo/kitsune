package com.kitsune.app.core.engine

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object EngineUpdateManager {

    private const val TAG = "EngineUpdateManager"

    sealed interface UpdateStatus {
        data object Checking : UpdateStatus
        data class Updated(val version: String) : UpdateStatus
        data object AlreadyUpToDate : UpdateStatus
        data class Error(val error: String) : UpdateStatus
    }

    suspend fun checkForUpdates(context: Context): UpdateStatus = withContext(Dispatchers.IO) {
        try {
            val initRes = YtDlpEngine.ensureInitialized(context)
            if (initRes.isFailure) {
                return@withContext UpdateStatus.Error(
                    initRes.exceptionOrNull()?.localizedMessage ?: "Falha ao inicializar engine local."
                )
            }
            val status = YoutubeDL.getInstance().updateYoutubeDL(
                context.applicationContext,
                YoutubeDL.UpdateChannel.STABLE
            )
            when (status) {
                YoutubeDL.UpdateStatus.DONE -> {
                    val currentVersion = YoutubeDL.getInstance().version(context) ?: "atualizado"
                    UpdateStatus.Updated(currentVersion)
                }
                YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> {
                    UpdateStatus.AlreadyUpToDate
                }
                else -> UpdateStatus.AlreadyUpToDate
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Falha na verificação de atualização da engine", t)
            val rawMsg = t.message ?: t.localizedMessage ?: ""
            val userFriendlyError = when {
                rawMsg.contains("403", ignoreCase = true) || rawMsg.contains("rate limit", ignoreCase = true) ->
                    "Limite temporário de requisições do GitHub atingido. Tente novamente mais tarde."
                rawMsg.contains("timeout", ignoreCase = true) || rawMsg.contains("connect", ignoreCase = true) ->
                    "Tempo limite de conexão esgotado. Verifique sua conexão com a internet."
                rawMsg.isNotBlank() && !rawMsg.matches(Regex("""^[a-zA-Z0-9_.$]+$""")) ->
                    rawMsg
                else ->
                    "Não foi possível baixar a atualização no momento. Verifique sua conexão."
            }
            UpdateStatus.Error(userFriendlyError)
        }
    }

    suspend fun getEngineVersion(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val initRes = YtDlpEngine.ensureInitialized(context)
            if (initRes.isFailure) return@withContext "N/A"
            YoutubeDL.getInstance().version(context.applicationContext) ?: "N/A"
        } catch (_: Throwable) {
            "Desconhecida"
        }
    }
}
