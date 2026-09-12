package com.minesword.browser

import com.minesword.browser.search.PersianNormalizer
import com.minesword.browser.search.SearchEngine
import com.minesword.browser.search.SearchEngineRouter
import com.minesword.browser.ui.tabs.TabManager
import org.junit.Assert.*
import org.junit.Test

class MineswordBrowserUnitTest {

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
    fun testSearchEngineRouter_formatsSearchQueries() {
        val queryResult = SearchEngineRouter.processInput("موضوعات برنامه‌نویسی", SearchEngine.GOOGLE)
        assertTrue(queryResult.startsWith("https://www.google.com/search?q="))
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
