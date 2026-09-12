# Architecture Documentation - Minesword Browser

## Overview
Minesword Browser is architected with modern Android engineering principles:

- **UI Layer**: Jetpack Compose (Declarative, Material 3, dynamic RTL and Persian font support).
- **ViewModel & State Management**: AndroidX `ViewModel`, `StateFlow`, and `Coroutines`.
- **Engine & Web Layer**: Android WebKit (`WebView`, `WebViewClient`, `WebChromeClient`) extended with strict security policies.
- **Persistence Layer**: Room Database (`MineswordDatabase`) with isolated DAOs for History, Bookmarks, Open Tabs, Downloads, and Site Permissions.
- **Network Layer**: Android `ConnectivityManager` callbacks evaluating validated internet vs. local network (LAN) access.
- **Search Module**: Intelligent query vs. URL parsing with Persian character normalization.
