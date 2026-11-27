package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.SearchEngines

/**
 * Bottom sheet showing the current page's identity and quick actions:
 * security status, desktop / reader toggles and find-in-page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteInfoSheet(
    url: String,
    isDesktop: Boolean,
    isReader: Boolean,
    isIncognito: Boolean,
    onDismiss: () -> Unit,
    onToggleDesktop: () -> Unit,
    onToggleReader: () -> Unit,
    onFindInPage: () -> Unit,
    onClearSiteData: () -> Unit
) {
    val domain = SearchEngines.displayDomain(url)
    val secure = SearchEngines.isSecureUrl(url)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Security header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (secure) Icons.Filled.Lock else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (secure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (secure) "Connection is secure" else "Connection is not secure",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isIncognito) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Incognito") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Page actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    ActionRow(
                        icon = Icons.Filled.DesktopWindows,
                        title = "Desktop site",
                        checked = isDesktop,
                        onClick = onToggleDesktop
                    )
                    ActionRow(
                        icon = Icons.Filled.Article,
                        title = "Reader mode",
                        checked = isReader,
                        onClick = onToggleReader
                    )
                    ActionRow(
                        icon = Icons.Filled.FindInPage,
                        title = "Find in page",
                        onClick = onFindInPage
                    )
                    ActionRow(
                        icon = Icons.Filled.Delete,
                        title = "Clear site data",
                        danger = true,
                        onClick = onClearSiteData
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableSafe(onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(12.dp))
        if (checked) {
            Icon(Icons.Filled.Check, contentDescription = "Enabled", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun Modifier.clickableSafe(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
