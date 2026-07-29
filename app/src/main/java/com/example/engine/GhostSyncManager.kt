package com.example.engine

import com.example.data.CryptoUtils
import java.util.UUID

data class GhostSyncPairingInfo(
    val syncRoomId: String = "GHOST-SYNC-" + UUID.randomUUID().toString().take(8).uppercase(),
    val pairingCode: String = (100000..999999).random().toString(),
    val isConnected: Boolean = true,
    val connectedDevices: List<String> = listOf("This Device (${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL})"),
    val lastSyncTime: String = "Just now"
)

object GhostSyncManager {
    fun generateEncryptedBackupJson(
        bookmarksJson: String,
        historyJson: String,
        tabsJson: String
    ): String {
        val rawData = "{\"bookmarks\":$bookmarksJson,\"history\":$historyJson,\"tabs\":$tabsJson}"
        val encrypted = CryptoUtils.encrypt(rawData)
        return "GHOST_BACKUP_V1:${encrypted.ivBase64}:${encrypted.dataBase64}"
    }

    fun parseEncryptedBackupJson(backupPayload: String): String {
        if (!backupPayload.startsWith("GHOST_BACKUP_V1:")) return backupPayload
        val parts = backupPayload.split(":")
        if (parts.size >= 3) {
            val iv = parts[1]
            val data = parts[2]
            return CryptoUtils.decrypt(iv, data)
        }
        return ""
    }
}
