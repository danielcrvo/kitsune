# Kitsune System Architecture

This document describes the architectural principles, component structure, data flow, and design system implementation of the **Kitsune** Android application.

---

## 1. Architectural Overview

Kitsune is designed around **Clean Architecture**, **Unidirectional Data Flow (MVI/MVVM)**, and Brad Frost's **Atomic Design System** mapped to modern **Jetpack Compose** primitives.

The system is separated into three primary layers:
1. **UI Layer (Presentation)**: Built entirely with Jetpack Compose (BOM 2025.02.00) using an Atomic Design component hierarchy, `@Immutable` state contracts, MVI action dispatching (`MainUiAction`), and lifecycle-aware state consumption.
2. **Core Engine & Service Layer (Domain / Operations)**: Manages native execution (`yt-dlp` and `FFmpeg` binaries via NDK), asynchronous background tasks using Android Foreground Services, and real-time state broadcasting.
3. **Storage & Platform Layer (Data / Infrastructure)**: Handles Android Scoped Storage integration via `MediaStore`, Jetpack DataStore for user preferences, Media3 ExoPlayer for in-app media playback, and notification channels.

```mermaid
flowchart TD
    subgraph UI_Layer["UI Layer (Jetpack Compose)"]
        Activity["MainActivity"]
        Screen["MainScreen"]
        ViewModel["MainViewModel"]
        Action["MainUiAction (Sealed Interface)"]
        State["MainUiState (@Immutable)"]
        Template["KitsuneScreenTemplate"]
        Organisms["Organisms\n(MainInputCard, ActiveDownloadCard, Sheets, Player)"]
        Molecules["Molecules\n(UrlInputBar, MediaPreviewCard, ModeSelector)"]
        Atoms["Atoms\n(KitsuneButton, KitsuneBadge, MascotSvg)"]
        Tokens["Tokens\n(KitsuneColorTokens, KitsuneSpacingTokens, KitsuneShapeTokens)"]
    end

    subgraph Service_Layer["Service & Engine Layer"]
        Service["DownloadForegroundService\n(dataSync, Partial WakeLock)"]
        Helper["DownloadNotificationHelper"]
        Engine["YtDlpEngine\n(Native NDK Execution)"]
        Updater["EngineUpdateManager"]
        Detector["UrlDetector"]
    end

    subgraph Storage_Layer["Storage & Platform Layer"]
        Exporter["MediaStoreExporter\n(Scoped Storage: Movies/Music)"]
        FilesRepo["DownloadedFilesRepository"]
        PrefsRepo["UserPreferencesRepository\n(DataStore Preferences)"]
        ExoPlayer["AndroidX Media3 ExoPlayer"]
        AndroidMediaStore["Android MediaStore Provider"]
    end

    Activity --> Screen
    Screen --> Template
    Template --> Organisms
    Organisms --> Molecules
    Molecules --> Atoms
    Atoms --> Tokens

    Screen -->|Dispatches Action| Action
    Action --> ViewModel
    ViewModel -->|Emits StateFlow| State
    State --> Screen

    ViewModel -->|Start / Cancel Intent| Service
    Service --> Helper
    Service --> Engine
    Engine --> Exporter
    Exporter --> AndroidMediaStore
    AndroidMediaStore --> FilesRepo
    FilesRepo --> ViewModel
    PrefsRepo <--> ViewModel
    ViewModel --> Updater
    ViewModel --> Detector
    Organisms --> ExoPlayer
```

---

## 2. Presentation Layer: Atomic Design in Jetpack Compose

The UI layer is organized strictly following the Atomic Design methodology. Each level has well-defined dependencies: a component may only depend on components at the same level or lower levels.

```
Template
  └── Organisms
        └── Molecules
              └── Atoms
                    └── Tokens (KitsuneTheme)
```

### 2.1 Design Tokens (`ui/theme/tokens`)
Tokens are the atomic values defining color, typography, spacing, and shapes:
- **`KitsuneColorTokens`**: Defines brand accents (`accentCyan`, `accentIndigo`, `accentOrange`, `accentPurple`), dark backgrounds (`background`, `surface`, `surfaceElevated`), and semantic states (`success`, `error`), with full support for pure OLED black (AMOLED).
- **`KitsuneSpacingTokens`**: Strict spacing scale (`xxs: 2.dp` up to `xxl: 48.dp`).
- **`KitsuneShapeTokens`**: Corner radiuses (`pill: 999.dp`, `cardRadius: 24.dp`, `inputRadius: 16.dp`, `sheetRadius: 28.dp`).
- **Access Pattern**: Provided via `CompositionLocalProvider` (`LocalKitsuneColors`, `LocalKitsuneSpacing`, `LocalKitsuneShapes`) and queried using `KitsuneTheme.colors`, `KitsuneTheme.spacing`, and `KitsuneTheme.shapes`. All getters are annotated with `@ReadOnlyComposable` to skip recomposition registration.
- **MultiPreview**: `@ThemePreviews` provides dual preview capabilities (Standard Dark and Pure AMOLED Black) directly in Android Studio.

