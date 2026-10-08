<div align="center">
  <br />
  <p>
    <img src="app/src/main/assets/kitsune_idle.svg" alt="Kitsune Logo" width="112" height="112" />
  </p>
  <h1>Kitsune</h1>
  <p>
    <strong>The cutest and fastest on-device media downloader on the web.</strong>
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

Kitsune is an open-source, cute, and fast on-device media downloader for Android that doesn't waste your time. It is friendly, private, lightweight, and contains no ads, no trackers, no paywalls, and no third-party backend servers.

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
- **Multi-Platform Service Support**: Videos and audio from YouTube, TikTok, public Instagram Reels, X (Twitter), Reddit, Bilibili, SoundCloud, Pinterest, and direct media links.
- **Tracking Parameter Stripper**: Automatically strips tracking query parameters (`utm_*`, `si`, `igsh`, `fbclid`, `share_id`) from incoming URLs before processing.
- **AMOLED Dark Mode**: True black (`#000000`) theme toggle for OLED/AMOLED screens to conserve battery and provide high contrast.
- **Built-in Media Player & Share**: Internal Media3 ExoPlayer for videos, animated mascot playback for audio, and direct Android Share Sheet integration to external apps.
- **Seamless System Integration**:
  - Android Share Sheet receiver: Share links directly to Kitsune from browsers and social apps.
  - Automatic clipboard detection: Instant detection of media URLs upon opening the app.
  - Foreground service: Dependable background downloads with progress notifications and cancel controls.
  - Scoped storage: Automatic indexing into public `Movies/Kitsune` and `Music/Kitsune` folders.
- **Download Queue & Playlist Batch Processing**: Queue multiple media tasks seamlessly and select items from YouTube and SoundCloud playlists with dedicated selection dialogs.
- **Wi-Fi Only Mode**: Enforce cellular data savings by restricting heavy media downloads to Wi-Fi networks with automatic waiting states.
- **Dynamic Theming (Material You)**: Runtime system wallpaper accent extraction on Android 12+ (API 31+) with fallback to dark theme.
- **Dynamic 20-State Mascot**: Fully reactive Lottie vector mascot animation system with dedicated static SVG fallbacks across all core application states and interactive touch response.
- **In-App Engine Updates**: Self-updater checks for and applies yt-dlp binary patches independently of app releases.
- **Media Library**: In-app management of downloaded files with playback, renaming, sharing, and deletion.
- **Full Internationalization (i18n)**: Bilingual support for English (default global) and Brazilian Portuguese (pt-BR), with Android 13+ Per-App Language Preferences.

---

## Supported Services

| Service | Video | Audio Extraction | URL Stripping | Scope |
| :--- | :---: | :---: | :---: | :--- |
| **YouTube** (`watch`, `shorts`, `youtu.be`) | Yes (up to 4K) | Yes | Yes | Videos, shorts, and playlists |
| **TikTok** (`vm.tiktok.com`, web URLs) | Yes | Yes | Yes | Public videos and audio tracks |
| **Instagram** (`reels`, `posts`, `tv`) | Yes | Yes | Yes | Public reels and video posts |
| **X / Twitter** (`twitter.com`, `x.com`) | Yes | Yes | Yes | Videos and GIF clips |
| **Reddit** (`reddit.com`, `redd.it`) | Yes | Yes | Yes | Video posts with audio |
| **Bilibili** (`bilibili.com`, `b23.tv`) | Yes | Yes | Yes | Videos and clips |
| **SoundCloud** | N/A | Yes | Yes | Audio tracks and playlists |
| **Pinterest** (`pin.it`, `pinterest.*`) | Yes | Yes | Yes | Video and animated pins |
| **Direct Media URLs** | Yes | Yes | Yes | Direct HTTP/HTTPS audio and video links |

---

## Architecture & Codebase

Kitsune is built with Kotlin and Jetpack Compose following **Clean Architecture**, **Atomic Design** and **Unidirectional Data Flow (MVI)**, with **Hilt** for dependency injection:

```
ui        Jetpack Compose (Atomic Design) and one @HiltViewModel per feature
  ├── theme / components   Tokens, atoms, molecules, organisms, templates
  ├── main                 MainScreen: composes feature states and routes MainUiAction
  └── download · history · settings · update   Feature ViewModels, states and actions

domain    Models, repository interfaces, use cases, UrlDetector (no dependency on data, service or ui)

data      Repository implementations
  ├── engine        YtDlpMediaEngine (yt-dlp / FFmpeg)
  ├── download      In-memory queue and service scheduler
  ├── media         MediaStore export and library
  ├── preferences   DataStore preferences
  ├── update        GitHub Releases updater
  └── network       Connectivity monitor

service   DownloadForegroundService and KitsuneNotifier
di        Hilt modules
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
./gradlew assembleGithubDebug
```
Output APKs will be located in `app/build/outputs/apk/github/debug/`.

### Distribution Flavors
- `github`: official builds published on GitHub Releases, including the in-app updater.
- `fdroid`: builds without the self-updater and without the `REQUEST_INSTALL_PACKAGES` permission (`./gradlew assembleFdroidRelease`).

Release builds are signed only when `KITSUNE_KEYSTORE_PATH`, `KITSUNE_KEYSTORE_PASSWORD`, `KITSUNE_KEY_ALIAS` and `KITSUNE_KEY_PASSWORD` are set; otherwise they are produced unsigned.

### Run Unit Tests
```bash
./gradlew testGithubDebugUnitTest
```

### Run Android Lint
```bash
./gradlew lintGithubDebug
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
