package com.kitsune.app.data.media

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.kitsune.app.di.IoDispatcher
import com.kitsune.app.domain.model.ExportedMedia
import com.kitsune.app.domain.repository.MediaExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class MediaStoreExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MediaExporter {

    private companion object {
        const val MEDIA_SCAN_TIMEOUT_MS = 10_000L
    }

    override suspend fun export(
        sourceFile: File,
        title: String,
        isAudioOnly: Boolean
    ): Result<ExportedMedia> = withContext(ioDispatcher) {
        try {
            runCatching {
                val extension = sourceFile.extension.lowercase()
                    .ifBlank { if (isAudioOnly) "m4a" else "mp4" }
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?.takeIf { it.startsWith(if (isAudioOnly) "audio/" else "video/") }
                    ?: MediaFileUtils.fallbackMimeType(extension, isAudioOnly)
                val baseName = MediaFileUtils.sanitizeFileName(title)
                    .ifBlank { MediaFileUtils.sanitizeFileName(sourceFile.nameWithoutExtension) }
                    .ifBlank { "Kitsune" }
                val sizeBytes = sourceFile.length()

                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportScoped(sourceFile, "$baseName.$extension", mimeType, isAudioOnly)
                } else {
                    exportLegacy(sourceFile, baseName, extension, mimeType, isAudioOnly)
                }
                ExportedMedia(contentUri = uri.toString(), displayName = baseName, sizeBytes = sizeBytes)
            }
        } finally {
            sourceFile.delete()
        }
    }

    private fun exportScoped(
        sourceFile: File,
        displayName: String,
        mimeType: String,
        isAudioOnly: Boolean
    ): Uri {
        val resolver = context.contentResolver
        val collection = if (isAudioOnly) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, MediaFileUtils.relativeFolder(isAudioOnly))
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val targetUri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Failed to insert record into MediaStore")

        try {
            resolver.openOutputStream(targetUri)?.use { output ->
                FileInputStream(sourceFile).use { input -> input.copyTo(output) }
            } ?: throw IllegalStateException("Could not open OutputStream for media export")

            val publishValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(targetUri, publishValues, null, null)
        } catch (t: Throwable) {
            runCatching { resolver.delete(targetUri, null, null) }
            throw t
        }
        return targetUri
    }

    @Suppress("DEPRECATION")
    private suspend fun exportLegacy(
        sourceFile: File,
        baseName: String,
        extension: String,
        mimeType: String,
        isAudioOnly: Boolean
    ): Uri {
        val publicRoot = Environment.getExternalStoragePublicDirectory(
            if (isAudioOnly) Environment.DIRECTORY_MUSIC else Environment.DIRECTORY_MOVIES
        )
        val targetDir = File(publicRoot, "Kitsune")
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw IllegalStateException("Could not create ${targetDir.absolutePath}")
        }

        val targetFile = MediaFileUtils.uniqueFile(targetDir, baseName, extension)
        try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output -> input.copyTo(output) }
            }
        } catch (t: Throwable) {
            targetFile.delete()
            throw t
        }

        val scannedUri = withTimeoutOrNull(MEDIA_SCAN_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf(mimeType)
                ) { _, uri ->
                    if (continuation.isActive) continuation.resume(uri)
                }
            }
        }

        return scannedUri ?: FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetFile
        )
    }
}
