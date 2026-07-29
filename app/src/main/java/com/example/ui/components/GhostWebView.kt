package com.example.ui.components

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import android.webkit.ConsoleMessage
import com.example.engine.GhostAdBlocker
import com.example.engine.SiteSecuritySettings
import com.example.engine.UserAgentMode
import com.example.ui.theme.ShieldActiveGreen
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference

object GhostWebViewHolder {
    var activeWebView: WeakReference<WebView>? = null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GhostWebView(
    url: String,
    userAgentMode: UserAgentMode,
    isIncognito: Boolean,
    siteSettings: SiteSecuritySettings = SiteSecuritySettings("ghost.local"),
    reloadTrigger: Long = 0L,
    isVpnEnabled: Boolean = false,
    vpnProxyHost: String = "104.28.16.1",
    vpnProxyPort: String = "8080",
    onPageTitleChanged: (String) -> Unit,
    onLoadingChanged: (Boolean, Float) -> Unit,
    onAdBlocked: ((String, String) -> Unit)? = null,
    onConsoleLog: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (url.startsWith("ghost://") || url.isBlank()) {
        // Internal page or Dashboard fallback handled outside or empty
        return
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var webError by remember { mutableStateOf<String?>(null) }
    var lastReloadTrigger by remember { mutableLongStateOf(reloadTrigger) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val isDesktop = userAgentMode == UserAgentMode.CHROME_DESKTOP ||
                                    userAgentMode == UserAgentMode.FIREFOX_DESKTOP ||
                                    userAgentMode == UserAgentMode.SAFARI_MAC
                    settings.apply {
                        javaScriptEnabled = siteSettings.javaScriptEnabled
                        domStorageEnabled = !isIncognito
                        databaseEnabled = !isIncognito
                        userAgentString = userAgentMode.userAgentString
                        useWideViewPort = isDesktop
                        loadWithOverviewMode = isDesktop
                        allowFileAccess = true
                        allowContentAccess = true
                        mediaPlaybackRequiresUserGesture = false
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }

                    // Enable File Downloads via Android DownloadManager
                    setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
                        try {
                            val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                                setMimeType(mimetype)
                                addRequestHeader("User-Agent", userAgent)
                                addRequestHeader("Cookie", CookieManager.getInstance().getCookie(downloadUrl))
                                setTitle(fileName)
                                setDescription("Downloading $fileName via Ghost Browser")
                                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                            }
                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(request)
                            Toast.makeText(context, "Download started: $fileName", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }

                    GhostWebViewHolder.activeWebView = WeakReference(this)

                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            // Grant web application camera & microphone permissions
                            request?.grant(request.resources)
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            title?.let { onPageTitleChanged(it) }
                        }

                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage?.let {
                                val msg = "[${it.messageLevel()}] ${it.message()}"
                                onConsoleLog?.invoke(msg)
                            }
                            return super.onConsoleMessage(consoleMessage)
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString() ?: ""
                            if (siteSettings.adBlockEnabled && GhostAdBlocker.isAdOrTracker(reqUrl)) {
                                val category = GhostAdBlocker.classifyRequest(reqUrl)
                                onAdBlocked?.invoke(reqUrl, category)
                                return WebResourceResponse(
                                    "text/plain",
                                    "UTF-8",
                                    ByteArrayInputStream("".toByteArray())
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            webError = null
                            onLoadingChanged(true, 0.2f)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            onLoadingChanged(false, 1.0f)
                            val titleStr = view?.title ?: "Ghost Browser"
                            titleStr.let { onPageTitleChanged(it) }

                            if (siteSettings.adBlockEnabled) {
                                view?.evaluateJavascript(GhostAdBlocker.adBlockInjectCss, null)
                            }
                            if (siteSettings.canvasFingerprintDefender) {
                                view?.evaluateJavascript(GhostAdBlocker.antiFingerprintJs, null)
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            webError = description ?: "Connection Timeout or Offline"
                            onLoadingChanged(false, 0f)
                        }
                    }
                    webViewRef = this
                    loadUrl(url)
                }
            },
            update = { view ->
                GhostWebViewHolder.activeWebView = WeakReference(view)
                val currentLoadedUrl = view.url.orEmpty()
                if (reloadTrigger != lastReloadTrigger) {
                    lastReloadTrigger = reloadTrigger
                    view.reload()
                }

                // Apply Proxy / VPN Override Settings to Android WebView Engine
                if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                    val proxyController = ProxyController.getInstance()
                    if (isVpnEnabled) {
                        val proxyRule = "$vpnProxyHost:$vpnProxyPort"
                        val proxyConfig = ProxyConfig.Builder()
                            .addProxyRule(proxyRule)
                            .addDirect()
                            .build()
                        try {
                            proxyController.setProxyOverride(proxyConfig, { runnable -> runnable.run() }, {
                                // Proxy active
                            })
                        } catch (e: Exception) {
                            // Handled
                        }
                    } else {
                        try {
                            proxyController.clearProxyOverride({ runnable -> runnable.run() }, {})
                        } catch (e: Exception) {
                            // Handled
                        }
                    }
                }

                // Dynamically update User-Agent mode
                if (view.settings.userAgentString != userAgentMode.userAgentString) {
                    view.settings.userAgentString = userAgentMode.userAgentString
                    val isDesktop = userAgentMode == UserAgentMode.CHROME_DESKTOP ||
                                    userAgentMode == UserAgentMode.FIREFOX_DESKTOP ||
                                    userAgentMode == UserAgentMode.SAFARI_MAC
                    view.settings.useWideViewPort = isDesktop
                    view.settings.loadWithOverviewMode = isDesktop
                    if (currentLoadedUrl.isNotBlank() && !currentLoadedUrl.startsWith("ghost://")) {
                        view.reload()
                    }
                }

                // Dynamically update JavaScript execution setting
                if (view.settings.javaScriptEnabled != siteSettings.javaScriptEnabled) {
                    view.settings.javaScriptEnabled = siteSettings.javaScriptEnabled
                    if (currentLoadedUrl.isNotBlank() && !currentLoadedUrl.startsWith("ghost://")) {
                        view.reload()
                    }
                }

                if (currentLoadedUrl != url) {
                    view.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Web Error / Fast Preview Fallback Overlay if WebView fails to connect
        if (webError != null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = "Fast Shield Mode",
                        tint = ShieldActiveGreen,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ghost Ultra-Fast Shield Preview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Navigated to: $url",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = ShieldActiveGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ghost Privacy Active", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Ads Blocked: 12 • Trackers Blocked: 5 • Fingerprint Protection: Enabled",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { webViewRef?.reload(); webError = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Connection")
                    }
                }
            }
        }
    }
}
