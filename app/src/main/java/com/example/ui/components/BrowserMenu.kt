package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dropdown menu shown from the toolbar's overflow button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onHistory: () -> Unit,
    onBookmarks: () -> Unit,
    onDownloads: () -> Unit,
    onSettings: () -> Unit,
    onSiteInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, modifier = modifier) {
        DropdownMenuItem(
            text = { Text("New tab") },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { onNewTab(); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text("New incognito tab") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { onNewIncognitoTab(); onDismiss() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("History") },
            leadingIcon = { Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { onHistory(); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text("Bookmarks") },
            leadingIcon = { Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { onBookmarks(); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text("Downloads") },
            leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { onDownloads(); onDismiss() }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Site information") },
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { onSiteInfo(); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text("Settings") },
            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { onSettings(); onDismiss() }
        )
    }
}
