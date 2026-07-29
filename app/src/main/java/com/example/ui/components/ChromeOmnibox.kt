package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import com.example.ui.theme.ShieldActiveGreen

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
    var isFocused by remember { mutableStateOf(false) }
    var onlineSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Live Google Search Suggestions Fetching
    LaunchedEffect(textFieldValue) {
        val query = textFieldValue.trim()
        if (query.length >= 2 && !query.startsWith("http://") && !query.startsWith("https://") && !query.startsWith("ghost://")) {
            withContext(Dispatchers.IO) {
                try {
                    val encoded = Uri.encode(query)
                    val endpoint = "https://suggestqueries.google.com/complete/search?client=chrome&q=$encoded"
                    val url = java.net.URL(endpoint)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.requestMethod = "GET"
                    if (conn.responseCode == 200) {
                        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                        val jsonArr = JSONArray(responseText)
                        val suggestionsArr = jsonArr.getJSONArray(1)
                        val list = mutableListOf<String>()
                        for (i in 0 until suggestionsArr.length()) {
                            list.add(suggestionsArr.getString(i))
                        }
                        withContext(Dispatchers.Main) {
                            onlineSuggestions = list.take(6)
                        }
                    }
                } catch (e: Exception) {
                    // Silently catch network or parsing issues
                }
            }
        } else {
            onlineSuggestions = emptyList()
        }
    }

    val containerColor = when {
        isIncognito -> MaterialTheme.colorScheme.primaryContainer
        isFocused -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        isFocused -> MaterialTheme.colorScheme.primary
        isIncognito -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Main Navigation & Omnibox Row (Spacious & Clean)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left Action: Back or Home
            IconButton(
                onClick = if (canGoBack) onGoBack else { { onNavigate("ghost://newtab") } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (canGoBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Home,
                    contentDescription = if (canGoBack) "Back" else "Home",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Spacious Omnibox Link Box (Takes maximum width)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(22.dp)
                    ),
                color = containerColor,
                tonalElevation = if (isFocused) 4.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Security Lock / Globe Icon
                    Icon(
                        imageVector = when {
                            currentUrl.startsWith("https://") -> Icons.Filled.Lock
                            currentUrl.startsWith("ghost://") -> Icons.Filled.Shield
                            else -> Icons.Filled.Language
                        },
                        contentDescription = "Security Status",
                        tint = if (currentUrl.startsWith("https://") || currentUrl.startsWith("ghost://")) ShieldActiveGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // TextField Input for Search / URL (Generous space)
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            focusManager.clearFocus()
                            if (textFieldValue.isNotBlank()) {
                                onNavigate(textFieldValue)
                            }
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            },
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (textFieldValue.isEmpty()) {
                                    Text(
                                        text = if (isIncognito) "Ghost search or URL" else "Search Google or type URL",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Clear button when typing
                    if (textFieldValue.isNotEmpty()) {
                        IconButton(
                            onClick = { textFieldValue = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else if (!isFocused) {
                        // Refresh button when not typing
                        IconButton(
                            onClick = onReload,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Bookmark Star
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Tab Counter Badge
            Surface(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onOpenTabSwitcher() },
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$tabCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Overflow 3-Dots Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Pro Unlocked Banner
                    Surface(
                        color = Color(0xFFFFB800).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = "Pro",
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GHOST PRO UNLOCKED ⚡",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFB800)
                            )
                        }
                    }

                    // Quick Action Icons Row in Menu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showMenu = false; onGoBack() },
                            enabled = canGoBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        IconButton(
                            onClick = { showMenu = false; onGoForward() },
                            enabled = canGoForward,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        IconButton(
                            onClick = { showMenu = false; onToggleBookmark() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showMenu = false; onReload() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(
                            onClick = {
                                showMenu = false
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, currentUrl)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Link"))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

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
                        text = { Text("AdBlocker Protection ($adsBlockedCount blocked)") },
                        leadingIcon = { Icon(Icons.Filled.Shield, contentDescription = null, tint = ShieldActiveGreen) },
                        onClick = { showMenu = false; onOpenAdBlockerSettings() }
                    )
                    DropdownMenuItem(
                        text = { Text("Extensions Store") },
                        leadingIcon = { Icon(Icons.Outlined.Extension, contentDescription = null) },
                        onClick = { showMenu = false; onOpenExtensionStore() }
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
                        text = { Text("Password Vault") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("PASSWORD_VAULT") }
                    )
                    DropdownMenuItem(
                        text = { Text("E2E Encrypted Sync") },
                        leadingIcon = { Icon(Icons.Outlined.Sync, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("GHOST_SYNC") }
                    )
                    DropdownMenuItem(
                        text = { Text("VPN & Proxy Shield") },
                        leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = null, tint = ShieldActiveGreen) },
                        onClick = { showMenu = false; onOpenMenuAction("VPN_PROXY") }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("SETTINGS") }
                    )
                    DropdownMenuItem(
                        text = { Text("About Ghost Browser") },
                        leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                        onClick = { showMenu = false; onOpenMenuAction("ABOUT_PAGE") }
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
                    .height(2.5.dp)
                    .padding(top = 4.dp),
                color = ShieldActiveGreen,
                trackColor = Color.Transparent
            )
        }

        // Expanded Overlay when Focused: Displays Complete Full Link & Quick Search
        if (isFocused) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Card 1: Full Link Display Card (Exact request: "লিংক ত স্পষ্ট ফুল দেখা যায় না")
                val activeDisplayUrl = if (textFieldValue.isNotBlank()) textFieldValue else currentUrl

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FULL WEB ADDRESS / LINK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Complete Wrapped Visible Link Text
                        SelectionContainer {
                            Text(
                                text = if (activeDisplayUrl.isBlank()) "ghost://newtab" else activeDisplayUrl,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Actions for Link: Copy, Share, Clear
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("URL", activeDisplayUrl))
                                    Toast.makeText(context, "Full link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Link", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, activeDisplayUrl)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Link"))
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Card 2: Live Online Search Suggestions & Quick Web Shortcuts
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = if (textFieldValue.isNotBlank()) "Search Google for \"$textFieldValue\"" else "Search Google or type web address",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.clickable {
                                if (textFieldValue.isNotBlank()) {
                                    focusManager.clearFocus()
                                    onNavigate(textFieldValue)
                                }
                            }
                        )

                        if (onlineSuggestions.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Text(
                                text = "ONLINE SEARCH SUGGESTIONS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                            onlineSuggestions.forEach { suggestion ->
                                ListItem(
                                    headlineContent = { Text(suggestion, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    leadingContent = {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    },
                                    modifier = Modifier.clickable {
                                        focusManager.clearFocus()
                                        onNavigate("https://www.google.com/search?q=" + Uri.encode(suggestion))
                                    }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        val shortcuts = listOf(
                            "Google" to "https://www.google.com",
                            "YouTube" to "https://www.youtube.com",
                            "Wikipedia" to "https://www.wikipedia.org",
                            "GitHub" to "https://www.github.com",
                            "Facebook" to "https://www.facebook.com",
                            "News" to "https://news.google.com",
                            "ChatGPT" to "https://chatgpt.com"
                        )

                        Text(
                            text = "QUICK SHORTCUTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        shortcuts.forEach { (name, url) ->
                            ListItem(
                                headlineContent = { Text(name, fontSize = 13.sp) },
                                supportingContent = { Text(url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                leadingContent = {
                                    Icon(Icons.Outlined.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.clickable {
                                    focusManager.clearFocus()
                                    onNavigate(url)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
