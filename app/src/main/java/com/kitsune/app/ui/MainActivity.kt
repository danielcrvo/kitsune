package com.kitsune.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kitsune.app.ui.screens.MainScreen
import com.kitsune.app.ui.theme.KitsuneTheme

class MainActivity : ComponentActivity() {

    private val sharedUrlState = mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)

        runCatching { checkNotificationPermission() }

        setContent {
            KitsuneTheme {
                val currentSharedUrl by sharedUrlState
                MainScreen(
                    initialSharedUrl = currentSharedUrl,
                    onClearSharedUrl = { sharedUrlState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                        ?: intent.getStringExtra(Intent.EXTRA_STREAM)
                    extractAndSetUrl(text)
                }
            }
            Intent.ACTION_VIEW -> {
                val data = intent.dataString
                if (!data.isNullOrBlank()) {
                    extractAndSetUrl(data)
                }
            }
        }
    }

    private fun extractAndSetUrl(rawText: String?) {
        if (rawText.isNullOrBlank()) return
        val urlRegex = Regex("""https?://[^\s]+""")
        val match = urlRegex.find(rawText)
        val extracted = match?.value ?: rawText.trim()
        sharedUrlState.value = extracted
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}
