# Search Engine Architecture - Minesword Browser

## Query Processing
`SearchEngineRouter` evaluates user input from the address bar:
1. **URL Recognition**: Identifies explicit schemes (`http://`, `https://`, `about:`, `file://`), IP addresses (`192.168.1.1`), `localhost`, and valid domain names (`domain.com`, `site.ir`).
2. **Persian Normalizer**: Normalizes Persian/Arabic variants (`ی` vs `ي`, `ک` vs `ك`), strips invalid control characters, and maps Persian numerals to standard digits for host lookup.
3. **Search Provider Routing**: Formats queries into Google, DuckDuckGo, Bing, or Parsijoo search templates.
