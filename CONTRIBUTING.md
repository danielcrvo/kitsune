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
git clone https://github.com/danielcrvo/kitsune.git
cd kitsune

./gradlew assembleGithubDebug

./gradlew testGithubDebugUnitTest

./gradlew lintGithubDebug
```

---

## 2. Architecture & Jetpack Compose Coding Guidelines

Kitsune follows strict **Unidirectional Data Flow (MVI/MVVM)** and Brad Frost's **Atomic Design System** mapped to Jetpack Compose primitives.

### 2.1 Atomic Design Hierarchy
Components must be placed in their respective packages under `com.kitsune.app.ui.components`:
- **`tokens`**: Design tokens (colors, spacing, shapes). Accessed via `KitsuneTheme.colors`, `KitsuneTheme.spacing`, `KitsuneTheme.shapes`.
- **`atoms`**: Single-responsibility primitives (`KitsuneButton`, `KitsuneTextField`, `KitsuneBadge`). They must not depend on molecules or organisms.
- **`molecules`**: Functional combinations of 2+ atoms (`UrlInputBar`, `KitsuneModeSelector`, `MediaPreviewCard`).
- **`organisms`**: Discrete screen regions and dialogs (`MainInputCard`, `ActiveDownloadCard`, `DownloadSettingsSheet`, `KitsuneMediaPlayerDialog`).
- **`templates`**: Layout wrappers handling safe insets and scroll behavior without hardcoded domain data (`KitsuneScreenTemplate`).
- **`screens`**: Top-level coordinators connecting ViewModels to templates using `MainUiAction`.

### 2.2 Compose-Expert Quality Checklist
- **Strict Design Tokens**: Never hardcode colors (`Color(0x...)`) or raw numbers directly in composable bodies. Always use `KitsuneTheme.colors`, `KitsuneTheme.spacing`, and `KitsuneTheme.shapes`.
- **Self-Documenting Code (Zero Comments)**: Do not add redundant or noisy inline comments. Write clean, self-explanatory code with expressive naming.
- **Immutable State Contracts**: All UI state classes and domain models must be annotated with `@Immutable` (e.g., `MainUiState`, `DownloadConfig`).
- **State Hoisting & MVI**: Pass state down and actions up using sealed interface actions (e.g. `MainUiAction`). Avoid passing raw ViewModels down into reusable UI components.
- **Modifier Ordering**:
  - Every reusable composable must accept `modifier: Modifier = Modifier` as the first optional parameter.
  - Apply the modifier to the root layout node.
  - Follow the canonical modifier ordering: sizing/layout -> background/border -> clipping -> clickable/semantics -> inner padding.
- **Accessibility & Touch Targets**:
  - Interactive elements must maintain a minimum touch target size of 48x48dp.
  - All icons and images must provide a localized `contentDescription` for TalkBack, or `null` if decorative.
- **Previews**: All new UI components must include a private preview annotated with `@ThemePreviews` to test rendering in both Dark and AMOLED modes.

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
- `ci`: CI/CD workflows, Gradle build, or GitHub Actions
- `chore`: Maintenance, dependencies, or configuration updates

---

## 4. Pull Request (PR) Checklist

Before submitting your pull request, please verify the following:
1. [ ] The project compiles successfully: `./gradlew assembleGithubDebug`.
2. [ ] All unit tests pass: `./gradlew testGithubDebugUnitTest`.
3. [ ] Android Lint passes without fatal errors: `./gradlew lintGithubDebug`.
4. [ ] Code follows the Atomic Design package structure.
5. [ ] No hardcoded colors or raw dimensions; 100% token usage.
6. [ ] Zero code comments (self-documenting clean code).
7. [ ] New composables provide `@ThemePreviews`.
8. [ ] Any new state models are marked with `@Immutable`.
9. [ ] Touch targets are at least 48x48dp with semantic accessibility descriptions.
10. [ ] Commit messages follow the Conventional Commits format.

---

## 5. Code of Conduct

All contributors and maintainers are expected to follow our [Code of Conduct](CODE_OF_CONDUCT.md) to ensure an open, welcoming, and inclusive community for everyone.
