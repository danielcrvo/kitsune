package com.kitsune.app.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.kitsune.app.R
import com.kitsune.app.ui.MainActivity
import java.io.File

object DownloadNotificationHelper {

    const val CHANNEL_ID = "kitsune_download_channel"
    const val NOTIFICATION_ID = 1001

    const val UPDATE_CHANNEL_ID = "kitsune_update_channel"
    const val UPDATE_NOTIFICATION_ID = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadChannel = NotificationChannel(
                CHANNEL_ID,
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

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(downloadChannel)
            manager.createNotificationChannel(updateChannel)
        }
    }

    fun buildProgressNotification(
        context: Context,
        title: String,
        statusText: String,
        progressPercent: Int,
        isIndeterminate: Boolean
    ): Notification {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(context, DownloadForegroundService::class.java).apply {
            action = DownloadForegroundService.ACTION_CANCEL_DOWNLOAD
        }
        val cancelPendingIntent = PendingIntent.getService(
            context,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(statusText)
            .setProgress(100, progressPercent, isIndeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(appPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.notif_cancel_action), cancelPendingIntent)
            .build()
    }

    fun buildCompletedNotification(
        context: Context,
        title: String,
        subtext: String,
        targetUri: Uri? = null,
        isAudioOnly: Boolean = false
    ): Notification {
        val clickPendingIntent = if (targetUri != null) {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(targetUri, if (isAudioOnly) "audio/*" else "video/*")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            PendingIntent.getActivity(
                context,
                2,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("${context.getString(R.string.notif_download_complete)}: $title")
            .setContentText(subtext)
            .setAutoCancel(true)
            .setContentIntent(clickPendingIntent)

        if (targetUri != null) {
            builder.addAction(android.R.drawable.ic_media_play, context.getString(R.string.btn_play), clickPendingIntent)
        }

        return builder.build()
    }

    fun buildAppUpdateAvailableNotification(
        context: Context,
        versionName: String
    ): Notification {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            201,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_notif_available_title, versionName))
            .setContentText(context.getString(R.string.update_notif_available_desc))
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .build()
    }

    fun buildAppUpdateProgressNotification(
        context: Context,
        versionName: String,
        progressPercent: Int,
        statusText: String
    ): Notification {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            202,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(context.getString(R.string.update_notif_downloading_title, versionName))
            .setContentText(statusText)
            .setProgress(100, progressPercent, progressPercent <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(appPendingIntent)
            .build()
    }

    fun buildAppUpdateReadyNotification(
        context: Context,
        versionName: String,
        apkFile: File
    ): Notification {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val installPendingIntent = PendingIntent.getActivity(
            context,
            203,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_notif_ready_title, versionName))
            .setContentText(context.getString(R.string.update_notif_ready_desc))
            .setAutoCancel(true)
            .setContentIntent(installPendingIntent)
            .addAction(android.R.drawable.stat_sys_download_done, context.getString(R.string.update_dialog_btn_install), installPendingIntent)
            .build()
    }
}
