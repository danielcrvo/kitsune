package com.kitsune.app

import android.app.Application
import android.util.Log
import com.kitsune.app.core.engine.YtDlpEngine
import com.kitsune.app.core.service.DownloadNotificationHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class KitsuneApp : Application() {

    companion object {
        private const val TAG = "KitsuneApp"
    }

    override fun onCreate() {
        super.onCreate()


        setupCrashHandler()


        try {
            DownloadNotificationHelper.createNotificationChannel(this)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to create notification channels", t)
        }


        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Uncaught exception in initialization coroutine", throwable)
        }

        CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler).launch {
            try {
                Log.d(TAG, "Triggering background pre-initialization of YtDlpEngine...")
                YtDlpEngine.initialize(this@KitsuneApp)
            } catch (t: Throwable) {
                Log.e(TAG, "Safe failure during YtDlpEngine background warmup", t)
            }
        }
    }


    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "CRASH DETECTED on thread ${thread.name}: ${throwable.message}", throwable)
                val stringWriter = StringWriter()
                throwable.printStackTrace(PrintWriter(stringWriter))
                val stackTrace = stringWriter.toString()

                val crashFile = File(filesDir, "last_crash.txt")
                crashFile.writeText("Thread: ${thread.name}\nTimestamp: ${System.currentTimeMillis()}\nError: ${throwable.localizedMessage}\n\n$stackTrace")
            } catch (loggingErr: Throwable) {
                Log.e(TAG, "Failed to persist crash report", loggingErr)
            }

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
