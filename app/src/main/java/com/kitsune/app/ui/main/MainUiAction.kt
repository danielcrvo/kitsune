package com.kitsune.app.ui.main

interface MainUiAction

enum class MainOverlay {
    NONE,
    SETTINGS,
    HISTORY,
    SUPPORTED_SERVICES,
    TERMS,
    ABOUT
}

data class ShowOverlay(val overlay: MainOverlay) : MainUiAction
