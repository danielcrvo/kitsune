package com.kitsune.app.data.media

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.kitsune.app.R
import com.kitsune.app.di.IoDispatcher
import com.kitsune.app.domain.model.DownloadedMediaFile
import com.kitsune.app.domain.repository.MediaLibraryRepository
import com.kitsune.app.domain.util.FileSizeFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreLibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MediaLibraryRepository {

    private data class MediaCollection(
        val contentUri: Uri,
        val folder: String,
        val publicDirectory: String,
        val isVideo: Boolean,
        val defaultName: String,
        val defaultMimeType: String
    )

    private val collections = listOf(
        MediaCollection(
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            folder = MediaFileUtils.VIDEO_FOLDER,
            publicDirectory = Environment.DIRECTORY_MOVIES,
            isVideo = true,
            defaultName = "Kitsune Video",
            defaultMimeType = "video/mp4"
        ),
        MediaCollection(
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            folder = MediaFileUtils.AUDIO_FOLDER,
            publicDirectory = Environment.DIRECTORY_MUSIC,
            isVideo = false,
            defaultName = "Kitsune Audio",
            defaultMimeType = "audio/mpeg"
        )
    )

    private val fileProviderAuthority = "${context.packageName}.fileprovider"

    override suspend fun getDownloadedFiles(): List<DownloadedMediaFile> = withContext(ioDispatcher) {
        val results = mutableListOf<DownloadedMediaFile>()
        val seenPaths = mutableSetOf<String>()

        collections.forEach { collection ->
            runCatching { queryCollection(collection, results, seenPaths) }
        }
        collections.forEach { collection ->
            runCatching { scanPublicFolder(collection, results, seenPaths) }
        }

        results.sortedByDescending { it.dateModified }
    }

    @Suppress("DEPRECATION")
    private fun queryCollection(
        collection: MediaCollection,
        outList: MutableList<DownloadedMediaFile>,
        seenPaths: MutableSet<String>
    ) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATA
        )

        context.contentResolver.query(
            collection.contentUri,
            projection,
            "${MediaStore.MediaColumns.DATA} LIKE ?",
            arrayOf("%/${collection.folder}/%"),
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = cursor.getString(nameCol) ?: collection.defaultName
                val dataPath = cursor.getString(dataCol)
                val file = dataPath?.takeIf { it.isNotBlank() }?.let(::File)
                val pathKey = file?.absolutePath ?: displayName
                if (!seenPaths.add(pathKey)) continue

                val size = cursor.getLong(sizeCol)
                outList.add(
                    DownloadedMediaFile(
                        id = id,
                        title = cursor.getString(titleCol) ?: displayName,
                        fileName = displayName,
                        uri = ContentUris.withAppendedId(collection.contentUri, id),
                        file = file,
                        sizeBytes = size,
                        sizeFormatted = FileSizeFormatter.format(size),
                        dateModified = cursor.getLong(dateCol) * 1000L,
                        isVideo = collection.isVideo,
                        mimeType = cursor.getString(mimeCol) ?: collection.defaultMimeType
                    )
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun scanPublicFolder(
        collection: MediaCollection,
        outList: MutableList<DownloadedMediaFile>,
        seenPaths: MutableSet<String>
    ) {
        val dir = File(Environment.getExternalStoragePublicDirectory(collection.publicDirectory), "Kitsune")
        if (!dir.isDirectory) return

        dir.listFiles()?.forEach { file ->
            if (!file.isFile || file.length() <= 0 || !seenPaths.add(file.absolutePath)) return@forEach

            val ext = file.extension.lowercase()
            val isVideo = collection.isVideo || MediaFileUtils.isVideoExtension(ext)
            val uri = runCatching { FileProvider.getUriForFile(context, fileProviderAuthority, file) }
                .getOrElse { Uri.fromFile(file) }

            outList.add(
                DownloadedMediaFile(
                    id = file.absolutePath.hashCode().toLong(),
                    title = file.nameWithoutExtension,
                    fileName = file.name,
                    uri = uri,
                    file = file,
                    sizeBytes = file.length(),
                    sizeFormatted = FileSizeFormatter.format(file.length()),
                    dateModified = file.lastModified(),
                    isVideo = isVideo,
                    mimeType = MediaFileUtils.fallbackMimeType(ext, isAudio = !isVideo)
                )
            )
        }
    }

    override fun openExternally(item: DownloadedMediaFile) {
        val playUri = item.file
            ?.takeIf { it.exists() }
            ?.let { runCatching { FileProvider.getUriForFile(context, fileProviderAuthority, it) }.getOrNull() }
            ?: item.uri

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(playUri, item.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(viewIntent, context.getString(R.string.player_external)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }

    override suspend fun delete(item: DownloadedMediaFile): Boolean = withContext(ioDispatcher) {
        var deleted = runCatching { context.contentResolver.delete(item.uri, null, null) > 0 }.getOrDefault(false)
        val file = item.file
        if (file != null && file.exists() && file.delete()) {
            deleted = true
        }
        deleted
    }

    override suspend fun rename(
        item: DownloadedMediaFile,
        newNameWithoutExtension: String
    ): Result<DownloadedMediaFile> = withContext(ioDispatcher) {
        runCatching {
            val cleanName = MediaFileUtils.sanitizeFileName(newNameWithoutExtension)
            require(cleanName.isNotBlank()) { "Invalid file name." }

            val ext = item.file?.extension ?: item.fileName.substringAfterLast('.', "")
            val fullNewName = if (ext.isNotBlank()) "$cleanName.$ext" else cleanName

            var newFile: File? = item.file
            val currentFile = item.file
            if (currentFile != null && currentFile.exists()) {
                val target = File(currentFile.parentFile, fullNewName)
                if (currentFile.renameTo(target)) newFile = target
            }

            runCatching {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullNewName)
                    put(MediaStore.MediaColumns.TITLE, cleanName)
                }
                context.contentResolver.update(item.uri, values, null, null)
            }

            item.copy(title = cleanName, fileName = fullNewName, file = newFile)
        }
    }
}
