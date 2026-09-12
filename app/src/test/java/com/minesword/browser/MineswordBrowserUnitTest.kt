package com.minesword.browser

import com.minesword.browser.engine.minesword.css.CssParser
import com.minesword.browser.engine.minesword.dom.Element
import com.minesword.browser.engine.minesword.dom.TextNode
import com.minesword.browser.engine.minesword.html.HtmlParser
import com.minesword.browser.engine.minesword.layout.LayoutEngine
import com.minesword.browser.engine.minesword.security.MineswordSecurityEngine
import com.minesword.browser.search.PersianNormalizer
import com.minesword.browser.search.SearchEngine
import com.minesword.browser.search.SearchEngineRouter
import com.minesword.browser.ui.tabs.TabManager
import org.junit.Assert.*
import org.junit.Test

class MineswordBrowserUnitTest {

    @Test
    fun testHtmlParser_buildsDomTreeCorrectly() {
        val html = "<html><body><h1 id='title'>سلام جهان</h1><p class='desc'>مرورگر ماین‌سورد</p></body></html>"
        val parser = HtmlParser(html)
        val document = parser.parse()

        assertNotNull(document.documentElement)
        assertEquals("html", document.documentElement?.tagName?.lowercase())

        val body = document.documentElement?.childNodes?.firstOrNull { it is Element && it.tagName.equals("BODY", ignoreCase = true) } as? Element
        assertNotNull(body)
        assertEquals(2, body?.childNodes?.size)
    }

    @Test
    fun testCssParserAndLayoutEngine() {
        val css = "h1 { color: cyan; margin-top: 10px; } p { color: white; }"
        val cssSheet = CssParser(css).parse()

        assertEquals(2, cssSheet.rules.size)

        val html = "<html><body><h1>Minesword Engine</h1></body></html>"
        val doc = HtmlParser(html).parse()

        val layoutEngine = LayoutEngine()
        val rootLayout = layoutEngine.buildLayoutTree(doc, cssSheet)

        assertNotNull(rootLayout)
        layoutEngine.computeLayout(rootLayout!!, 1080f)
        assertTrue(rootLayout.bounds.width > 0f)
    }

    @Test
    fun testSecurityEngine_enforcesSameOriginPolicy() {
        assertTrue(MineswordSecurityEngine.validateOrigin("https://example.com/page1", "https://example.com/page2"))
        assertFalse(MineswordSecurityEngine.validateOrigin("https://example.com", "http://example.com"))
        assertFalse(MineswordSecurityEngine.validateOrigin("https://example.com", "https://other.com"))
    }

    @Test
    fun testPersianNormalizer_convertsPersianNumbersAndCharacters() {
        val input = "۰۱۲۳۴۵۶۷۸۹ ي ك"
        val normalized = PersianNormalizer.normalize(input)
        assertEquals("0123456789 ی ک", normalized)
    }

    @Test
    fun testSearchEngineRouter_identifiesDirectUrls() {
        assertEquals("https://google.com", SearchEngineRouter.processInput("google.com"))
        assertEquals("https://www.wikipedia.org", SearchEngineRouter.processInput("https://www.wikipedia.org"))
        assertEquals("http://192.168.1.1:8080", SearchEngineRouter.processInput("192.168.1.1:8080"))
        assertEquals("http://localhost:3000", SearchEngineRouter.processInput("localhost:3000"))
    }

    @Test
    fun testTabManager_addSelectAndCloseTabs() {
        val manager = TabManager()
        val tab1 = manager.addTab("https://site1.com")
        val tab2 = manager.addTab("https://site2.com")

        assertEquals(2, manager.getTabs().size)
        assertEquals(tab2.id, manager.getActiveTab()?.id)

        manager.selectTab(tab1.id)
        assertEquals(tab1.id, manager.getActiveTab()?.id)

        manager.closeTab(tab1.id)
        assertEquals(1, manager.getTabs().size)
        assertEquals(tab2.id, manager.getActiveTab()?.id)
    }
}
