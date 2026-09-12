package com.minesword.browser.engine.minesword.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.minesword.browser.engine.minesword.dom.Element
import com.minesword.browser.engine.minesword.dom.TextNode
import com.minesword.browser.engine.minesword.layout.LayoutNode

sealed class DisplayCommand {
    abstract val bounds: RectF

    data class DrawRect(override val bounds: RectF, val color: Int) : DisplayCommand()
    data class DrawText(override val bounds: RectF, val text: String, val x: Float, val y: Float, val fontSize: Float, val color: Int) : DisplayCommand()
    data class DrawBorder(override val bounds: RectF, val color: Int, val strokeWidth: Float, val href: String? = null) : DisplayCommand()
}

class RenderEngine {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    fun generateDisplayList(layoutRoot: LayoutNode): List<DisplayCommand> {
        val displayList = mutableListOf<DisplayCommand>()
        buildDisplayListRecursive(layoutRoot, displayList)
        return displayList
    }

    private fun buildDisplayListRecursive(node: LayoutNode, list: MutableList<DisplayCommand>) {
        val rectF = RectF(
            node.bounds.x,
            node.bounds.y,
            node.bounds.x + node.bounds.width,
            node.bounds.y + node.bounds.height
        )

        val bgColorStr = node.computedStyle["background-color"] ?: node.computedStyle["background"]
        val bgColor = parseColor(bgColorStr, Color.TRANSPARENT)
        if (bgColor != Color.TRANSPARENT) {
            list.add(DisplayCommand.DrawRect(rectF, bgColor))
        }

        if (node.node is Element) {
            if (node.node.tagName.equals("a", ignoreCase = true)) {
                val href = node.node.getAttribute("href")
                list.add(DisplayCommand.DrawBorder(rectF, Color.parseColor("#00E5FF"), 2f, href))
            }
        }

        if (node.node is TextNode) {
            val textColorStr = node.computedStyle["color"]
            val textColor = parseColor(textColorStr, Color.WHITE)
            val fontSize = parsePx(node.computedStyle["font-size"] ?: "16")
            list.add(DisplayCommand.DrawText(rectF, node.node.text, node.bounds.x, node.bounds.y, fontSize, textColor))
        }

        for (child in node.children) {
            buildDisplayListRecursive(child, list)
        }
    }

    fun renderToCanvas(canvas: Canvas, displayList: List<DisplayCommand>, scrollY: Float) {
        canvas.save()
        canvas.translate(0f, -scrollY)

        for (command in displayList) {
            when (command) {
                is DisplayCommand.DrawRect -> {
                    paint.style = Paint.Style.FILL
                    paint.color = command.color
                    canvas.drawRect(command.bounds, paint)
                }
                is DisplayCommand.DrawBorder -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = command.strokeWidth
                    paint.color = command.color
                    canvas.drawRect(command.bounds, paint)
                }
                is DisplayCommand.DrawText -> {
                    textPaint.color = command.color
                    textPaint.textSize = command.fontSize * 1.5f

                    val availableWidth = command.bounds.width().toInt().coerceAtLeast(100)
                    val staticLayout = StaticLayout.Builder
                        .obtain(command.text, 0, command.text.length, textPaint, availableWidth)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setIncludePad(false)
                        .build()

                    canvas.save()
                    canvas.translate(command.x, command.y)
                    staticLayout.draw(canvas)
                    canvas.restore()
                }
            }
        }

        canvas.restore()
    }

    private fun parseColor(colorStr: String?, defaultColor: Int): Int {
        if (colorStr.isNullOrBlank()) return defaultColor
        return try {
            if (colorStr.startsWith("#")) {
                Color.parseColor(colorStr)
            } else when (colorStr.lowercase()) {
                "white" -> Color.WHITE
                "black" -> Color.BLACK
                "red" -> Color.RED
                "blue" -> Color.BLUE
                "green" -> Color.GREEN
                "cyan" -> Color.parseColor("#00E5FF")
                "dark" -> Color.parseColor("#121824")
                else -> defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }

    private fun parsePx(value: String): Float {
        val digits = value.filter { it.isDigit() || it == '.' }
        return digits.toFloatOrNull() ?: 16f
    }
}
