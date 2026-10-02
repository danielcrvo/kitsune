## Description

Please provide a concise summary of your changes, including motivation and context.

Fixes / Closes #(issue_number)

---

## Type of Change

- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 🎨 UI/UX enhancement (visual styling, animation, or theme refinement)
- [ ] ♻️ Code refactoring (no functional changes)
- [ ] ⚡ Performance optimization
- [ ] 📝 Documentation update
- [ ] 🔧 Build / CI / Dependency update

---

## Jetpack Compose & Quality Checklist

Please ensure your contribution satisfies the following project standards:

- [ ] **Build:** The project builds cleanly via `./gradlew assembleDebug`.
- [ ] **Tests:** Unit tests run and pass via `./gradlew testDebugUnitTest`.
- [ ] **Atomic Design:** UI components are placed in their proper level (`atoms`, `molecules`, `organisms`, `templates`).
- [ ] **Stability:** State classes and UI models are marked with `@Immutable`.
- [ ] **Modifier Hygiene:** Reusable composables accept `modifier: Modifier = Modifier` as the first optional parameter and apply it to the outermost layout node.
- [ ] **Theming:** Colors, shapes, and spacing are obtained through `KitsuneTheme.*` tokens without hardcoded values.
- [ ] **Edge-to-Edge:** Screen layouts handle system bar window insets safely.
- [ ] **Clean Code:** Code is free of unnecessary logging or dead code.
- [ ] **Language:** PR title, description, and git commits are in English using Conventional Commits.

---

## Screenshots / Screen Recordings (if UI changes were made)

_Attach before/after screenshots or screen recordings demonstrating the change._
