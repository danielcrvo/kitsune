# Changelog

All notable changes to the Kitsune project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.1] - 2026-10-05

### Added
- Sequential **Download Queue** system processing queued items in background with pending count indicator and queue management.
- Multi-item **Playlist Support** with metadata pre-fetching, individual track selection dialog (`PlaylistSelectionDialog`), duration calculation, and batch queue dispatch.
- **Wi-Fi Only Mode** toggle backed by persistent DataStore preferences and dynamic `NetworkMonitor` connectivity state observation.
- Dynamic Theming (**Material You**) support on Android 12+ (API 31+) with runtime system wallpaper color scheme extraction and smooth fallback to dark theme.
- Complete **20-State Mascot System** integrating full Lottie animation sequences (1200 frames at 30 FPS) with interactive touch feedback (waving gesture with haptic feedback) and dynamic state resolution across main screen, active downloads, dialogs, audio player, and history empty states.
- 16 new dedicated high-fidelity static SVG mascot assets in `assets/mascot/` providing complete 1:1 fallback coverage across all 20 mascot states.

### Changed
- Upgraded `KitsuneMediaPlayerDialog` audio player to display dynamic `SINGING` and `PAUSED` mascot animations.
- Refactored `ActiveDownloadCard` to display stateful mascots for Wi-Fi waiting, media muxing, and download queue headers.
- Enhanced `DownloadsHistorySheet` empty state to differentiate between empty history (`SLEEPING`) and unmatched search filters (`SEARCHING`).
- Integrated thematic mascot avatars into header dialogs for playlists (`SURPRISED`), legal terms (`TALKING`), and supported platforms (`LOVE`).

### Fixed
- Fixed ProGuard and R8 reflection optimization rules for `MainViewModel` constructor dependency injection.

## [1.1.0] - 2026-10-05

### Added
- Full internationalization (i18n) framework with English (`en`) as the default global locale and Brazilian Portuguese (`pt-BR`) localized dictionaries.
- Android 13+ Per-App Language Preferences support via `locales_config.xml` and manifest integration.
- Modern AGP resource filtering (`androidResources.localeFilters`) to optimize APK packaging sizes for supported languages.
- Type-safe `UiText` wrapper abstraction (`DynamicString` and `StringResource`) decoupling UI text resolution from ViewModels and domain models.
- Type-safe string resource annotation (`@StringRes val labelRes: Int`) in `DownloadStage` domain model.
- Fully localized background download notifications and Android system notification channel configurations.
- Standalone `.editorconfig` and `.gitattributes` files enforcing LF line endings and consistent indent rules across IDEs.
- Automated release notes extraction and curated changelog integration in `.github/release.yml`.

### Changed
- Refactored all Jetpack Compose screens, dialogs, organisms, molecules, and atoms to use `stringResource(R.string.xxx)` instead of hardcoded strings.
- Redesigned `README.md` into a minimal, assertive, and direct documentation layout inspired by Cobalt.
- Replaced literal text glyphs with official vector icons (`Icons.Outlined.ContentPaste` and `Icons.Default.PlayArrow`).
- Standardized all GitHub Actions workflows (`ci.yml`, `nightly.yml`, `release.yml`, and `dependabot.yml`) without emojis and with clean step names.

### Fixed
- Resolved string resource parity across locales, maintaining 100% parity across all 113 string resource keys.
- Modernized deprecated `resourceConfigurations` DSL in `build.gradle.kts` to `androidResources.localeFilters`.

### Removed
- Removed all inline code comments and emoji symbols across the entire repository to uphold total clean-code consistency.

## [1.0.1] - 2026-10-05

### Added
- Automated **Nightly Dev Builds** workflow on GitHub Actions generating multi-ABI split APKs (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`, and `universal`).
- Nocturnal pure AMOLED dark launcher icons with golden crescent moon and cyan starlight accents for development and Nightly builds.
- True black **AMOLED Mode** (`#000000`) toggle in settings with instant recomposition and persistent DataStore storage.
- In-app media player dialog (`KitsuneMediaPlayerDialog`) with Media3 ExoPlayer for videos and animated mascot playback for tracks.
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
