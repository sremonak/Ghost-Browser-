package com.example.engine

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * Owns the single [WebView] used by the active tab.
 *
 * - Saves/restore each tab's scroll + form state via [WebView.saveState]
 *   so switching tabs does not lose progress.
 * - Applies the privacy engine (ad / tracker blocking) at the network layer.
 * - Routes downloads through the system [DownloadManager].
 * - Exposes find-in-page, desktop mode and per-site toggles.
 *
 * The controller is created by the Compose layer and handed to the ViewModel
 * so navigation intents (back / forward / reload / search) can reach the view.
 */
@SuppressLint("SetJavaScriptEnabled")
class WebViewController(private val context: Context) {

    private var webView: WebView? = null

    var onTitleChanged: ((String) -> Unit)? = null
    var onLoadingChanged: ((Boolean, Float) -> Unit)? = null
    var onUrlChanged: ((String) -> Unit)? = null
    var onAdBlocked: ((String, String) -> Unit)? = null
    var onDownload: ((String, String, String, String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onFindResult: ((Int) -> Unit)? = null
    var onContextMenu: ((url: String, selection: String, imageUrl: String) -> Unit)? = null

    // Per-tab saved state, keyed by tab id.
    private val savedStates = mutableMapOf<String, android.os.Bundle>()

    private var currentUserAgent: UserAgentMode = UserAgentMode.CHROME_MOBILE
    private var customUserAgent: String = ""
    private var isDesktopMode = false
    private var isReaderMode = false
    private var javascriptEnabled = true
    private var adBlockEnabled = true
    private var trackerBlockEnabled = true
    private var blockThirdPartyCookies = true
    private var dataSaverEnabled = false

    // Snapshot of the current navigation stack reported by the WebView itself.
    private var currentLoadedUrl: String = ""
    private var currentTitle: String = ""

    val view: WebView?
        get() = webView

    val loadedUrl: String
        get() = currentLoadedUrl

    val title: String
        get() = currentTitle

    /**
     * Builds and configures the WebView. Call once when the composable mounts.
     */
    fun createWebView(): WebView {
        val view = WebView(context)
        view.settings.apply {
            javaScriptEnabled = this@WebViewController.javascriptEnabled
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = false
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            blockNetworkImage = false
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }
        view.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val reqUrl = request?.url?.toString() ?: return null

                    if (adBlockEnabled && NovaAdBlocker.isAdOrTracker(reqUrl)) {
                        val category = NovaAdBlocker.classifyRequest(reqUrl)
                        onAdBlocked?.invoke(reqUrl, category)
                        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                    }

                    // Data saver: block image sub-resources of the main frame.
                    if (dataSaverEnabled && request?.isForMainFrame == false &&
                        request.method == "GET"
                    ) {
                        val mime = request.url?.let { safeMimeType(it.toString()) } ?: ""
                        if (mime == "image") {
                            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    currentLoadedUrl = url ?: ""
                    onUrlChanged?.invoke(currentLoadedUrl)
                    onLoadingChanged?.invoke(true, 0.15f)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onLoadingChanged?.invoke(false, 1.0f)
                    val t = view?.title ?: ""
                    if (t.isNotEmpty()) {
                        currentTitle = t
                        onTitleChanged?.invoke(t)
                    }
                    if (adBlockEnabled) {
                        view?.evaluateJavascript(NovaAdBlocker.adBlockInjectCss, null)
                    }
                    if (isReaderMode) {
                        view?.evaluateJavascript(readerModeJs, null)
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    val isMainFrame = request?.isForMainFrame == true
                    if (isMainFrame) {
                        onLoadingChanged?.invoke(false, 0f)
                        onError?.invoke((error?.description ?: "Connection failed").toString())
                    }
                }
            }

            view.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    onLoadingChanged?.invoke(true, newProgress / 100f)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    title?.let {
                        currentTitle = it
                        onTitleChanged?.invoke(it)
                    }
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    // Grant page-requested hardware permissions (camera, mic, location)
                    // for the active site, matching standard browser behaviour.
                    request?.grant(request.resources)
                }

                override fun onShowFileChooser(
                    view: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    // No file-chooser flow is wired in this build; deny to avoid
                    // an infinite callback loop.
                    filePathCallback?.onReceiveValue(null)
                    return true
                }
            }

            // Long-press opens the Chrome-style context menu. The hit test
            // result tells us whether the user pressed a link, an image or
            // plain text; the selection text is read from the page.
            view.setOnLongClickListener {
                val hit = view.hitTestResult
                val imageUrl = if (hit != null && hit.type == WebView.HitTestResult.IMAGE_TYPE) {
                    hit.extra ?: ""
                } else ""
                val linkUrl = if (hit != null && (
                        hit.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                        hit.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                    )) {
                    hit.extra ?: ""
                } else ""
                view.evaluateJavascript("window.getSelection() ? window.getSelection().toString() : ''") { selection ->
                    onContextMenu?.invoke(
                        linkUrl.ifBlank { currentLoadedUrl },
                        selection?.removeSurrounding("\"") ?: "",
                        imageUrl
                    )
                }
                true
            }

            view.setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
                val info = DownloadHelper.enqueue(
                    context,
                    downloadUrl,
                    userAgent,
                    contentDisposition,
                    mimetype
                )
                onDownload?.invoke(info.fileName, info.url, info.mimeType, info.downloadId.toString())
            }

            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                if (blockThirdPartyCookies) {
                    setAcceptThirdPartyCookies(view, false)
                }
            }
        webView = view
        applyUserAgent(view)
        return view
    }

    fun destroy() {
        webView?.apply {
            stopLoading()
            removeJavascriptInterface("searchBoxJavaBridge_")
            destroy()
        }
        webView = null
        // Saved Bundles intentionally survive the WebView teardown so a tab's
        // scroll position and form state can be restored on the next creation.
    }

    // ---------------------------------------------------------------- navigation

    fun loadUrl(url: String) {
        webView?.loadUrl(url)
        currentLoadedUrl = url
        currentTitle = ""
    }

    fun reload() = webView?.reload()

    fun stopLoading() = webView?.stopLoading()

    fun goBack(): Boolean = webView?.let { it.canGoBack().also { can -> if (can) it.goBack() } } ?: false

    fun goForward(): Boolean = webView?.let { it.canGoForward().also { can -> if (can) it.goForward() } } ?: false

    fun canGoBack(): Boolean = webView?.canGoBack() ?: false

    fun canGoForward(): Boolean = webView?.canGoForward() ?: false

    // ------------------------------------------------------------- tab state save

    /**
     * Persists the WebView state (scroll position, forms, back/forward list)
     * under [tabId] so it survives a tab switch.
     */
    fun saveTabState(tabId: String) {
        webView?.let { view ->
            try {
                view.saveState()?.let { snapshot -> savedStates[tabId] = snapshot }
            } catch (e: Exception) {
                // Ignore save failures — a fresh load is always possible.
            }
        }
    }

    /**
     * Restores a previously saved tab state. Returns true when a snapshot
     * was found and applied.
     */
    fun restoreTabState(tabId: String): Boolean {
        val snapshot = savedStates[tabId] ?: return false
        return try {
            webView?.restoreState(snapshot) == true
        } catch (e: Exception) {
            false
        }
    }

    fun hasSavedState(tabId: String): Boolean = savedStates.containsKey(tabId)

    // --------------------------------------------------------------- appearance

    fun applyUserAgent(view: WebView? = webView) {
        view ?: return
        val ua = if (customUserAgent.isNotBlank()) customUserAgent else currentUserAgent.userAgentString
        if (view.settings.userAgentString != ua) {
            view.settings.userAgentString = ua
        }
        val desktop = isDesktopMode || currentUserAgent.isDesktop
        view.settings.useWideViewPort = desktop
        view.settings.loadWithOverviewMode = desktop
    }

    fun setUserAgent(mode: UserAgentMode, custom: String = "") {
        currentUserAgent = mode
        customUserAgent = custom
        applyUserAgent()
    }

    fun setDesktopMode(enabled: Boolean) {
        isDesktopMode = enabled
        applyUserAgent()
        currentLoadedUrl.takeIf { it.isNotBlank() }?.let { webView?.reload() }
    }

    fun setReaderMode(enabled: Boolean) {
        isReaderMode = enabled
        webView?.evaluateJavascript(
            if (enabled) readerModeJs else "null",
            null
        )
        if (!enabled && currentLoadedUrl.isNotBlank()) {
            webView?.reload()
        }
    }

    fun setJavaScriptEnabled(enabled: Boolean) {
        javascriptEnabled = enabled
        webView?.settings?.javaScriptEnabled = enabled
        if (currentLoadedUrl.isNotBlank()) webView?.reload()
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        adBlockEnabled = enabled
    }

    fun setTrackerBlockEnabled(enabled: Boolean) {
        trackerBlockEnabled = enabled
    }

    fun setBlockThirdPartyCookies(enabled: Boolean) {
        blockThirdPartyCookies = enabled
        webView?.let { view ->
            CookieManager.getInstance().setAcceptThirdPartyCookies(view, !enabled)
        }
    }

    fun setDataSaverEnabled(enabled: Boolean) {
        dataSaverEnabled = enabled
        webView?.settings?.blockNetworkImage = false // network images handled in interceptor
    }

    // ------------------------------------------------------------------ find in page

    fun findInPage(query: String): Int {
        if (query.isBlank()) {
            onFindResult?.invoke(0)
            return 0
        }
        // JSON-quote the query so it can be safely embedded in the JS literal.
        val jsQuery = org.json.JSONObject.quote(query)
        webView?.evaluateJavascript(
            """
            (function() {
                var q = $jsQuery;
                var count = 0;
                var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
                var node;
                var ql = q.toLowerCase();
                while ((node = walker.nextNode())) {
                    var text = node.textContent || '';
                    if (text.toLowerCase().includes(ql)) {
                        count++;
                        var range = document.createRange();
                        range.selectNodeContents(node);
                        var highlight = document.createElement('span');
                        highlight.style.background = '#FFE082';
                        highlight.style.color = '#000';
                        range.surroundContents(highlight);
                    }
                }
                return count;
            })();
            """.trimIndent(),
            { result ->
                val count = (result?.trim('"') ?: "0").toIntOrNull() ?: 0
                onFindResult?.invoke(count)
            }
        )
        return -1
    }

    fun clearFindInPage() {
        webView?.evaluateJavascript(
            "document.getElementById('nova-find-highlight')?.remove(); " +
                "document.querySelectorAll('span[style*=\"FFE082\"]').forEach(function(el){ " +
                "var parent = el.parentNode; parent.replaceChild(document.createTextNode(el.textContent), el); " +
                "parent.normalize(); });",
            null
        )
    }

    private fun safeMimeType(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val ext = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase() ?: ""
            when (ext) {
                "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp" -> "image"
                "mp4", "webm", "mkv", "avi" -> "video"
                "mp3", "ogg", "wav", "m4a" -> "audio"
                "html", "htm" -> "html"
                "css" -> "css"
                "js" -> "js"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        /**
         * Reader-mode CSS/JS: strips clutter and presents article text in a
         * clean, distraction-free layout.
         */
        val readerModeJs: String = """
            (function() {
                var style = document.createElement('style');
                style.id = 'nova-reader-style';
                style.innerHTML = [
                    'body { background:#fff !important; color:#1a1a1a !important; }',
                    'nav, header, footer, aside, .sidebar, .ads, .ad, [class*="nav"], [class*="menu"],',
                    '[class*="footer"], [class*="sidebar"], [class*="promo"], [class*="related"]',
                    '{ display:none !important; }',
                    'article, main, [role="main"] { max-width: 680px !important; margin: 0 auto !important;',
                    '  font-size: 18px !important; line-height: 1.7 !important; }',
                    'img { max-width: 100% !important; height: auto !important; }'
                ].join('\\n');
                document.head.appendChild(style);
            })();
        """.trimIndent()
    }
}
