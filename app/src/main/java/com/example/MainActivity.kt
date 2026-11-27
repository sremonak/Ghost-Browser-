package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.WebViewController
import com.example.ui.components.*
import com.example.ui.theme.NovaBrowserTheme
import com.example.ui.viewmodel.BrowserTabState
import com.example.ui.viewmodel.ContextMenuState
import com.example.ui.viewmodel.NovaBrowserViewModel
import com.example.ui.viewmodel.NovaBrowserUiState
import com.example.ui.viewmodel.NovaModal
import com.example.ui.viewmodel.NovaScreen

class MainActivity : ComponentActivity() {

    private val viewModel: NovaBrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val history by viewModel.historyState.collectAsStateWithLifecycle()
            val bookmarks by viewModel.bookmarksState.collectAsStateWithLifecycle()
            val downloads by viewModel.downloadsState.collectAsStateWithLifecycle()
            val readingList by viewModel.readingListState.collectAsStateWithLifecycle()
            val contextMenu by viewModel.contextMenuState.collectAsStateWithLifecycle()

            val state = uiState
            val activeTab = state.tabs.firstOrNull { it.id == state.activeTabId }

            // The WebView controller lives for the lifetime of the browser screen.
            val webController = remember { WebViewController(this@MainActivity) }
            LaunchedEffect(state.screen) {
                if (state.screen == NovaScreen.BROWSER) {
                    viewModel.attachWebController(webController)
                } else {
                    viewModel.detachWebController()
                }
            }

            // Toast handling
            LaunchedEffect(state.toastMessage) {
                state.toastMessage?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            NovaBrowserTheme(darkTheme = state.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        current = state.screen,
                        onNavigate = viewModel::showScreen,
                        state = state,
                        history = history,
                        bookmarks = bookmarks,
                        downloads = downloads,
                        readingList = readingList,
                        contextMenu = contextMenu,
                        webController = webController,
                        viewModel = viewModel,
                        activeTab = activeTab
                    )
                }
            }
        }
    }
}

@Composable
private fun NavHost(
    current: NovaScreen,
    onNavigate: (NovaScreen) -> Unit,
    state: NovaBrowserUiState,
    history: List<com.example.data.HistoryEntity>,
    bookmarks: List<com.example.data.BookmarkEntity>,
    downloads: List<com.example.data.DownloadEntity>,
    readingList: List<com.example.data.ReadingListEntity>,
    contextMenu: ContextMenuState,
    webController: WebViewController,
    viewModel: NovaBrowserViewModel,
    activeTab: com.example.ui.viewmodel.BrowserTabState?
) {
    when (current) {
        NovaScreen.BROWSER -> {
            BrowserScreen(
                state = state,
                activeTab = activeTab,
                history = history,
                bookmarks = bookmarks,
                readingList = readingList,
                contextMenu = contextMenu,
                webController = webController,
                viewModel = viewModel,
                onNavigate = onNavigate
            )
        }
        NovaScreen.HISTORY -> {
            HistoryScreen(
                history = history,
                onOpen = { url -> viewModel.loadUrl(url) },
                onDelete = { id -> viewModel.deleteHistoryItem(id) },
                onClearAll = viewModel::clearHistory,
                onBack = { onNavigate(NovaScreen.BROWSER) }
            )
        }
        NovaScreen.BOOKMARKS -> {
            BookmarksScreen(
                bookmarks = bookmarks,
                onOpen = { url -> viewModel.loadUrl(url) },
                onRemove = { id -> viewModel.removeBookmark(id) },
                onBack = { onNavigate(NovaScreen.BROWSER) }
            )
        }
        NovaScreen.DOWNLOADS -> {
            DownloadsScreen(
                downloads = downloads,
                onOpen = { url -> viewModel.loadUrl(url) },
                onDelete = { id -> viewModel.removeDownload(id) },
                onClearAll = viewModel::clearDownloads,
                onBack = { onNavigate(NovaScreen.BROWSER) }
            )
        }
        NovaScreen.READING_LIST -> {
            ReadingListScreen(
                items = readingList,
                onOpen = { url -> viewModel.loadUrl(url) },
                onRemove = { id -> viewModel.removeReadingList(id) },
                onBack = { onNavigate(NovaScreen.BROWSER) }
            )
        }
        NovaScreen.SETTINGS -> {
            SettingsScreen(
                settings = state.settings,
                versionName = "1.0",
                viewModel = viewModel,
                onBack = { onNavigate(NovaScreen.BROWSER) }
            )
        }
    }
}

