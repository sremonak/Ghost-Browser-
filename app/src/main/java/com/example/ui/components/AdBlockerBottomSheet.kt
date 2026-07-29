package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AdBlockStats
import com.example.engine.BlockedRequestItem
import com.example.engine.SiteSecuritySettings
import com.example.ui.theme.ShieldActiveGreen
import com.example.ui.theme.AdBlockRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdBlockerBottomSheet(
    stats: AdBlockStats,
    siteSettings: SiteSecuritySettings,
    recentBlockedRequests: List<BlockedRequestItem>,
    onUpdateSiteSettings: (SiteSecuritySettings) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adBlockOn by remember(siteSettings) { mutableStateOf(siteSettings.adBlockEnabled) }
    var scriptOn by remember(siteSettings) { mutableStateOf(siteSettings.javaScriptEnabled) }
    var blockCookies by remember(siteSettings) { mutableStateOf(siteSettings.blockThirdPartyCookies) }
    var antiFingerprint by remember(siteSettings) { mutableStateOf(siteSettings.canvasFingerprintDefender) }

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
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Shield",
                        tint = ShieldActiveGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ghost Shield & Script Control",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Protection for ${siteSettings.domain}",
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

            // Metrics Summary Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = ShieldActiveGreen.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${stats.adsBlocked}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ShieldActiveGreen)
                        Text("Ads Blocked", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${stats.trackersBlocked}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Trackers", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${stats.dataSavedKb / 1024} MB", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ShieldActiveGreen)
                        Text("Saved", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggles
            Text(
                text = "SITE PRIVACY & SCRIPT CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            SwitchRow(
                title = "Ad-Blocking Extension",
                subtitle = "Blocks intrusive ads, banners, and video popups",
                checked = adBlockOn,
                onCheckedChange = { 
                    adBlockOn = it
                    onUpdateSiteSettings(siteSettings.copy(adBlockEnabled = it))
                },
                icon = Icons.Outlined.Block
            )

            SwitchRow(
                title = "JavaScript Execution",
                subtitle = "Script control for current site",
                checked = scriptOn,
                onCheckedChange = { 
                    scriptOn = it
                    onUpdateSiteSettings(siteSettings.copy(javaScriptEnabled = it))
                },
                icon = Icons.Outlined.Code
            )

            SwitchRow(
                title = "Block Third-Party Cookies",
                subtitle = "Prevents cross-site user tracking",
                checked = blockCookies,
                onCheckedChange = { 
                    blockCookies = it
                    onUpdateSiteSettings(siteSettings.copy(blockThirdPartyCookies = it))
                },
                icon = Icons.Outlined.Cookie
            )

            SwitchRow(
                title = "Canvas Anti-Fingerprinting",
                subtitle = "Spoofs canvas & WebGL hardware signatures",
                checked = antiFingerprint,
                onCheckedChange = { 
                    antiFingerprint = it
                    onUpdateSiteSettings(siteSettings.copy(canvasFingerprintDefender = it))
                },
                icon = Icons.Outlined.Fingerprint
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Blocked Items Log
            Text(
                text = "RECENTLY BLOCKED ON PAGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (recentBlockedRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ad or tracker requests blocked on this page yet",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    items(recentBlockedRequests.size) { index ->
                        val item = recentBlockedRequests[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = null, tint = AdBlockRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.url,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(item.category, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
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
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
