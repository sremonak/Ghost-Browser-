package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TabGroupEntity
import com.example.ui.theme.ShieldActiveGreen
import com.example.ui.theme.AdBlockRed
import com.example.ui.viewmodel.BrowserTabState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromeTabSwitcher(
    tabs: List<BrowserTabState>,
    activeTabId: String,
    tabGroups: List<TabGroupEntity>,
    isIncognitoMode: Boolean,
    activeGroupFilterId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onCreateGroup: (String, String) -> Unit,
    onSetGroupFilter: (String?) -> Unit,
    onToggleIncognito: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var groupNameInput by remember { mutableStateOf("") }

    val filteredTabs = tabs.filter {
        it.isIncognito == isIncognitoMode && (activeGroupFilterId == null || it.tabGroupId == activeGroupFilterId)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Incognito / Normal Switcher Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Normal vs Incognito Pill Selector
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = !isIncognitoMode,
                        onClick = { onToggleIncognito(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Tab, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Normal (${tabs.count { !it.isIncognito }})", fontSize = 12.sp)
                        }
                    }
                    SegmentedButton(
                        selected = isIncognitoMode,
                        onClick = { onToggleIncognito(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ghost (${tabs.count { it.isIncognito }})", fontSize = 12.sp)
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Tab Switcher")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Groups Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TAB GROUPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showCreateGroupDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("New Group", fontSize = 11.sp)
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = activeGroupFilterId == null,
                        onClick = { onSetGroupFilter(null) },
                        label = { Text("All Tabs") }
                    )
                }
                items(tabGroups) { group ->
                    FilterChip(
                        selected = activeGroupFilterId == group.id,
                        onClick = { onSetGroupFilter(if (activeGroupFilterId == group.id) null else group.id) },
                        label = { Text(group.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(group.colorHex))
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs Grid
            if (filteredTabs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isIncognitoMode) Icons.Filled.VisibilityOff else Icons.Outlined.Tab,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isIncognitoMode) "No Incognito Tabs Open" else "No Open Tabs",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onNewTab(isIncognitoMode) }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open New Tab")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTabs, key = { it.id }) { tab ->
                        TabGridCard(
                            tab = tab,
                            isActive = tab.id == activeTabId,
                            tabGroup = tabGroups.find { it.id == tab.tabGroupId },
                            onSelect = { onSelectTab(tab.id) },
                            onClose = { onCloseTab(tab.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Add New Tab FAB Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTabs.size} tabs open",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FloatingActionButton(
                    onClick = { onNewTab(isIncognitoMode) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Tab")
                }
            }
        }

        // Create Group Dialog
        if (showCreateGroupDialog) {
            AlertDialog(
                onDismissRequest = { showCreateGroupDialog = false },
                title = { Text("Create Tab Group") },
                text = {
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        label = { Text("Group Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (groupNameInput.isNotBlank()) {
                                onCreateGroup(groupNameInput, "#8AB4F8")
                                groupNameInput = ""
                                showCreateGroupDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateGroupDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TabGridCard(
    tab: BrowserTabState,
    isActive: Boolean,
    tabGroup: TabGroupEntity?,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(160.dp)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) ShieldActiveGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tab.isIncognito) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Header with Title & Close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tabGroup != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(tabGroup.colorHex))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = tab.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tab Preview Snapshot Mock
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (tab.isIncognito) Icons.Filled.VisibilityOff else Icons.Outlined.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.url,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tab.isSleeping) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = ShieldActiveGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Sleeping (RAM Saved)",
                                fontSize = 9.sp,
                                color = ShieldActiveGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF8AB4F8)
    }
}
