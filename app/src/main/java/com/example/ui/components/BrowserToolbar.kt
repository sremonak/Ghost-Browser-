package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SearchEngines
import com.example.ui.viewmodel.BrowserTabState

/**
 * The browser address bar (omnibox) plus the surrounding navigation buttons.
 * Doubles as a search box: typed input that is not a URL is resolved through
 * the active search engine.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserToolbar(
    tab: BrowserTabState,
    searchEngine: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLoading: Boolean,
    progress: Float,
    isBookmarked: Boolean,
    isIncognito: Boolean,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onOpenTabs: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Progress indicator under the bar while a page is loading.
        if (isLoading && progress > 0f) {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth(if (progress >= 1f) 1f else progress.coerceIn(0.05f, 0.99f))
                        .height(3.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation buttons
            IconButton(onClick = onGoBack, enabled = canGoBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            }
            IconButton(onClick = onGoForward, enabled = canGoForward) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Forward", tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            }
            IconButton(onClick = if (isLoading) onStop else onReload) {
                Icon(
                    if (isLoading) Icons.Filled.Clear else Icons.Filled.Refresh,
                    contentDescription = if (isLoading) "Stop" else "Reload",
                    modifier = Modifier.size(22.dp)
                )
            }

            // Omnibox
            var text by remember(tab.url, tab.isIncognito) { mutableStateOf(displayText(tab.url)) }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                leadingIcon = {
                    Icon(
                        if (SearchEngines.isSecureUrl(tab.url)) Icons.Filled.Lock else Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (SearchEngines.isSecureUrl(tab.url)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onOpenTabs) {
                            Text(
                                text = "${tab.title.take(1).uppercase()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isIncognito) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onOpenMenu) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(21.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onNavigate(text) })
            )
        }
    }
}

private fun displayText(url: String): String {
    return when {
        url.startsWith("nova://home") -> ""
        url.startsWith("nova://") -> ""
        url.startsWith("about:") -> url
        else -> url
    }
}
