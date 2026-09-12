package com.minesword.browser.engine.minesword.security

import java.net.URI

object MineswordSecurityEngine {

    fun validateOrigin(originUrl: String, targetUrl: String): Boolean {
        return try {
            val originUri = URI(originUrl)
            val targetUri = URI(targetUrl)

            val sameScheme = originUri.scheme != null && originUri.scheme.equals(targetUri.scheme, ignoreCase = true)
            val sameHost = originUri.host != null && originUri.host.equals(targetUri.host, ignoreCase = true)
            val samePort = (originUri.port == targetUri.port)

            sameScheme && sameHost && samePort
        } catch (e: Exception) {
            false
        }
    }

    fun isSecureScheme(url: String): Boolean {
        return url.startsWith("https://", ignoreCase = true) || url.startsWith("file://", ignoreCase = true)
    }
}
