package com.kitsune.app.domain.repository

import com.kitsune.app.domain.model.DownloadedMediaFile
import com.kitsune.app.domain.model.ExportedMedia
import java.io.File

interface MediaLibraryRepository {
    suspend fun getDownloadedFiles(): List<DownloadedMediaFile>
    suspend fun delete(item: DownloadedMediaFile): Boolean
    suspend fun rename(item: DownloadedMediaFile, newNameWithoutExtension: String): Result<DownloadedMediaFile>
    fun openExternally(item: DownloadedMediaFile)
}

interface MediaExporter {
    suspend fun export(sourceFile: File, title: String, isAudioOnly: Boolean): Result<ExportedMedia>
}
