# Contributing to Kitsune

Thank you for your interest in contributing to **Kitsune**! We welcome contributions from developers, designers, and documentation writers to help make Kitsune the cleanest, most reliable on-device media downloader for Android.

Please review this guide before submitting issues or pull requests.

---

## 1. Development Environment Setup

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or higher
- **Android SDK:** API Level 35 (VanillaIceCream)
- **Android NDK:** Required for `youtubedl-android` (automatically resolved via Gradle)
- **IDE:** Android Studio Ladybug / Meerkat or later (recommended)

### Cloning and Building
```bash
# Clone the repository
git clone https://github.com/danielcrvo/kitsune.git
cd kitsune

# Build the debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

---

## 2. Architecture & Jetpack Compose Coding Guidelines

Kitsune follows strict **Unidirectional Data Flow (MVI/MVVM)** and Brad Frost's **Atomic Design System** mapped to Jetpack Compose. Please adhere to these principles:

### 2.1 Atomic Design Hierarchy
Components must be placed in their respective packages under `com.kitsune.app.ui.components`:
- **`tokens`**: Design tokens (colors, spacing, shapes). Accessed via `KitsuneTheme.colors`, `KitsuneTheme.spacing`, `KitsuneTheme.shapes`.
- **`atoms`**: Single-responsibility primitives (`KitsuneButton`, `KitsuneTextField`, `KitsuneBadge`). They must not depend on molecules or organisms.
- **`molecules`**: Functional combinations of 2+ atoms (`UrlInputBar`, `KitsuneModeSelector`, `MediaPreviewCard`).
- **`organisms`**: Discrete screen regions and dialogs (`MainInputCard`, `ActiveDownloadCard`, `DownloadSettingsSheet`).
- **`templates`**: Layout wrappers handling safe insets and scroll behavior without hardcoded domain data (`KitsuneScreenTemplate`).
- **`screens`**: Top-level coordinators connecting ViewModels to templates.

### 2.2 Compose Stability & Performance
- **Immutable State Contracts**: All UI state classes and domain models must be annotated with `@Immutable` (e.g., `MainUiState`, `DownloadConfig`).
- **State Hoisting**: Pass state downward and events upward via lambdas. Avoid instantiating ViewModels inside atoms, molecules, or organisms.
- **Modifier Guidelines**:
  - Every reusable composable must accept `modifier: Modifier = Modifier` as the first optional parameter.
  - The passed `modifier` must be applied to the root layout node of the composable.
  - Avoid duplicate or unchained modifiers.
- **Lifecycle-Aware State Collection**: When collecting `StateFlow` inside `@Composable` functions, always use `collectAsStateWithLifecycle()` to prevent unnecessary background processing.
- **Previews**: All atoms, molecules, and organisms should provide a `@Preview` composable wrapped in `KitsuneTheme`.

### 2.3 Theming & Design Tokens
- Never hardcode color values or raw dimension numbers (`dp`/`sp`) directly inside composables.
- Always retrieve spacing via `KitsuneTheme.spacing.<size>`, corner radiuses via `KitsuneTheme.shapes.<type>`, and palette colors via `KitsuneTheme.colors.<token>`.

---

## 3. Git Workflow & Commit Guidelines

### Branching Convention
Create focused branches branching off `main`:
- `feature/your-feature-name` (for new features or UI additions)
- `fix/issue-description` (for bug fixes)
- `docs/documentation-update` (for documentation improvements)
- `refactor/component-name` (for code refactoring)

### Commit Message Convention
Kitsune follows the **Conventional Commits** specification:

```
<type>(<scope>): <short description in imperative mood>

[optional body explaining motivation and changes]

[optional footer referencing issue numbers, e.g., Closes #42]
```

**Common Types:**
- `feat`: A new feature or user-facing functionality
- `fix`: A bug fix
- `docs`: Documentation updates or additions
- `style`: Formatting, whitespace, or token adjustments (no logic change)
- `refactor`: Code changes that neither fix a bug nor add a feature
- `perf`: Performance improvements
- `test`: Adding or correcting tests
- `chore`: Gradle, build config, or dependency updates

**Examples:**
```bash
git commit -m "feat(ui): add support for Instagram story downloads"
git commit -m "fix(engine): handle query sanitization for short YouTube URLs"
git commit -m "docs(readme): add troubleshooting section for Android 15"
```

---

## 4. Pull Request (PR) Checklist

Before submitting your pull request, please verify the following:
1. [ ] The project compiles successfully: `./gradlew assembleDebug`.
2. [ ] All unit tests pass: `./gradlew testDebugUnitTest`.
3. [ ] Code follows the Atomic Design package structure.
4. [ ] UI components do not hardcode colors, shapes, or spacing values.
5. [ ] Any new state models are marked with `@Immutable`.
6. [ ] Edge-to-edge system insets are properly respected.
7. [ ] All documentation, commit messages, and PR descriptions are written in **English**.

---

## 5. Code of Conduct

All contributors and maintainers are expected to follow our [Code of Conduct](CODE_OF_CONDUCT.md) to ensure an open, welcoming, and inclusive community for everyone.
