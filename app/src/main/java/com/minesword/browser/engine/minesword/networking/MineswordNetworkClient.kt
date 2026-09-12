package com.minesword.browser.engine.minesword.networking

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: String,
    val contentType: String?
)

class MineswordNetworkClient {

    fun fetchUrl(urlString: String): HttpResponse {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "MineswordEngine/1.0 (Android Native Engine)")

        if (connection is HttpsURLConnection) {
            // Strict TLS setup
        }

        val statusCode = connection.responseCode
        val contentType = connection.contentType
        val headers = connection.headerFields

        val reader = if (statusCode in 200..299) {
            BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
        } else {
            BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream, "UTF-8"))
        }

        val bodyBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            bodyBuilder.append(line).append("\n")
        }
        reader.close()
        connection.disconnect()

        return HttpResponse(statusCode, headers, bodyBuilder.toString(), contentType)
    }
}
