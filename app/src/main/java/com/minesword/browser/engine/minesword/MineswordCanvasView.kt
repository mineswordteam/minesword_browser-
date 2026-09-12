package com.minesword.browser.engine.minesword

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.minesword.browser.engine.minesword.css.CssParser
import com.minesword.browser.engine.minesword.css.CssStyleSheet
import com.minesword.browser.engine.minesword.dom.Document
import com.minesword.browser.engine.minesword.dom.Element
import com.minesword.browser.engine.minesword.html.HtmlParser
import com.minesword.browser.engine.minesword.layout.LayoutEngine
import com.minesword.browser.engine.minesword.layout.LayoutNode
import com.minesword.browser.engine.minesword.rendering.DisplayCommand
import com.minesword.browser.engine.minesword.rendering.RenderEngine
import java.net.URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MineswordCanvasView(context: Context) : View(context) {

    private var document: Document? = null
    private var stylesheet: CssStyleSheet = CssStyleSheet(emptyList())
    private var layoutRoot: LayoutNode? = null
    private var displayList: List<DisplayCommand> = emptyList()

    private val layoutEngine = LayoutEngine()
    private val renderEngine = RenderEngine()

    private var scrollYOffset = 0f
    private val scope = CoroutineScope(Dispatchers.Main)
    private var baseUrl: String = "https://www.google.com"

    var onLinkClicked: ((url: String) -> Unit)? = null

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            scrollYOffset = (scrollYOffset + distanceY).coerceAtLeast(0f)
            invalidate()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val touchX = e.x
            val touchY = e.y + scrollYOffset

            for (command in displayList) {
                if (command is DisplayCommand.DrawBorder && command.href != null) {
                    if (command.bounds.contains(touchX, touchY)) {
                        val resolvedUrl = resolveUrl(baseUrl, command.href)
                        onLinkClicked?.invoke(resolvedUrl)
                        return true
                    }
                }
            }
            return super.onSingleTapUp(e)
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun loadHtmlContent(htmlContent: String, cssContent: String = "", pageUrl: String = "https://www.google.com") {
        this.baseUrl = pageUrl
        scope.launch(Dispatchers.Default) {
            val parser = HtmlParser(htmlContent)
            val doc = parser.parse()

            // Extract inline <style> tags from DOM
            val inlineCssBuilder = StringBuilder(cssContent)
            extractStyleText(doc, inlineCssBuilder)

            val cssParser = CssParser(inlineCssBuilder.toString())
            val sheet = cssParser.parse()

            val width = if (measuredWidth > 0) measuredWidth.toFloat() else 1080f
            val rootLayout = layoutEngine.buildLayoutTree(doc, sheet)

            if (rootLayout != null) {
                layoutEngine.computeLayout(rootLayout, width)
                val commands = renderEngine.generateDisplayList(rootLayout)

                withContext(Dispatchers.Main) {
                    document = doc
                    stylesheet = sheet
                    layoutRoot = rootLayout
                    displayList = commands
                    scrollYOffset = 0f
                    invalidate()
                }
            }
        }
    }

    private fun extractStyleText(node: com.minesword.browser.engine.minesword.dom.Node, sb: StringBuilder) {
        if (node is Element && node.tagName.equals("style", ignoreCase = true)) {
            sb.append("\n").append(node.textContent())
        } else {
            for (child in node.childNodes) {
                extractStyleText(child, sb)
            }
        }
    }

    private fun resolveUrl(base: String, href: String): String {
        return try {
            val baseUri = URI(base)
            val resolved = baseUri.resolve(href)
            resolved.toString()
        } catch (e: Exception) {
            href
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#121824")) // Dark Theme Background

        if (displayList.isNotEmpty()) {
            renderEngine.renderToCanvas(canvas, displayList, scrollYOffset)
        }
    }
}
