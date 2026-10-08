package com.kitsune.app.domain.repository

import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadProgress
import com.kitsune.app.domain.model.EngineUpdateResult
import com.kitsune.app.domain.model.MediaInfo
import com.kitsune.app.domain.model.PlaylistInfo
import java.io.File

interface MediaEngine {
    suspend fun warmUp(): Result<Unit>
    suspend fun fetchMediaInfo(url: String): Result<MediaInfo>
    suspend fun fetchPlaylistInfo(url: String): Result<PlaylistInfo>
    suspend fun download(
        url: String,
        config: DownloadConfig,
        outputDir: File,
        onProgress: (DownloadProgress) -> Unit
    ): Result<File>
    suspend fun engineVersion(): String
    suspend fun updateEngine(): EngineUpdateResult
}
