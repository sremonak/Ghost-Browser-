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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GhostSyncPairingInfo
import com.example.ui.theme.ShieldActiveGreen
import com.example.ui.theme.AdBlockRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostSyncModal(
    pairingInfo: GhostSyncPairingInfo,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onNukeData: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmNuke by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "E2E Encrypted Sync & Sovereignty",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No Centralized Cloud Telemetry",
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

            // Pairing Room Info Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DEVICE PAIRING CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pairingInfo.syncRoomId, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Surface(
                            color = ShieldActiveGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "PIN: ${pairingInfo.pairingCode}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldActiveGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Syncs open tabs, encrypted bookmarks, and plugin states peer-to-peer across devices.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("CONNECTED DEVICES (${pairingInfo.connectedDevices.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pairingInfo.connectedDevices.forEach { dev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Devices, contentDescription = null, tint = ShieldActiveGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dev, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Active", fontSize = 11.sp, color = ShieldActiveGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("USER DATA SOVEREIGNTY & BACKUPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportBackup,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Backup", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onImportBackup,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import Backup", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showConfirmNuke = true },
                colors = ButtonDefaults.buttonColors(containerColor = AdBlockRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Panic / Instant Nuke All Data", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showConfirmNuke) {
            AlertDialog(
                onDismissRequest = { showConfirmNuke = false },
                title = { Text("Confirm Data Nuke", color = AdBlockRed, fontWeight = FontWeight.Bold) },
                text = { Text("This action will immediately wipe ALL local browsing history, saved passwords, tabs, cookies, and cache permanently. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onNukeData()
                            showConfirmNuke = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdBlockRed)
                    ) {
                        Text("NUKE EVERYTHING NOW")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmNuke = false }) { Text("Cancel") }
                }
            )
        }
    }
}