@Composable
private fun BrowserScreen(
    state: NovaBrowserUiState,
    activeTab: com.example.ui.viewmodel.BrowserTabState?,
    history: List<com.example.data.HistoryEntity>,
    bookmarks: List<com.example.data.BookmarkEntity>,
    readingList: List<com.example.data.ReadingListEntity>,
    contextMenu: ContextMenuState,
    webController: WebViewController,
    viewModel: NovaBrowserViewModel,
    onNavigate: (NovaScreen) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showFindBar by remember { mutableStateOf(false) }

    val currentUrl = activeTab?.url ?: state.settings.homepage
    val isHome = currentUrl.startsWith("nova://home") || currentUrl.isBlank()
    val isBookmarked = bookmarks.any { it.url == currentUrl && !currentUrl.startsWith("nova://") }
    val isInReadingList = readingList.any { it.url == currentUrl }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 0.dp
            ) {
                BottomNavItem(
                    selected = state.screen == NovaScreen.BROWSER,
                    icon = Icons.Filled.Public,
                    outlinedIcon = Icons.Outlined.Public,
                    label = "Browser",
                    onClick = { onNavigate(NovaScreen.BROWSER) }
                )
                BottomNavItem(
                    selected = state.screen == NovaScreen.HISTORY,
                    icon = Icons.Filled.History,
                    outlinedIcon = Icons.Outlined.History,
                    label = "History",
                    onClick = { onNavigate(NovaScreen.HISTORY) }
                )
                BottomNavItem(
                    selected = state.screen == NovaScreen.BOOKMARKS,
                    icon = Icons.Filled.Bookmark,
                    outlinedIcon = Icons.Outlined.BookmarkBorder,
                    label = "Bookmarks",
                    onClick = { onNavigate(NovaScreen.BOOKMARKS) }
                )
                BottomNavItem(
                    selected = state.screen == NovaScreen.READING_LIST,
                    icon = Icons.Filled.Article,
                    outlinedIcon = Icons.Outlined.Article,
                    label = "Reading",
                    onClick = { onNavigate(NovaScreen.READING_LIST) }
                )
                BottomNavItem(
                    selected = state.screen == NovaScreen.DOWNLOADS,
                    icon = Icons.Filled.Download,
                    outlinedIcon = Icons.Outlined.Download,
                    label = "Downloads",
                    onClick = { onNavigate(NovaScreen.DOWNLOADS) }
                )
                BottomNavItem(
                    selected = state.screen == NovaScreen.SETTINGS,
                    icon = Icons.Filled.Settings,
                    outlinedIcon = Icons.Outlined.Settings,
                    label = "Settings",
                    onClick = { onNavigate(NovaScreen.SETTINGS) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabStrip(
                tabs = state.tabs,
                activeTabId = state.activeTabId,
                onSelectTab = viewModel::selectTab,
                onCloseTab = viewModel::closeTab,
                onNewTab = { viewModel.openNewTab(isIncognito = false) }
            )

            BrowserToolbar(
                tab = activeTab ?: state.settings.homepage.let {
                    com.example.ui.viewmodel.BrowserTabState(url = it)
                },
                searchEngine = state.searchEngine,
                canGoBack = state.canGoBack,
                canGoForward = state.canGoForward,
                isLoading = activeTab?.isLoading ?: false,
                progress = activeTab?.progress ?: 0f,
                isBookmarked = isBookmarked,
                isIncognito = activeTab?.isIncognito == true,
                onNavigate = viewModel::navigateTo,
                onGoBack = viewModel::goBack,
                onGoForward = viewModel::goForward,
                onReload = viewModel::reload,
                onStop = viewModel::stopLoading,
                onOpenTabs = { viewModel.setModal(NovaModal.TAB_SWITCHER) },
                onToggleBookmark = {
                    val title = activeTab?.title ?: currentUrl
                    viewModel.toggleBookmark(title, currentUrl)
                },
                onOpenMenu = { menuExpanded = true }
            )

            // Menu dropdown anchored to the toolbar
            BrowserMenu(
                expanded = menuExpanded,
                onDismiss = { menuExpanded = false },
                onNewTab = { viewModel.openNewTab(isIncognito = false) },
                onNewIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                onHistory = { onNavigate(NovaScreen.HISTORY) },
                onBookmarks = { onNavigate(NovaScreen.BOOKMARKS) },
                onDownloads = { onNavigate(NovaScreen.DOWNLOADS) },
                onSettings = { onNavigate(NovaScreen.SETTINGS) },
                onSiteInfo = { viewModel.setModal(NovaModal.SITE_INFO) }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isHome -> {
                        HomeScreen(
                            searchEngine = state.searchEngine,
                            recentSites = history.take(12).map {
                                RecentSite(title = it.title, url = it.url, domain = it.domain)
                            },
                            onNavigate = viewModel::navigateTo
                        )
                    }
                    activeTab?.errorMessage != null -> {
                        ErrorScreen(
                            url = currentUrl,
                            message = activeTab.errorMessage,
                            onRetry = viewModel::reload,
                            onGoHome = viewModel::goHome
                        )
                    }
                    else -> {
                        BrowserWebView(
                            webController = webController,
                            tabId = state.activeTabId,
                            url = currentUrl,
                            onPageTitleChanged = viewModel::onPageTitleChanged,
                            onLoadingChanged = viewModel::onLoadingChanged,
                            onUrlChanged = viewModel::onUrlChanged,
                            onAdBlocked = viewModel::onAdBlocked,
                            onDownload = viewModel::onDownload,
                            onError = viewModel::onError,
                            onNavigationStateChanged = viewModel::onNavigationStateChanged,
                            onFindResult = viewModel::onFindResult,
                            onContextMenu = viewModel::showContextMenu
                        )
                    }
                }

                // Find in page bar
                if (showFindBar) {
                    FindInPageBar(
                        query = state.findQuery,
                        resultCount = state.findCount,
                        onQueryChange = viewModel::updateFindQuery,
                        onDismiss = {
                            showFindBar = false
                            viewModel.clearFindQuery()
                        }
                    )
                }
            }

            // Modals
            when (state.modal) {
                NovaModal.TAB_SWITCHER -> {
                    TabSwitcher(
                        tabs = state.tabs,
                        activeTabId = state.activeTabId,
                        onSelect = { id -> viewModel.selectTab(id) },
                        onClose = { id -> viewModel.closeTab(id) },
                        onNewTab = { viewModel.openNewTab(isIncognito = false) },
                        onNewIncognito = { viewModel.openNewTab(isIncognito = true) },
                        onDismiss = { viewModel.setModal(NovaModal.NONE) }
                    )
                }
                NovaModal.SITE_INFO -> {
                    activeTab?.let { tab ->
                        SiteInfoSheet(
                            url = tab.url,
                            isDesktop = tab.isDesktop,
                            isReader = tab.isReader,
                            isIncognito = tab.isIncognito,
                            onDismiss = { viewModel.setModal(NovaModal.NONE) },
                            onToggleDesktop = viewModel::toggleDesktopMode,
                            onToggleReader = viewModel::toggleReaderMode,
                            onFindInPage = {
                                viewModel.setModal(NovaModal.NONE)
                                showFindBar = true
                            },
                            onClearSiteData = {
                                viewModel.clearSiteData(com.example.engine.SearchEngines.displayDomain(tab.url))
                            }
                        )
                    }
                }
                NovaModal.MENU -> {
                    if (contextMenu.isVisible) {
                        ContextMenuSheet(
                            state = contextMenu,
                            isBookmarked = isBookmarked,
                            isInReadingList = isInReadingList,
                            viewModel = viewModel,
                            onDismiss = { viewModel.dismissContextMenu() }
                        )
                    }
                }
                NovaModal.NONE -> { /* handled by toolbar */ }
            }
        }
    }

    // Global back handling: close modal -> close find bar -> WebView back -> leave.
    BackHandler(enabled = state.screen == NovaScreen.BROWSER) {
        when {
            state.modal != NovaModal.NONE -> viewModel.setModal(NovaModal.NONE)
            showFindBar -> { showFindBar = false; viewModel.clearFindQuery() }
            state.canGoBack -> viewModel.goBack()
            else -> onNavigate(NovaScreen.BROWSER)
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    icon: ImageVector,
    outlinedIcon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = if (selected) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) {
        Icon(
            imageVector = if (selected) icon else outlinedIcon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}
