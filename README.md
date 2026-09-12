# Minesword Browser

**Developer:** `mineswordteam`
**Team:** Minesword Team

Minesword Browser is a production-grade, secure, Persian-first Android web browser built using Kotlin, Jetpack Compose, Room DB, and Android WebKit security features.

## Highlights
- **Real Web Browsing**: Full HTTPS/HTTP support, navigation lifecycle, cookie & session management.
- **Persian-First & RTL Support**: Native Persian character and numeral normalization (ی/ي, ک/ك, ZWNJ handling, Persian digits).
- **Security & Privacy**: Strict HTTPS enforce, Cookie management, Incognito isolated mode, and Safe Browsing integration.
- **Data Persistence**: SQLite / Room DB persistence for History, Bookmarks, Open Tabs, Downloads, and Site Permissions.
- **Tab System**: Multi-tab manager with instant switching, tab restoration, and background memory efficiency.
- **Download Management**: Native system download service integration with progress tracking and metadata saving.
- **Network State Awareness**: Online, Local Network Only (LAN), and Offline state detection.

## Build Instructions
```bash
./gradlew assembleDebug
```
The APK artifact will be output at:
`app/build/outputs/apk/debug/app-debug.apk`
