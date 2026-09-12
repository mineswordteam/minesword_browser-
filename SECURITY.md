# Security Model - Minesword Browser

## Security Implementations
- **HTTPS Enforcement**: Automatic upgrades and SSL certificate validation failure cancellation.
- **File Access Restrictions**: Disabled file and content access (`allowFileAccess = false`).
- **Mixed Content Enforcement**: Configured to `MIXED_CONTENT_NEVER_ALLOW`.
- **Safe Browsing**: Enabled via `androidx.webkit.WebViewCompat`.
- **Permission Isolation**: Site permissions explicitly handled per-origin.
