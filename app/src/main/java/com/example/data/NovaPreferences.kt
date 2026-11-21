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

    private val keys = {
        mapOf(
            isDarkTheme to booleanPreferencesKey("is_dark_theme"),
            homepage to stringPreferencesKey("homepage"),
            searchEngine to stringPreferencesKey("search_engine"),
            adBlockEnabled to booleanPreferencesKey("ad_block_enabled"),
            trackerBlockEnabled to booleanPreferencesKey("tracker_block_enabled"),
            javascriptEnabled to booleanPreferencesKey("javascript_enabled"),
            popupBlockerEnabled to booleanPreferencesKey("popup_blocker_enabled"),
            blockThirdPartyCookies to booleanPreferencesKey("block_third_party_cookies"),
            dataSaverEnabled to booleanPreferencesKey("data_saver_enabled"),
            desktopModeDefault to booleanPreferencesKey("desktop_mode_default"),
            restoreTabsOnStart to booleanPreferencesKey("restore_tabs_on_start"),
            customUserAgent to stringPreferencesKey("custom_user_agent"),
            userAgentMode to stringPreferencesKey("user_agent_mode")
        )
    }()

    val settings: Flow<BrowserSettings> = context.novaPrefs.data.map { prefs ->
        BrowserSettings(
            isDarkTheme = prefs[keys[BrowserSettings::isDarkTheme]!!] ?: true,
            homepage = prefs[keys[BrowserSettings::homepage]!!] ?: "nova://home",
            searchEngine = prefs[keys[BrowserSettings::searchEngine]!!] ?: "Google",
            adBlockEnabled = prefs[keys[BrowserSettings::adBlockEnabled]!!] ?: true,
            trackerBlockEnabled = prefs[keys[BrowserSettings::trackerBlockEnabled]!!] ?: true,
            javascriptEnabled = prefs[keys[BrowserSettings::javascriptEnabled]!!] ?: true,
            popupBlockerEnabled = prefs[keys[BrowserSettings::popupBlockerEnabled]!!] ?: true,
            blockThirdPartyCookies = prefs[keys[BrowserSettings::blockThirdPartyCookies]!!] ?: true,
            dataSaverEnabled = prefs[keys[BrowserSettings::dataSaverEnabled]!!] ?: false,
            desktopModeDefault = prefs[keys[BrowserSettings::desktopModeDefault]!!] ?: false,
            restoreTabsOnStart = prefs[keys[BrowserSettings::restoreTabsOnStart]!!] ?: true,
            customUserAgent = prefs[keys[BrowserSettings::customUserAgent]!!] ?: "",
            userAgentMode = prefs[keys[BrowserSettings::userAgentMode]!!] ?: "CHROME_MOBILE"
        )
    }

    suspend fun update(transform: (BrowserSettings) -> BrowserSettings) {
        context.novaPrefs.edit { prefs ->
            val current = transform(readCurrent(prefs))
            prefs[keys[BrowserSettings::isDarkTheme]!!] = current.isDarkTheme
            prefs[keys[BrowserSettings::homepage]!!] = current.homepage
            prefs[keys[BrowserSettings::searchEngine]!!] = current.searchEngine
            prefs[keys[BrowserSettings::adBlockEnabled]!!] = current.adBlockEnabled
            prefs[keys[BrowserSettings::trackerBlockEnabled]!!] = current.trackerBlockEnabled
            prefs[keys[BrowserSettings::javascriptEnabled]!!] = current.javascriptEnabled
            prefs[keys[BrowserSettings::popupBlockerEnabled]!!] = current.popupBlockerEnabled
            prefs[keys[BrowserSettings::blockThirdPartyCookies]!!] = current.blockThirdPartyCookies
            prefs[keys[BrowserSettings::dataSaverEnabled]!!] = current.dataSaverEnabled
            prefs[keys[BrowserSettings::desktopModeDefault]!!] = current.desktopModeDefault
            prefs[keys[BrowserSettings::restoreTabsOnStart]!!] = current.restoreTabsOnStart
            prefs[keys[BrowserSettings::customUserAgent]!!] = current.customUserAgent
            prefs[keys[BrowserSettings::userAgentMode]!!] = current.userAgentMode
        }
    }

    private fun readCurrent(prefs: Preferences): BrowserSettings = BrowserSettings(
        isDarkTheme = prefs[keys[BrowserSettings::isDarkTheme]!!] ?: true,
        homepage = prefs[keys[BrowserSettings::homepage]!!] ?: "nova://home",
        searchEngine = prefs[keys[BrowserSettings::searchEngine]!!] ?: "Google",
        adBlockEnabled = prefs[keys[BrowserSettings::adBlockEnabled]!!] ?: true,
        trackerBlockEnabled = prefs[keys[BrowserSettings::trackerBlockEnabled]!!] ?: true,
        javascriptEnabled = prefs[keys[BrowserSettings::javascriptEnabled]!!] ?: true,
        popupBlockerEnabled = prefs[keys[BrowserSettings::popupBlockerEnabled]!!] ?: true,
        blockThirdPartyCookies = prefs[keys[BrowserSettings::blockThirdPartyCookies]!!] ?: true,
        dataSaverEnabled = prefs[keys[BrowserSettings::dataSaverEnabled]!!] ?: false,
        desktopModeDefault = prefs[keys[BrowserSettings::desktopModeDefault]!!] ?: false,
        restoreTabsOnStart = prefs[keys[BrowserSettings::restoreTabsOnStart]!!] ?: true,
        customUserAgent = prefs[keys[BrowserSettings::customUserAgent]!!] ?: "",
        userAgentMode = prefs[keys[BrowserSettings::userAgentMode]!!] ?: "CHROME_MOBILE"
    )
}
