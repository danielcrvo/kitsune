package com.kitsune.app.core.storage

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.kitsune.app.R
import com.kitsune.app.core.model.DownloadedMediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DownloadedFilesRepository {

    suspend fun getDownloadedFiles(context: Context): List<DownloadedMediaFile> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<DownloadedMediaFile>()
        val seenPaths = mutableSetOf<String>()

        try {
            queryMediaStoreVideos(context, resultList, seenPaths)
        } catch (_: Throwable) {}

        try {
            queryMediaStoreAudios(context, resultList, seenPaths)
        } catch (_: Throwable) {}

        try {
            scanFilesystemDirectory(
                context,
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Kitsune"),
                isVideo = true,
                resultList,
                seenPaths
            )
        } catch (_: Throwable) {}

        try {
            scanFilesystemDirectory(
                context,
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Kitsune"),
                isVideo = false,
                resultList,
                seenPaths
            )
        } catch (_: Throwable) {}

        resultList.sortedByDescending { it.dateModified }
    }

    private fun queryMediaStoreVideos(
        context: Context,
        outList: MutableList<DownloadedMediaFile>,
        seenPaths: MutableSet<String>
    ) {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATA
        )

        val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/Movies/Kitsune/%")

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = cursor.getString(nameCol) ?: "Kitsune Video"
                val title = cursor.getString(titleCol) ?: displayName
                val size = cursor.getLong(sizeCol)
                val date = cursor.getLong(dateCol) * 1000L
                val mime = cursor.getString(mimeCol) ?: "video/mp4"
                val dataPath = cursor.getString(dataCol)

                val file = if (!dataPath.isNullOrBlank()) File(dataPath) else null
                val pathKey = file?.absolutePath ?: displayName

                if (seenPaths.add(pathKey)) {
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    outList.add(
                        DownloadedMediaFile(
                            id = id,
                            title = title,
                            fileName = displayName,
                            uri = uri,
                            file = file,
                            sizeBytes = size,
                            sizeFormatted = formatFileSize(size),
                            dateModified = date,
                            isVideo = true,
                            mimeType = mime
                        )
                    )
                }
            }
        }
    }

    private fun queryMediaStoreAudios(
        context: Context,
        outList: MutableList<DownloadedMediaFile>,
        seenPaths: MutableSet<String>
    ) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/Music/Kitsune/%")

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = cursor.getString(nameCol) ?: "Kitsune Audio"
                val title = cursor.getString(titleCol) ?: displayName
                val size = cursor.getLong(sizeCol)
                val date = cursor.getLong(dateCol) * 1000L
                val mime = cursor.getString(mimeCol) ?: "audio/mpeg"
                val dataPath = cursor.getString(dataCol)

                val file = if (!dataPath.isNullOrBlank()) File(dataPath) else null
                val pathKey = file?.absolutePath ?: displayName

                if (seenPaths.add(pathKey)) {
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    outList.add(
                        DownloadedMediaFile(
                            id = id,
                            title = title,
                            fileName = displayName,
                            uri = uri,
                            file = file,
                            sizeBytes = size,
                            sizeFormatted = formatFileSize(size),
                            dateModified = date,
                            isVideo = false,
                            mimeType = mime
                        )
                    )
                }
            }
        }
    }

    private fun scanFilesystemDirectory(
        context: Context,
        dir: File,
        isVideo: Boolean,
        outList: MutableList<DownloadedMediaFile>,
        seenPaths: MutableSet<String>
    ) {
        if (!dir.exists() || !dir.isDirectory) return

        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.length() > 0 && seenPaths.add(file.absolutePath)) {
                val ext = file.extension.lowercase()
                val isVid = isVideo || ext in listOf("mp4", "mkv", "webm", "avi", "mov")
                val mime = if (isVid) "video/mp4" else if (ext == "mp3") "audio/mpeg" else "audio/*"
                val safeUri = try {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } catch (_: Throwable) {
                    Uri.fromFile(file)
                }

                outList.add(
                    DownloadedMediaFile(
                        id = file.hashCode().toLong(),
                        title = file.nameWithoutExtension,
                        fileName = file.name,
                        uri = safeUri,
                        file = file,
                        sizeBytes = file.length(),
                        sizeFormatted = formatFileSize(file.length()),
                        dateModified = file.lastModified(),
                        isVideo = isVid,
                        mimeType = mime
                    )
                )
            }
        }
    }

    fun playFile(context: Context, item: DownloadedMediaFile) {
        val playUri = if (item.file != null && item.file.exists()) {
            try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", item.file)
            } catch (_: Throwable) {
                item.uri
            }
        } else {
            item.uri
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(playUri, item.mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.player_external)))
        } catch (_: Throwable) {}
    }

    suspend fun deleteFile(context: Context, item: DownloadedMediaFile): Boolean = withContext(Dispatchers.IO) {
        var deleted = false
        try {
            val rows = context.contentResolver.delete(item.uri, null, null)
            if (rows > 0) deleted = true
        } catch (_: Throwable) {}

        if (item.file != null && item.file.exists()) {
            val fileDeleted = item.file.delete()
            if (fileDeleted) deleted = true
        }
        deleted
    }

    suspend fun renameFile(
        context: Context,
        item: DownloadedMediaFile,
        newNameWithoutExt: String
    ): Result<DownloadedMediaFile> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanName = newNameWithoutExt.trim().replace(Regex("[^a-zA-Z0-9._ -]"), "_")
            if (cleanName.isBlank()) throw IllegalArgumentException("Invalid file name.")

            val ext = item.file?.extension ?: item.fileName.substringAfterLast('.', "")
            val fullNewName = if (ext.isNotBlank()) "$cleanName.$ext" else cleanName

            var newFile: File? = item.file
            if (item.file != null && item.file.exists()) {
                val target = File(item.file.parentFile, fullNewName)
                if (item.file.renameTo(target)) {
                    newFile = target
                }
            }

            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullNewName)
                    put(MediaStore.MediaColumns.TITLE, cleanName)
                }
                context.contentResolver.update(item.uri, values, null, null)
            } catch (_: Throwable) {}

            item.copy(
                title = cleanName,
                fileName = fullNewName,
                file = newFile
            )
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
