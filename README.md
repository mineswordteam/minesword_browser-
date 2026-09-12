# Minesword Browser Documentation

**Developer:** `mineswordteam`
**Team:** Minesword Team
**Package:** `com.minesword.browser`

Minesword Browser is a production-grade, secure, Persian-first Android web browser built using Kotlin, Jetpack Compose, Room DB, and the custom native **Minesword Engine**.

## Implementation Status Summary
- **Minesword Engine**: `IMPLEMENTED` — Native HTML/CSS parser, DOM tree, Box-model layout, Canvas painter with multi-line text wrapping, link hit-testing, and native HTTP/HTTPS networking client.
- **Futuristic UI/UX**: `IMPLEMENTED` — Jetpack Compose home screen with logo mark, glassmorphic top address bar, dark bottom navigation bar, 3D tab switcher, and 14-category settings UI.
- **Persian & RTL**: `IMPLEMENTED` — Native Persian/Arabic character & numeral normalization (ی/ي, ک/ك, ZWNJ) and RTL layout support.
- **Security & Privacy**: `IMPLEMENTED` — Strict `usesCleartextTraffic="false"`, Same-Origin Policy checks, HTTPS enforcement, cookie management, and Incognito mode.
- **Storage**: `IMPLEMENTED` — SQLite / Room database for History, Bookmarks, Open Tabs, Downloads, and Site Permissions.
- **Network State**: `IMPLEMENTED` — Dynamic connectivity monitoring (`ONLINE`, `LOCAL_NETWORK_ONLY`, `OFFLINE`).

## Build Commands
```bash
# Run Unit Tests
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew :app:assembleDebug
```

## Generated Artifacts
- **APK Path**: `apk/Minesword-Browser.apk` (~18 MB)
- **ZIP Path**: `minesword-browser.zip` (~19 MB)
