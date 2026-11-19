package net.coolshell.poite.shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiConfig {
    const val BASE_URL = "https://coolshell.free.beeceptor.com"
}

/**
 * 统一 HttpClient：base URL、JSON、超时。各业务域 Api 注入使用，
 * 不要各自 new HttpClient。非 2xx 会抛 ResponseException，由 safeApiCall 统一映射。
 */
fun createHttpClient(): HttpClient = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10_000
        connectTimeoutMillis = 10_000
    }
    defaultRequest { url(ApiConfig.BASE_URL) }
}
