package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExtensionPluginEntity
import com.example.ui.theme.ShieldActiveGreen

data class OnlineStoreExtension(
    val name: String,
    val description: String,
    val category: String,
    val version: String,
    val rating: String,
    val downloads: String,
    val iconName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionStoreModal(
    plugins: List<ExtensionPluginEntity>,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallCustomPlugin: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    var customName by remember { mutableStateOf("") }
    var customDesc by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("UserScript") }

    val onlineCatalog = remember {
        listOf(
            OnlineStoreExtension(
                name = "SponsorBlock for Web",
                description = "Skip YouTube sponsor segments, intros, outros, and subscription reminders automatically.",
                category = "Media",
                version = "5.4.1",
                rating = "4.9 ★",
                downloads = "1.2M",
                iconName = "play_circle"
            ),
            OnlineStoreExtension(
                name = "Tampermonkey JS Engine",
                description = "Run custom Userscripts and DOM automation macros on any website.",
                category = "Developer",
                version = "5.1.0",
                rating = "4.8 ★",
                downloads = "2.5M",
                iconName = "code"
            ),
            OnlineStoreExtension(
                name = "AdGuard Extra Anti-Ad",
                description = "Advanced ad-blocking scripts designed to bypass ad-block detection on complex sites.",
                category = "Ad Blocking",
                version = "1.3.88",
                rating = "4.9 ★",
                downloads = "890K",
                iconName = "shield"
            ),
            OnlineStoreExtension(
                name = "JSON Pretty Viewer Pro",
                description = "Automatically formats raw JSON responses into readable, syntax-highlighted tree views.",
                category = "Developer",
                version = "2.0.4",
                rating = "4.7 ★",
                downloads = "450K",
                iconName = "code"
            ),
            OnlineStoreExtension(
                name = "User-Agent Switcher Pro",
                description = "Quickly spoof device signatures to simulate Chrome Desktop, Firefox, Safari, or Linux.",
                category = "Utilities",
                version = "3.2.1",
                rating = "4.6 ★",
                downloads = "620K",
                iconName = "fingerprint"
            ),
            OnlineStoreExtension(
                name = "Cyberpunk Matrix HUD FX",
                description = "Gives the browser interface a retro hacker green HUD overlay with matrix diagnostics.",
                category = "Appearance",
                version = "1.0.0",
                rating = "5.0 ★",
                downloads = "310K",
                iconName = "dark_mode"
            )
        )
    }

    val filteredInstalled = plugins.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
    }

    val filteredOnline = onlineCatalog.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Extension,
                        contentDescription = "Extensions",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Web Extension Store",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Download, Install & Manage Extensions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.weight(1f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Installed (${plugins.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Online Store 🌐") }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showCreateDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Script", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (selectedTab == 0) "Search installed extensions..." else "Search online extensions store...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Installed Extensions List
                if (filteredInstalled.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No installed extensions found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredInstalled, key = { it.id }) { plugin ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = getPluginIcon(plugin.iconName),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = plugin.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "v${plugin.version}",
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = plugin.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Switch(
                                        checked = plugin.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            onTogglePlugin(plugin.id, isChecked)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Online Store Catalog List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOnline) { storeExt ->
                        val isAlreadyInstalled = plugins.any { it.name.contains(storeExt.name.take(10), ignoreCase = true) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape),
                                    color = ShieldActiveGreen.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = getPluginIcon(storeExt.iconName),
                                            contentDescription = null,
                                            tint = ShieldActiveGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = storeExt.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = ShieldActiveGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = storeExt.rating,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ShieldActiveGreen,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = storeExt.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (isAlreadyInstalled) {
                                    Button(
                                        onClick = { },
                                        enabled = false,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Installed", fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            onInstallCustomPlugin(
                                                storeExt.name,
                                                storeExt.description,
                                                storeExt.category,
                                                storeExt.iconName
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ShieldActiveGreen),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Install", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Custom Script Creation Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Install Custom Script / Extension") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Extension Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customDesc,
                            onValueChange = { customDesc = it },
                            label = { Text("Description") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customCategory,
                            onValueChange = { customCategory = it },
                            label = { Text("Category (e.g. Privacy, Developer, AdBlock)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customName.isNotBlank()) {
                                onInstallCustomPlugin(
                                    customName,
                                    if (customDesc.isBlank()) "Custom installed extension script." else customDesc,
                                    customCategory,
                                    "code"
                                )
                                showCreateDialog = false
                                customName = ""
                                customDesc = ""
                            }
                        }
                    ) {
                        Text("Download & Install")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun getPluginIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "shield" -> Icons.Outlined.Shield
        "security" -> Icons.Outlined.Security
        "lock" -> Icons.Outlined.Lock
        "dark_mode" -> Icons.Outlined.DarkMode
        "code" -> Icons.Outlined.Code
        "cookie" -> Icons.Outlined.Cookie
        "fingerprint" -> Icons.Outlined.Fingerprint
        "play_circle" -> Icons.Outlined.PlayCircle
        else -> Icons.Outlined.Extension
    }
}
