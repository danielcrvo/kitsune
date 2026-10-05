<p align="center">
  <img src="app/src/main/assets/mascot/mascot_idle.svg" alt="Kitsune Logo" width="128" height="128" />
</p>

<h1 align="center">Kitsune</h1>

<p align="center">
  <strong>Fast, private, and modern on-device media downloader and audio extractor for Android.</strong>
</p>

<p align="center">
  <a href="https://github.com/danielcrvo/kitsune/actions/workflows/ci.yml">
    <img src="https://github.com/danielcrvo/kitsune/actions/workflows/ci.yml/badge.svg" alt="CI Status" />
  </a>
  <a href="https://github.com/danielcrvo/kitsune/releases/tag/nightly">
    <img src="https://img.shields.io/badge/Nightly-Dev%20Build-blueviolet.svg" alt="Nightly Build" />
  </a>
  <a href="https://github.com/danielcrvo/kitsune/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License: GPL-3.0" />
  </a>
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg" alt="Platform: Android 8.0+" />
  <img src="https://img.shields.io/badge/Kotlin-2.1.10-purple.svg" alt="Kotlin 2.1.10" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.02.00-teal.svg" alt="Jetpack Compose BOM" />
  <img src="https://img.shields.io/badge/Engine-yt--dlp%20%2B%20FFmpeg-orange.svg" alt="Engine: yt-dlp + FFmpeg" />
</p>

---

## Overview

