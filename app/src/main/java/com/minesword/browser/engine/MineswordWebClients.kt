package com.minesword.browser.engine

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Message
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.minesword.browser.network.NetworkStatus

class MineswordWebViewClient(
    private val onPageStartedCallback: (url: String, favicon: Bitmap?) -> Unit,
    private val onPageFinishedCallback: (url: String) -> Unit,
    private val onErrorReceivedCallback: (errorCode: Int, description: String, failingUrl: String) -> Unit,
    private val getNetworkStatus: () -> NetworkStatus
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url != null) {
            onPageStartedCallback(url, favicon)
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url != null) {
            onPageFinishedCallback(url)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        // Allow HTTP and HTTPS internal navigation within the webview
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("about:") || url.startsWith("file://")) {
            return false
        }
        return true
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val failingUrl = request.url.toString()
            val description = error?.description?.toString() ?: "Network error"
            val errorCode = error?.errorCode ?: -1
            onErrorReceivedCallback(errorCode, description, failingUrl)
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        // Strict Security: Cancel untrusted SSL certificates to protect user security
        handler?.cancel()
        val failingUrl = error?.url ?: ""
        onErrorReceivedCallback(-11, "SSL Certificate Validation Failed for $failingUrl", failingUrl)
    }
}

class MineswordWebChromeClient(
    private val onProgressChangedCallback: (progress: Int) -> Unit,
    private val onTitleReceivedCallback: (title: String) -> Unit,
    private val onFaviconReceivedCallback: (icon: Bitmap) -> Unit,
    private val onPermissionRequestedCallback: (request: PermissionRequest) -> Unit
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChangedCallback(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        if (!title.isNullOrEmpty()) {
            onTitleReceivedCallback(title)
        }
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        if (icon != null) {
            onFaviconReceivedCallback(icon)
        }
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        if (request != null) {
            onPermissionRequestedCallback(request)
        }
    }
}
