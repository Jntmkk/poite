package net.coolshell.poite.shared.feature.theme.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import net.coolshell.poite.shared.feature.theme.domain.ThemePack
import net.coolshell.poite.shared.feature.theme.domain.ThemePackSummary

interface ThemeApi {
    suspend fun listPacks(): List<ThemePackSummary>
    suspend fun getPack(id: String): ThemePack
}

class ThemeApiImpl(private val client: HttpClient) : ThemeApi {
    override suspend fun listPacks(): List<ThemePackSummary> =
        client.get("api/theme/list").body()

    override suspend fun getPack(id: String): ThemePack =
        client.get("api/theme/$id").body()
}