**Kitsune** is a modern Android application designed for seamless media downloading and audio conversion directly on your device. Powered by native on-device builds of [yt-dlp](https://github.com/yt-dlp/yt-dlp) and [FFmpeg](https://ffmpeg.org/), Kitsune does not rely on third-party backend servers or cloud conversion APIs. All extraction, downloading, and audio/video muxing happens strictly locally on your Android device.

Built from the ground up with **Kotlin 2.1.10** and **Jetpack Compose** following **Atomic Design** principles, Kitsune provides a fluid, edge-to-edge dark interface with rich micro-animations, comprehensive media controls, pure AMOLED dark mode, and zero ads or trackers.

---

## 📦 Downloads

Choose the build that best fits your device:

| Version | Download Link | Target Device | Notes |
| :--- | :--- | :--- | :--- |
| **Nightly (ARM64)** | [Kitsune-arm64-v8a-debug.apk](https://github.com/danielcrvo/kitsune/releases/tag/nightly) | 🚀 Celulares modernos (64 bits) | **Recomendado** (~37 MB) |
| **Nightly (Universal)** | [Kitsune-universal-debug.apk](https://github.com/danielcrvo/kitsune/releases/tag/nightly) | 🌐 Qualquer celular Android | Contém todas as arquiteturas |
| **Nightly (ARM 32-bit)** | [Kitsune-armeabi-v7a-debug.apk](https://github.com/danielcrvo/kitsune/releases/tag/nightly) | 📱 Celulares mais antigos | Arquitetura de 32 bits |
| **Releases Oficiais** | [GitHub Releases](https://github.com/danielcrvo/kitsune/releases) | 🏷️ Versões de Produção Estáveis | Assinadas e otimizadas |

> [!TIP]
> A versão **Nightly** possui o ID de pacote `com.kitsune.app.debug` e ícones exclusivos noturnos em preto puro AMOLED, permitindo que você a instale lado a lado com a versão oficial de produção sem conflitos.

---

## ✨ Features

- **100% On-Device Processing**: Downloads and media conversions are executed natively using bundled yt-dlp and FFmpeg NDK binaries—no external servers, no tracking, complete privacy.
- **Universal Multi-Platform Support**: Works with YouTube, TikTok, Instagram, X (Twitter), Reddit, Bilibili, SoundCloud, Pinterest, and hundreds of generic media hosts.
- **Privacy URL Sanitization**: Automatically strips tracking query parameters (`utm_*`, `si`, `igsh`, `fbclid`, `share_id`, etc.) from pasted links.
- **Pure AMOLED Black Theme**: High-contrast, battery-saving true black mode (`#000000`) for OLED and AMOLED displays.
- **In-App Media Player & Direct Sharing**: Integrated Media3 ExoPlayer with animated audio visualizer, playback speed controls, and one-tap Android Share Sheet to WhatsApp, Telegram, or Google Drive.
- **Smart Android Integration**:
  - **Android Share Sheet**: Share links directly to Kitsune from any browser or social media app.
  - **Clipboard Auto-Detection**: Instant detection and suggestion of valid media URLs from your clipboard upon opening.
  - **Foreground Service**: Reliable background downloading with interactive progress notifications and wake-lock management.
  - **Scoped Storage & MediaStore**: Downloaded files are automatically indexed into Android's public `Movies/Kitsune` and `Music/Kitsune` folders for immediate gallery access.
- **Flexible Extraction Options**:
  - Video resolutions from SD up to 4K Ultra HD (2160p).
  - High-fidelity audio extraction in MP3, Opus, or original source formats (up to 320 kbps).
  - Option to mute video streams (video-only extraction).
- **Rolling Engine Updates**: In-app self-updater checks for and applies yt-dlp engine patches directly from official channels without requiring an APK update.
- **Built-in Media Library**: Access your downloaded videos and audio tracks with in-app playback, renaming, file sharing, and deletion.

---

## 🌐 Supported Platforms

| Platform | Video | Audio-Only | Quality Selector | Tracking Stripper |
| :--- | :---: | :---: | :---: | :---: |
| **YouTube** (`watch`, `shorts`, `youtu.be`) | ✅ (Up to 4K) | ✅ | ✅ | ✅ |
| **TikTok** (`vm.tiktok.com`, standard URLs) | ✅ | ✅ | ✅ | ✅ |
| **Instagram** (`reels`, `posts`, `tv`) | ✅ | ✅ | ✅ | ✅ |
| **X / Twitter** (`twitter.com`, `x.com`) | ✅ | ✅ | ✅ | ✅ |
| **Reddit** (`reddit.com`, `redd.it`) | ✅ | ✅ | ✅ | ✅ |
| **Bilibili** (`bilibili.com`, `b23.tv`) | ✅ | ✅ | ✅ | ✅ |
| **SoundCloud** | ❌ (Audio only) | ✅ | ✅ | ✅ |
| **Pinterest** (`pin.it`, `pinterest.*`) | ✅ | ✅ | ✅ | ✅ |
| **Generic URLs** (Direct media streams) | ✅ | ✅ | ✅ | ✅ |

---

## 🏛 Architecture & Design System

Kitsune follows **Unidirectional Data Flow (MVI/MVVM)** and Brad Frost's **Atomic Design System** mapped to Jetpack Compose primitives:

```
UI Layer (Jetpack Compose)
  ├── Tokens (KitsuneTheme: KitsuneColorTokens, KitsuneSpacingTokens, KitsuneShapeTokens)
  ├── Atoms (KitsuneButton, KitsuneIconButton, KitsuneBadge, KitsuneTextField, MascotSvg)
  ├── Molecules (UrlInputBar, MediaPreviewCard, KitsuneModeSelector, DownloadStatusDisplay)
  ├── Organisms (MainInputCard, ActiveDownloadCard, DownloadSettingsSheet, DownloadsHistorySheet, KitsuneMediaPlayerDialog)
  ├── Templates (KitsuneScreenTemplate)
  └── Screens (MainScreen + MainViewModel + MainUiState + MainUiAction)
          │
          ▼
Core Architecture
  ├── Engine (YtDlpEngine, EngineUpdateManager, UrlDetector)
  ├── Services (DownloadForegroundService, DownloadNotificationHelper)
  └── Storage (MediaStoreExporter, DownloadedFilesRepository, UserPreferencesRepository)
```

For an in-depth breakdown of the component hierarchy, threading model, and Scoped Storage implementation, refer to [ARCHITECTURE.md](ARCHITECTURE.md).

---

## 🛠 Tech Stack

- **Language:** [Kotlin 2.1.10](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Compose BOM `2025.02.00`)
- **Design System:** Material 3 with custom Kitsune Design Tokens (`CompositionLocalProvider`)
- **Native Engine:** [youtubedl-android](https://github.com/junkfood02/youtubedl-android) (v0.18.1 bundling yt-dlp and FFmpeg)
- **Media Playback:** [AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3) (v1.5.1)
- **Animations & Micro-interactions:** [Lottie Compose](https://airbnb.io/lottie/#/android-compose) (v6.6.2)
- **Image & Vector Loading:** [Coil Compose](https://coil-kt.github.io/coil/) (with SVG decoder)
- **Local Persistence:** [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) (v1.1.3)
- **Asynchronous & Streams:** Kotlin Coroutines & StateFlow (v1.10.1)
- **Build System:** Gradle 8.11.1 with Android Gradle Plugin 8.8.2 and Kotlin Compose Compiler plugin

---

## 🚀 Building from Source

### Prerequisites
- **JDK 17** or higher
- **Android SDK** with Platform `API 35` and Build-Tools installed
- Supported NDK ABIs: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`

### Clone the Repository
```bash
git clone https://github.com/danielcrvo/kitsune.git
cd kitsune
```

### Build Debug APKs
```bash
./gradlew assembleDebug
```
The compiled APKs will be generated in `app/build/outputs/apk/debug/`:
- `Kitsune-v1.0.1-nightly-arm64-v8a-debug.apk`
- `Kitsune-v1.0.1-nightly-universal-debug.apk`
- `Kitsune-v1.0.1-nightly-armeabi-v7a-debug.apk`
- `Kitsune-v1.0.1-nightly-x86_64-debug.apk`

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Android Lint
```bash
./gradlew lintDebug
```

---

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on code style, Jetpack Compose atomic conventions, stability rules (`@Immutable`), and our pull request process.

---

## 🔒 Security

For security vulnerability disclosures, please review [SECURITY.md](SECURITY.md).

---

## 📜 License & Disclaimers

### License
Kitsune is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for the full license text.

### Fair Use & Copyright Disclaimer
Kitsune is designed for personal backup, offline viewing of authorized content, and fair-use archiving. Users are solely responsible for ensuring their use of the application complies with the terms of service of each content provider and all applicable local and international copyright regulations.

### Acknowledgments
- [yt-dlp](https://github.com/yt-dlp/yt-dlp) for their extraordinary command-line media extraction tool.
- [FFmpeg](https://ffmpeg.org/) for media format transcoding and multiplexing.
- [youtubedl-android](https://github.com/junkfood02/youtubedl-android) for the Android NDK runtime ports.
- Inspired by clean, privacy-focused open source utility tools.
