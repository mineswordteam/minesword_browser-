# Engine Documentation - Minesword Browser

## Browser Engine Choice
Minesword Browser utilizes Android's native WebKit browser engine via `android.webkit.WebView` and `androidx.webkit`.

### Technical Rationale
- Production stability on Android.
- Access to hardware-accelerated GPU rendering, Chromium web standards support (HTML5, CSS3, ES2023 JS, WebGL, WebSockets).
- Full security isolation sandbox provided by Android OS.
- Customizable `WebViewClient` and `WebChromeClient` hooks for custom navigation routing, SSL error enforcement, and site permission delegation.
