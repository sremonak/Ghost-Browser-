package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShieldActiveGreen
import com.example.ui.theme.HackerGreen
import com.example.ui.theme.AdBlockRed

data class VpnNode(
    val name: String,
    val countryCode: String,
    val pingMs: Int,
    val flagEmoji: String,
    val isPremium: Boolean = false
)

enum class ProxyType(val displayName: String) {
    GHOST_TUNNEL("Ghost Encrypted Tunnel (Fast)"),
    TOR_ONION("Tor Onion Privacy Network"),
    SOCKS5("SOCKS5 Proxy"),
    HTTP_HTTPS("HTTP/HTTPS Proxy"),
    DIRECT("Direct (No Proxy)")
}

enum class DohProvider(val displayName: String, val address: String) {
    CLOUDFLARE("Cloudflare (1.1.1.1)", "https://1.1.1.1/dns-query"),
    QUAD9("Quad9 Privacy DNS", "https://dns.quad9.net/dns-query"),
    ADGUARD("AdGuard AdBlock DNS", "https://dns.adguard.com/dns-query"),
    GOOGLE("Google Public DNS", "https://dns.google/dns-query")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostVpnModal(
    isVpnEnabled: Boolean,
    selectedNode: String,
    proxyType: ProxyType,
    dohProvider: DohProvider,
    customProxyHost: String,
    customProxyPort: String,
    isHackerTheme: Boolean = false,
    onToggleVpn: (Boolean) -> Unit,
    onSelectNode: (String) -> Unit,
    onSelectProxyType: (ProxyType) -> Unit,
    onSelectDohProvider: (DohProvider) -> Unit,
    onUpdateCustomProxy: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hostInput by remember { mutableStateOf(customProxyHost) }
    var portInput by remember { mutableStateOf(customProxyPort) }
    var showDohMenu by remember { mutableStateOf(false) }
    var showProxyMenu by remember { mutableStateOf(false) }

    val nodes = listOf(
        VpnNode("Frankfurt, Germany", "DE", 24, "🇩🇪"),
        VpnNode("Zurich, Switzerland", "CH", 18, "🇨🇭"),
        VpnNode("Tokyo, Japan", "JP", 65, "🇯🇵"),
        VpnNode("Singapore Node", "SG", 42, "🇸🇬"),
        VpnNode("London, UK", "GB", 32, "🇬🇧"),
        VpnNode("New York, USA", "US", 78, "🇺🇸")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.VpnKey,
                        contentDescription = "VPN Proxy",
                        tint = if (isVpnEnabled) ShieldActiveGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Built-in VPN & Proxy Engine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default
                        )
                        Text(
                            text = if (isVpnEnabled) "Tunnel Active • IP Masked" else "Direct Connection • Protection Idle",
                            fontSize = 12.sp,
                            color = if (isVpnEnabled) ShieldActiveGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main VPN Toggle Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isVpnEnabled) ShieldActiveGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (isVpnEnabled) ShieldActiveGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isVpnEnabled) ShieldActiveGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isVpnEnabled) Icons.Filled.Security else Icons.Outlined.Security,
                                    contentDescription = "VPN Status",
                                    tint = if (isVpnEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (isVpnEnabled) "VPN PRIVACY TUNNEL ENABLED" else "VPN TUNNEL DISCONNECTED",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isVpnEnabled) ShieldActiveGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isVpnEnabled) "Routing traffic via $selectedNode" else "Tap switch to enable proxy shielding",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isVpnEnabled,
                        onCheckedChange = { onToggleVpn(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Protocol & DNS Configuration
            Text(
                text = "PROXY ENGINE & DNS OVER HTTPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Proxy Protocol Selector
            Box {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProxyMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Router, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Proxy Protocol Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(proxyType.displayName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = showProxyMenu,
                    onDismissRequest = { showProxyMenu = false }
                ) {
                    ProxyType.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName) },
                            onClick = {
                                onSelectProxyType(mode)
                                showProxyMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Encrypted DoH Selector
            Box {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDohMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Encrypted DNS-over-HTTPS (DoH)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(dohProvider.displayName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = showDohMenu,
                    onDismissRequest = { showDohMenu = false }
                ) {
                    DohProvider.entries.forEach { doh ->
                        DropdownMenuItem(
                            text = { Text(doh.displayName) },
                            onClick = {
                                onSelectDohProvider(doh)
                                showDohMenu = false
                            }
                        )
                    }
                }
            }

            // Custom SOCKS5 / HTTP Proxy fields if SOCKS5 or HTTP selected
            if (proxyType == ProxyType.SOCKS5 || proxyType == ProxyType.HTTP_HTTPS) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Custom Proxy Configuration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hostInput,
                                onValueChange = {
                                    hostInput = it
                                    onUpdateCustomProxy(hostInput, portInput)
                                },
                                label = { Text("Proxy Host/IP") },
                                modifier = Modifier.weight(2f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = portInput,
                                onValueChange = {
                                    portInput = it
                                    onUpdateCustomProxy(hostInput, portInput)
                                },
                                label = { Text("Port") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // VPN Server Node Selector List
            Text(
                text = "GHOST PRIVACY NODES & RELAYS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = if (isHackerTheme) FontFamily.Monospace else FontFamily.Default,
                color = if (isHackerTheme) HackerGreen else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                nodes.forEach { node ->
                    val isSelected = selectedNode == node.name
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ShieldActiveGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) ShieldActiveGreen else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectNode(node.name) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(node.flagEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(node.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${node.pingMs} ms ping • Encrypted AES-256", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = ShieldActiveGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
