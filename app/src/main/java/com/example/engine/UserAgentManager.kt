package com.example.engine

enum class UserAgentMode(val displayName: String, val userAgentString: String) {
    CHROME_MOBILE(
        "Chrome Mobile (Default)",
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36 GhostBrowser/1.0"
    ),
    CHROME_DESKTOP(
        "Chrome Desktop (Windows)",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    ),
    FIREFOX_DESKTOP(
        "Firefox Desktop (Linux)",
        "Mozilla/5.0 (X11; Linux x86_64; rv:127.0) Gecko/20100101 Firefox/127.0"
    ),
    SAFARI_MAC(
        "Safari (macOS)",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15"
    ),
    TOR_BROWSER(
        "Tor Browser (Privacy Mask)",
        "Mozilla/5.0 (Windows NT 10.0; rv:115.0) Gecko/20100101 Firefox/115.0"
    ),
    CUSTOM(
        "Custom Mask",
        "GhostBrowser/1.0 PrivacyShield/2.0 (Mobile; UltraFast Engine)"
    );

    companion object {
        fun fromName(name: String): UserAgentMode {
            return entries.find { it.name == name } ?: CHROME_MOBILE
        }
    }
}
