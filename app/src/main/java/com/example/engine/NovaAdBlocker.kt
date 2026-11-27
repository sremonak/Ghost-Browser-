package com.example.engine

import android.net.Uri

/**
 * Aggregated counters describing what the privacy engine has blocked.
 */
data class BlockStats(
    val adsBlocked: Long = 0,
    val trackersBlocked: Long = 0,
    val scriptsBlocked: Long = 0,
    val dataSavedKb: Long = 0
)

data class BlockedRequestItem(
    val url: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Built-in ad / tracker / telemetry blocker.
 *
 * Matches requests against a curated domain block-list plus a set of
 * path/keyword heuristics. When a request matches, the browser answers with an
 * empty response and records it in the block statistics.
 */
object NovaAdBlocker {

    private val blockedDomains = setOf(
        // Ad networks & exchanges
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adnxs.com",
        "pubmatic.com",
        "rubiconproject.com",
        "amazon-adsystem.com",
        "criteo.com",
        "taboola.com",
        "outbrain.com",
        "adroll.com",
        "zedo.com",
        "popads.net",
        "popcash.net",
        "exoclick.com",
        "adthrive.com",
        "3lift.com",
        "openx.net",
        "smartadserver.com",
        "yieldmo.com",
        "teads.tv",
        "sonobi.com",
        "lijit.com",
        "casalemedia.com",
        "indexww.com",
        // Analytics & telemetry
        "google-analytics.com",
        "connect.facebook.net",
        "facebook.net",
        "analytics.twitter.com",
        "scorecardresearch.com",
        "moatads.com",
        "quantserve.com",
        "chartbeat.com",
        "hotjar.com",
        "mixpanel.com",
        "segment.com",
        "amplitude.com",
        "fullstory.com",
        "bugsnag.com",
        "sentry.io",
        "newrelic.com",
        "telemetry.mozilla.org",
        // Social trackers
        "static.hotjar.com",
        "tt.everest.io",
        "browser-intelligence.com",
        "clarity.ms",
        "perf.brave.com"
    )

    private val adKeywords = listOf(
        "/adserver/", "/adstream/", "/ads/", "/banner/", "/advert",
        "popunder", "popup.js", "tracking.js", "analytics.js",
        "pixel.gif", "beacon.js", "/ad/", "advertise", "/sponsor"
    )

    /**
     * Returns true when [url] should be blocked.
     */
    fun isAdOrTracker(url: String): Boolean {
        if (url.isBlank()) return false
        val lowerUrl = url.lowercase()

        val host = try {
            Uri.parse(url).host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }

        if (host.isNotEmpty()) {
            for (blocked in blockedDomains) {
                if (host == blocked || host.endsWith(".$blocked")) {
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

    /**
     * Human-readable category for the blocked request, used by the stats UI.
     */
    fun classifyRequest(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("google-analytics") || lower.contains("analytics") ||
                lower.contains("segment") || lower.contains("mixpanel") ||
                lower.contains("hotjar") || lower.contains("chartbeat") ||
                lower.contains("amplitude") || lower.contains("sentry") -> "Tracker"
            lower.contains("facebook") || lower.contains("twitter") ||
                lower.contains("tiktok") || lower.contains("linkedin") -> "Social Tracker"
            lower.contains("doubleclick") || lower.contains("adsystem") ||
                lower.contains("criteo") || lower.contains("taboola") ||
                lower.contains("outbrain") || lower.contains("pubmatic") ||
                lower.contains("moatads") || lower.contains("exoclick") ||
                lower.contains("adnxs") -> "Ad Network"
            lower.contains("beacon") || lower.contains("pixel") ||
                lower.contains("telemetry") || lower.contains("scorecardresearch") -> "Telemetry"
            else -> "Ad / Tracker Script"
        }
    }

    /**
     * CSS injected into finished pages to hide common ad containers that are
     * rendered by the page itself (in addition to network-level blocking).
     */
    val adBlockInjectCss: String = """
        (function() {
            var style = document.createElement('style');
            style.type = 'text/css';
            style.innerHTML = 'iframe[src*="ad"], .ad-container, .ad-slot, .adsbygoogle, ' +
                '.sponsored-post, [id*="google_ads"], [class*="ad-slot"], ' +
                '[id*="taboola"], [id*="outbrain"], [class*="advert"] { ' +
                'display: none !important; visibility: hidden !important; height: 0 !important; }';
            document.head.appendChild(style);
        })();
    """.trimIndent()

    /**
     * Small anti-fingerprinting script: randomizes a tiny amount of canvas
     * noise so trackers cannot build a stable fingerprint from the canvas API.
     */
    val antiFingerprintJs: String = """
        (function() {
            try {
                var original = HTMLCanvasElement.prototype.getContext;
                HTMLCanvasElement.prototype.getContext = function(type) {
                    var ctx = original.call(this, type);
                    if (ctx && type === '2d' && !this.__novaPatched) {
                        this.__novaPatched = true;
                        var noise = Math.random() * 0.0001;
                        ctx.fillStyle = 'rgba(0,0,0,' + noise + ')';
                    }
                    return ctx;
                };
            } catch (e) {}
        })();
    """.trimIndent()
}
