package com.minesword.browser.engine.minesword.layout

import com.minesword.browser.engine.minesword.css.CssStyleSheet
import com.minesword.browser.engine.minesword.dom.Element
import com.minesword.browser.engine.minesword.dom.Node
import com.minesword.browser.engine.minesword.dom.TextNode

data class Rect(var x: Float = 0f, var y: Float = 0f, var width: Float = 0f, var height: Float = 0f)

data class EdgeSizes(var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f, var left: Float = 0f)

enum class Display { BLOCK, INLINE, NONE }

class LayoutNode(val node: Node) {
    val computedStyle = mutableMapOf<String, String>()
    var display: Display = Display.BLOCK
    val bounds = Rect()
    val margin = EdgeSizes()
    val padding = EdgeSizes()
    val border = EdgeSizes()
    val children = mutableListOf<LayoutNode>()

    private val nonVisualTags = setOf("head", "script", "style", "meta", "link", "title", "noscript")

    fun applyStyle(stylesheet: CssStyleSheet) {
        if (node is Element) {
            val tag = node.tagName.lowercase()
            if (nonVisualTags.contains(tag)) {
                display = Display.NONE
                return
            }

            for (rule in stylesheet.rules) {
                for (selector in rule.selectors) {
                    if (selector.matches(node)) {
                        for (decl in rule.declarations) {
                            computedStyle[decl.property] = decl.value
                        }
                    }
                }
            }
        }

        val displayVal = computedStyle["display"] ?: "block"
        display = when (displayVal.lowercase()) {
            "none" -> Display.NONE
            "inline" -> Display.INLINE
            else -> Display.BLOCK
        }

        margin.top = parsePx(computedStyle["margin-top"] ?: computedStyle["margin"] ?: "0")
        margin.bottom = parsePx(computedStyle["margin-bottom"] ?: computedStyle["margin"] ?: "0")
        padding.top = parsePx(computedStyle["padding-top"] ?: computedStyle["padding"] ?: "4")
        padding.bottom = parsePx(computedStyle["padding-bottom"] ?: computedStyle["padding"] ?: "4")
        padding.left = parsePx(computedStyle["padding-left"] ?: computedStyle["padding"] ?: "8")
        padding.right = parsePx(computedStyle["padding-right"] ?: computedStyle["padding"] ?: "8")
    }

    private fun parsePx(value: String): Float {
        val digits = value.filter { it.isDigit() || it == '.' }
        return digits.toFloatOrNull() ?: 0f
    }
}

class LayoutEngine {
    fun buildLayoutTree(node: Node, stylesheet: CssStyleSheet): LayoutNode? {
        val layoutNode = LayoutNode(node)
        layoutNode.applyStyle(stylesheet)

        if (layoutNode.display == Display.NONE) return null

        for (child in node.childNodes) {
            val childLayout = buildLayoutTree(child, stylesheet)
            if (childLayout != null) {
                layoutNode.children.add(childLayout)
            }
        }
        return layoutNode
    }

    fun computeLayout(root: LayoutNode, containingWidth: Float) {
        root.bounds.x = 0f
        root.bounds.y = 0f
        root.bounds.width = containingWidth

        var currentY = 0f

        for (child in root.children) {
            child.bounds.x = root.bounds.x + child.margin.left + child.padding.left
            child.bounds.y = currentY + child.margin.top + child.padding.top

            val childWidth = root.bounds.width - child.margin.left - child.margin.right - child.padding.left - child.padding.right
            child.bounds.width = childWidth.coerceAtLeast(0f)

            if (child.node is TextNode) {
                val charWidthApprox = 14f
                val maxCharsPerLine = ((child.bounds.width / charWidthApprox).toInt()).coerceAtLeast(1)
                val lineCount = (child.node.text.length / maxCharsPerLine) + 1
                child.bounds.height = lineCount * 28f
            } else {
                computeLayout(child, child.bounds.width)
                if (child.bounds.height == 0f) {
                    val childrenHeight = child.children.sumOf { (it.bounds.height + it.margin.top + it.margin.bottom + it.padding.top + it.padding.bottom).toDouble() }.toFloat()
                    child.bounds.height = childrenHeight.coerceAtLeast(32f)
                }
            }

            currentY += child.bounds.height + child.margin.top + child.margin.bottom + child.padding.top + child.padding.bottom
        }

        root.bounds.height = currentY
    }
}
