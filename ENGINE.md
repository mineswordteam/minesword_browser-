# Minesword Engine Architecture

## Overview
Minesword Engine is a native, modular browser engine built in Kotlin for Android (`com.minesword.browser.engine.minesword`).

### Capability Status Matrix
| Module | Implementation Status | Description |
| :--- | :--- | :--- |
| **HTML Parser** | `IMPLEMENTED` | Lexer, Tokenizer, DOM tree builder, non-visual element filtering (`head`, `script`, `style`, `meta`, `link`). |
| **CSS Engine** | `IMPLEMENTED` | Tokenizer, CSS Parser, rule matching, specificity calculation, inline style extraction. |
| **DOM Engine** | `IMPLEMENTED` | `Document`, `Element`, `TextNode`, `CommentNode` representation with query traversal. |
| **Layout Engine** | `IMPLEMENTED` | Box-model calculation (`margin`, `padding`, `border`), display mode calculation (`block`, `inline`, `none`). |
| **Render Engine** | `IMPLEMENTED` | Paint command generator (`DrawRect`, `DrawBorder`, `DrawText` with multi-line `StaticLayout` text wrapping). |
| **Minesword Canvas View** | `IMPLEMENTED` | Custom Android `View` rendering display lists with vertical scrolling and link single-tap hit testing. |
| **Networking** | `IMPLEMENTED` | Native HTTP/HTTPS network client (`HttpURLConnection`) with header parsing and TLS checks. |
| **Security Engine** | `IMPLEMENTED` | Same-Origin Policy (SOP) origin validation and scheme checking. |
| **JavaScript Bridge** | `PARTIALLY IMPLEMENTED` | Abstraction bridge for DOM manipulation and script execution. |
| **WebKit / WebView Fallback** | `PARTIALLY IMPLEMENTED` | Isolated legacy helper classes (`MineswordWebViewClient`, `MineswordWebChromeClient`, `SecurityManager`) available for fallback tasks. |
| **WebAssembly / GPU Shader Pipeline** | `NOT IMPLEMENTED` | Planned for future engine upgrades. |
