package com.example.engine

import com.example.data.ExtensionPluginEntity

object PluginArchitectureManager {
    val defaultChromiumStorePlugins = listOf(
        ExtensionPluginEntity(
            id = "ublock_ghost",
            name = "uBlock Ghost AdBlock Ultra",
            description = "High-efficiency blocklist extension for ads, tracking scripts, and popups with minimal memory footprint.",
            version = "1.58.0",
            isEnabled = true,
            iconName = "shield",
            category = "Ad Blocking"
        ),
        ExtensionPluginEntity(
            id = "privacy_badger",
            name = "Privacy Badger Engine",
            description = "Automatically learns and blocks invisible third-party trackers based on tracking behavior.",
            version = "2024.5.1",
            isEnabled = true,
            iconName = "security",
            category = "Privacy"
        ),
        ExtensionPluginEntity(
            id = "https_everywhere",
            name = "HTTPS Everywhere",
            description = "Encrypted communications. Automatically upgrades HTTP connections to secure HTTPS.",
            version = "2024.1.2",
            isEnabled = true,
            iconName = "lock",
            category = "Security"
        ),
        ExtensionPluginEntity(
            id = "dark_reader",
            name = "Dark Reader Extension",
            description = "Inverts bright colors making all web pages readable in high-contrast dark theme.",
            version = "4.9.8",
            isEnabled = false,
            iconName = "dark_mode",
            category = "Appearance"
        ),
        ExtensionPluginEntity(
            id = "noscript_lite",
            name = "NoScript Control Lite",
            description = "Allows JavaScript and active content only for trusted domains you choose.",
            version = "11.4.3",
            isEnabled = false,
            iconName = "code",
            category = "Script Control"
        ),
        ExtensionPluginEntity(
            id = "cookie_autodelete",
            name = "Cookie AutoDelete",
            description = "Automatically deletes unused cookies when closing tabs or ending private sessions.",
            version = "3.8.2",
            isEnabled = true,
            iconName = "cookie",
            category = "Privacy"
        ),
        ExtensionPluginEntity(
            id = "canvas_defender",
            name = "Canvas & WebGL Anti-Fingerprint",
            description = "Spoofs HTML canvas drawings and WebGL parameters to prevent hardware fingerprinting.",
            version = "1.2.0",
            isEnabled = true,
            iconName = "fingerprint",
            category = "Anti-Fingerprinting"
        )
    )
}