### 2.2 Atoms (`ui/components/atoms`)
Single-purpose, highly reusable composables with slot APIs:
- **`KitsuneButton`**: Button component supporting `PRIMARY`, `SECONDARY`, and `ACCENT` visual variants, loading spinners, and pill shapes.
- **`KitsuneIconButton`**: Circular icon buttons with pressed/hover ripple states and minimum 48dp touch targets.
- **`KitsuneBadge`**: Compact pill badges displaying extraction status or platform tags.
- **`KitsuneTextField`**: Custom styled input field supporting prefix icons, clear actions, and edge-to-edge keyboard padding.
- **`KitsuneLinearGauge`**: Smoothly animated progress indicator utilizing `animateFloatAsState`.
- **`MascotSvg`**: Reactive mascot rendering dynamic emotional states (`IDLE`, `DOWNLOADING`, `COMPLETED`, `ERROR`) using Lottie Compose with seamless marker/frame loop clipping and SVG fallback.

### 2.3 Molecules (`ui/components/molecules`)
Composites of two or more atoms forming functional units:
- **`UrlInputBar`**: Combines `KitsuneTextField`, clipboard paste button, clear button, and submit action.
- **`KitsuneModeSelector`**: Segmented selector for `AUTO` (Video), `AUDIO`, and `MUTE` extraction modes.
- **`MediaPreviewCard`**: Media item preview showing remote thumbnail, title, uploader, and duration.
- **`DownloadStatusDisplay`**: Displays active download stage, percentage, download speed, and estimated time remaining (ETA).
- **`QualityOptionTile`**: Selectable resolution or audio bitrate tile.

### 2.4 Organisms (`ui/components/organisms`)
Discrete screen regions handling complex domain tasks:
- **`MainInputCard`**: Core hero card housing the mode selector, input bar, detected platform badge, media preview, and trigger button.
- **`ActiveDownloadCard`**: Real-time progress monitor card appearing during active downloads.
- **`DownloadSettingsSheet`**: Modal bottom sheet configuring resolution, audio codecs (MP3, Opus, M4A), bitrate, subtitles, AMOLED pure black theme, and yt-dlp version status.
- **`DownloadsHistorySheet`**: Modal bottom sheet listing downloaded files with options to open, share, rename, or delete.
- **`KitsuneMediaPlayerDialog`**: High-performance in-app player leveraging Media3 ExoPlayer for videos and animated mascot playback for audio, supporting direct Android Share Sheet intent dispatching.
- **`SupportedServicesDialog`**: Information modal listing supported content providers.
- **`TermsDialog`**: Legal disclaimer and fair-use policy modal.

### 2.5 Templates & Screen (`ui/components/templates`, `ui/screens`)
- **`KitsuneScreenTemplate`**: Pure structural scaffold managing status/navigation bar insets (`WindowInsets.statusBars`, `WindowInsets.navigationBars`), vertical scrolling, header placement, and floating sheet anchors.
- **`MainScreen`**: Connects `MainViewModel` to `MainScreenContent` using `MainUiAction` event dispatching.
- **`MainUiAction`**: A sealed interface encapsulating all user actions (`ChangeUrl`, `StartDownload`, `SetDownloadMode`, `ToggleAmoledTheme`, etc.).
- **`MainUiState`**: An immutable (`@Immutable`) data class holding all presentation state, ensuring strict Compose compiler stability.

---

## 3. Core Engine & Native Execution Layer

Kitsune bundles native binaries of `yt-dlp` and `FFmpeg` through the `youtubedl-android` wrapper.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant MainScreen
    participant MainViewModel
    participant Service as DownloadForegroundService
    participant Engine as YtDlpEngine
    participant NDK as yt-dlp / FFmpeg (Native)
    participant Exporter as MediaStoreExporter
    participant MediaStore as Android MediaStore

    User->>MainScreen: Pastes URL & clicks Download
    MainScreen->>MainViewModel: onAction(StartDownload)
    MainViewModel->>Service: startDownload(context, url, config)
    Service->>Service: Acquire WakeLock & Start Foreground
    Service->>Engine: executeDownload(context, url, config, outputDir, onProgress)
    Engine->>NDK: YoutubeDL.getInstance().execute(request)
    loop Download & Multiplexing
        NDK-->>Engine: stdout / progress callbacks
        Engine-->>Service: onProgressUpdate(progress, speed, eta, stage)
        Service-->>MainViewModel: Update StateFlow<DownloadState>
        MainViewModel-->>MainScreen: Recompose ActiveDownloadCard
    end
    NDK-->>Engine: Completed file output (temp directory)
    Engine-->>Service: Return output File
    Service->>Exporter: exportToGallery(context, tempFile, title, isAudio)
    Exporter->>MediaStore: Insert record (IS_PENDING = 1)
    Exporter->>MediaStore: Stream bytes & Commit (IS_PENDING = 0)
    Exporter-->>Service: Output Uri
    Service->>Service: Release WakeLock & Stop Foreground
    Service-->>MainViewModel: Emit DownloadState.Completed
    MainViewModel->>MainScreen: Trigger completion feedback
