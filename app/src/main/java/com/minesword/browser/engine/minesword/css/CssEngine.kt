package com.minesword.browser.engine.minesword.css

import com.minesword.browser.engine.minesword.dom.Element

data class Specificity(val a: Int, val b: Int, val c: Int) : Comparable<Specificity> {
    override fun compareTo(other: Specificity): Int {
        if (a != other.a) return a.compareTo(other.a)
        if (b != other.b) return b.compareTo(other.b)
        return c.compareTo(other.c)
    }
}

sealed class Selector {
    abstract fun matches(element: Element): Boolean
    abstract fun specificity(): Specificity

    data class Simple(
        val tagName: String? = null,
        val id: String? = null,
        val className: String? = null
    ) : Selector() {
        override fun matches(element: Element): Boolean {
            if (tagName != null && !element.tagName.equals(tagName, ignoreCase = true)) return false
            if (id != null && element.id != id) return false
            if (className != null && !element.className.contains(className)) return false
            return true
        }

        override fun specificity(): Specificity {
            val a = if (id != null) 1 else 0
            val b = if (className != null) 1 else 0
            val c = if (tagName != null) 1 else 0
            return Specificity(a, b, c)
        }
    }
}

data class Declaration(val property: String, val value: String)

data class Rule(val selectors: List<Selector>, val declarations: List<Declaration>)

class CssStyleSheet(val rules: List<Rule>)

class CssParser(private val cssText: String) {
    fun parse(): CssStyleSheet {
        val rules = mutableListOf<Rule>()
        var pos = 0
        val len = cssText.length

        while (pos < len) {
            // Skip whitespace and comments
            while (pos < len && cssText[pos].isWhitespace()) pos++
            if (pos >= len) break

            if (cssText.startsWith("/*", pos)) {
                pos += 2
                while (pos < len && !cssText.startsWith("*/", pos)) pos++
                if (pos < len) pos += 2
                continue
            }

            // Parse selector
            val selectorStart = pos
            while (pos < len && cssText[pos] != '{') pos++
            if (pos >= len) break

            val selectorStr = cssText.substring(selectorStart, pos).trim()
            pos++ // skip '{'

            // Parse declarations
            val declStart = pos
            while (pos < len && cssText[pos] != '}') pos++
            val declStr = cssText.substring(declStart, pos)
            if (pos < len) pos++ // skip '}'

            val declarations = parseDeclarations(declStr)
            val selectors = parseSelectors(selectorStr)

            if (selectors.isNotEmpty() && declarations.isNotEmpty()) {
                rules.add(Rule(selectors, declarations))
            }
        }

        return CssStyleSheet(rules)
    }

    private fun parseSelectors(str: String): List<Selector> {
        val list = mutableListOf<Selector>()
        val rawSelectors = str.split(",")
        for (raw in rawSelectors) {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) continue

            var tag: String? = null
            var id: String? = null
            var cls: String? = null

            if (trimmed.startsWith("#")) {
                id = trimmed.substring(1)
            } else if (trimmed.startsWith(".")) {
                cls = trimmed.substring(1)
            } else {
                tag = trimmed
            }
            list.add(Selector.Simple(tag, id, cls))
        }
        return list
    }

    private fun parseDeclarations(str: String): List<Declaration> {
        val decls = mutableListOf<Declaration>()
        val pairs = str.split(";")
        for (pair in pairs) {
            val parts = pair.split(":")
            if (parts.size == 2) {
                val prop = parts[0].trim().lowercase()
                val valStr = parts[1].trim()
                if (prop.isNotEmpty() && valStr.isNotEmpty()) {
                    decls.add(Declaration(prop, valStr))
                }
            }
        }
        return decls
    }
}
