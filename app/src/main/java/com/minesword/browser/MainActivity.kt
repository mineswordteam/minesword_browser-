package com.minesword.browser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.minesword.browser.engine.minesword.MineswordCanvasView
import com.minesword.browser.engine.minesword.MineswordEngineHost
import com.minesword.browser.network.NetworkStatus
import com.minesword.browser.ui.components.*
import com.minesword.browser.ui.theme.MineswordBrowserTheme
import com.minesword.browser.ui.viewmodel.BrowserViewModel

enum class ScreenState {
    HOME,
    BROWSER,
    TABS,
    SETTINGS,
    HISTORY,
    BOOKMARKS
}

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()
    private val engineHost = MineswordEngineHost()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            MineswordBrowserTheme {
                val currentUrl by viewModel.currentUrl.collectAsState()
                val currentTitle by viewModel.currentTitle.collectAsState()
                val tabs by viewModel.tabs.collectAsState()
                val selectedTab by viewModel.selectedTab.collectAsState()
                val isIncognito by viewModel.isIncognitoMode.collectAsState()
                val networkStatus by viewModel.networkMonitor.networkStatus.collectAsState()

                var currentScreen by remember { mutableStateOf(ScreenState.HOME) }
                var showMenu by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        if (currentScreen != ScreenState.HOME) {
                            Column {
                                FuturisticAddressBar(
                                    url = currentUrl,
                                    onUrlSubmitted = { input ->
                                        viewModel.submitQueryOrUrl(input)
                                        currentScreen = ScreenState.BROWSER
                                    },
                                    isIncognito = isIncognito,
                                    onTabsClicked = { currentScreen = ScreenState.TABS },
                                    tabCount = tabs.size,
                                    onMenuClicked = { showMenu = true }
                                )

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
                        }
                    },
                    bottomBar = {
                        if (currentScreen == ScreenState.BROWSER || currentScreen == ScreenState.HOME) {
                            FuturisticBottomNav(
                                canGoBack = selectedTab?.canGoBack ?: false,
                                canGoForward = selectedTab?.canGoForward ?: false,
                                onBack = { viewModel.goBackInTab() },
                                onForward = { viewModel.goForwardInTab() },
                                onHome = { currentScreen = ScreenState.HOME },
                                onRefresh = { viewModel.refreshActiveTab() },
                                onAddTab = {
                                    viewModel.addNewTab("https://www.google.com", false)
                                    currentScreen = ScreenState.BROWSER
                                }
                            )
                        }
                    },
                    containerColor = Color(0xFF121824)
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (currentScreen) {
                            ScreenState.HOME -> {
                                MineswordHomeScreen(
                                    onSearchSubmitted = { query ->
                                        viewModel.submitQueryOrUrl(query)
                                        currentScreen = ScreenState.BROWSER
                                    }
                                )
                            }
                            ScreenState.BROWSER -> {
                                var loadedHtmlBody by remember { mutableStateOf("") }

                                LaunchedEffect(currentUrl) {
                                    val response = engineHost.loadPage(currentUrl)
                                    loadedHtmlBody = response.body
                                }

                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        MineswordCanvasView(ctx).also { view ->
                                            view.onLinkClicked = { clickedUrl ->
                                                viewModel.submitQueryOrUrl(clickedUrl)
                                            }
                                        }
                                    },
                                    update = { view ->
                                        if (loadedHtmlBody.isNotEmpty()) {
                                            view.loadHtmlContent(loadedHtmlBody, pageUrl = currentUrl)
                                        }
                                    }
                                )
                            }
                            ScreenState.TABS -> {
                                FuturisticTabSwitcher(
                                    tabs = tabs,
                                    selectedTabId = selectedTab?.id,
                                    onSelectTab = { id ->
                                        viewModel.selectTab(id)
                                        currentScreen = ScreenState.BROWSER
                                    },
                                    onCloseTab = { id -> viewModel.closeTab(id) },
                                    onNewTab = { incognito ->
                                        viewModel.addNewTab(isIncognito = incognito)
                                        currentScreen = ScreenState.BROWSER
                                    },
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
                                    onUrlSelected = { url ->
                                        viewModel.submitQueryOrUrl(url)
                                        currentScreen = ScreenState.BROWSER
                                    },
                                    onBack = { currentScreen = ScreenState.BROWSER }
                                )
                            }
                            ScreenState.BOOKMARKS -> {
                                BookmarksScreen(
                                    viewModel = viewModel,
                                    onUrlSelected = { url ->
                                        viewModel.submitQueryOrUrl(url)
                                        currentScreen = ScreenState.BROWSER
                                    },
                                    onBack = { currentScreen = ScreenState.BROWSER }
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_tab)) },
                                onClick = {
                                    viewModel.addNewTab("https://www.google.com", false)
                                    currentScreen = ScreenState.BROWSER
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.incognito_tab)) },
                                onClick = {
                                    viewModel.addNewTab("https://www.google.com", true)
                                    currentScreen = ScreenState.BROWSER
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) }
                            )
                            HorizontalDivider()
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
                            HorizontalDivider()
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
