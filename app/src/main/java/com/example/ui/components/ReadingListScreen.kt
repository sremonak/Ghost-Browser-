package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ReadingListEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reading-list screen: pages saved for later reading, grouped by day and
 * searchable, like the reading list of top-tier browsers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingListScreen(
    items: List<ReadingListEntity>,
    modifier: Modifier = Modifier,
    onOpen: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading list") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear all", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = { Text("Search reading list") },
                leadingIcon = { Icon(Icons.Filled.Article, contentDescription = null) },
                shapes = listOf(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(Modifier.height(12.dp))

            val filtered = items.filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.domain.contains(query, ignoreCase = true) ||
                    it.url.contains(query, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Article,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (items.isEmpty()) "Nothing saved yet" else "No matches",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (items.isEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Long-press a link and pick “Save for later reading”",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val dayFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                val sections = filtered.groupBy { dayFormatter.format(Date(it.savedAt)) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sections.forEach { (day, entries) ->
                        item(key = "header_$day") {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }
                        items(entries, key = { it.id }) { entry ->
                            ReadingListRow(
                                entry = entry,
                                onOpen = { onOpen(entry.url) },
                                onDelete = { onDelete(entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingListRow(entry: ReadingListEntity, onOpen: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Article,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title.ifBlank { entry.domain },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.domain,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
