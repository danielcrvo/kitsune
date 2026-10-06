# Changelog

All notable changes to the Kitsune project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.1] - 2026-10-06

### Added
- Dedicated About dialog (`AboutDialog`) highlighting open-source licensing (GNU GPL-3.0), on-device privacy guarantee, direct GitHub/Issues buttons, and core technology credits.
- Automatic update check toggle in user preferences with silent startup check support.
- Update cache manager in settings displaying downloaded APK storage footprint and one-tap cache cleanup.
- Grouped list card styling for Settings (`DownloadSettingsSheet`) with thematic leading icons and subtle dividers.

### Changed
- Refactored settings sheet layout into 4 clean cards (Appearance, Downloads & Network, System & Updates, About).
- Progressive disclosure for audio download settings (audio format and bitrate controls are concealed until audio-only mode is active).
- Replaced talking mascot avatar in Terms of Service dialog with standard neutral idle mascot.

### Removed
- Removed talking mascot asset (`kitsune_talking.svg`) and `TALKING` enum state, while keeping all Lottie animation sequences for all remaining mascot states.

## [1.2.0] - 2026-10-06

### Added
- In-app self-updater (`AppUpdateManager`) querying GitHub Releases API with semantic version comparison and automatic device ABI resolution.
- Dedicated background notification channel (`kitsune_update_channel`) for release alerts, real-time download progress, and direct installation actions.
- Interactive update dialog (`AppUpdateDialog`) with dynamic mascot states, release notes changelog viewer, linear progress gauge, and installation dispatch.
- Manual app update check option in Download Settings alongside engine updates.
- System installation permission flow via `REQUEST_INSTALL_PACKAGES` and `FileProvider`.

### Changed
- Standardized project terminology to strictly use "downloader" across Portuguese and English documentation and UI.
- Updated mascot vector assets and unified SVG styling across all 20 mascot states.

## [1.1.1] - 2026-10-05

### Added
- Sequential **Download Queue** system processing queued items in background with pending count indicator and queue management.
- Multi-item **Playlist Support** with metadata pre-fetching, individual track selection dialog (`PlaylistSelectionDialog`), duration calculation, and batch queue dispatch.
- **Wi-Fi Only Mode** toggle backed by persistent DataStore preferences and dynamic `NetworkMonitor` connectivity state observation.
- Dynamic Theming (**Material You**) support on Android 12+ (API 31+) with runtime system wallpaper color scheme extraction and smooth fallback to dark theme.
- Complete **20-State Mascot System** integrating full Lottie animation sequences (1200 frames at 30 FPS) with interactive touch feedback (waving gesture with haptic feedback) and dynamic state resolution across main screen, active downloads, dialogs, audio player, and history empty states.
- 20 dedicated high-fidelity static SVG mascot assets in `assets/` providing complete 1:1 fallback coverage across all 20 mascot states.

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
