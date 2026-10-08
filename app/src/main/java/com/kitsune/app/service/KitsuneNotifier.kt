package com.kitsune.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.kitsune.app.R
import com.kitsune.app.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KitsuneNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val DOWNLOAD_CHANNEL_ID = "kitsune_download_channel"
        const val UPDATE_CHANNEL_ID = "kitsune_update_channel"
        const val DOWNLOAD_NOTIFICATION_ID = 1001
        const val UPDATE_NOTIFICATION_ID = 2001
        private const val COMPLETED_NOTIFICATION_BASE_ID = 3000

        private const val REQUEST_OPEN_APP = 0
        private const val REQUEST_CANCEL_DOWNLOAD = 1
        private const val REQUEST_OPEN_MEDIA = 2
        private const val REQUEST_UPDATE_AVAILABLE = 201
        private const val REQUEST_UPDATE_PROGRESS = 202
        private const val REQUEST_UPDATE_INSTALL = 203
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val completedNotificationIds = AtomicInteger(COMPLETED_NOTIFICATION_BASE_ID)

    fun createChannels() {
        val downloadChannel = NotificationChannel(
            DOWNLOAD_CHANNEL_ID,
            context.getString(R.string.download_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.download_channel_desc)
            setShowBadge(false)
        }

        val updateChannel = NotificationChannel(
            UPDATE_CHANNEL_ID,
            context.getString(R.string.update_notif_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.update_notif_channel_desc)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannel(downloadChannel)
        notificationManager.createNotificationChannel(updateChannel)
    }

    fun buildDownloadProgress(
        title: String,
        statusText: String,
        progressPercent: Int,
        isIndeterminate: Boolean
    ): Notification {
        val cancelIntent = Intent(context, DownloadForegroundService::class.java)
            .setAction(DownloadForegroundService.ACTION_CANCEL_ALL)
        val cancelPendingIntent = PendingIntent.getService(
            context,
            REQUEST_CANCEL_DOWNLOAD,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(statusText)
            .setProgress(100, progressPercent, isIndeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent(REQUEST_OPEN_APP))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.notif_cancel_action),
                cancelPendingIntent
            )
            .build()
    }

    fun showDownloadProgress(title: String, statusText: String, progressPercent: Int, isIndeterminate: Boolean) {
        notificationManager.notify(
            DOWNLOAD_NOTIFICATION_ID,
            buildDownloadProgress(title, statusText, progressPercent, isIndeterminate)
        )
    }

    fun showDownloadCompleted(title: String, subtext: String, targetUri: Uri?, isAudioOnly: Boolean) {
        val clickPendingIntent = if (targetUri != null) {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(targetUri, if (isAudioOnly) "audio/*" else "video/*")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            PendingIntent.getActivity(
                context,
                REQUEST_OPEN_MEDIA,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            openAppIntent(REQUEST_OPEN_APP)
        }

        val builder = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("${context.getString(R.string.notif_download_complete)}: $title")
            .setContentText(subtext)
            .setAutoCancel(true)
            .setContentIntent(clickPendingIntent)

        if (targetUri != null) {
            builder.addAction(android.R.drawable.ic_media_play, context.getString(R.string.btn_play), clickPendingIntent)
        }

        notificationManager.notify(completedNotificationIds.incrementAndGet(), builder.build())
    }

    fun showUpdateAvailable(versionName: String) {
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_notif_available_title, versionName))
            .setContentText(context.getString(R.string.update_notif_available_desc))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(REQUEST_UPDATE_AVAILABLE))
            .build()
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun showUpdateProgress(versionName: String, progressPercent: Int, statusText: String) {
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(context.getString(R.string.update_notif_downloading_title, versionName))
            .setContentText(statusText)
            .setProgress(100, progressPercent, progressPercent <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent(REQUEST_UPDATE_PROGRESS))
            .build()
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun showUpdateReady(versionName: String, apkFile: File) {
        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val installPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_UPDATE_INSTALL,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_notif_ready_title, versionName))
            .setContentText(context.getString(R.string.update_notif_ready_desc))
            .setAutoCancel(true)
            .setContentIntent(installPendingIntent)
            .addAction(
                android.R.drawable.stat_sys_download_done,
                context.getString(R.string.update_dialog_btn_install),
                installPendingIntent
            )
            .build()
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun cancelUpdateNotification() {
        notificationManager.cancel(UPDATE_NOTIFICATION_ID)
    }

    private fun openAppIntent(requestCode: Int): PendingIntent {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
