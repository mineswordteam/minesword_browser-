package com.minesword.browser.ui.tabs

import android.graphics.Bitmap
import android.webkit.WebView
import java.util.ArrayDeque
import java.util.UUID

data class TabModel(
    val id: String = UUID.randomUUID().toString(),
    var url: String = "https://www.google.com",
    var title: String = "New Tab",
    var favicon: Bitmap? = null,
    val isIncognito: Boolean = false,
    var progress: Int = 0,
    var webView: WebView? = null
) {
    val backStack = ArrayDeque<String>()
    val forwardStack = ArrayDeque<String>()

    val canGoBack: Boolean get() = backStack.isNotEmpty()
    val canGoForward: Boolean get() = forwardStack.isNotEmpty()

    fun navigateTo(newUrl: String) {
        if (url.isNotEmpty() && url != newUrl && url != "about:blank") {
            backStack.push(url)
        }
        forwardStack.clear()
        url = newUrl
    }

    fun goBack(): String? {
        if (canGoBack) {
            forwardStack.push(url)
            val prevUrl = backStack.pop()
            url = prevUrl
            return prevUrl
        }
        return null
    }

    fun goForward(): String? {
        if (canGoForward) {
            backStack.push(url)
            val nextUrl = forwardStack.pop()
            url = nextUrl
            return nextUrl
        }
        return null
    }
}

class TabManager {
    private val tabsList = mutableListOf<TabModel>()
    private var activeTabId: String? = null

    fun getTabs(): List<TabModel> = tabsList.toList()

    fun getActiveTab(): TabModel? {
        return tabsList.find { it.id == activeTabId }
    }

    fun addTab(initialUrl: String = "https://www.google.com", isIncognito: Boolean = false): TabModel {
        val newTab = TabModel(
            url = initialUrl,
            title = if (initialUrl == "about:blank" || initialUrl == "https://www.google.com") "New Tab" else initialUrl,
            isIncognito = isIncognito
        )
        tabsList.add(newTab)
        activeTabId = newTab.id
        return newTab
    }

    fun selectTab(id: String) {
        if (tabsList.any { it.id == id }) {
            activeTabId = id
        }
    }

    fun closeTab(id: String) {
        val index = tabsList.indexOfFirst { it.id == id }
        if (index != -1) {
            val tabToClose = tabsList[index]
            tabToClose.webView?.destroy()
            tabToClose.webView = null
            tabsList.removeAt(index)

            if (activeTabId == id) {
                activeTabId = when {
                    tabsList.isEmpty() -> null
                    index < tabsList.size -> tabsList[index].id
                    else -> tabsList.last().id
                }
            }
        }
    }

    fun updateActiveTabUrl(url: String) {
        getActiveTab()?.let {
            it.navigateTo(url)
        }
    }

    fun updateActiveTabTitle(title: String) {
        getActiveTab()?.let {
            it.title = title
        }
    }

    fun updateActiveTabProgress(progress: Int) {
        getActiveTab()?.let {
            it.progress = progress
        }
    }
}
