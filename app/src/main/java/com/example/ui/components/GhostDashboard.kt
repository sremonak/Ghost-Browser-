package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.AdBlockStats
import com.example.engine.MemoryStatus
import com.example.ui.theme.ShieldActiveGreen
import com.example.ui.theme.AdBlockRed
import com.example.ui.theme.PurpleGhostGlow
import com.example.ui.theme.HackerGreen

data class SpeedDialItem(
    val title: String,
    val url: String,
    val icon: ImageVector,
    val color: Color
)

data class EssentialWebItem(
    val name: String,
    val url: String,
    val category: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostDashboard(
    isIncognito: Boolean,
    searchEngine: String,
    adBlockStats: AdBlockStats,
    memoryStatus: MemoryStatus,
    isHackerTheme: Boolean = false,
    onNavigate: (String) -> Unit,
    onSelectSearchEngine: (String) -> Unit,
    onOptimizeMemory: () -> Unit,
    onOpenAdBlocker: () -> Unit,
    onToggleHackerTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    var showEngineMenu by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    val speedDials = listOf(
        SpeedDialItem("DuckDuckGo", "https://duckduckgo.com", Icons.Outlined.Shield, Color(0xFFDE5833)),
        SpeedDialItem("YouTube", "https://youtube.com", Icons.Outlined.PlayCircle, Color(0xFFFF0000)),
        SpeedDialItem("Wikipedia", "https://wikipedia.org", Icons.Outlined.MenuBook, Color(0xFF636466)),
        SpeedDialItem("GitHub", "https://github.com", Icons.Outlined.Code, Color(0xFF24292E)),
        SpeedDialItem("Reddit", "https://reddit.com", Icons.Outlined.Forum, Color(0xFFFF4500)),
        SpeedDialItem("Hacker News", "https://news.ycombinator.com", Icons.Outlined.Newspaper, Color(0xFFFF6600)),
        SpeedDialItem("TechCrunch", "https://techcrunch.com", Icons.Outlined.Article, Color(0xFF02A169)),
        SpeedDialItem("BBC News", "https://bbc.com/news", Icons.Outlined.Public, Color(0xFFB80000))
    )

    val essentialWebList = remember {
        listOf(
            EssentialWebItem("VirusTotal", "https://www.virustotal.com", "Security & OSINT", "Analyze suspicious files & URLs", Icons.Outlined.Security),
            EssentialWebItem("Shodan", "https://www.shodan.io", "Security & OSINT", "Search engine for Internet-connected devices", Icons.Outlined.Terminal),
            EssentialWebItem("HaveIBeenPwned", "https://haveibeenpwned.com", "Security & OSINT", "Check data breach exposure", Icons.Outlined.Lock),
            EssentialWebItem("CyberChef", "https://gchq.github.io/CyberChef", "Security & OSINT", "The Cyber Swiss Army Knife", Icons.Outlined.Build),
            EssentialWebItem("DevDocs.io", "https://devdocs.io", "Developer Tools", "Fast offline API Documentation", Icons.Outlined.Code),
            EssentialWebItem("RegEx101", "https://regex101.com", "Developer Tools", "Regular expression tester & debugger", Icons.Outlined.FindInPage),
            EssentialWebItem("JSFiddle", "https://jsfiddle.net", "Developer Tools", "Online JavaScript web playground", Icons.Outlined.Laptop),
            EssentialWebItem("Claude AI", "https://claude.ai", "AI Tools", "Anthropic Claude Assistant", Icons.Outlined.SmartToy),
            EssentialWebItem("Gemini AI", "https://gemini.google.com", "AI Tools", "Google Gemini AI Engine", Icons.Outlined.Psychology),
            EssentialWebItem("ChatGPT", "https://chatgpt.com", "AI Tools", "OpenAI Chat Interface", Icons.Outlined.AutoAwesome),
            EssentialWebItem("ProtonMail", "https://proton.me/mail", "Privacy & Tech", "Encrypted email service", Icons.Outlined.Email),
            EssentialWebItem("EFF Org", "https://www.eff.org", "Privacy & Tech", "Defending digital privacy rights", Icons.Outlined.Gavel)
        )
    }

    val categories = listOf("All", "Security & OSINT", "Developer Tools", "AI Tools", "Privacy & Tech")

    val filteredEssentials = if (selectedCategory == "All") essentialWebList else essentialWebList.filter { it.category == selectedCategory }

    val engines = listOf("DuckDuckGo", "Google", "Brave", "Ecosia", "Startpage")

    val bgGradient = if (isHackerTheme) {
        Brush.verticalGradient(colors = listOf(Color(0xFF060D08), Color(0xFF0F1710)))
    } else {
        Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (isHackerTheme) {
            // Cyberpunk Terminal Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, HackerGreen, RoundedCornerShape(12.dp)),
                color = Color(0xFF060D08)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[CYBERPUNK MATRIX TERMINAL]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = HackerGreen
                        )

                        TextButton(onClick = onToggleHackerTheme) {
                            Text("Standard Mode", fontSize = 10.sp, color = HackerGreen)
                        }
                    }

                    Text(
                        text = "STATUS: ONLINE • PROXY: GHOST-ANON-RELAY • TLS 1.3 • ENCRYPTION: AES-256",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = HackerGreen.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // App Logo & Phantom Header
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isHackerTheme) HackerGreen.copy(alpha = 0.3f) else if (isIncognito) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else PurpleGhostGlow.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hacker_icon_1785262134387),
                contentDescription = "Ghost Hacker Logo",
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isHackerTheme) "GHOST HACKER OS" else "Ghost Browser",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = ShieldActiveGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isHackerTheme) "v2.5 MATRIX" else "LITE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                    color = ShieldActiveGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = if (isHackerTheme) "Ghost Cyberpunk Security Engine • Zero Leakage" else if (isIncognito) "Ghost Incognito Session • Zero Footprint" else "Privacy-First • High Speed • Low Memory",
            fontSize = 12.sp,
            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
            color = if (isHackerTheme) HackerGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar Omnibox Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 1.dp,
                    color = if (isHackerTheme) HackerGreen else if (isIncognito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(28.dp)
                ),
            color = if (isHackerTheme) Color(0xFF060D08) else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Engine Picker Button
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showEngineMenu = true },
                        color = if (isHackerTheme) Color(0xFF0F1710) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Engine",
                                tint = if (isHackerTheme) HackerGreen else ShieldActiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = searchEngine,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                                color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showEngineMenu,
                        onDismissRequest = { showEngineMenu = false }
                    ) {
                        engines.forEach { eng ->
                            DropdownMenuItem(
                                text = { Text(eng) },
                                onClick = {
                                    onSelectSearchEngine(eng)
                                    showEngineMenu = false
                                },
                                trailingIcon = {
                                    if (eng == searchEngine) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = ShieldActiveGreen)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = {
                        Text(
                            "Search or enter web address",
                            fontSize = 14.sp,
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (isHackerTheme) HackerGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchInput.isNotBlank()) {
                            onNavigate(searchInput)
                        }
                    }),
                    modifier = Modifier.weight(1f)
                )

                if (searchInput.isNotEmpty()) {
                    IconButton(onClick = { searchInput = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp), tint = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Speed Dial Shortcut Grid
        Text(
            text = "QUICK DIAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
            color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            speedDials.take(4).forEach { item ->
                SpeedDialCard(item = item, isHackerTheme = isHackerTheme, onClick = { onNavigate(item.url) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            speedDials.drop(4).forEach { item ->
                SpeedDialCard(item = item, isHackerTheme = isHackerTheme, onClick = { onNavigate(item.url) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Essential Web List Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ESSENTIAL WEB DIRECTORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                cat,
                                fontSize = 11.sp,
                                fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                                color = if (selectedCategory == cat) Color.Black else if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredEssentials.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(item.url) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHackerTheme) Color(0xFF0C1710) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isHackerTheme) androidx.compose.foundation.BorderStroke(1.dp, HackerGreen.copy(alpha = 0.4f)) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                color = if (isHackerTheme) HackerGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                                    color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 11.sp,
                                    fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                                    color = if (isHackerTheme) HackerGreen.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Shield & Memory Optimizer Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isHackerTheme) Color(0xFF0C1710) else MaterialTheme.colorScheme.surface),
            border = if (isHackerTheme) androidx.compose.foundation.BorderStroke(1.dp, HackerGreen.copy(alpha = 0.4f)) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Shield",
                            tint = ShieldActiveGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ghost Shield Protection",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(onClick = onOpenAdBlocker) {
                        Text("Configure", fontSize = 12.sp, color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetricColumn("Ads Blocked", "${adBlockStats.adsBlocked}", ShieldActiveGreen, isHackerTheme)
                    StatMetricColumn("Trackers", "${adBlockStats.trackersBlocked}", if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary, isHackerTheme)
                    StatMetricColumn("Data Saved", "${adBlockStats.dataSavedKb / 1024} MB", PurpleGhostGlow, isHackerTheme)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Low Memory Optimizer Meter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isHackerTheme) Color(0xFF0C1710) else MaterialTheme.colorScheme.surface),
            border = if (isHackerTheme) androidx.compose.foundation.BorderStroke(1.dp, HackerGreen.copy(alpha = 0.4f)) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Memory,
                            contentDescription = "RAM Optimizer",
                            tint = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RAM Usage & Optimizer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onOptimizeMemory,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Optimize RAM", fontSize = 11.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Memory Usage: ${memoryStatus.usedMemoryMb} MB / ${memoryStatus.maxMemoryMb} MB (${memoryStatus.memoryUsagePercent}%)",
                        fontSize = 12.sp,
                        fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                        color = if (isHackerTheme) HackerGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${memoryStatus.suspendedTabsCount} Tabs Sleeping",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                        color = ShieldActiveGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { memoryStatus.memoryUsagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (memoryStatus.memoryUsagePercent > 75) AdBlockRed else if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary,
                    trackColor = if (isHackerTheme) Color(0xFF060D08) else MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SpeedDialCard(
    item: SpeedDialItem,
    isHackerTheme: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape),
            color = if (isHackerTheme) HackerGreen.copy(alpha = 0.15f) else item.color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (isHackerTheme) HackerGreen else item.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun StatMetricColumn(
    label: String,
    value: String,
    color: Color,
    isHackerTheme: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
            color = if (isHackerTheme) HackerGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
