<div align="center">
  <br />
  <p>
    <img src="app/src/main/assets/mascot/mascot_idle.svg" alt="Kitsune Logo" width="112" height="112" />
  </p>
  <h1>Kitsune</h1>
  <p>
    <strong>Fast, private, and modern on-device media downloader for Android.</strong>
    <br />
    Save what you love, on your own terms.
  </p>
  <p>
    <a href="https://github.com/danielcrvo/kitsune/actions/workflows/ci.yml">
      <img src="https://github.com/danielcrvo/kitsune/actions/workflows/ci.yml/badge.svg" alt="CI Status" />
    </a>
    <a href="https://github.com/danielcrvo/kitsune/releases/tag/nightly">
      <img src="https://img.shields.io/badge/Nightly-Dev%20Build-blueviolet.svg" alt="Nightly Build" />
    </a>
    <a href="https://github.com/danielcrvo/kitsune/blob/main/LICENSE">
      <img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License: GPL-3.0" />
    </a>
    <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen.svg" alt="Platform: Android 8.0+" />
    <img src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.02.00-teal.svg" alt="Jetpack Compose BOM" />
  </p>
  <br />
</div>

Kitsune is an on-device media downloader and audio extractor for Android that doesn't waste your time. It is friendly, fast, private, and contains no ads, no trackers, no paywalls, and no third-party backend servers.

Paste the link, get the file, move on. That simple, just how it should be.

