package com.minesword.browser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minesword.browser.R
import com.minesword.browser.data.local.entity.BookmarkEntity
import com.minesword.browser.data.local.entity.HistoryEntity
import com.minesword.browser.search.SearchEngine
import com.minesword.browser.ui.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    onBack: () -> Unit
) {
    val selectedEngine by viewModel.selectedSearchEngine.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Search Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            SearchEngine.values().forEach { engine ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSearchEngine(engine) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (engine == selectedEngine),
                        onClick = { viewModel.setSearchEngine(engine) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = engine.displayName, fontSize = 16.sp)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(id = R.string.about_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Minesword Browser v1.0.0",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(id = R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Developer: mineswordteam\nTeam: Minesword Team",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
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
                title = { Text(stringResource(id = R.string.history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History")
                    }
                }
            )
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No history recorded yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(historyList) { item ->
                    ListItem(
                        headlineContent = { Text(item.title, maxLines = 1) },
                        supportingContent = { Text(item.url, maxLines = 1) },
                        modifier = Modifier.clickable {
                            onUrlSelected(item.url)
                            onBack()
                        }
                    )
                    Divider()
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
                title = { Text(stringResource(id = R.string.bookmarks)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (bookmarksList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No bookmarks added yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(bookmarksList) { bookmark ->
                    ListItem(
                        headlineContent = { Text(bookmark.title, maxLines = 1) },
                        supportingContent = { Text(bookmark.url, maxLines = 1) },
                        modifier = Modifier.clickable {
                            onUrlSelected(bookmark.url)
                            onBack()
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
