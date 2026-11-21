package com.example.engine

import java.net.URLEncoder
import java.util.Locale

/**
 * Supported search engines and helpers to build search / URL queries.
 */
object SearchEngines {

    data class Engine(val name: String, val homepage: String, val searchUrlTemplate: String) {
        fun buildSearchUrl(query: String): String =
            searchUrlTemplate.replace("{q}", URLEncoder.encode(query, "UTF-8"))
    }

    val all: List<Engine> = listOf(
        Engine("Google", "https://www.google.com", "https://www.google.com/search?q={q}"),
        Engine("Bing", "https://www.bing.com", "https://www.bing.com/search?q={q}"),
        Engine("DuckDuckGo", "https://duckduckgo.com", "https://duckduckgo.com/?q={q}"),
        Engine("Brave", "https://search.brave.com", "https://search.brave.com/search?q={q}"),
        Engine("Ecosia", "https://www.ecosia.org", "https://www.ecosia.org/search?q={q}")
    )

    fun byName(name: String): Engine = all.firstOrNull { it.name == name } ?: all.first()

    /**
     * Decides whether [input] is already a URL or should be treated as a
     * search query, and returns the URL to navigate to.
     */
    fun resolveNavigation(input: String, searchEngine: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        // Already has a scheme (http, https, nova://, file:, about:, chrome:...)
        if (Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(trimmed)) {
            return trimmed
        }

        // "example.com", "example.com/path", "192.168.0.1"
        val looksLikeDomain = Regex(
            "^(([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|([0-9]{1,3}\\.){3}[0-9]{1,3})([:/][^\\s]*)?$"
        ).containsMatchIn(trimmed)

        // "example.com/path/page.html" style with file extension
        val looksLikeUrlPath = Regex(
            "^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+/[^\\s]*$"
        ).containsMatchIn(trimmed)

        if (looksLikeDomain || looksLikeUrlPath) {
            return if (trimmed.startsWith("//")) "https:$trimmed" else "https://$trimmed"
        }

        return byName(searchEngine).buildSearchUrl(trimmed)
    }

    /**
     * Extracts a readable domain from a URL for display purposes.
     */
    fun displayDomain(url: String): String {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.host?.lowercase(Locale.US) ?: url
        } catch (e: Exception) {
            url
        }
    }

    fun isSecureUrl(url: String): Boolean = url.startsWith("https://", ignoreCase = true)
}
