package net.coolshell.poite.shared.feature.splash.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import net.coolshell.poite.shared.feature.splash.domain.SplashConfig
import net.coolshell.poite.shared.feature.splash.domain.SplashPoster

/** splash 域 API。base URL 由统一 HttpClient 携带，这里只写 path */
interface SplashApi {
    suspend fun fetchPosters(): List<SplashPoster>
}

class SplashApiImpl(private val client: HttpClient) : SplashApi {
    override suspend fun fetchPosters(): List<SplashPoster> =
        client.get("api/splash/config").body<SplashConfig>().posters
}
