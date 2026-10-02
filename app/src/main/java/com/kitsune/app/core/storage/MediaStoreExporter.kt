package com.kitsune.app.core.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

object MediaStoreExporter {


    suspend fun exportToGallery(
        context: Context,
        sourceFile: File,
        title: String,
        isAudioOnly: Boolean
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val extension = sourceFile.extension.ifBlank { if (isAudioOnly) "m4a" else "mp4" }
            val mimeType = if (isAudioOnly) "audio/*" else "video/mp4"
            val displayName = "${title.take(60).replace(Regex("[^a-zA-Z0-9._ -]"), "_")}.$extension"

            val contentUri = if (isAudioOnly) {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val subFolder = if (isAudioOnly) "Music/Kitsune" else "Movies/Kitsune"
                    put(MediaStore.MediaColumns.RELATIVE_PATH, subFolder)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val targetUri = resolver.insert(contentUri, values)
                ?: throw IllegalStateException("Falha ao criar entrada no MediaStore")

            resolver.openOutputStream(targetUri)?.use { output ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Não foi possível abrir OutputStream para gravação do arquivo")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(targetUri, values, null, null)
            }


            sourceFile.delete()

            targetUri
        }
    }
}
