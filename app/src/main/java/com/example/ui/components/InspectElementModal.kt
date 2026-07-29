package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HackerGreen
import com.example.ui.theme.ShieldActiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectElementModal(
    pageSource: String,
    consoleLogs: List<String>,
    httpHeaders: Map<String, String>,
    isHackerTheme: Boolean,
    onExecuteJs: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var jsInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val mainAccent = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isHackerTheme) Color(0xFF0C1710) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = "Inspect Element",
                        tint = mainAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DOM & Console Inspector",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Developer Tools • Live JavaScript REPL • Network Headers",
                            fontSize = 11.sp,
                            color = if (isHackerTheme) HackerGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "HTML Source",
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (selectedTab == 0) mainAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "DOM Tree",
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (selectedTab == 1) mainAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "JS Console (${consoleLogs.size})",
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (selectedTab == 2) mainAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            "HTTP Headers",
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                            color = if (selectedTab == 3) mainAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // HTML Source Code View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PAGE SOURCE HTML",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(pageSource))
                            }
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = mainAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Source", fontSize = 11.sp, color = mainAccent)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isHackerTheme) HackerGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        color = if (isHackerTheme) Color(0xFF060D08) else Color(0xFF1E1E1E)
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            val lines = pageSource.lines()
                            items(lines.size) { index ->
                                Row {
                                    Text(
                                        text = "${index + 1}".padStart(3, ' '),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Gray,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Text(
                                        text = lines[index],
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isHackerTheme) HackerGreen else Color(0xFFD4D4D4)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Real DOM Tree Structure Inspector & Live Page Manipulator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE DOM TREE & QUICK MANIPULATORS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live Manipulation Action Chips
                    ScrollableTabRow(
                        selectedTabIndex = -1,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(
                                onClick = { onExecuteJs("document.querySelectorAll('input[type=\"password\"]').forEach(i => i.type = 'text')") },
                                label = { Text("Reveal Passwords 🔓", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = mainAccent.copy(alpha = 0.15f), labelColor = mainAccent)
                            )
                            AssistChip(
                                onClick = { onExecuteJs("document.querySelectorAll('a').forEach(a => a.style.outline = '2px dashed #00FF66')") },
                                label = { Text("Outline Links 🔗", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = mainAccent.copy(alpha = 0.15f), labelColor = mainAccent)
                            )
                            AssistChip(
                                onClick = { onExecuteJs("document.body.style.filter = document.body.style.filter ? '' : 'invert(1) hue-rotate(180deg)'") },
                                label = { Text("Invert Theme 🌙", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = mainAccent.copy(alpha = 0.15f), labelColor = mainAccent)
                            )
                            AssistChip(
                                onClick = { onExecuteJs("document.designMode = document.designMode === 'on' ? 'off' : 'on'") },
                                label = { Text("Toggle Design Mode ✏️", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = mainAccent.copy(alpha = 0.15f), labelColor = mainAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val domLines = remember(pageSource) {
                        pageSource.lines()
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .take(120)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isHackerTheme) HackerGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        color = if (isHackerTheme) Color(0xFF060D08) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (domLines.isNotEmpty()) {
                                items(domLines.size) { idx ->
                                    val line = domLines[idx]
                                    val indentLevel = (line.indexOf("<") / 2).coerceIn(0, 5)
                                    DomNodeItem(line, level = indentLevel, isHackerTheme = isHackerTheme)
                                }
                            } else {
                                item { DomNodeItem("<html lang=\"en\">", level = 0, isHackerTheme = isHackerTheme) }
                                item { DomNodeItem("<head><title>Ghost Engine</title></head>", level = 1, isHackerTheme = isHackerTheme) }
                                item { DomNodeItem("<body><div id=\"root\">Live DOM Empty</div></body>", level = 1, isHackerTheme = isHackerTheme) }
                                item { DomNodeItem("</html>", level = 0, isHackerTheme = isHackerTheme) }
                            }
                        }
                    }
                }

                2 -> {
                    // Live JavaScript REPL & Console Logs
                    Text(
                        text = "LIVE JAVASCRIPT REPL CONSOLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isHackerTheme) HackerGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        color = if (isHackerTheme) Color(0xFF060D08) else Color(0xFF181818)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(consoleLogs) { log ->
                                Text(
                                    text = log,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (log.startsWith(">")) Color(0xFF569CD6) else if (log.contains("SEC") || log.contains("STATUS")) ShieldActiveGreen else if (isHackerTheme) HackerGreen else Color(0xFFDCDCAA)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = jsInput,
                            onValueChange = { jsInput = it },
                            placeholder = { Text("e.g. document.title, location.href", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            leadingIcon = { Text(">", fontWeight = FontWeight.Bold, color = mainAccent, modifier = Modifier.padding(start = 12.dp)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (jsInput.isNotBlank()) {
                                    onExecuteJs(jsInput)
                                    jsInput = ""
                                }
                            }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (jsInput.isNotBlank()) {
                                    onExecuteJs(jsInput)
                                    jsInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = mainAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RUN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                3 -> {
                    // HTTP Request/Response Headers
                    Text(
                        text = "HTTP HEADERS & SECURITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isHackerTheme) HackerGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        color = if (isHackerTheme) Color(0xFF060D08) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(httpHeaders.toList()) { (key, valStr) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isHackerTheme) Color(0xFF0F1710) else MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = mainAccent,
                                        modifier = Modifier.weight(0.4f)
                                    )
                                    Text(
                                        text = valStr,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DomNodeItem(
    tagText: String,
    level: Int,
    isHackerTheme: Boolean
) {
    Row(
        modifier = Modifier.padding(start = (level * 14).dp)
    ) {
        Text(
            text = tagText,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = if (isHackerTheme) HackerGreen else if (tagText.startsWith("</")) Color(0xFF808080) else Color(0xFF569CD6)
        )
    }
}
