package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MemoryOptimizationLevel
import com.example.engine.UserAgentMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostSettingsModal(
    isDarkTheme: Boolean,
    userAgentMode: UserAgentMode,
    memoryLevel: MemoryOptimizationLevel,
    searchEngine: String,
    isHackerTheme: Boolean = false,
    onToggleTheme: () -> Unit,
    onToggleHackerTheme: () -> Unit = {},
    onSelectUserAgentMode: (UserAgentMode) -> Unit,
    onSelectMemoryLevel: (MemoryOptimizationLevel) -> Unit,
    onSelectSearchEngine: (String) -> Unit,
    onOpenVpnModal: () -> Unit = {},
    onOpenAboutModal: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUaMenu by remember { mutableStateOf(false) }
    var showMemoryMenu by remember { mutableStateOf(false) }
    var showEngineMenu by remember { mutableStateOf(false) }

    val searchEngines = listOf("DuckDuckGo", "Google", "Brave", "Ecosia", "Startpage")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ghost Browser Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chrome Style • Speed & Privacy Engine",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(360.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Theme Toggle
                item {
                    SettingsCard(
                        title = "Browser Theme",
                        subtitle = if (isDarkTheme) "Chrome Dark Mode" else "Chrome Light Mode",
                        icon = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        trailing = {
                            IconButton(onClick = onToggleTheme) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        }
                    )
                }

                // Hacker Matrix Mode Toggle
                item {
                    SettingsCard(
                        title = "Hacker Cyberpunk Theme 🟢",
                        subtitle = if (isHackerTheme) "Matrix Green Console Active" else "Standard Theme Active",
                        icon = Icons.Outlined.Terminal,
                        trailing = {
                            Switch(
                                checked = isHackerTheme,
                                onCheckedChange = { onToggleHackerTheme() }
                            )
                        }
                    )
                }

                // User Agent Masking
                item {
                    Box {
                        SettingsCard(
                            title = "User Agent Masking (Anti-Fingerprint)",
                            subtitle = userAgentMode.displayName,
                            icon = Icons.Outlined.Fingerprint,
                            onClick = { showUaMenu = true }
                        )

                        DropdownMenu(
                            expanded = showUaMenu,
                            onDismissRequest = { showUaMenu = false }
                        ) {
                            UserAgentMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.displayName) },
                                    onClick = {
                                        onSelectUserAgentMode(mode)
                                        showUaMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Low RAM Optimization Level
                item {
                    Box {
                        SettingsCard(
                            title = "Low Memory RAM Optimization",
                            subtitle = memoryLevel.label,
                            icon = Icons.Outlined.Memory,
                            onClick = { showMemoryMenu = true }
                        )

                        DropdownMenu(
                            expanded = showMemoryMenu,
                            onDismissRequest = { showMemoryMenu = false }
                        ) {
                            MemoryOptimizationLevel.entries.forEach { lvl ->
                                DropdownMenuItem(
                                    text = { Text(lvl.label) },
                                    onClick = {
                                        onSelectMemoryLevel(lvl)
                                        showMemoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Default Search Engine
                item {
                    Box {
                        SettingsCard(
                            title = "Default Search Engine",
                            subtitle = searchEngine,
                            icon = Icons.Outlined.Search,
                            onClick = { showEngineMenu = true }
                        )

                        DropdownMenu(
                            expanded = showEngineMenu,
                            onDismissRequest = { showEngineMenu = false }
                        ) {
                            searchEngines.forEach { eng ->
                                DropdownMenuItem(
                                    text = { Text(eng) },
                                    onClick = {
                                        onSelectSearchEngine(eng)
                                        showEngineMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Built-in VPN & Proxy Engine
                item {
                    SettingsCard(
                        title = "Built-in VPN & Proxy Service 🔑",
                        subtitle = "Encrypted Tunnel • DoH • SOCKS5 / Tor Relay",
                        icon = Icons.Filled.VpnKey,
                        onClick = { onOpenVpnModal() }
                    )
                }

                // About Ghost Browser
                item {
                    SettingsCard(
                        title = "About Ghost Browser ℹ️",
                        subtitle = "Developer Shahriar Rahman • Student of Tamirul Millat",
                        icon = Icons.Outlined.Info,
                        onClick = { onOpenAboutModal() }
                    )
                }

                // Open Source & License
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("OPEN SOURCE & DATA SOVEREIGNTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ghost Browser is fully open-source. All history, passwords, and tab states are stored locally on your device with hardware encryption. Zero corporate telemetry.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
