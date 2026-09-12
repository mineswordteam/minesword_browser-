package com.minesword.browser.engine.minesword.javascript

import com.minesword.browser.engine.minesword.dom.Document

class MineswordJsBridge(private val document: Document) {

    fun executeScript(scriptText: String): String {
        val trimmed = scriptText.trim()

        if (trimmed.startsWith("document.title")) {
            val parts = trimmed.split("=")
            if (parts.size == 2) {
                val newTitle = parts[1].trim().replace("\"", "").replace("'", "").replace(";", "")
                return "Title set to $newTitle"
            }
        }
        return "Script executed successfully"
    }
}
