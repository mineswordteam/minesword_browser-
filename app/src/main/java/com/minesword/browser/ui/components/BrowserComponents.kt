package com.minesword.browser.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.DownloadListener
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.minesword.browser.R
import com.minesword.browser.data.local.entity.BookmarkEntity
import com.minesword.browser.data.local.entity.DownloadEntity
import com.minesword.browser.data.local.entity.HistoryEntity
import com.minesword.browser.engine.MineswordWebChromeClient
import com.minesword.browser.engine.MineswordWebViewClient
import com.minesword.browser.network.NetworkStatus
import com.minesword.browser.search.SearchEngine
import com.minesword.browser.security.SecurityManager
import com.minesword.browser.ui.tabs.TabModel
import com.minesword.browser.ui.viewmodel.BrowserViewModel

@Composable
fun AddressBar(
    url: String,
    onUrlSubmitted: (String) -> Unit,
    isIncognito: Boolean,
    onTabsClicked: () -> Unit,
    tabCount: Int,
    onMenuClicked: () -> Unit
) {
    var text by remember(url) { mutableStateOf(url) }
    val isSecure = SecurityManager.isSecureUrl(url)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isIncognito) Icons.Default.VisibilityOff else if (isSecure) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Security Status",
                tint = if (isIncognito) Color.Magenta else if (isSecure) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_hint),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onUrlSubmitted(text) })
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onTabsClicked() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabCount.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onMenuClicked) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onRefresh: () -> Unit,
    onAddTab: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, enabled = canGoBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = onForward, enabled = canGoForward) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
            }
            IconButton(onClick = onHome) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
            IconButton(onClick = onAddTab) {
                Icon(Icons.Default.Add, contentDescription = "New Tab")
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: TabModel,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentNetworkStatus by viewModel.networkMonitor.networkStatus.collectAsState()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                SecurityManager.configureWebSettings(ctx, this, tab.isIncognito)

                webViewClient = MineswordWebViewClient(
                    onPageStartedCallback = { url, _ ->
                        viewModel.onPageStarted(url)
                    },
                    onPageFinishedCallback = { url ->
                        viewModel.onPageFinished(url, title ?: url)
                        tab.canGoBack = canGoBack()
                        tab.canGoForward = canGoForward()
                    },
                    onErrorReceivedCallback = { code, desc, failingUrl ->
                        // Load custom offline or error page
                    },
                    getNetworkStatus = { viewModel.networkMonitor.getCurrentStatus() }
                )

                webChromeClient = MineswordWebChromeClient(
                    onProgressChangedCallback = { progress ->
                        viewModel.onProgressChanged(progress)
                    },
                    onTitleReceivedCallback = { title ->
                        viewModel.onPageFinished(url ?: "", title)
                    },
                    onFaviconReceivedCallback = { bitmap ->
                        tab.favicon = bitmap
                    },
                    onPermissionRequestedCallback = { request ->
                        request.grant(request.resources)
                    }
                )

                setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                    viewModel.downloadManager.startDownload(url, userAgent, contentDisposition, mimetype)
                }

                tab.webView = this
                loadUrl(tab.url)
            }
        },
        update = { webView ->
            if (webView.url != tab.url && tab.url != "about:blank") {
                webView.loadUrl(tab.url)
            }
        }
    )
}

@Composable
fun TabGridOverlay(
    tabs: List<TabModel>,
    selectedTabId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.tabs),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    Button(onClick = { onNewTab(false) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.new_tab))
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Tabs")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tabs) { tab ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable {
                                onSelectTab(tab.id)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (tab.id == selectedTabId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tab.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                IconButton(
                                    onClick = { onCloseTab(tab.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Tab", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = tab.url,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
