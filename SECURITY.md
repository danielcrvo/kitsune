# Security Policy

The Kitsune development team takes the security and privacy of our users seriously. As an on-device application that processes external media links and executes native extraction binaries, maintaining a rigorous security posture is essential.

---

## 1. Supported Versions

Security updates are actively provided for the latest minor and patch versions:

| Version | Supported |
| :---    | :---:     |
| 1.0.x   | Yes       |
| < 1.0.0 | No        |

---

## 2. Reporting a Vulnerability

If you discover a security vulnerability within Kitsune, please do not report it through public GitHub issues or public forums.

Instead, please submit a Private Security Advisory on GitHub:
[Report a Security Advisory on Kitsune](https://github.com/danielcrvo/kitsune/security/advisories/new)

If GitHub Security Advisories are unavailable, you may reach out directly to the maintainer via GitHub profile contact options.

### What to Include in Your Report
To help us triage and resolve the issue quickly, please provide:
- A clear description of the vulnerability and its potential impact.
- Step-by-step instructions to reproduce the issue (proof-of-concept URL, payload, or APK build).
- Affected Android versions and device models tested.
- Any suggested mitigations or patches if available.

### What to Expect
- Initial Response: You will receive an acknowledgment of your report within 48 hours.
- Triage & Assessment: We will validate the vulnerability and assess its severity.
- Fix & Disclosure: Once a fix is prepared and verified, an update will be published alongside an advisory crediting the researcher (if desired).

---

## 3. Security Considerations & Invariants

- Zero Remote Cloud APIs: Kitsune does not transmit URLs or extracted content to proprietary intermediary servers. All requests go directly to the target media provider via the local yt-dlp executable.
- Input Sanitization: Incoming URLs are filtered and stripped of tracking and unnecessary parameters before being passed to native command lines.
- Scoped Storage: Files are written strictly through Android's MediaStore content resolver API with no raw filesystem tampering outside internal sandbox directories.
