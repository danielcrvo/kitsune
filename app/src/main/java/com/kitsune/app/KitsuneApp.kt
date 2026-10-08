package com.kitsune.app

import android.app.Application
import android.util.Log
import com.kitsune.app.di.ApplicationScope
import com.kitsune.app.domain.repository.MediaEngine
import com.kitsune.app.service.KitsuneNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

@HiltAndroidApp
class KitsuneApp : Application() {

    companion object {
        private const val TAG = "KitsuneApp"
        private const val CRASH_LOG_FILE_NAME = "last_crash.txt"
    }

    @Inject lateinit var notifier: KitsuneNotifier
    @Inject lateinit var mediaEngine: MediaEngine
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()

        runCatching { notifier.createChannels() }
            .onFailure { Log.e(TAG, "Failed to create notification channels", it) }

        applicationScope.launch {
            mediaEngine.warmUp().onFailure { Log.e(TAG, "Engine warmup failed", it) }
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "CRASH DETECTED on thread ${thread.name}: ${throwable.message}", throwable)
                val stackTrace = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
                File(filesDir, CRASH_LOG_FILE_NAME).writeText(
                    "Thread: ${thread.name}\nTimestamp: ${System.currentTimeMillis()}\n" +
                        "Error: ${throwable.localizedMessage}\n\n$stackTrace"
                )
            } catch (loggingErr: Throwable) {
                Log.e(TAG, "Failed to persist crash report", loggingErr)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
