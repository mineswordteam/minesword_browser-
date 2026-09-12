package com.minesword.browser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minesword.browser.network.NetworkStatus
import com.minesword.browser.ui.components.*
import com.minesword.browser.ui.theme.MineswordBrowserTheme
import com.minesword.browser.ui.viewmodel.BrowserViewModel

enum class ScreenState {
    BROWSER,
    TABS,
    SETTINGS,
    HISTORY,
    BOOKMARKS
}

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            MineswordBrowserTheme {
                val currentUrl by viewModel.currentUrl.collectAsState()
                val currentTitle by viewModel.currentTitle.collectAsState()
                val loadingProgress by viewModel.loadingProgress.collectAsState()
                val tabs by viewModel.tabs.collectAsState()
                val selectedTab by viewModel.selectedTab.collectAsState()
                val isIncognito by viewModel.isIncognitoMode.collectAsState()
                val networkStatus by viewModel.networkMonitor.networkStatus.collectAsState()

                var currentScreen by remember { mutableStateOf(ScreenState.BROWSER) }
                var showMenu by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        Column {
                            // Address Bar
                            AddressBar(
                                url = currentUrl,
                                onUrlSubmitted = { input ->
                                    viewModel.submitQueryOrUrl(input)
                                },
                                isIncognito = isIncognito,
                                onTabsClicked = { currentScreen = ScreenState.TABS },
                                tabCount = tabs.size,
                                onMenuClicked = { showMenu = true }
                            )

                            // Loading Progress Bar
                            if (loadingProgress in 1..99) {
                                LinearProgressIndicator(
                                    progress = { loadingProgress / 100f },
                                    modifier = Modifier.fillMaxWidth().height(3.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Network Status Alert Banner
                            if (networkStatus != NetworkStatus.ONLINE) {
                                Surface(
                                    color = if (networkStatus == NetworkStatus.OFFLINE) Color(0xFFD32F2F) else Color(0xFFF57C00),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (networkStatus == NetworkStatus.OFFLINE)
                                            stringResource(R.string.offline_message)
                                        else
                                            stringResource(R.string.local_network_message),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (currentScreen == ScreenState.BROWSER) {
                            BottomNavigationBar(
                                canGoBack = selectedTab?.canGoBack ?: false,
                                canGoForward = selectedTab?.canGoForward ?: false,
                                onBack = { selectedTab?.webView?.goBack() },
                                onForward = { selectedTab?.webView?.goForward() },
                                onHome = { viewModel.submitQueryOrUrl("https://www.google.com") },
                                onRefresh = { selectedTab?.webView?.reload() },
                                onAddTab = { viewModel.addNewTab("https://www.google.com", false) }
                            )
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (currentScreen) {
                            ScreenState.BROWSER -> {
                                selectedTab?.let { tab ->
                                    BrowserWebView(
                                        tab = tab,
                                        viewModel = viewModel
                                    )
                                }
                            }
                            ScreenState.TABS -> {
                                TabGridOverlay(
                                    tabs = tabs,
                                    selectedTabId = selectedTab?.id,
                                    onSelectTab = { id -> viewModel.selectTab(id) },
                                    onCloseTab = { id -> viewModel.closeTab(id) },
                                    onNewTab = { incognito -> viewModel.addNewTab(isIncognito = incognito) },
                                    onDismiss = { currentScreen = ScreenState.BROWSER }
                                )
                            }
                            ScreenState.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = ScreenState.BROWSER }
                                )
                            }
                            ScreenState.HISTORY -> {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    onUrlSelected = { url -> viewModel.submitQueryOrUrl(url) },
                                    onBack = { currentScreen = ScreenState.BROWSER }
                                )
                            }
                            ScreenState.BOOKMARKS -> {
                                BookmarksScreen(
                                    viewModel = viewModel,
                                    onUrlSelected = { url -> viewModel.submitQueryOrUrl(url) },
                                    onBack = { currentScreen = ScreenState.BROWSER }
                                )
                            }
                        }

                        // Overflow Menu Dropdown
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_tab)) },
                                onClick = {
                                    viewModel.addNewTab("https://www.google.com", false)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.incognito_tab)) },
                                onClick = {
                                    viewModel.addNewTab("https://www.google.com", true)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_bookmark)) },
                                onClick = {
                                    viewModel.toggleBookmark(currentUrl, currentTitle)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.BookmarkBorder, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.bookmarks)) },
                                onClick = {
                                    currentScreen = ScreenState.BOOKMARKS
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.history)) },
                                onClick = {
                                    currentScreen = ScreenState.HISTORY
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                onClick = {
                                    currentScreen = ScreenState.SETTINGS
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val dataUrl = intent.dataString
            if (!dataUrl.isNullOrEmpty()) {
                viewModel.submitQueryOrUrl(dataUrl)
            }
        }
    }
}
