# Changelog

All notable changes to the Kitsune project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-10-05

### Added
- Automated **Nightly Dev Builds** workflow on GitHub Actions generating multi-ABI split APKs (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`, and `universal`).
- Nocturnal pure AMOLED dark launcher icons with golden crescent moon and cyan starlight accents for development and Nightly builds.
- True black **AMOLED Mode** (`#000000`) toggle in settings with instant recomposition and persistent DataStore storage.
- In-app media player dialog (`KitsuneMediaPlayerDialog`) with Media3 ExoPlayer for videos and animated pulsing audio visualizer for tracks.
- Direct file sharing via Android Share Sheet (`ACTION_SEND`) directly from the media player.
- MultiPreview annotations (`@ThemePreviews`) for seamless component design inspection in Android Studio.
- Compose Compiler metrics and stability reports configuration in Gradle build.
- Dependabot configuration for automated dependency security audits.
- Unit test suite for `MainUiState`, `DownloadConfig`, and `UrlDetector`.

### Changed
- Replaced legacy naming across the entire codebase to **Kitsune** branding.
- Unified all color declarations to central design tokens (`KitsuneTheme.colors`).
- Refactored `MainScreen` to Unidirectional Data Flow / MVI action dispatching using sealed interface `MainUiAction`.
- Enhanced Android CI workflow with `lintDebug` static analysis and artifact uploads.

### Removed
- Removed all redundant code comments across Kotlin, XML, Gradle, and CI workflows following Clean Code self-documenting principles.

## [1.0.0] - 2026-10-02

### Added
- Initial public release of Kitsune on Android 8.0+ (API 26+).
- On-device media extraction powered by native NDK `yt-dlp` and `FFmpeg` binaries.
- Multi-platform URL support including YouTube, TikTok, Instagram, X (Twitter), Reddit, Bilibili, and SoundCloud.
- Background downloads via Android Foreground Service with real-time notification progress and cancel actions.
- Automatic Scoped Storage indexing into Android MediaStore (`Movies/Kitsune` and `Music/Kitsune`).
- Atomic Design System in Jetpack Compose (BOM 2025.02.00) with reactive fox mascot animations.
