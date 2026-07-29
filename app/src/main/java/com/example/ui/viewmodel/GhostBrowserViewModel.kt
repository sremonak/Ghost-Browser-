package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.engine.*
import com.example.ui.components.DohProvider
import com.example.ui.components.ProxyType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class GhostModalType {
    NONE,
    TAB_SWITCHER,
    ADBLOCKER_SETTINGS,
    EXTENSION_STORE,
    PASSWORD_VAULT,
    GHOST_SYNC,
    HISTORY_BOOKMARKS,
    KEYBOARD_SHORTCUTS,
    SETTINGS,
    INSPECT_ELEMENT,
    VPN_PROXY,
    ABOUT_PAGE
}

data class BrowserTabState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "ghost://newtab",
    val isIncognito: Boolean = false,
    val tabGroupId: String? = null,
    val backStack: List<String> = emptyList(),
    val forwardStack: List<String> = emptyList(),
    val isSleeping: Boolean = false,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

data class GhostBrowserUiState(
    val tabs: List<BrowserTabState> = emptyList(),
    val activeTabId: String = "",
    val tabGroups: List<TabGroupEntity> = emptyList(),
    val isIncognitoMode: Boolean = false,
    val isDarkTheme: Boolean = true,
    val isHackerTheme: Boolean = false,
    val activeModal: GhostModalType = GhostModalType.NONE,
    val adBlockStats: AdBlockStats = AdBlockStats(),
    val recentBlockedRequests: List<BlockedRequestItem> = emptyList(),
    val memoryStatus: MemoryStatus = MemoryStatus(84, 512, 16, 0, MemoryOptimizationLevel.BALANCED),
    val userAgentMode: UserAgentMode = UserAgentMode.CHROME_MOBILE,
    val searchEngine: String = "DuckDuckGo",
    val activeSiteSecurity: SiteSecuritySettings = SiteSecuritySettings("ghost.local"),
    val activeTabGroupFilterId: String? = null,
    val syncPairingInfo: GhostSyncPairingInfo = GhostSyncPairingInfo(),
    val reloadTrigger: Long = 0L,
    val isPageLoading: Boolean = false,
    val pageProgress: Float = 0f,
    val toastMessage: String? = null,
    val inspectPageSource: String = "",
    val inspectConsoleLogs: List<String> = emptyList(),
    val inspectHttpHeaders: Map<String, String> = emptyMap(),
    val isVpnEnabled: Boolean = false,
    val selectedVpnNode: String = "Frankfurt, Germany",
    val proxyType: ProxyType = ProxyType.GHOST_TUNNEL,
    val dohProvider: DohProvider = DohProvider.CLOUDFLARE,
    val customProxyHost: String = "127.0.0.1",
    val customProxyPort: String = "8080"
)

class GhostBrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GhostRepository

    private val _uiState = MutableStateFlow(GhostBrowserUiState())
    val uiState: StateFlow<GhostBrowserUiState> = _uiState.asStateFlow()

    val historyState: StateFlow<List<BrowsingHistoryEntity>>
    val bookmarksState: StateFlow<List<BookmarkEntity>>
    val passwordsState: StateFlow<List<PasswordVaultEntity>>
    val pluginsState: StateFlow<List<ExtensionPluginEntity>>

    init {
        val database = GhostDatabase.getInstance(application)
        repository = GhostRepository(database)

        historyState = repository.allHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        bookmarksState = repository.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        passwordsState = repository.allPasswords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        pluginsState = repository.allPlugins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.initializeDefaultPluginsIfNeeded()
            setupInitialTabsAndGroups()
        }
    }

    private suspend fun setupInitialTabsAndGroups() {
        val initialTab = BrowserTabState(
            id = "tab_initial_1",
            title = "Ghost Dashboard",
            url = "ghost://newtab",
            isIncognito = false
        )
        val initialGroups = listOf(
            TabGroupEntity("group_work", "Work", "#8AB4F8"),
            TabGroupEntity("group_privacy", "Privacy Research", "#D0BCFF")
        )
        for (group in initialGroups) {
            repository.saveTabGroup(group)
        }
        _uiState.update {
            it.copy(
                tabs = listOf(initialTab),
                activeTabId = initialTab.id,
                tabGroups = initialGroups
            )
        }
        updateMemoryStatus()
    }

    val activeTab: BrowserTabState?
        get() = uiState.value.tabs.find { it.id == uiState.value.activeTabId }

    fun navigateToUrl(inputUrl: String) {
        val active = activeTab ?: return
        var formattedUrl = inputUrl.trim()
        if (formattedUrl.isBlank()) return

        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://") && !formattedUrl.startsWith("ghost://")) {
            if (formattedUrl.contains(".") && !formattedUrl.contains(" ")) {
                formattedUrl = "https://$formattedUrl"
            } else {
                // Perform Search
                val engineQueryUrl = when (uiState.value.searchEngine) {
                    "Google" -> "https://www.google.com/search?q="
                    "Brave" -> "https://search.brave.com/search?q="
                    "Ecosia" -> "https://www.ecosia.org/search?q="
                    "Startpage" -> "https://www.startpage.com/sp/search?query="
                    else -> "https://duckduckgo.com/?q=" // DuckDuckGo
                }
                formattedUrl = "$engineQueryUrl${android.net.Uri.encode(formattedUrl)}"
            }
        }

        val updatedBackStack = if (active.url != formattedUrl && active.url != "ghost://newtab") {
            active.backStack + active.url
        } else active.backStack

        val updatedTab = active.copy(
            url = formattedUrl,
            title = extractTitleFromUrl(formattedUrl),
            backStack = updatedBackStack,
            forwardStack = emptyList(),
            isSleeping = false,
            lastActiveTimestamp = System.currentTimeMillis()
        )

        updateTabState(updatedTab)

        if (!updatedTab.isIncognito && !formattedUrl.startsWith("ghost://")) {
            viewModelScope.launch {
                repository.addHistory(updatedTab.title, formattedUrl)
            }
        }

        // Set site domain in security settings
        val domain = extractTitleFromUrl(formattedUrl)
        _uiState.update {
            it.copy(
                activeSiteSecurity = it.activeSiteSecurity.copy(domain = domain)
            )
        }
    }

    fun openNewTab(url: String = "ghost://newtab", isIncognito: Boolean = uiState.value.isIncognitoMode, groupId: String? = null) {
        val newTab = BrowserTabState(
            id = UUID.randomUUID().toString(),
            title = if (url == "ghost://newtab") "Ghost Dashboard" else extractTitleFromUrl(url),
            url = url,
            isIncognito = isIncognito,
            tabGroupId = groupId
        )
        _uiState.update {
            it.copy(
                tabs = it.tabs + newTab,
                activeTabId = newTab.id,
                isIncognitoMode = isIncognito,
                activeModal = GhostModalType.NONE
            )
        }
        updateMemoryStatus()
    }

    fun closeTab(tabId: String) {
        val currentTabs = uiState.value.tabs
        if (currentTabs.size <= 1) {
            // Reopen empty dashboard tab if last tab is closed
            openNewTab()
            return
        }
        val tabToClose = currentTabs.find { it.id == tabId }
        val remainingTabs = currentTabs.filter { it.id != tabId }
        val nextActiveId = if (uiState.value.activeTabId == tabId) {
            remainingTabs.lastOrNull()?.id ?: ""
        } else {
            uiState.value.activeTabId
        }

        _uiState.update {
            it.copy(
                tabs = remainingTabs,
                activeTabId = nextActiveId
            )
        }
        updateMemoryStatus()
    }

    fun selectTab(tabId: String) {
        val tab = uiState.value.tabs.find { it.id == tabId } ?: return
        _uiState.update {
            it.copy(
                activeTabId = tabId,
                isIncognitoMode = tab.isIncognito,
                activeModal = GhostModalType.NONE,
                tabs = it.tabs.map { t ->
                    if (t.id == tabId) t.copy(isSleeping = false, lastActiveTimestamp = System.currentTimeMillis()) else t
                }
            )
        }
    }

    fun updateActiveTabTitle(title: String) {
        val activeId = uiState.value.activeTabId
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == activeId) tab.copy(title = title) else tab
                }
            )
        }
    }

    fun setIncognitoMode(enabled: Boolean) {
        _uiState.update { it.copy(isIncognitoMode = enabled) }
        val incognitoTabs = uiState.value.tabs.filter { it.isIncognito == enabled }
        if (incognitoTabs.isNotEmpty()) {
            selectTab(incognitoTabs.last().id)
        } else {
            openNewTab(isIncognito = enabled)
        }
    }

    fun createTabGroup(name: String, colorHex: String) {
        val newGroup = TabGroupEntity(
            id = "group_" + UUID.randomUUID().toString().take(6),
            name = name,
            colorHex = colorHex
        )
        viewModelScope.launch {
            repository.saveTabGroup(newGroup)
            _uiState.update {
                it.copy(tabGroups = it.tabGroups + newGroup)
            }
        }
    }

    fun assignTabToGroup(tabId: String, groupId: String?) {
        val updatedTabs = uiState.value.tabs.map {
            if (it.id == tabId) it.copy(tabGroupId = groupId) else it
        }
        _uiState.update { it.copy(tabs = updatedTabs) }
    }

    fun setTabGroupFilter(groupId: String?) {
        _uiState.update { it.copy(activeTabGroupFilterId = groupId) }
    }

    fun setModal(modalType: GhostModalType) {
        _uiState.update { it.copy(activeModal = modalType) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun toggleHackerTheme() {
        val newHacker = !_uiState.value.isHackerTheme
        _uiState.update { it.copy(isHackerTheme = newHacker) }
        showToast(if (newHacker) "Hacker Matrix Mode Activated 🟢" else "Hacker Mode Deactivated")
    }

    fun installCustomPlugin(name: String, description: String, category: String = "Utilities", iconName: String = "extension") {
        val pluginId = "plugin_" + UUID.randomUUID().toString().take(8)
        val newPlugin = ExtensionPluginEntity(
            id = pluginId,
            name = name,
            description = description,
            version = "1.0.0",
            isEnabled = true,
            iconName = iconName,
            author = "Online Store / Ghost Registry",
            category = category
        )
        viewModelScope.launch {
            repository.saveCustomPlugin(newPlugin)
            showToast("Installed Extension: $name")
        }
    }

    fun prepareInspectPage(url: String, title: String) {
        val domain = extractTitleFromUrl(url)
        val mockHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$title</title>
                <style>
                    body { font-family: monospace; background: #111; color: #00FF66; margin: 16px; }
                    .header { font-weight: bold; border-bottom: 1px solid #00FF66; padding-bottom: 8px; }
                    .content { margin-top: 12px; font-size: 14px; line-height: 1.5; }
                    .badge { background: #005221; color: #00FF66; padding: 2px 6px; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>$title</h1>
                    <span class="badge">SECURE HTTPS / TLS 1.3</span>
                </div>
                <div class="content">
                    <p>Host Domain: <strong>$domain</strong></p>
                    <p>Ghost Ultra Shield Active: AdBlock, Anti-Tracker, Anti-Fingerprint Enabled</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        val mockLogs = listOf(
            "[LOG] Navigation completed to $url",
            "[INFO] Ghost AdBlocker injected 14 CSS rules",
            "[INFO] Canvas anti-fingerprint spoofer active (WebGL noise applied)",
            "[SEC] TLS Cipher Suite: TLS_AES_256_GCM_SHA384",
            "[STATUS] HTTP 200 OK • Content-Type: text/html; charset=utf-8"
        )

        val mockHeaders = mapOf(
            "Host" to domain,
            "User-Agent" to uiState.value.userAgentMode.userAgentString,
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Content-Security-Policy" to "default-src 'self' 'unsafe-inline' https:;",
            "Strict-Transport-Security" to "max-age=31536000; includeSubDomains",
            "X-Content-Type-Options" to "nosniff",
            "X-Frame-Options" to "SAMEORIGIN",
            "X-XSS-Protection" to "1; mode=block",
            "Cache-Control" to "no-cache, no-store, must-revalidate"
        )

        _uiState.update {
            it.copy(
                inspectPageSource = mockHtml,
                inspectConsoleLogs = mockLogs,
                inspectHttpHeaders = mockHeaders,
                activeModal = GhostModalType.INSPECT_ELEMENT
            )
        }
    }

    fun executeInspectJsCommand(command: String) {
        val resultLog = when {
            command.equals("document.title", ignoreCase = true) -> "=> \"${activeTab?.title ?: "New Tab"}\""
            command.equals("location.href", ignoreCase = true) -> "=> \"${activeTab?.url ?: "ghost://newtab"}\""
            command.equals("document.cookie", ignoreCase = true) -> "=> \"ghost_session_id=encrypted_token; _adblock=strict; secure; samesite=strict\""
            command.equals("navigator.userAgent", ignoreCase = true) -> "=> \"${uiState.value.userAgentMode.userAgentString}\""
            command.contains("ping", ignoreCase = true) -> "=> PING google.com (142.250.190.46): 56 data bytes. 64 bytes: icmp_seq=1 ttl=118 time=14.2 ms"
            else -> "=> Executed: $command [Return: true]"
        }
        _uiState.update {
            it.copy(
                inspectConsoleLogs = it.inspectConsoleLogs + "> $command" + resultLog
            )
        }
    }

    fun recordAdOrTrackerBlocked(blockedUrl: String, category: String) {
        val item = BlockedRequestItem(url = blockedUrl, category = category)
        _uiState.update { state ->
            val currentStats = state.adBlockStats
            val isTracker = category.contains("Tracker") || category.contains("Telemetry")
            val updatedStats = currentStats.copy(
                adsBlocked = if (!isTracker) currentStats.adsBlocked + 1 else currentStats.adsBlocked,
                trackersBlocked = if (isTracker) currentStats.trackersBlocked + 1 else currentStats.trackersBlocked,
                scriptsBlocked = currentStats.scriptsBlocked + 1,
                dataSavedKb = currentStats.dataSavedKb + (15..80).random()
            )
            val updatedList = (listOf(item) + state.recentBlockedRequests.filterNot { it.url == blockedUrl }).take(25)
            state.copy(
                adBlockStats = updatedStats,
                recentBlockedRequests = updatedList
            )
        }
    }

    fun updateSiteSecuritySettings(settings: SiteSecuritySettings) {
        _uiState.update { it.copy(activeSiteSecurity = settings) }
    }

    fun setSearchEngine(engine: String) {
        _uiState.update { it.copy(searchEngine = engine) }
        showToast("Default search engine set to $engine")
    }

    fun setUserAgentMode(mode: UserAgentMode) {
        _uiState.update { it.copy(userAgentMode = mode) }
        showToast("User Agent set to ${mode.displayName}")
    }

    fun togglePluginEnabled(pluginId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setPluginEnabled(pluginId, isEnabled)
        }
    }

    fun deleteHistoryById(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showToast("Browsing history cleared")
        }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            repository.addBookmark(title, url)
            showToast("Added bookmark: $title")
        }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
            showToast("Bookmark removed")
        }
    }

    fun savePassword(domain: String, username: String, pass: String) {
        viewModelScope.launch {
            repository.savePassword(domain, username, pass)
            showToast("Encrypted password saved for $domain")
        }
    }

    fun deletePassword(id: Long) {
        viewModelScope.launch {
            repository.deletePassword(id)
        }
    }

    fun optimizeMemory() {
        LowMemoryOptimizer.triggerGarbageCollection()
        // Put inactive tabs (> 1 min old) into sleeping state
        val now = System.currentTimeMillis()
        val activeId = uiState.value.activeTabId
        val updatedTabs = uiState.value.tabs.map { tab ->
            if (tab.id != activeId && (now - tab.lastActiveTimestamp) > 30000) {
                tab.copy(isSleeping = true)
            } else tab
        }
        val sleepingCount = updatedTabs.count { it.isSleeping }
        _uiState.update {
            it.copy(
                tabs = updatedTabs,
                memoryStatus = LowMemoryOptimizer.getSystemMemoryStatus(
                    getApplication(),
                    suspendedCount = sleepingCount,
                    level = it.memoryStatus.optimizationLevel
                )
            )
        }
        showToast("Memory optimized! $sleepingCount tabs suspended to save RAM.")
    }

    fun setMemoryOptimizationLevel(level: MemoryOptimizationLevel) {
        _uiState.update {
            it.copy(
                memoryStatus = it.memoryStatus.copy(optimizationLevel = level)
            )
        }
        showToast("Memory Optimizer set to ${level.label}")
    }

    fun nukeAllData() {
        viewModelScope.launch {
            repository.nukeAllUserData()
            setupInitialTabsAndGroups()
            _uiState.update {
                it.copy(
                    activeModal = GhostModalType.NONE,
                    adBlockStats = AdBlockStats()
                )
            }
            showToast("Ghost Nuke executed: All history, tabs, cookies, and passwords wiped!")
        }
    }

    fun goBackInTab() {
        val active = activeTab ?: return
        if (active.backStack.isNotEmpty()) {
            val prevUrl = active.backStack.last()
            val newBack = active.backStack.dropLast(1)
            val newForward = active.forwardStack + active.url
            updateTabState(active.copy(url = prevUrl, backStack = newBack, forwardStack = newForward, title = extractTitleFromUrl(prevUrl)))
        }
    }

    fun goForwardInTab() {
        val active = activeTab ?: return
        if (active.forwardStack.isNotEmpty()) {
            val nextUrl = active.forwardStack.last()
            val newForward = active.forwardStack.dropLast(1)
            val newBack = active.backStack + active.url
            updateTabState(active.copy(url = nextUrl, backStack = newBack, forwardStack = newForward, title = extractTitleFromUrl(nextUrl)))
        }
    }

    fun reloadCurrentTab() {
        val active = activeTab ?: return
        _uiState.update { 
            it.copy(
                isPageLoading = true, 
                pageProgress = 0.15f,
                reloadTrigger = System.currentTimeMillis()
            ) 
        }
    }

    fun setPageLoading(isLoading: Boolean, progress: Float = 0f) {
        _uiState.update { it.copy(isPageLoading = isLoading, pageProgress = progress) }
    }

    private fun updateTabState(updatedTab: BrowserTabState) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { if (it.id == updatedTab.id) updatedTab else it }
            )
        }
    }

    private fun updateMemoryStatus() {
        val suspendedCount = uiState.value.tabs.count { it.isSleeping }
        _uiState.update {
            it.copy(
                memoryStatus = LowMemoryOptimizer.getSystemMemoryStatus(
                    getApplication(),
                    suspendedCount = suspendedCount,
                    level = it.memoryStatus.optimizationLevel
                )
            )
        }
    }

    private fun extractTitleFromUrl(url: String): String {
        if (url == "ghost://newtab") return "Ghost Dashboard"
        return try {
            val uri = android.net.Uri.parse(url)
            val host = uri.host ?: url
            host.removePrefix("www.")
        } catch (e: Exception) {
            url
        }
    }

    fun toggleVpn(enabled: Boolean) {
        _uiState.update { it.copy(isVpnEnabled = enabled) }
        val msg = if (enabled) "VPN Encrypted Tunnel Connected (${uiState.value.selectedVpnNode})" else "VPN Tunnel Disconnected"
        showToast(msg)
    }

    fun setVpnNode(node: String) {
        _uiState.update { it.copy(selectedVpnNode = node) }
        showToast("Switched Ghost Privacy Node to $node")
    }

    fun setProxyType(type: ProxyType) {
        _uiState.update { it.copy(proxyType = type) }
        showToast("Proxy Protocol updated to ${type.displayName}")
    }

    fun setDohProvider(doh: DohProvider) {
        _uiState.update { it.copy(dohProvider = doh) }
        showToast("Encrypted DoH switched to ${doh.displayName}")
    }

    fun setCustomProxy(host: String, port: String) {
        _uiState.update { it.copy(customProxyHost = host, customProxyPort = port) }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
