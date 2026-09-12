package com.minesword.browser.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.minesword.browser.data.local.db.MineswordDatabase
import com.minesword.browser.data.local.entity.BookmarkEntity
import com.minesword.browser.data.local.entity.HistoryEntity
import com.minesword.browser.download.MineswordDownloadManager
import com.minesword.browser.network.NetworkStateMonitor
import com.minesword.browser.network.NetworkStatus
import com.minesword.browser.search.SearchEngine
import com.minesword.browser.search.SearchEngineRouter
import com.minesword.browser.ui.tabs.TabManager
import com.minesword.browser.ui.tabs.TabModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MineswordDatabase.getInstance(application)
    val networkMonitor = NetworkStateMonitor(application)
    val downloadManager = MineswordDownloadManager(application, db.downloadDao())
    val tabManager = TabManager()

    val historyList = db.historyDao().getAllHistory()
    val bookmarksList = db.bookmarkDao().getAllBookmarks()
    val downloadsList = db.downloadDao().getAllDownloads()

    private val _currentUrl = MutableStateFlow("https://www.google.com")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _currentTitle = MutableStateFlow("Minesword Browser")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _tabs = MutableStateFlow<List<TabModel>>(emptyList())
    val tabs: StateFlow<List<TabModel>> = _tabs.asStateFlow()

    private val _selectedTab = MutableStateFlow<TabModel?>(null)
    val selectedTab: StateFlow<TabModel?> = _selectedTab.asStateFlow()

    private val _isIncognitoMode = MutableStateFlow(false)
    val isIncognitoMode: StateFlow<Boolean> = _isIncognitoMode.asStateFlow()

    private val _selectedSearchEngine = MutableStateFlow(SearchEngine.GOOGLE)
    val selectedSearchEngine: StateFlow<SearchEngine> = _selectedSearchEngine.asStateFlow()

    init {
        // Initialize default tab
        val tab = tabManager.addTab("https://www.google.com")
        refreshTabsState()
    }

    private fun refreshTabsState() {
        _tabs.value = tabManager.getTabs()
        val active = tabManager.getActiveTab()
        _selectedTab.value = active
        active?.let {
            _currentUrl.value = it.url
            _currentTitle.value = it.title
            _loadingProgress.value = it.progress
            _isIncognitoMode.value = it.isIncognito
        }
    }

    fun submitQueryOrUrl(input: String) {
        val destinationUrl = SearchEngineRouter.processInput(input, _selectedSearchEngine.value)
        _currentUrl.value = destinationUrl
        tabManager.updateActiveTabUrl(destinationUrl)
        refreshTabsState()
    }

    fun onPageStarted(url: String) {
        _currentUrl.value = url
        tabManager.updateActiveTabUrl(url)
        refreshTabsState()
    }

    fun onPageFinished(url: String, title: String) {
        _currentUrl.value = url
        _currentTitle.value = title
        tabManager.updateActiveTabUrl(url)
        tabManager.updateActiveTabTitle(title)
        refreshTabsState()

        // Persist history if not in Incognito mode
        if (!(_selectedTab.value?.isIncognito ?: false) && url != "about:blank") {
            viewModelScope.launch {
                db.historyDao().insertHistory(
                    HistoryEntity(
                        url = url,
                        title = title.ifBlank { url }
                    )
                )
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        _loadingProgress.value = progress
        tabManager.updateActiveTabProgress(progress)
    }

    fun addNewTab(url: String = "https://www.google.com", isIncognito: Boolean = false) {
        tabManager.addTab(url, isIncognito)
        refreshTabsState()
    }

    fun selectTab(tabId: String) {
        tabManager.selectTab(tabId)
        refreshTabsState()
    }

    fun closeTab(tabId: String) {
        tabManager.closeTab(tabId)
        if (tabManager.getTabs().isEmpty()) {
            tabManager.addTab("https://www.google.com")
        }
        refreshTabsState()
    }

    fun toggleBookmark(url: String, title: String) {
        viewModelScope.launch {
            val existing = db.bookmarkDao().getBookmarkByUrl(url)
            if (existing != null) {
                db.bookmarkDao().deleteBookmarkByUrl(url)
            } else {
                db.bookmarkDao().insertBookmark(BookmarkEntity(url = url, title = title))
            }
        }
    }

    fun setSearchEngine(engine: SearchEngine) {
        _selectedSearchEngine.value = engine
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.historyDao().clearAllHistory()
        }
    }
}
