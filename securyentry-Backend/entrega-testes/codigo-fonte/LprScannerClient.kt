package com.example.demo.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

// Faz a chamada HTTP para a câmera LPR.
// Está separado do LprService justamente pra poder ser mockado nos testes.
@Component
class LprScannerClient {
    private val lprUrl = System.getenv("LPR_SERVER_URL") ?: "http://localhost:8001"
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val mapper = ObjectMapper()

    @Suppress("UNCHECKED_CAST")
    fun scan(): Map<String, Any?>? = runCatching {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$lprUrl/lpr/scan"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) return null
        mapper.readValue(response.body(), Map::class.java) as Map<String, Any?>
    }.getOrNull()
}