All media extraction, network requests, and audio/video muxing run strictly locally on your phone using native NDK builds of [yt-dlp](https://github.com/yt-dlp/yt-dlp) and [FFmpeg](https://ffmpeg.org/).

---

## Downloads

| Build | Download Link | Target Architecture | Notes |
| :--- | :--- | :--- | :--- |
| **Nightly (ARM64)** | [Kitsune-arm64-v8a-debug.apk](https://github.com/danielcrvo/kitsune/releases/tag/nightly) | 64-bit ARM devices | Recommended for modern phones (~37 MB) |
| **Nightly (Universal)** | [Kitsune-universal-debug.apk](https://github.com/danielcrvo/kitsune/releases/tag/nightly) | All architectures | Contains all native binaries (~128 MB) |
| **Nightly (ARM 32-bit)** | [Kitsune-armeabi-v7a-debug.apk](https://github.com/danielcrvo/kitsune/releases/tag/nightly) | 32-bit ARM devices | Legacy Android devices |
| **Nightly (x86_64)** | [Kitsune-x86_64-debug.apk](https://github.com/danielcrvo/kitsune/releases/tag/nightly) | x86_64 devices | Emulators and ChromeOS |
| **Production Releases** | [GitHub Releases](https://github.com/danielcrvo/kitsune/releases) | Stable releases | Signed production builds with release notes |

> [!NOTE]
> The **Nightly** build uses package ID `com.kitsune.app.debug` and nocturnal dark launcher icons. It can be installed side-by-side with production builds on the same device without conflicts.

---

## Features

- **100% On-Device Processing**: Downloads and media conversions execute locally via bundled yt-dlp and FFmpeg binaries. Your data never touches intermediate proxy servers.
- **Universal Service Support**: Works with YouTube, TikTok, Instagram, X (Twitter), Reddit, Bilibili, SoundCloud, Pinterest, and direct media links.
- **Tracking Parameter Stripper**: Automatically strips tracking query parameters (`utm_*`, `si`, `igsh`, `fbclid`, `share_id`) from incoming URLs before processing.
- **AMOLED Dark Mode**: True black (`#000000`) theme toggle for OLED/AMOLED screens to conserve battery and provide high contrast.
- **Built-in Media Player & Share**: Internal Media3 ExoPlayer with animated audio visualizer and direct Android Share Sheet integration to WhatsApp, Telegram, or Drive.
- **Seamless System Integration**:
  - Android Share Sheet receiver: Share links directly to Kitsune from browsers and social apps.
  - Automatic clipboard detection: Instant detection of media URLs upon opening the app.
  - Foreground service: Dependable background downloads with progress notifications and cancel controls.
  - Scoped storage: Automatic indexing into public `Movies/Kitsune` and `Music/Kitsune` folders.
- **In-App Engine Updates**: Self-updater checks for and applies yt-dlp binary patches independently of app releases.
- **Media Library**: In-app management of downloaded files with playback, renaming, sharing, and deletion.
- **Full Internationalization (i18n)**: Bilingual support for English (default global) and Brazilian Portuguese (pt-BR), with Android 13+ Per-App Language Preferences.

---

## Supported Services

| Service | Video | Audio Extraction | URL Stripping |
| :--- | :---: | :---: | :---: |
| **YouTube** (`watch`, `shorts`, `youtu.be`) | Yes (up to 4K) | Yes | Yes |
| **TikTok** (`vm.tiktok.com`, web URLs) | Yes | Yes | Yes |
| **Instagram** (`reels`, `posts`, `tv`) | Yes | Yes | Yes |
| **X / Twitter** (`twitter.com`, `x.com`) | Yes | Yes | Yes |
| **Reddit** (`reddit.com`, `redd.it`) | Yes | Yes | Yes |
| **Bilibili** (`bilibili.com`, `b23.tv`) | Yes | Yes | Yes |
| **SoundCloud** | Audio only | Yes | Yes |
| **Pinterest** (`pin.it`, `pinterest.*`) | Yes | Yes | Yes |
| **Direct Media URLs** | Yes | Yes | Yes |

---

## Architecture & Codebase

Kitsune is built with Kotlin and Jetpack Compose following **Atomic Design** and **Unidirectional Data Flow (MVI)**:

```
UI Layer (Jetpack Compose)
  ├── tokens      Design tokens (KitsuneTheme: colors, typography, shapes, spacing)
  ├── atoms       Single-responsibility primitives (KitsuneButton, KitsuneTextField, KitsuneBadge)
  ├── molecules   Combinations of atoms (UrlInputBar, KitsuneModeSelector, MediaPreviewCard)
  ├── organisms   Self-contained regions (MainInputCard, DownloadSettingsSheet, KitsuneMediaPlayerDialog)
  ├── templates   Layout containers handling window insets and scrolling (KitsuneScreenTemplate)
  └── screens     State coordination and action dispatching (MainScreen, MainViewModel, MainUiAction)

Core Layer
  ├── engine      Extraction orchestrator (YtDlpEngine, EngineUpdateManager, UrlDetector)
  ├── service     Background execution (DownloadForegroundService, DownloadNotificationHelper)
  └── storage     Persistence & MediaStore (MediaStoreExporter, DownloadedFilesRepository, UserPreferencesRepository)
```

---

## Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK with Platform API 35
- NDK toolchain for ABIs: `arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`

### Build Debug APKs
```bash
git clone https://github.com/danielcrvo/kitsune.git
cd kitsune
./gradlew assembleDebug
```
Output APKs will be located in `app/build/outputs/apk/debug/`.

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Android Lint
```bash
./gradlew lintDebug
```

---

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md): Architectural design, data flow, threading, and Scoped Storage details.
- [CONTRIBUTING.md](CONTRIBUTING.md): Contribution guidelines, atomic design rules, and pull request checklist.
- [CHANGELOG.md](CHANGELOG.md): Version history following Keep a Changelog.
- [SECURITY.md](SECURITY.md): Security policy and vulnerability disclosure instructions.
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md): Community standards and conduct expectations.

---

## License & Acknowledgments

### License
Kitsune is open-source software licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for details.

### Acknowledgments
- [yt-dlp](https://github.com/yt-dlp/yt-dlp): On-device media extraction engine.
- [FFmpeg](https://ffmpeg.org/): Audio transcoding and video muxing library.
- [youtubedl-android](https://github.com/junkfood02/youtubedl-android): Android NDK runtime ports.
