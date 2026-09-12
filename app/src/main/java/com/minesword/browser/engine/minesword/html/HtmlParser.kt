package com.minesword.browser.engine.minesword.html

import com.minesword.browser.engine.minesword.dom.CommentNode
import com.minesword.browser.engine.minesword.dom.Document
import com.minesword.browser.engine.minesword.dom.Element
import com.minesword.browser.engine.minesword.dom.Node
import com.minesword.browser.engine.minesword.dom.TextNode

sealed class HtmlToken {
    data class StartTag(val name: String, val attributes: Map<String, String>, val isSelfClosing: Boolean = false) : HtmlToken()
    data class EndTag(val name: String) : HtmlToken()
    data class CharacterToken(val text: String) : HtmlToken()
    data class CommentToken(val text: String) : HtmlToken()
    object EOF : HtmlToken()
}

class HtmlLexer(private val input: String) {
    private var pos = 0
    private val length = input.length

    fun nextToken(): HtmlToken {
        if (pos >= length) return HtmlToken.EOF

        val ch = input[pos]
        if (ch == '<') {
            if (pos + 1 < length && input[pos + 1] == '!') {
                // Comment token
                pos += 2
                val start = pos
                while (pos < length && !input.startsWith("-->", pos)) {
                    pos++
                }
                val commentText = input.substring(start, pos)
                if (pos < length) pos += 3
                return HtmlToken.CommentToken(commentText)
            } else if (pos + 1 < length && input[pos + 1] == '/') {
                // End tag
                pos += 2
                val start = pos
                while (pos < length && input[pos] != '>') {
                    pos++
                }
                val tagName = input.substring(start, pos).trim().lowercase()
                if (pos < length) pos++ // Skip '>'
                return HtmlToken.EndTag(tagName)
            } else {
                // Start tag
                pos++
                val start = pos
                while (pos < length && input[pos] != '>' && !input[pos].isWhitespace()) {
                    pos++
                }
                val tagName = input.substring(start, pos).trim().lowercase()
                val attributes = mutableMapOf<String, String>()

                // Parse attributes
                while (pos < length && input[pos] != '>') {
                    while (pos < length && input[pos].isWhitespace()) pos++
                    if (pos >= length || input[pos] == '>') break

                    val attrStart = pos
                    while (pos < length && input[pos] != '=' && input[pos] != '>' && !input[pos].isWhitespace()) {
                        pos++
                    }
                    val attrName = input.substring(attrStart, pos).lowercase()
                    var attrValue = ""

                    while (pos < length && input[pos].isWhitespace()) pos++
                    if (pos < length && input[pos] == '=') {
                        pos++ // skip '='
                        while (pos < length && input[pos].isWhitespace()) pos++
                        if (pos < length && (input[pos] == '"' || input[pos] == '\'')) {
                            val quote = input[pos]
                            pos++
                            val valStart = pos
                            while (pos < length && input[pos] != quote) pos++
                            attrValue = input.substring(valStart, pos)
                            if (pos < length) pos++ // skip quote
                        } else {
                            val valStart = pos
                            while (pos < length && !input[pos].isWhitespace() && input[pos] != '>') pos++
                            attrValue = input.substring(valStart, pos)
                        }
                    }
                    if (attrName.isNotEmpty()) {
                        attributes[attrName] = attrValue
                    }
                }

                var isSelfClosing = false
                if (pos < length && input[pos] == '>') {
                    if (pos > 0 && input[pos - 1] == '/') isSelfClosing = true
                    pos++
                }

                val autoSelfClosingTags = setOf("img", "input", "br", "hr", "meta", "link")
                if (autoSelfClosingTags.contains(tagName)) {
                    isSelfClosing = true
                }

                return HtmlToken.StartTag(tagName, attributes, isSelfClosing)
            }
        } else {
            // Text token
            val start = pos
            while (pos < length && input[pos] != '<') {
                pos++
            }
            val text = input.substring(start, pos)
            return HtmlToken.CharacterToken(text)
        }
    }
}

class HtmlParser(private val html: String) {
    fun parse(): Document {
        val document = Document()
        val lexer = HtmlLexer(html)
        val stack = mutableListOf<Node>(document)

        var htmlElement: Element? = null

        while (true) {
            val token = lexer.nextToken()
            if (token is HtmlToken.EOF) break

            val currentParent = stack.last()

            when (token) {
                is HtmlToken.StartTag -> {
                    val elem = Element(token.name)
                    token.attributes.forEach { (k, v) -> elem.setAttribute(k, v) }
                    currentParent.appendChild(elem)

                    if (token.name == "html" && htmlElement == null) {
                        htmlElement = elem
                        document.documentElement = elem
                    }

                    if (!token.isSelfClosing) {
                        stack.add(elem)
                    }
                }
                is HtmlToken.EndTag -> {
                    var idx = stack.size - 1
                    while (idx > 0) {
                        val node = stack[idx]
                        if (node is Element && node.tagName.equals(token.name, ignoreCase = true)) {
                            while (stack.size > idx) {
                                stack.removeAt(stack.size - 1)
                            }
                            break
                        }
                        idx--
                    }
                }
                is HtmlToken.CharacterToken -> {
                    if (token.text.isNotBlank()) {
                        currentParent.appendChild(TextNode(token.text))
                    }
                }
                is HtmlToken.CommentToken -> {
                    currentParent.appendChild(CommentNode(token.text))
                }
                else -> {}
            }
        }

        if (document.documentElement == null) {
            val root = Element("html")
            val body = Element("body")
            root.appendChild(body)
            document.appendChild(root)
            document.documentElement = root
        }

        return document
    }
}
