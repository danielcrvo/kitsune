package com.kitsune.app.domain.model

data class DownloadProgress(
    val progress: Float,
    val speed: String,
    val eta: String,
    val stage: DownloadStage
)

sealed interface EngineUpdateResult {
    data class Updated(val version: String) : EngineUpdateResult
    data object AlreadyUpToDate : EngineUpdateResult
    data class Error(val message: String) : EngineUpdateResult
}
