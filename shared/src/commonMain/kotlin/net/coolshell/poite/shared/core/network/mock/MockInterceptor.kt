package net.coolshell.poite.shared.core.network.mock

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.utils.io.InternalAPI
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.mp.KoinPlatform

/** 端上 Mock 拦截：命中场景则构造响应/异常/延迟，否则放行真实请求 */
fun HttpClient.installMockInterceptor() {
    val client = this
    plugin(HttpSend).intercept { request ->
        val controller = KoinPlatform.getKoin().get<MockController>()
        if (!controller.active()) return@intercept execute(request)

        when (controller.scenario.value) {
            MockScenario.NORMAL -> execute(request)
            MockScenario.SLOW -> {
                delay(3000)
                execute(request)
            }
            MockScenario.OFFLINE -> throw IOException("Mock offline")
            MockScenario.ALL_TIMEOUT -> {
                delay(3000)
                throw HttpRequestTimeoutException(request)
            }
            MockScenario.ALL_500 -> client.mockCall(request, 500, """{"error":"mock server error"}""")
            MockScenario.EMPTY -> client.mockCall(request, 200, emptyBodyFor(request.url.build().encodedPath))
            MockScenario.BAD_JSON -> client.mockCall(request, 200, "not json")
        }
    }
}

@OptIn(InternalAPI::class)
private fun HttpClient.mockCall(request: HttpRequestBuilder, status: Int, body: String): HttpClientCall {
    val responseData = HttpResponseData(
        statusCode = HttpStatusCode(status, "Mock"),
        requestTime = GMTDate(),
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        version = HttpProtocolVersion.HTTP_1_1,
        body = ByteReadChannel(body.encodeToByteArray()),
        callContext = Job(),
    )
    return HttpClientCall(this, request.build(), responseData)
}

private fun emptyBodyFor(path: String): String = when {
    path.endsWith("/list") -> "[]"
    path.endsWith("/config") -> """{"posters":[]}"""
    else -> "{}"
}
