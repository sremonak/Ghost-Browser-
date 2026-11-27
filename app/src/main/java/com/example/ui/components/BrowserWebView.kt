package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.WebViewController

/**
 * Hosts the single [android.webkit.WebView] used by the active tab.
 *
 * The controller is created by the caller and wired to the ViewModel here, so
 * navigation intents (back / forward / reload / search) can reach the live
 * view. A previously saved tab snapshot is restored when available; otherwise
 * the target URL is loaded. The view is destroyed when the composable leaves
 * the composition.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    webController: WebViewController,
    tabId: String,
    url: String,
    onPageTitleChanged: (String) -> Unit,
    onLoadingChanged: (Boolean, Float) -> Unit,
    onUrlChanged: (String) -> Unit,
    onAdBlocked: (String, String) -> Unit,
    onDownload: (String, String, String, String) -> Unit,
    onError: (String) -> Unit,
    onNavigationStateChanged: (Boolean, Boolean) -> Unit,
    onFindResult: (Int) -> Unit,
    onContextMenu: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var createdView by remember { mutableStateOf<android.webkit.WebView?>(null) }
    // Remember which tab the WebView currently shows so a tab switch can
    // restore the incoming tab's saved snapshot instead of reloading.
    var boundTabId by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                webController.createWebView().apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                webController.onTitleChanged = onPageTitleChanged
                webController.onLoadingChanged = onLoadingChanged
                webController.onUrlChanged = onUrlChanged
                webController.onAdBlocked = onAdBlocked
                webController.onDownload = onDownload
                webController.onError = onError
                webController.onFindResult = onFindResult
                webController.onContextMenu = onContextMenu

                createdView = view

                // On a tab switch, restore the incoming tab's saved snapshot.
                if (boundTabId != tabId) {
                    boundTabId = tabId
                    if (webController.restoreTabState(tabId)) {
                        return@AndroidView
                    }
                }

                // Restore a previously saved tab snapshot when available;
                // otherwise load the target URL.
                if (view.url.isNullOrEmpty()) {
                    if (!webController.restoreTabState(tabId)) {
                        view.loadUrl(url)
                    }
                } else if (view.url != url) {
                    // Stop any in-flight load of the previous page before
                    // navigating to the new URL.
                    if (view.isLoading()) view.stopLoading()
                    view.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Report WebView back/forward availability after a load starts.
    LaunchedEffect(url, createdView) {
        val view = createdView ?: return@LaunchedEffect
        onNavigationStateChanged(view.canGoBack(), view.canGoForward())
    }

    // Save the tab state and release the view when leaving the composition.
    DisposableEffect(Unit) {
        onDispose {
            webController.saveTabState(tabId)
            webController.destroy()
        }
    }
}
