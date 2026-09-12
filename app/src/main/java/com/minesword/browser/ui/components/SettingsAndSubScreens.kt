package com.minesword.browser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minesword.browser.R
import com.minesword.browser.search.SearchEngine
import com.minesword.browser.ui.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    onBack: () -> Unit
) {
    val selectedEngine by viewModel.selectedSearchEngine.collectAsState()

    val categories = listOf(
        "General", "Appearance", "Search Engine", "Privacy", "Security",
        "Permissions", "Downloads", "Passwords", "Languages", "Accessibility",
        "Performance", "Offline", "Advanced", "About"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings), color = Color(0xFF00E5FF)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121824))
            )
        },
        containerColor = Color(0xFF121824)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Search Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchEngine.values().forEach { engine ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setSearchEngine(engine) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (engine == selectedEngine),
                            onClick = { viewModel.setSearchEngine(engine) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = engine.displayName, fontSize = 16.sp, color = Color.White)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)
            }

            items(categories) { category ->
                ListItem(
                    headlineContent = { Text(category, color = Color.White, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Configure $category settings", color = Color.Gray, fontSize = 12.sp) },
                    colors = ListItemDefaults.colors(containerColor = Color(0xFF1E2638))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: BrowserViewModel,
    onUrlSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val historyList by viewModel.historyList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.history), color = Color(0xFF00E5FF)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121824))
            )
        },
        containerColor = Color(0xFF121824)
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No history recorded yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(historyList) { item ->
                    ListItem(
                        headlineContent = { Text(item.title, maxLines = 1, color = Color.White) },
                        supportingContent = { Text(item.url, maxLines = 1, color = Color(0xFF00E5FF)) },
                        colors = ListItemDefaults.colors(containerColor = Color(0xFF1E2638)),
                        modifier = Modifier.clickable {
                            onUrlSelected(item.url)
                            onBack()
                        }
                    )
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BrowserViewModel,
    onUrlSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val bookmarksList by viewModel.bookmarksList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.bookmarks), color = Color(0xFF00E5FF)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121824))
            )
        },
        containerColor = Color(0xFF121824)
    ) { padding ->
        if (bookmarksList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No bookmarks added yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(bookmarksList) { bookmark ->
                    ListItem(
                        headlineContent = { Text(bookmark.title, maxLines = 1, color = Color.White) },
                        supportingContent = { Text(bookmark.url, maxLines = 1, color = Color(0xFF00E5FF)) },
                        colors = ListItemDefaults.colors(containerColor = Color(0xFF1E2638)),
                        modifier = Modifier.clickable {
                            onUrlSelected(bookmark.url)
                            onBack()
                        }
                    )
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
        }
    }
}
