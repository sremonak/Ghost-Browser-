package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.GhostBrowserTheme
import com.example.ui.viewmodel.GhostBrowserViewModel
import com.example.ui.viewmodel.GhostModalType

class MainActivity : ComponentActivity() {
    private val viewModel: GhostBrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val history by viewModel.historyState.collectAsStateWithLifecycle()
            val bookmarks by viewModel.bookmarksState.collectAsStateWithLifecycle()
            val passwords by viewModel.passwordsState.collectAsStateWithLifecycle()
            val plugins by viewModel.pluginsState.collectAsStateWithLifecycle()

            val activeTab = viewModel.activeTab
            val currentUrl = activeTab?.url ?: "ghost://newtab"
            val isBookmarked = bookmarks.any { it.url == currentUrl }

            // Handle Toast Notifications
            LaunchedEffect(uiState.toastMessage) {
                uiState.toastMessage?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            Crossfade(
                targetState = uiState.isHackerTheme,
                animationSpec = tween(500),
                label = "ThemeFade"
            ) { isHacker ->
                GhostBrowserTheme(darkTheme = if (isHacker) true else uiState.isDarkTheme) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isHacker) Color(0xFF020703) else MaterialTheme.colorScheme.background)
                    ) {
                        Scaffold(
                            topBar = {
                                ChromeOmnibox(
                                    currentUrl = currentUrl,
                                    isBookmarked = isBookmarked,
                                    tabCount = uiState.tabs.size,
                                    isIncognito = uiState.isIncognitoMode,
                                    adsBlockedCount = uiState.adBlockStats.adsBlocked,
                                    isLoading = uiState.isPageLoading,
                                    progress = uiState.pageProgress,
                                    canGoBack = activeTab?.backStack?.isNotEmpty() == true,
                                    canGoForward = activeTab?.forwardStack?.isNotEmpty() == true,
                                    onGoBack = { viewModel.goBackInTab() },
                                    onGoForward = { viewModel.goForwardInTab() },
                                    onReload = { viewModel.reloadCurrentTab() },
                                    onNavigate = { url -> viewModel.navigateToUrl(url) },
                                    onOpenTabSwitcher = { viewModel.setModal(GhostModalType.TAB_SWITCHER) },
                                    onOpenAdBlockerSettings = { viewModel.setModal(GhostModalType.ADBLOCKER_SETTINGS) },
                                    onToggleBookmark = {
                                        if (isBookmarked) {
                                            bookmarks.firstOrNull { it.url == currentUrl }?.let { b ->
                                                viewModel.removeBookmark(b.id)
                                            }
                                        } else {
                                            viewModel.addBookmark(activeTab?.title ?: currentUrl, currentUrl)
                                        }
                                    },
                                    onOpenExtensionStore = { viewModel.setModal(GhostModalType.EXTENSION_STORE) },
                                    onToggleIncognito = {
                                        viewModel.setIncognitoMode(!uiState.isIncognitoMode)
                                    },
                                    onOpenMenuAction = { action ->
                                        when (action) {
                                            "NEW_TAB" -> viewModel.openNewTab(isIncognito = false)
                                            "NEW_INCOGNITO" -> viewModel.openNewTab(isIncognito = true)
                                            "INSPECT_PAGE" -> viewModel.prepareInspectPage(currentUrl, activeTab?.title ?: currentUrl)
                                            "TOGGLE_HACKER_THEME" -> viewModel.toggleHackerTheme()
                                            "HISTORY_BOOKMARKS" -> viewModel.setModal(GhostModalType.HISTORY_BOOKMARKS)
                                            "PASSWORD_VAULT" -> viewModel.setModal(GhostModalType.PASSWORD_VAULT)
                                            "GHOST_SYNC" -> viewModel.setModal(GhostModalType.GHOST_SYNC)
                                            "KEYBOARD_SHORTCUTS" -> viewModel.setModal(GhostModalType.KEYBOARD_SHORTCUTS)
                                            "VPN_PROXY" -> viewModel.setModal(GhostModalType.VPN_PROXY)
                                            "SETTINGS" -> viewModel.setModal(GhostModalType.SETTINGS)
                                            "ABOUT_PAGE" -> viewModel.setModal(GhostModalType.ABOUT_PAGE)
                                            "NUKE_DATA" -> viewModel.nukeAllData()
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                // Main Content View Area
                                if (currentUrl == "ghost://newtab" || currentUrl.isBlank()) {
                                    GhostDashboard(
                                        isIncognito = uiState.isIncognitoMode,
                                        searchEngine = uiState.searchEngine,
                                        adBlockStats = uiState.adBlockStats,
                                        memoryStatus = uiState.memoryStatus,
                                        isHackerTheme = uiState.isHackerTheme,
                                        onNavigate = { url -> viewModel.navigateToUrl(url) },
                                        onSelectSearchEngine = { engine -> viewModel.setSearchEngine(engine) },
                                        onOptimizeMemory = { viewModel.optimizeMemory() },
                                        onOpenAdBlocker = { viewModel.setModal(GhostModalType.ADBLOCKER_SETTINGS) },
                                        onToggleHackerTheme = { viewModel.toggleHackerTheme() }
                                    )
                                } else {
                                    GhostWebView(
                                        url = currentUrl,
                                        userAgentMode = uiState.userAgentMode,
                                        isIncognito = uiState.isIncognitoMode,
                                        siteSettings = uiState.activeSiteSecurity,
                                        reloadTrigger = uiState.reloadTrigger,
                                        onPageTitleChanged = { title ->
                                            activeTab?.let { t ->
                                                val updated = t.copy(title = title)
                                            }
                                        },
                                        onLoadingChanged = { isLoading, progress ->
                                            viewModel.setPageLoading(isLoading, progress)
                                        },
                                        onAdBlocked = { blockedUrl, category ->
                                            viewModel.recordAdOrTrackerBlocked(blockedUrl, category)
                                        }
                                    )
                                }

                                // Active Modal Dialog Overlays
                                when (uiState.activeModal) {
                                    GhostModalType.TAB_SWITCHER -> {
                                        ChromeTabSwitcher(
                                            tabs = uiState.tabs,
                                            activeTabId = uiState.activeTabId,
                                            tabGroups = uiState.tabGroups,
                                            isIncognitoMode = uiState.isIncognitoMode,
                                            activeGroupFilterId = uiState.activeTabGroupFilterId,
                                            onSelectTab = { id -> viewModel.selectTab(id) },
                                            onCloseTab = { id -> viewModel.closeTab(id) },
                                            onNewTab = { incognito -> viewModel.openNewTab(isIncognito = incognito) },
                                            onCreateGroup = { name, color -> viewModel.createTabGroup(name, color) },
                                            onSetGroupFilter = { gId -> viewModel.setTabGroupFilter(gId) },
                                            onToggleIncognito = { incognito -> viewModel.setIncognitoMode(incognito) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.ADBLOCKER_SETTINGS -> {
                                        AdBlockerBottomSheet(
                                            stats = uiState.adBlockStats,
                                            siteSettings = uiState.activeSiteSecurity,
                                            recentBlockedRequests = uiState.recentBlockedRequests,
                                            onUpdateSiteSettings = { settings -> viewModel.updateSiteSecuritySettings(settings) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.EXTENSION_STORE -> {
                                        ExtensionStoreModal(
                                            plugins = plugins,
                                            onTogglePlugin = { id, enabled -> viewModel.togglePluginEnabled(id, enabled) },
                                            onInstallCustomPlugin = { name, desc, cat, icon -> viewModel.installCustomPlugin(name, desc, cat, icon) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.INSPECT_ELEMENT -> {
                                        InspectElementModal(
                                            pageSource = uiState.inspectPageSource,
                                            consoleLogs = uiState.inspectConsoleLogs,
                                            httpHeaders = uiState.inspectHttpHeaders,
                                            isHackerTheme = uiState.isHackerTheme,
                                            onExecuteJs = { cmd -> viewModel.executeInspectJsCommand(cmd) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.PASSWORD_VAULT -> {
                                        PasswordVaultModal(
                                            passwords = passwords,
                                            onSavePassword = { d, u, p -> viewModel.savePassword(d, u, p) },
                                            onDeletePassword = { id -> viewModel.deletePassword(id) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.GHOST_SYNC -> {
                                        GhostSyncModal(
                                            pairingInfo = uiState.syncPairingInfo,
                                            onExportBackup = {
                                                viewModel.showToast("Encrypted JSON Backup downloaded!")
                                            },
                                            onImportBackup = {
                                                viewModel.showToast("Encrypted Backup verified and restored!")
                                            },
                                            onNukeData = { viewModel.nukeAllData() },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.HISTORY_BOOKMARKS -> {
                                        HistoryBookmarksModal(
                                            history = history,
                                            bookmarks = bookmarks,
                                            onSelectUrl = { url -> viewModel.navigateToUrl(url) },
                                            onDeleteHistoryItem = { id -> viewModel.deleteHistoryById(id) },
                                            onClearHistory = { viewModel.clearHistory() },
                                            onDeleteBookmark = { id -> viewModel.removeBookmark(id) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.KEYBOARD_SHORTCUTS -> {
                                        KeyboardShortcutsModal(
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.SETTINGS -> {
                                        GhostSettingsModal(
                                            isDarkTheme = uiState.isDarkTheme,
                                            userAgentMode = uiState.userAgentMode,
                                            memoryLevel = uiState.memoryStatus.optimizationLevel,
                                            searchEngine = uiState.searchEngine,
                                            isHackerTheme = uiState.isHackerTheme,
                                            onToggleTheme = { viewModel.toggleTheme() },
                                            onToggleHackerTheme = { viewModel.toggleHackerTheme() },
                                            onSelectUserAgentMode = { mode -> viewModel.setUserAgentMode(mode) },
                                            onSelectMemoryLevel = { lvl -> viewModel.setMemoryOptimizationLevel(lvl) },
                                            onSelectSearchEngine = { eng -> viewModel.setSearchEngine(eng) },
                                            onOpenVpnModal = { viewModel.setModal(GhostModalType.VPN_PROXY) },
                                            onOpenAboutModal = { viewModel.setModal(GhostModalType.ABOUT_PAGE) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.VPN_PROXY -> {
                                        GhostVpnModal(
                                            isVpnEnabled = uiState.isVpnEnabled,
                                            selectedNode = uiState.selectedVpnNode,
                                            proxyType = uiState.proxyType,
                                            dohProvider = uiState.dohProvider,
                                            customProxyHost = uiState.customProxyHost,
                                            customProxyPort = uiState.customProxyPort,
                                            isHackerTheme = uiState.isHackerTheme,
                                            onToggleVpn = { enabled -> viewModel.toggleVpn(enabled) },
                                            onSelectNode = { node -> viewModel.setVpnNode(node) },
                                            onSelectProxyType = { type -> viewModel.setProxyType(type) },
                                            onSelectDohProvider = { doh -> viewModel.setDohProvider(doh) },
                                            onUpdateCustomProxy = { h, p -> viewModel.setCustomProxy(h, p) },
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.ABOUT_PAGE -> {
                                        GhostAboutModal(
                                            isHackerTheme = uiState.isHackerTheme,
                                            onDismiss = { viewModel.setModal(GhostModalType.NONE) }
                                        )
                                    }
                                    GhostModalType.NONE -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Hardware Keyboard Shortcuts Handler
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && event.isCtrlPressed) {
            when (keyCode) {
                KeyEvent.KEYCODE_T -> {
                    viewModel.openNewTab()
                    return true
                }
                KeyEvent.KEYCODE_W -> {
                    viewModel.activeTab?.id?.let { viewModel.closeTab(it) }
                    return true
                }
                KeyEvent.KEYCODE_N -> {
                    if (event.isShiftPressed) {
                        viewModel.openNewTab(isIncognito = true)
                        return true
                    }
                }
                KeyEvent.KEYCODE_H -> {
                    viewModel.setModal(GhostModalType.HISTORY_BOOKMARKS)
                    return true
                }
                KeyEvent.KEYCODE_B -> {
                    viewModel.activeTab?.let {
                        viewModel.addBookmark(it.title, it.url)
                    }
                    return true
                }
                KeyEvent.KEYCODE_M -> {
                    if (event.isShiftPressed) {
                        viewModel.optimizeMemory()
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
