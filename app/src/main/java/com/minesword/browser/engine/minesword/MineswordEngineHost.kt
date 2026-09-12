package com.minesword.browser.engine.minesword

import com.minesword.browser.engine.minesword.networking.HttpResponse
import com.minesword.browser.engine.minesword.networking.MineswordNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MineswordEngineHost {

    private val networkClient = MineswordNetworkClient()

    suspend fun loadPage(url: String): HttpResponse = withContext(Dispatchers.IO) {
        if (url == "about:blank") {
            return@withContext HttpResponse(
                statusCode = 200,
                headers = emptyMap(),
                body = "<html><body><h1>Minesword Engine</h1><p>Ready to navigate the web.</p></body></html>",
                contentType = "text/html"
            )
        }

        try {
            networkClient.fetchUrl(url)
        } catch (e: Exception) {
            HttpResponse(
                statusCode = 500,
                headers = emptyMap(),
                body = "<html><body><h1 style='color:red;'>Network Error</h1><p>${e.message}</p></body></html>",
                contentType = "text/html"
            )
        }
    }
}
