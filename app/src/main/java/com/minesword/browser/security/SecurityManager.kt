package com.minesword.browser.security

import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewCompat

object SecurityManager {

    private const val USER_AGENT_SUFFIX = " MineswordBrowser/1.0 (Mobile; Persian-First)"

    /**
     * Configures WebSettings with production security controls.
     */
    fun configureWebSettings(context: Context, webView: WebView, isIncognito: Boolean) {
        val settings = webView.settings

        // JavaScript capability
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = false

        // DOM Storage & Data Caching
        if (isIncognito) {
            settings.domStorageEnabled = false
            settings.databaseEnabled = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
        } else {
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
        }

        // Security controls against file access attacks
        settings.allowFileAccess = false
        settings.allowContentAccess = false

        // Force HTTPS & mixed content safety
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        // Viewport and rendering settings
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        // User Agent customization
        val defaultUserAgent = settings.userAgentString
        settings.userAgentString = "$defaultUserAgent $USER_AGENT_SUFFIX"

        // Secure Cookie configuration
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(!isIncognito)
        cookieManager.setAcceptThirdPartyCookies(webView, !isIncognito)

        // Enable Safe Browsing if supported on device
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebViewCompat.startSafeBrowsing(context.applicationContext) { success ->
                // Safe Browsing initialized
            }
        }
    }

    /**
     * Checks if a target URL is secure (HTTPS).
     */
    fun isSecureUrl(url: String): Boolean {
        val uri = Uri.parse(url)
        return uri.scheme?.equals("https", ignoreCase = true) == true
    }

    /**
     * Extracts host origin for permission checks.
     */
    fun extractOrigin(url: String): String {
        val uri = Uri.parse(url)
        return "${uri.scheme}://${uri.host}"
    }
}
