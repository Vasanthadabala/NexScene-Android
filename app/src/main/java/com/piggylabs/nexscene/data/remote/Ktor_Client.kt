package com.piggylabs.nexscene.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.InetAddress

object KtorClientProvider {
    private const val REQUEST_TIMEOUT_MS = 45_000L
    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val SOCKET_TIMEOUT_MS = 45_000L

    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    val client: HttpClient by lazy {
        val bootstrapClient = OkHttpClient.Builder().build()
        val cloudflareDns = DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url("https://1.1.1.1/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                InetAddress.getByName("1.1.1.1"),
                InetAddress.getByName("1.0.0.1")
            )
            .build()

        val dohClient = OkHttpClient.Builder()
            .dns(cloudflareDns)
            .build()

        HttpClient(OkHttp) {
            engine {
                preconfigured = dohClient
            }
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
            install(Logging) {
                level = LogLevel.HEADERS
            }
        }
    }
}
