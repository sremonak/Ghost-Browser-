package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.SearchEngines
import com.example.ui.viewmodel.BrowserTabState

/**
 * Horizontal strip of open tabs shown above the toolbar, like a desktop
 * browser. The active tab is highlighted; long titles are truncated.
 */
@Composable
fun TabStrip(
    tabs: List<BrowserTabState>,
    activeTabId: String,
    modifier: Modifier = Modifier,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 38.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(start = 8.dp, end = 4.dp),
                userScrollEnabled = true
            ) {
                items(tabs, key = { it.id }) { tab ->
                    val isActive = tab.id == activeTabId
                    val targetColor = MaterialTheme.colorScheme.surface
                    val elevation = if (isActive) 2.dp else 0.dp
                    TabChip(
                        tab = tab,
                        isActive = isActive,
                        onClick = { onSelectTab(tab.id) },
                        onClose = { onCloseTab(tab.id) }
                    )
                }
            }
            IconButton(onClick = onNewTab) {
                Icon(Icons.Filled.Add, contentDescription = "New tab")
            }
        }
    }
}

@Composable
private fun TabChip(
    tab: BrowserTabState,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabChip"
    )
    Surface(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        color = containerColor,
        tonalElevation = if (isActive) 1.dp else 0.dp,
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tab.isIncognito) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = tab.title.ifBlank { SearchEngines.displayDomain(tab.url) }
                        .ifBlank { "New Tab" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close tab",
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
