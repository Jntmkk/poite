package net.coolshell.poite.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import net.coolshell.poite.shared.core.network.AppError
import net.coolshell.poite.shared.core.network.Resource
import net.coolshell.poite.shared.core.network.safeApiCall
import net.coolshell.poite.shared.feature.splash.data.SplashApiImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplashApiTest {
    private val sampleJson = """
        {"posters":[
            {"id":"a","imageUrl":"https://example.com/a.png","startAt":100,"endAt":200,"priority":5,"extraField":"ignored"},
            {"id":"b","imageUrl":"https://example.com/b.png","startAt":0,"endAt":1000}
        ]}
    """.trimIndent()

    private fun clientWith(handler: MockEngine) = HttpClient(handler) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test
    fun parsesPostersAndIgnoresUnknownFields() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/splash/config", request.url.encodedPath)
            respond(sampleJson, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val posters = SplashApiImpl(clientWith(engine)).fetchPosters()
        assertEquals(2, posters.size)
        assertEquals("a", posters[0].id)
        assertEquals(5, posters[0].priority)
        assertEquals(0, posters[1].priority)
    }

    @Test
    fun httpErrorMapsToAppErrorHttp() = runTest {
        val engine = MockEngine { respond("boom", status = HttpStatusCode.InternalServerError) }
        val result = safeApiCall { SplashApiImpl(clientWith(engine)).fetchPosters() }
        assertTrue(result is Resource.Error)
        assertEquals(AppError.Http(500), (result as Resource.Error).error)
    }

    @Test
    fun badJsonMapsToAppErrorParse() = runTest {
        val engine = MockEngine {
            respond("not json", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val result = safeApiCall { SplashApiImpl(clientWith(engine)).fetchPosters() }
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).error is AppError.Parse)
    }
}
