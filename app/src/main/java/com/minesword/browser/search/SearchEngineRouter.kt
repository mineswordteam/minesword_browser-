package com.minesword.browser.search

import java.net.URI

object PersianNormalizer {
    /**
     * Normalizes Persian/Arabic variants of characters (ی -> ي, ک -> ك)
     * and maps Persian/Arabic numerals to standard digits for query/URL parsing.
     */
    fun normalize(input: String): String {
        var text = input
        // Map Persian and Arabic numbers to ASCII standard digits
        val numMap = mapOf(
            '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
            '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
            '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
            '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
        )
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(numMap[ch] ?: ch)
        }
        text = sb.toString()

        // Normalize Persian/Arabic character variations
        text = text.replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('\u200C', ' ') // Replace ZWNJ with space for search consistency where needed
            .trim()

        return text
    }

    /**
     * Converts standard digits to Persian digits for display purposes.
     */
    fun toPersianDigits(input: String): String {
        val digitsMap = mapOf(
            '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
            '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
        )
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(digitsMap[ch] ?: ch)
        }
        return sb.toString()
    }
}

enum class SearchEngine(val displayName: String, val searchUrlTemplate: String) {
    GOOGLE("Google", "https://www.google.com/search?q=%s"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
    BING("Bing", "https://www.bing.com/search?q=%s"),
    PARSIJOO("Parsijoo (Persian)", "https://parsijoo.ir/api?q=%s")
}

object SearchEngineRouter {

    /**
     * Evaluates user input from the address bar.
     * If input is a URL (e.g., http://, https://, domain.com, 192.168.1.1, localhost), return formatted URL.
     * Otherwise, format as a search query targeting the selected engine.
     */
    fun processInput(rawInput: String, engine: SearchEngine = SearchEngine.GOOGLE): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return "about:blank"

        // Handle internal URLs
        if (trimmed.equals("about:blank", ignoreCase = true) ||
            trimmed.startsWith("chrome://") ||
            trimmed.startsWith("file://") ||
            trimmed.startsWith("content://")) {
            return trimmed
        }

        val normalized = PersianNormalizer.normalize(trimmed)

        // Check if explicitly prefixed with protocol
        if (normalized.startsWith("http://", ignoreCase = true) || normalized.startsWith("https://", ignoreCase = true)) {
            return normalized
        }

        // Check for local network / IP address format (e.g. 192.168.1.1:8080 or 10.0.0.1)
        val ipPattern = Regex("""^([0-9]{1,3}\.){3}[0-9]{1,3}(:[0-9]{1,5})?(/.*)?$""")
        if (ipPattern.matches(normalized)) {
            return "http://$normalized"
        }

        // Check for localhost
        if (normalized.startsWith("localhost", ignoreCase = true)) {
            return "http://$normalized"
        }

        // Check for standard domain patterns (e.g., google.com, wikipedia.org, site.ir)
        val domainPattern = Regex("""^([a-zA-Z0-9\-]+\.)+[a-zA-Z]{2,18}(:[0-9]{1,5})?(/.*)?$""")
        if (domainPattern.matches(normalized) && !normalized.contains(" ")) {
            return "https://$normalized"
        }

        // Otherwise, treat as search query using the selected engine
        val encodedQuery = java.net.URLEncoder.encode(trimmed, "UTF-8")
        return String.format(engine.searchUrlTemplate, encodedQuery)
    }
}
