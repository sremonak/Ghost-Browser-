package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShieldActiveGreen
import com.example.ui.theme.AdBlockRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromeOmnibox(
    currentUrl: String,
    isIncognito: Boolean,
    isBookmarked: Boolean,
    tabCount: Int,
    adsBlockedCount: Long,
    isLoading: Boolean,
    progress: Float,
    canGoBack: Boolean = false,
    canGoForward: Boolean = false,
    onGoBack: () -> Unit = {},
    onGoForward: () -> Unit = {},
    onReload: () -> Unit = {},
    onNavigate: (String) -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onOpenAdBlockerSettings: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenExtensionStore: () -> Unit,
    onOpenMenuAction: (String) -> Unit,
    onToggleIncognito: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(currentUrl) {
        mutableStateOf(if (currentUrl == "ghost://newtab") "" else currentUrl)
    }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val containerColor = if (isIncognito) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Previous (Back) Button
            IconButton(
                onClick = onGoBack,
                enabled = canGoBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Next (Forward) Button
            IconButton(
                onClick = onGoForward,
                enabled = canGoForward,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Incognito Ghost Badge / Home Button
            IconButton(
                onClick = onToggleIncognito,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isIncognito) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            ) {
                Icon(
                    imageVector = if (isIncognito) Icons.Filled.VisibilityOff else Icons.Filled.Home,
                    contentDescription = if (isIncognito) "Incognito Ghost Active" else "Home",
                    tint = if (isIncognito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Omnibox URL Box
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .border(
                        width = 1.dp,
                        color = if (isIncognito) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(21.dp)
                    ),
                color = containerColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lock Security / Shield Icon
                    Icon(
                        imageVector = if (currentUrl.startsWith("https://")) Icons.Filled.Lock else if (currentUrl.startsWith("ghost://")) Icons.Filled.Shield else Icons.Filled.Language,
                        contentDescription = "SSL Secure",
                        tint = if (currentUrl.startsWith("https://") || currentUrl.startsWith("ghost://")) ShieldActiveGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // TextField Input - Clear High Contrast Display
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            focusManager.clearFocus()
                            onNavigate(textFieldValue)
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (textFieldValue.isEmpty()) {
                                    Text(
                                        text = if (isIncognito) "Ghost Search or URL" else "Search or web address",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Clear URL input button
                    if (textFieldValue.isNotEmpty()) {
                        IconButton(
                            onClick = { textFieldValue = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear URL",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Dedicated Refresh / Reload Button
                    IconButton(
                        onClick = onReload,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Page",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Shield AdBlock Badge Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenAdBlockerSettings() },
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "Shield",
                                tint = ShieldActiveGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$adsBlockedCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldActiveGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Bookmark Star
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Extension Store Button
            IconButton(
                onClick = onOpenExtensionStore,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Extension,
                    contentDescription = "Chromium Extensions",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Tab Counter Button Badge
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(7.dp)
                    )
                    .clickable { onOpenTabSwitcher() },
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$tabCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Chrome Overflow Menu (3 Dots)
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reload Page") },
                        leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                        onClick = { showMenu = false; onReload() }
                    )
                    DropdownMenuItem(
                        text = { Text("New Tab") },
                        leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("NEW_TAB") }
                    )
                    DropdownMenuItem(
                        text = { Text("New Incognito Tab") },
                        leadingIcon = { Icon(Icons.Outlined.VisibilityOff, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("NEW_INCOGNITO") }
                    )
                    DropdownMenuItem(
                        text = { Text("Inspect Page (DOM & Console)", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Outlined.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = { showMenu = false; onOpenMenuAction("INSPECT_PAGE") }
                    )
                    DropdownMenuItem(
                        text = { Text("Hacker Matrix Mode 🟢") },
                        leadingIcon = { Icon(Icons.Outlined.Terminal, contentDescription = null, tint = ShieldActiveGreen) },
                        onClick = { showMenu = false; onOpenMenuAction("TOGGLE_HACKER_THEME") }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("History & Bookmarks") },
                        leadingIcon = { Icon(Icons.Outlined.History, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("HISTORY_BOOKMARKS") }
                    )
                    DropdownMenuItem(
                        text = { Text("Password Vault (Encrypted)") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("PASSWORD_VAULT") }
                    )
                    DropdownMenuItem(
                        text = { Text("E2E Encrypted Sync") },
                        leadingIcon = { Icon(Icons.Outlined.Sync, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("GHOST_SYNC") }
                    )
                    DropdownMenuItem(
                        text = { Text("Keyboard Shortcuts") },
                        leadingIcon = { Icon(Icons.Outlined.Keyboard, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("KEYBOARD_SHORTCUTS") }
                    )
                    DropdownMenuItem(
                        text = { Text("Built-in VPN & Proxy", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = null, tint = ShieldActiveGreen) },
                        onClick = { showMenu = false; onOpenMenuAction("VPN_PROXY") }
                    )
                    DropdownMenuItem(
                        text = { Text("Browser Settings") },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("SETTINGS") }
                    )
                    DropdownMenuItem(
                        text = { Text("About Ghost Browser") },
                        leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = { showMenu = false; onOpenMenuAction("ABOUT_PAGE") }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Panic / Nuke All Data", color = AdBlockRed, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = AdBlockRed) },
                        onClick = { showMenu = false; onOpenMenuAction("NUKE_DATA") }
                    )
                }
            }
        }

        // Loading Progress Bar
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .padding(top = 4.dp),
                color = ShieldActiveGreen,
                trackColor = Color.Transparent
            )
        }
    }
}

