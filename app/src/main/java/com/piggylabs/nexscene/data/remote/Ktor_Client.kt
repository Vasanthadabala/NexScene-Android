package com.piggylabs.nexscene.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClientProvider {
    private const val REQUEST_TIMEOUT_MS = 10_000L
    private const val CONNECT_TIMEOUT_MS = 10_000L
    private const val SOCKET_TIMEOUT_MS = 10_000L

    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    val client: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }
}
