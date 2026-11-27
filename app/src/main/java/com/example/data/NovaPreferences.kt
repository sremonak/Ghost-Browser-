package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.novaPrefs: DataStore<Preferences> by preferencesDataStore(name = "nova-browser-settings")

/**
 * Persisted user preferences: theme, home page, search engine, blocker toggles,
 * per-site security overrides and the custom user-agent string.
 */
data class BrowserSettings(
    val isDarkTheme: Boolean = true,
    val homepage: String = "nova://home",
    val searchEngine: String = "Google",
    val adBlockEnabled: Boolean = true,
    val trackerBlockEnabled: Boolean = true,
    val javascriptEnabled: Boolean = true,
    val popupBlockerEnabled: Boolean = true,
    val blockThirdPartyCookies: Boolean = true,
    val dataSaverEnabled: Boolean = false,
    val desktopModeDefault: Boolean = false,
    val restoreTabsOnStart: Boolean = true,
    val customUserAgent: String = "",
    val userAgentMode: String = "CHROME_MOBILE"
)

class NovaPreferences(private val context: Context) {

    private val isDarkThemeKey = booleanPreferencesKey("is_dark_theme")
    private val homepageKey = stringPreferencesKey("homepage")
    private val searchEngineKey = stringPreferencesKey("search_engine")
    private val adBlockEnabledKey = booleanPreferencesKey("ad_block_enabled")
    private val trackerBlockEnabledKey = booleanPreferencesKey("tracker_block_enabled")
    private val javascriptEnabledKey = booleanPreferencesKey("javascript_enabled")
    private val popupBlockerEnabledKey = booleanPreferencesKey("popup_blocker_enabled")
    private val blockThirdPartyCookiesKey = booleanPreferencesKey("block_third_party_cookies")
    private val dataSaverEnabledKey = booleanPreferencesKey("data_saver_enabled")
    private val desktopModeDefaultKey = booleanPreferencesKey("desktop_mode_default")
    private val restoreTabsOnStartKey = booleanPreferencesKey("restore_tabs_on_start")
    private val customUserAgentKey = stringPreferencesKey("custom_user_agent")
    private val userAgentModeKey = stringPreferencesKey("user_agent_mode")

    val settings: Flow<BrowserSettings> = context.novaPrefs.data.map { prefs ->
        BrowserSettings(
            isDarkTheme = prefs[isDarkThemeKey] ?: true,
            homepage = prefs[homepageKey] ?: "nova://home",
            searchEngine = prefs[searchEngineKey] ?: "Google",
            adBlockEnabled = prefs[adBlockEnabledKey] ?: true,
            trackerBlockEnabled = prefs[trackerBlockEnabledKey] ?: true,
            javascriptEnabled = prefs[javascriptEnabledKey] ?: true,
            popupBlockerEnabled = prefs[popupBlockerEnabledKey] ?: true,
            blockThirdPartyCookies = prefs[blockThirdPartyCookiesKey] ?: true,
            dataSaverEnabled = prefs[dataSaverEnabledKey] ?: false,
            desktopModeDefault = prefs[desktopModeDefaultKey] ?: false,
            restoreTabsOnStart = prefs[restoreTabsOnStartKey] ?: true,
            customUserAgent = prefs[customUserAgentKey] ?: "",
            userAgentMode = prefs[userAgentModeKey] ?: "CHROME_MOBILE"
        )
    }

    suspend fun update(transform: (BrowserSettings) -> BrowserSettings) {
        context.novaPrefs.edit { prefs ->
            val current = transform(readCurrent(prefs))
            prefs[isDarkThemeKey] = current.isDarkTheme
            prefs[homepageKey] = current.homepage
            prefs[searchEngineKey] = current.searchEngine
            prefs[adBlockEnabledKey] = current.adBlockEnabled
            prefs[trackerBlockEnabledKey] = current.trackerBlockEnabled
            prefs[javascriptEnabledKey] = current.javascriptEnabled
            prefs[popupBlockerEnabledKey] = current.popupBlockerEnabled
            prefs[blockThirdPartyCookiesKey] = current.blockThirdPartyCookies
            prefs[dataSaverEnabledKey] = current.dataSaverEnabled
            prefs[desktopModeDefaultKey] = current.desktopModeDefault
            prefs[restoreTabsOnStartKey] = current.restoreTabsOnStart
            prefs[customUserAgentKey] = current.customUserAgent
            prefs[userAgentModeKey] = current.userAgentMode
        }
    }

    private fun readCurrent(prefs: Preferences): BrowserSettings = BrowserSettings(
        isDarkTheme = prefs[isDarkThemeKey] ?: true,
        homepage = prefs[homepageKey] ?: "nova://home",
        searchEngine = prefs[searchEngineKey] ?: "Google",
        adBlockEnabled = prefs[adBlockEnabledKey] ?: true,
        trackerBlockEnabled = prefs[trackerBlockEnabledKey] ?: true,
        javascriptEnabled = prefs[javascriptEnabledKey] ?: true,
        popupBlockerEnabled = prefs[popupBlockerEnabledKey] ?: true,
        blockThirdPartyCookies = prefs[blockThirdPartyCookiesKey] ?: true,
        dataSaverEnabled = prefs[dataSaverEnabledKey] ?: false,
        desktopModeDefault = prefs[desktopModeDefaultKey] ?: false,
        restoreTabsOnStart = prefs[restoreTabsOnStartKey] ?: true,
        customUserAgent = prefs[customUserAgentKey] ?: "",
        userAgentMode = prefs[userAgentModeKey] ?: "CHROME_MOBILE"
    )
}
