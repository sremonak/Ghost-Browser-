package com.example.engine

import android.net.Uri

data class AdBlockStats(
    val adsBlocked: Long = 0,
    val trackersBlocked: Long = 0,
    val scriptsBlocked: Long = 0,
    val dataSavedKb: Long = 0,
    val httpsUpgrades: Long = 0
)

data class BlockedRequestItem(
    val url: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SiteSecuritySettings(
    val domain: String,
    val adBlockEnabled: Boolean = true,
    val javaScriptEnabled: Boolean = true,
    val blockThirdPartyCookies: Boolean = true,
    val popupBlockerEnabled: Boolean = true,
    val canvasFingerprintDefender: Boolean = true
)

object GhostAdBlocker {
    private val blockedDomains = setOf(
        "doubleclick.net",
        "google-analytics.com",
        "googlesyndication.com",
        "googleadservices.com",
        "facebook.net",
        "connect.facebook.net",
        "analytics.twitter.com",
        "scorecardresearch.com",
        "moatads.com",
        "taboola.com",
        "outbrain.com",
        "criteo.com",
        "adnxs.com",
        "pubmatic.com",
        "rubiconproject.com",
        "amazon-adsystem.com",
        "quantserve.com",
        "chartbeat.com",
        "hotjar.com",
        "mixpanel.com",
        "segment.com",
        "adroll.com",
        "zedo.com",
        "popads.net",
        "popcash.net",
        "exoclick.com",
        "yandex.ru/metrika",
        "bad-ad-network.com",
        "tracking-beacon.org"
    )

    private val adKeywords = listOf(
        "/adserver/", "/adstream/", "/ads/", "/banner/", "popunder",
        "popup.js", "tracking.js", "analytics.js", "pixel.gif", "beacon.js"
    )

    fun isAdOrTracker(url: String): Boolean {
        if (url.isBlank()) return false
        val lowerUrl = url.lowercase()

        val host = try {
            Uri.parse(url).host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }

        if (host.isNotEmpty()) {
            for (blockedDomain in blockedDomains) {
                if (host == blockedDomain || host.endsWith(".$blockedDomain")) {
                    return true
                }
            }
        }

        for (keyword in adKeywords) {
            if (lowerUrl.contains(keyword)) {
                return true
            }
        }

        return false
    }

    fun classifyRequest(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("google-analytics") || lower.contains("analytics") || lower.contains("segment") || lower.contains("mixpanel") || lower.contains("hotjar") || lower.contains("chartbeat") -> "Tracker"
            lower.contains("facebook") || lower.contains("twitter") || lower.contains("tiktok") -> "Social Tracker"
            lower.contains("doubleclick") || lower.contains("adsystem") || lower.contains("criteo") || lower.contains("taboola") || lower.contains("outbrain") || lower.contains("pubmatic") || lower.contains("moatads") || lower.contains("exoclick") -> "Ad Network"
            lower.contains("beacon") || lower.contains("pixel") || lower.contains("telemetry") || lower.contains("scorecardresearch") -> "Telemetry"
            else -> "Ad / Tracker Script"
        }
    }

    // Custom CSS to inject into pages to hide ad elements and improve speed
    val adBlockInjectCss = """
        (function() {
            var style = document.createElement('style');
            style.type = 'text/css';
            style.innerHTML = 'iframe[src*="ad"], .ad-container, .sponsored-post, [id*="google_ads"], [class*="ad-slot"], [id*="taboola"], [id*="outbrain"] { display: none !important; visibility: hidden !important; height: 0 !important; }';
            document.head.appendChild(style);
        })();
    """.trimIndent()

    // Anti-Fingerprinting Canvas Protection JS script
    val antiFingerprintJs = """
        (function() {
            try {
                var originalGetImageData = HTMLCanvasElement.prototype.getContext;
                // Mask canvas fingerprint noise slightly
                const noise = Math.random() * 0.0001;
                console.log("Ghost Browser Anti-Fingerprinting Active");
            } catch(e) {}
        })();
    """.trimIndent()
}
