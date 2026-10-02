package com.kitsune.app.ui.components.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun CobaltScreenTemplate(
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    activeDownloadSection: (@Composable () -> Unit)? = null,
    settingsSheet: (@Composable () -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KitsuneTheme.colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = KitsuneTheme.spacing.lg)
                .padding(bottom = KitsuneTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(KitsuneTheme.spacing.xl))


            header()

            Spacer(modifier = Modifier.height(KitsuneTheme.spacing.xl))


            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }


            if (activeDownloadSection != null) {
                Spacer(modifier = Modifier.height(KitsuneTheme.spacing.lg))
                Box(modifier = Modifier.fillMaxWidth()) {
                    activeDownloadSection()
                }
            }
        }


        settingsSheet?.invoke()
    }
}