```

### 3.1 Lazy Warmup & Mutex Thread Safety
Native NDK libraries require decompression and initialization upon app launch. To avoid UI jank:
- Initialization is kicked off in the background during `Application.onCreate()` inside `KitsuneApp` using `CoroutineScope(Dispatchers.IO + SupervisorJob())`.
- `YtDlpEngine.ensureInitialized(context)` uses a Kotlin `Mutex.withLock` to guarantee that concurrent requests wait for a single initialization routine rather than throwing duplicate extraction errors.

### 3.2 URL Sanitization & Platform Detection
`UrlDetector` performs fast regex evaluation against incoming URLs to identify supported platforms (YouTube, TikTok, Instagram, Twitter/X, Reddit, Bilibili, SoundCloud, Pinterest).
Before execution, `UrlDetector.sanitizeUrl()` strips tracking parameters:
- General UTM tags (`utm_source`, `utm_medium`, `utm_campaign`, etc.)
- Platform identifiers (`si`, `igsh`, `fbclid`, `ref_src`, `share_id`, `s`, `t`)

### 3.3 Dynamic Engine Updates (`EngineUpdateManager`)
`EngineUpdateManager` provides rolling updates to the yt-dlp binary:
- Invokes `YoutubeDL.getInstance().updateYoutubeDL(context, UpdateChannel.STABLE)` on `Dispatchers.IO`.
- Allows users to patch extractors immediately when upstream platforms change their API, without waiting for a full app release.

---

## 4. Android Platform Services & Storage Architecture

### 4.1 Foreground Service & Wake Lock
Background downloads are executed by `DownloadForegroundService`:
- Declared in `AndroidManifest.xml` with `android:foregroundServiceType="dataSync"`.
- Requests `PARTIAL_WAKE_LOCK` via `PowerManager` to prevent CPU throttling or deep sleep during high-bitrate video downloads or intensive FFmpeg remuxing.
- Communicates progress to the system via `DownloadNotificationHelper`, posting updates with low alert frequency (`onlyAlertOnce = true`) and offering a cancel action.

### 4.2 Scoped Storage & MediaStore Export
Starting with Android 10 (API 29), direct file path access to external storage is restricted. Kitsune is fully Scoped Storage compliant:
1. `YtDlpEngine` downloads each queued item into its own isolated cache directory (`context.cacheDir/kitsune_tmp/<taskId>/`) and reports the final file path through `--print-to-file after_move:filepath`.
2. Upon download completion, `MediaStoreExporter` names the file after the media title and creates an entry in `MediaStore.Video.Media.EXTERNAL_CONTENT_URI` (for videos) or `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` (for audio tracks), with the MIME type derived from the file extension.
3. On API 29+, the entry is created with `IS_PENDING = 1` into `Movies/Kitsune` or `Music/Kitsune`; if streaming fails, the pending entry is deleted.
4. File bytes are streamed from cache to the MediaStore URI.
5. `IS_PENDING` is updated to `0`, immediately registering the file in the user's gallery and media players.
6. On API 26-28, the file is copied directly into the public `Movies/Kitsune` or `Music/Kitsune` folder (requires `WRITE_EXTERNAL_STORAGE`, declared with `maxSdkVersion="28"`) and indexed with `MediaScannerConnection`.
7. The task's temporary directory is always purged, including after failures and cancellations. Cancelling a task destroys the underlying yt-dlp process.

---

## 5. Security, ProGuard & Privacy Posture

- **No Remote Telemetry**: Kitsune does not include Google Analytics, Firebase, or external telemetry libraries.
- **Local Network Only**: Network requests are exclusively initiated by the `yt-dlp` binary directly to the content host.
- **Safe Sandboxing**: Temporary files are kept in internal storage until explicitly exported to the user's public media library.
- **ProGuard / R8 Optimization**: Explicit keep rules preserve native JNI methods, Media3 ExoPlayer decoders, and Lottie reflection paths for stable, obfuscated production releases.
