package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BrowserSettings
import com.example.engine.SearchEngines
import com.example.engine.UserAgentMode
import com.example.ui.viewmodel.NovaBrowserViewModel

/**
 * Professional settings screen: appearance, home page, search engine,
 * privacy & security toggles, advanced options and data management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: BrowserSettings,
    versionName: String,
    viewModel: NovaBrowserViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    // Local mutable copy so toggles apply immediately and persist on change.
    var current by remember(settings) { mutableStateOf(settings) }

    fun update(transform: (BrowserSettings) -> BrowserSettings) {
        current = transform(current)
        viewModel.updateSettings(transform)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Appearance
            SettingsSection("Appearance") {
                SettingsRow(
                    icon = Icons.Filled.Palette,
                    title = "Dark theme",
                    control = {
                        Switch(
                            checked = current.isDarkTheme,
                            onCheckedChange = { update { it.copy(isDarkTheme = it.isDarkTheme.not()) } }
                        )
                    }
                )
            }

            // Home & search
            SettingsSection("Home & search") {
                SettingsRow(
                    icon = Icons.Filled.Home,
                    title = "Homepage",
                    control = {
                        DropdownChoice(
                            value = current.homepage,
                            options = listOf(
                                "nova://home",
                                "https://www.google.com",
                                "https://www.bing.com",
                                "https://duckduckgo.com"
                            ),
                            onSelect = { selected -> update { s -> s.copy(homepage = selected) } }
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Filled.Search,
                    title = "Default search engine",
                    control = {
                        DropdownChoice(
                            value = current.searchEngine,
                            options = SearchEngines.all.map { it.name },
                            onSelect = { selected -> update { s -> s.copy(searchEngine = selected) } }
                        )
                    }
                )
            }

            // Privacy & security
            SettingsSection("Privacy & security") {
                SettingsRow(
                    icon = Icons.Filled.Block,
                    title = "Ad & tracker blocker",
                    subtitle = "Block ads, trackers and telemetry",
                    control = {
                        Switch(
                            checked = current.adBlockEnabled,
                            onCheckedChange = { update { it.copy(adBlockEnabled = it.adBlockEnabled.not()) } }
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Filled.Visibility,
                    title = "Block third-party cookies",
                    control = {
                        Switch(
                            checked = current.blockThirdPartyCookies,
                            onCheckedChange = { update { it.copy(blockThirdPartyCookies = it.blockThirdPartyCookies.not()) } }
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Filled.Article,
                    title = "Pop-up blocker",
                    control = {
                        Switch(
                            checked = current.popupBlockerEnabled,
                            onCheckedChange = { update { it.copy(popupBlockerEnabled = it.popupBlockerEnabled.not()) } }
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Filled.DataUsage,
                    title = "Data saver",
                    subtitle = "Block images on pages to save data",
                    control = {
                        Switch(
                            checked = current.dataSaverEnabled,
                            onCheckedChange = { update { it.copy(dataSaverEnabled = it.dataSaverEnabled.not()) } }
                        )
                    }
                )
            }

            // Advanced
            SettingsSection("Advanced") {
                SettingsRow(
                    icon = Icons.Filled.DesktopWindows,
                    title = "Desktop mode by default",
                    subtitle = "Request desktop versions of sites",
                    control = {
                        Switch(
                            checked = current.desktopModeDefault,
                            onCheckedChange = { update { it.copy(desktopModeDefault = it.desktopModeDefault.not()) } }
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Filled.Code,
                    title = "JavaScript",
                    control = {
                        Switch(
                            checked = current.javascriptEnabled,
                            onCheckedChange = { update { it.copy(javascriptEnabled = it.javascriptEnabled.not()) } }
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Filled.Restore,
                    title = "Restore tabs on start",
                    control = {
                        Switch(
                            checked = current.restoreTabsOnStart,
                            onCheckedChange = { update { it.copy(restoreTabsOnStart = it.restoreTabsOnStart.not()) } }
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Filled.Smartphone,
                    title = "User agent profile",
                    control = {
                        DropdownChoice(
                            value = UserAgentMode.fromName(current.userAgentMode).displayName,
                            options = UserAgentMode.entries.map { it.displayName },
                            onSelect = { displayName ->
                                val mode = UserAgentMode.entries.first { it.displayName == displayName }
                                update {
                                    it.copy(userAgentMode = mode.name, desktopModeDefault = mode.isDesktop)
                                }
                            }
                        )
                    }
                )
            }

            // Data management
            SettingsSection("Data") {
                SettingsRow(
                    icon = Icons.Filled.Delete,
                    title = "Clear browsing data",
                    subtitle = "History, bookmarks, downloads & cookies",
                    control = {
                        TextButton(
                            onClick = { viewModel.clearBrowsingData() },
                            colors = TextButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Clear")
                        }
                    }
                )
            }

            // About
            SettingsSection("About") {
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = "Nova Browser",
                    subtitle = "Version $versionName",
                    control = { }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * A compact labelled dropdown that manages its own open/closed state.
 */
@Composable
private fun DropdownChoice(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextButton(onClick = { expanded = true }) {
            Text(value, maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
    )
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        control()
    }
}
