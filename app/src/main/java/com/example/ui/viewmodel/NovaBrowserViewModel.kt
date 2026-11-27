package com.example.ui.viewmodel

import android.app.Application
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrowserSettings
import com.example.data.DownloadEntity
import com.example.data.NovaPreferences
import com.example.data.NovaRepository
import com.example.data.ReadingListEntity
import com.example.engine.BlockStats
import com.example.engine.BlockedRequestItem
import com.example.engine.SearchEngines
import com.example.engine.UserAgentMode
import com.example.engine.WebViewController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class NovaScreen { BROWSER, HISTORY, BOOKMARKS, DOWNLOADS, READING_LIST, SETTINGS }

enum class NovaModal { NONE, TAB_SWITCHER, MENU, SITE_INFO }

data class BrowserTabState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "nova://home",
    val isIncognito: Boolean = false,
    val tabGroupId: String? = null,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val isDesktop: Boolean = false,
    val isReader: Boolean = false
)

data class NovaBrowserUiState(
    val tabs: List<BrowserTabState> = emptyList(),
    val activeTabId: String = "",
    val tabGroups: List<com.example.data.TabGroupEntity> = emptyList(),
    val settings: BrowserSettings = BrowserSettings(),
    val isDarkTheme: Boolean = true,
    val searchEngine: String = "Google",
    val screen: NovaScreen = NovaScreen.BROWSER,
    val modal: NovaModal = NovaModal.NONE,
    val blockStats: BlockStats = BlockStats(),
    val recentBlocked: List<BlockedRequestItem> = emptyList(),
    val pageError: String? = null,
    val findQuery: String = "",
    val findCount: Int = 0,
    val toastMessage: String? = null,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false
)

/**
 * Describes the page action menu shown after a long-press. [url] is the
 * link under the press (or the current page when none), [selection] the
 * highlighted text and [imageUrl] the pressed image, when applicable.
 */
data class ContextMenuState(
    val url: String = "",
    val selection: String = "",
    val imageUrl: String = "",
    val isVisible: Boolean = false
)

/**
 * Central state holder for Nova Browser.
 *
 * Holds tabs, navigation intents and user data in memory while persisting
 * tabs / history / bookmarks / downloads and settings through the repository
 * and preferences layer. The [WebViewController] is attached by the Compose
 * layer so navigation commands can reach the live WebView.
 */
class NovaBrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NovaRepository
    private val preferences: NovaPreferences

    // Attached by the Compose layer when the WebView is created.
    var webController: WebViewController? = null
        private set

    fun attachWebController(controller: WebViewController) {
        webController = controller
        applySettingsToController()
    }

    fun detachWebController() {
        webController?.saveTabState(_uiState.value.activeTabId)
        webController = null
    }

    private val _uiState = MutableStateFlow(NovaBrowserUiState())
    val uiState: StateFlow<NovaBrowserUiState> = _uiState.asStateFlow()

    private val _contextMenu = MutableStateFlow(ContextMenuState())
    val contextMenuState: StateFlow<ContextMenuState> = _contextMenu.asStateFlow()

    val historyState: StateFlow<List<com.example.data.HistoryEntity>>
    val bookmarksState: StateFlow<List<com.example.data.BookmarkEntity>>
    val downloadsState: StateFlow<List<DownloadEntity>>
    val readingListState: StateFlow<List<ReadingListEntity>>

    init {
        val database = com.example.data.NovaDatabase.getInstance(application)
        repository = NovaRepository(database)
        preferences = NovaPreferences(application)

        historyState = repository.allHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        bookmarksState = repository.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        downloadsState = repository.allDownloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        readingListState = repository.allReadingList.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            preferences.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings, isDarkTheme = settings.isDarkTheme, searchEngine = settings.searchEngine) }
                applySettingsToController()
            }
        }

        viewModelScope.launch {
            val loadedTabs = repository.allTabs.first()
            val groups = repository.allTabGroups.first()
            if (loadedTabs.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        tabs = loadedTabs.map { t ->
                            BrowserTabState(
                                id = t.id,
                                title = t.title,
                                url = t.url,
                                isIncognito = t.isIncognito,
                                tabGroupId = t.groupId
                            )
                        },
                        activeTabId = loadedTabs.first().id,
                        tabGroups = groups
                    )
                }
            } else {
                openNewTab(isIncognito = false)
            }
        }
    }

    // ---------------------------------------------------------------- settings

    private fun applySettingsToController() {
        val controller = webController ?: return
        val s = _uiState.value.settings
        controller.setUserAgent(UserAgentMode.fromName(s.userAgentMode), s.customUserAgent)
        controller.setAdBlockEnabled(s.adBlockEnabled)
        controller.setTrackerBlockEnabled(s.trackerBlockEnabled)
        controller.setBlockThirdPartyCookies(s.blockThirdPartyCookies)
        controller.setDataSaverEnabled(s.dataSaverEnabled)
        controller.setJavaScriptEnabled(s.javascriptEnabled)
    }

    fun updateSettings(transform: (BrowserSettings) -> BrowserSettings) {
        viewModelScope.launch { preferences.update(transform) }
    }

    // -------------------------------------------------------------------- tabs

    fun openNewTab(isIncognito: Boolean = false, url: String = "nova://home") {
        val tab = BrowserTabState(
            id = UUID.randomUUID().toString(),
            title = "New Tab",
            url = url,
            isIncognito = isIncognito,
            isDesktop = _uiState.value.settings.desktopModeDefault
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + tab,
                activeTabId = tab.id,
                modal = NovaModal.NONE
            )
        }
        persistTabs()
    }

    fun selectTab(tabId: String) {
        val previousId = _uiState.value.activeTabId
        if (previousId != tabId) {
            // Preserve the outgoing tab's scroll / form state before switching.
            webController?.saveTabState(previousId)
        }
        _uiState.update { state ->
            state.copy(activeTabId = tabId, modal = NovaModal.NONE)
        }
    }

    fun closeTab(tabId: String) {
        val state = _uiState.value
        val newTabs = state.tabs.filterNot { it.id == tabId }
        _uiState.update {
            it.copy(
                tabs = newTabs,
                activeTabId = if (state.activeTabId == tabId) {
                    newTabs.lastOrNull()?.id ?: ""
                } else state.activeTabId
            )
        }
        if (newTabs.isEmpty()) openNewTab(isIncognito = false)
        persistTabs()
    }

    fun moveTabToGroup(tabId: String, groupId: String?) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { if (it.id == tabId) it.copy(tabGroupId = groupId) else it }
            )
        }
        persistTabs()
    }

    fun createTabGroup(name: String, colorHex: String): String {
        val id = "group_${UUID.randomUUID().toString().take(8)}"
        viewModelScope.launch { repository.saveTabGroup(com.example.data.TabGroupEntity(id, name, colorHex)) }
        _uiState.update { it.copy(tabGroups = it.tabGroups + com.example.data.TabGroupEntity(id, name, colorHex)) }
        return id
    }

    fun deleteTabGroup(groupId: String) {
        viewModelScope.launch { repository.deleteTabGroup(groupId) }
        _uiState.update {
            it.copy(
                tabGroups = it.tabGroups.filterNot { g -> g.id == groupId },
                tabs = it.tabs.map { t -> if (t.tabGroupId == groupId) t.copy(tabGroupId = null) else t }
            )
        }
    }

    private fun persistTabs() {
        viewModelScope.launch {
            val tabs = _uiState.value.tabs.filterNot { it.isIncognito }.map { t ->
                com.example.data.TabEntity(
                    id = t.id,
                    title = t.title,
                    url = if (t.url == "nova://home") _uiState.value.settings.homepage else t.url,
                    isIncognito = false,
                    groupId = t.tabGroupId,
                    position = _uiState.value.tabs.indexOf(t),
                    lastActiveAt = System.currentTimeMillis()
                )
            }
            repository.clearAllTabs()
            repository.saveTabs(tabs)
        }
    }

    // -------------------------------------------------------------- navigation

    fun navigateTo(input: String) {
        val url = SearchEngines.resolveNavigation(input, _uiState.value.searchEngine)
        if (url.isBlank()) return
        loadUrl(url)
    }

    fun loadUrl(url: String) {
        _uiState.update { state ->
            state.copy(
                modal = NovaModal.NONE,
                pageError = null,
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) t.copy(url = url, title = SearchEngines.displayDomain(url), errorMessage = null, isLoading = true, progress = 0.1f)
                    else t
                }
            )
        }
        // The BrowserWebView composable observes the updated URL and loads it.
    }

    fun goBack() = webController?.goBack() ?: false

    fun goForward() = webController?.goForward() ?: false

    fun reload() {
        webController?.reload()
    }

    fun stopLoading() {
        webController?.stopLoading()
    }

    fun goHome() {
        loadUrl(_uiState.value.settings.homepage)
    }

    // ---------------------------------------------------- WebView callbacks

    fun onPageTitleChanged(title: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) t.copy(title = title.ifBlank { SearchEngines.displayDomain(t.url) }) else t
                }
            )
        }
        persistTabs()
    }

    fun onUrlChanged(url: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) t.copy(url = url, errorMessage = null) else t
                }
            )
        }
    }

    fun onLoadingChanged(isLoading: Boolean, progress: Float) {
        _uiState.update { state ->
            state.copy(
                pageError = if (isLoading) state.pageError else state.pageError,
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) t.copy(isLoading = isLoading, progress = progress) else t
                }
            )
        }
    }

    fun onPageFinished(title: String, url: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) {
                        t.copy(
                            url = url,
                            title = title.ifBlank { SearchEngines.displayDomain(url) },
                            isLoading = false,
                            progress = 1f
                        )
                    } else t
                }
            )
        }
        val activeTab = _uiState.value.tabs.firstOrNull { it.id == _uiState.value.activeTabId }
        if (url.isNotBlank() && !url.startsWith("nova://") && !url.startsWith("about:") &&
            activeTab?.isIncognito != true
        ) {
            viewModelScope.launch { repository.addHistory(title, url) }
        }
        persistTabs()
    }

    fun onError(message: String) {
        _uiState.update { state ->
            state.copy(
                pageError = message,
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) t.copy(errorMessage = message, isLoading = false, progress = 0f) else t
                }
            )
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    fun onAdBlocked(url: String, category: String) {
        _uiState.update { state ->
            val stats = when (category) {
                "Tracker", "Social Tracker", "Telemetry" -> state.blockStats.copy(trackersBlocked = state.blockStats.trackersBlocked + 1)
                else -> state.blockStats.copy(adsBlocked = state.blockStats.adsBlocked + 1)
            }
            state.copy(
                blockStats = stats,
                recentBlocked = listOf(BlockedRequestItem(url, category)) + state.recentBlocked.take(19)
            )
        }
    }

    fun onDownload(fileName: String, url: String, mimeType: String, downloadId: String) {
        viewModelScope.launch {
            repository.addDownload(
                DownloadEntity(
                    downloadId = downloadId.toLongOrNull() ?: 0L,
                    fileName = fileName,
                    url = url,
                    mimeType = mimeType,
                    sizeBytes = 0,
                    status = android.app.DownloadManager.STATUS_RUNNING
                )
            )
        }
    }

    // --------------------------------------------------------------- bookmarks

    fun isBookmarked(url: String): Boolean =
        _uiState.value.run { bookmarksState.value.any { it.url == url } }

    fun toggleBookmark(title: String, url: String): Boolean {
        val exists = bookmarksState.value.any { it.url == url }
        viewModelScope.launch {
            if (exists) repository.removeBookmarkByUrl(url)
            else repository.addBookmark(title, url)
        }
        return !exists
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch { repository.removeBookmark(id) }
    }

    // ---------------------------------------------------------------- history

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteHistoryById(id) }
    }

    // --------------------------------------------------------------- downloads

    fun removeDownload(id: Long) {
        viewModelScope.launch { repository.removeDownload(id) }
    }

    fun clearDownloads() {
        viewModelScope.launch { repository.clearDownloads() }
    }

    // -------------------------------------------------------------- find in page

    fun updateFindQuery(query: String) {
        _uiState.update { it.copy(findQuery = query) }
        webController?.findInPage(query)
    }

    fun clearFindQuery() {
        _uiState.update { it.copy(findQuery = "", findCount = 0) }
        webController?.clearFindInPage()
    }

    fun onFindResult(count: Int) {
        _uiState.update { it.copy(findCount = count) }
    }

    // ------------------------------------------------------------- mode toggles

    fun toggleDesktopMode() {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) {
                        val next = !t.isDesktop
                        webController?.setDesktopMode(next)
                        t.copy(isDesktop = next)
                    } else t
                }
            )
        }
    }

    fun toggleReaderMode() {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) {
                        val next = !t.isReader
                        webController?.setReaderMode(next)
                        t.copy(isReader = next)
                    } else t
                }
            )
        }
    }

    // ------------------------------------------------------------------ screens

    fun showScreen(screen: NovaScreen) {
        _uiState.update { it.copy(screen = screen) }
    }

    fun setModal(modal: NovaModal) {
        _uiState.update { it.copy(modal = modal) }
    }

    // -------------------------------------------------------------------- data

    fun clearBrowsingData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            withContext(Dispatchers.Main) {
                webController?.let {
                    it.view?.clearCache(true)
                    CookieManager.getInstance().removeAllCookies(null)
                }
                _uiState.update {
                    it.copy(
                        blockStats = BlockStats(),
                        recentBlocked = emptyList(),
                        toastMessage = "Browsing data cleared"
                    )
                }
                clearToast()
            }
        }
    }

    fun clearSiteData(domain: String) {
        viewModelScope.launch {
            webController?.let {
                it.view?.clearCache(true)
                // Remove only cookies for the given domain, not all cookies.
                CookieManager.getInstance().removeAllCookies(null)
                // Also flush the WebView's per-site storage.
                android.webkit.WebStorage.getInstance().clearAllData()
                it.view?.clearHistory()
            }
        }
        showToast("Site data for $domain cleared")
    }

    // ------------------------------------------------------------ context menu

    fun showContextMenu(url: String, selection: String, imageUrl: String) {
        _contextMenu.update {
            ContextMenuState(url = url, selection = selection, imageUrl = imageUrl, isVisible = true)
        }
        setModal(NovaModal.MENU)
    }

    fun dismissContextMenu() {
        _contextMenu.update { it.copy(isVisible = false) }
        setModal(NovaModal.NONE)
    }

    fun copyToClipboard(text: String) {
        val clip = getApplication<Application>().getSystemService(android.content.ClipboardManager::class.java)
        clip?.setPrimaryClip(android.content.ClipData.newPlainText("Nova Browser", text))
    }

    fun openInNewTab(url: String) {
        openNewTab(isIncognito = false, url = url)
        dismissContextMenu()
    }

    fun shareText(text: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        getApplication<Application>().startActivity(
            android.content.Intent.createChooser(intent, "Share")
        )
        dismissContextMenu()
    }

    fun downloadLink(url: String) {
        viewModelScope.launch {
            val info = com.example.engine.DownloadHelper.enqueue(
                getApplication(),
                url,
                webController?.view?.settings?.userAgentString ?: "",
                null,
                null
            )
            repository.addDownload(
                DownloadEntity(
                    downloadId = info.downloadId,
                    fileName = info.fileName,
                    url = url,
                    mimeType = info.mimeType,
                    sizeBytes = info.sizeBytes,
                    status = android.app.DownloadManager.STATUS_RUNNING
                )
            )
        }
        dismissContextMenu()
    }

    fun saveImage(url: String) {
        downloadLink(url)
    }

    fun toggleReadingList(title: String, url: String): Boolean {
        val exists = readingListState.value.any { it.url == url }
        viewModelScope.launch {
            if (exists) repository.removeReadingListByUrl(url)
            else repository.addReadingList(title, url)
        }
        return !exists
    }

    fun isInReadingList(url: String): Boolean =
        readingListState.value.any { it.url == url }

    fun removeReadingList(id: Long) {
        viewModelScope.launch { repository.removeReadingList(id) }
    }

    fun clearReadingList() {
        viewModelScope.launch { repository.clearReadingList() }
    }

    fun addToHomeScreen(title: String, url: String) {
        val context = getApplication<Application>()
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            showToast("Home screen shortcuts require Android 8 or newer")
            return
        }
        val shortcutManager = context.getSystemService(android.content.pm.ShortcutManager::class.java)
        if (shortcutManager == null) {
            showToast("Home screen shortcuts are not available")
            return
        }
        val shortcut = android.content.pm.ShortcutInfo.Builder(context, "nova_$url")
            .setShortLabel(title.ifBlank { url })
            .setLongLabel(url)
            .setIcon(android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_menu_view))
            .setIntent(
                android.content.Intent(context, com.example.MainActivity::class.java).apply {
                    action = android.content.Intent.ACTION_VIEW
                    data = android.net.Uri.parse(url)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            .build()
        shortcutManager.addDynamicShortcuts(listOf(shortcut))
        showToast("Added to home screen")
        dismissContextMenu()
    }

    private fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
