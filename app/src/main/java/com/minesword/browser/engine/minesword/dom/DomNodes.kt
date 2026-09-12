package com.minesword.browser.engine.minesword.dom

open class Node(val nodeName: String) {
    var parentNode: Node? = null
    val childNodes = mutableListOf<Node>()

    fun appendChild(child: Node) {
        child.parentNode = this
        childNodes.add(child)
    }

    fun removeChild(child: Node) {
        if (childNodes.remove(child)) {
            child.parentNode = null
        }
    }

    open fun textContent(): String {
        val sb = StringBuilder()
        for (child in childNodes) {
            if (child is TextNode) {
                sb.append(child.text)
            } else if (child is Element) {
                sb.append(child.textContent())
            }
        }
        return sb.toString()
    }
}

class Document : Node("#document") {
    var documentElement: Element? = null

    fun createElement(tagName: String): Element {
        return Element(tagName.lowercase())
    }

    fun createTextNode(text: String): TextNode {
        return TextNode(text)
    }

    fun createCommentNode(text: String): CommentNode {
        return CommentNode(text)
    }
}

class Element(val tagName: String) : Node(tagName.uppercase()) {
    val attributes = mutableMapOf<String, String>()
    val id: String get() = attributes["id"] ?: ""
    val className: String get() = attributes["class"] ?: ""

    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) {
        attributes[name] = value
    }

    override fun textContent(): String {
        val sb = StringBuilder()
        for (child in childNodes) {
            if (child is TextNode) {
                sb.append(child.text)
            } else if (child is Element) {
                sb.append(child.textContent())
            }
        }
        return sb.toString()
    }
}

class TextNode(var text: String) : Node("#text") {
    override fun textContent(): String = text
}

class CommentNode(val text: String) : Node("#comment") {
    override fun textContent(): String = ""
}
