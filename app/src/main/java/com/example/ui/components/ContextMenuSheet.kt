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
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ContextMenuState
import com.example.ui.viewmodel.NovaBrowserViewModel

/**
 * Chrome-style context menu shown after long-pressing a link, text or image
 * on a page. Actions are routed through the [NovaBrowserViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextMenuSheet(
    state: ContextMenuState,
    isBookmarked: Boolean,
    isInReadingList: Boolean,
    viewModel: NovaBrowserViewModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Page actions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val actions = buildList {
                add(
                    ContextAction(
                        icon = Icons.Filled.ContentCopy,
                        title = "Copy link",
                        onClick = { viewModel.copyToClipboard(state.url); onDismiss() }
                    )
                )
                if (state.selection.isNotBlank()) {
                    add(
                        ContextAction(
                            icon = Icons.Filled.TextFields,
                            title = "Copy text",
                            onClick = { viewModel.copyToClipboard(state.selection); onDismiss() }
                        )
                    )
                }
                add(
                    ContextAction(
                        icon = Icons.Filled.OpenInNew,
                        title = "Open in new tab",
                        onClick = { viewModel.openInNewTab(state.url) }
                    )
                )
                add(
                    ContextAction(
                        icon = Icons.Filled.Share,
                        title = "Share link",
                        onClick = { viewModel.shareText(state.url); onDismiss() }
                    )
                )
                add(
                    ContextAction(
                        icon = Icons.Filled.Download,
                        title = "Download link",
                        onClick = { viewModel.downloadLink(state.url) }
                    )
                )
                if (state.imageUrl.isNotBlank()) {
                    add(
                        ContextAction(
                            icon = Icons.Filled.Image,
                            title = "Save image",
                            onClick = { viewModel.saveImage(state.imageUrl) }
                        )
                    )
                }
                add(
                    ContextAction(
                        icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        title = if (isBookmarked) "Remove bookmark" else "Bookmark this page",
                        onClick = {
                            viewModel.toggleBookmark(state.url, state.url)
                            onDismiss()
                        }
                    )
                )
                add(
                    ContextAction(
                        icon = if (isInReadingList) Icons.Filled.Article else Icons.Filled.Article,
                        title = if (isInReadingList) "Remove from reading list" else "Save for later reading",
                        onClick = {
                            viewModel.toggleReadingList(state.url, state.url)
                            onDismiss()
                        }
                    )
                )
                add(
                    ContextAction(
                        icon = Icons.Filled.AddToHomeScreen,
                        title = "Add to home screen",
                        onClick = { viewModel.addToHomeScreen(state.url, state.url) }
                    )
                )
            }

            actions.forEach { action ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableSafe(action.onClick)
                        .padding(horizontal = 12.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private data class ContextAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val onClick: () -> Unit
)

@Composable
private fun Modifier.clickableSafe(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
