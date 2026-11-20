package net.coolshell.poite.shared.feature.splash.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.coolshell.poite.shared.core.cache.KvStore
import net.coolshell.poite.shared.core.logging.AppLogger
import net.coolshell.poite.shared.feature.splash.domain.SplashPoster
import net.coolshell.poite.shared.feature.splash.domain.selectActivePoster
import kotlin.time.Clock

/**
 * 启动海报仓库。启动时只读本地缓存（activePoster，同步、无网络）；
 * 主页面展示后调 refreshAsync() 后台准备下次的海报。
 */
class SplashRepository(
    private val api: SplashApi,
    private val client: HttpClient,
    private val kvStore: KvStore,
    private val logger: AppLogger,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun activePoster(now: Long = Clock.System.now().toEpochMilliseconds()): SplashPoster? =
        selectActivePoster(readPosters().filter { SplashFiles.exists(it.localPath) }, now)

    fun refreshAsync() {
        scope.launch {
            runCatching { refresh() }
                .onFailure { logger.w(TAG, "refresh failed: ${it.message}") }
        }
    }

    suspend fun refresh() {
        val posters = api.fetchPosters()
        val old = readPosters().associateBy { it.id }
        val updated = posters.map { poster ->
            val cached = old[poster.id]
            if (cached != null && cached.imageUrl == poster.imageUrl && SplashFiles.exists(cached.localPath)) {
                poster.copy(localPath = cached.localPath)
            } else {
                val bytes = client.get(poster.imageUrl).bodyAsBytes()
                val path = SplashFiles.pathFor(poster.id)
                SplashFiles.save(path, bytes)
                poster.copy(localPath = path)
            }
        }
        val keptPaths = updated.map { it.localPath }.toSet()
        old.values.filter { it.localPath.isNotEmpty() && it.localPath !in keptPaths }
            .forEach { SplashFiles.delete(it.localPath) }
        kvStore.putString(KEY_POSTERS, json.encodeToString(updated))
        logger.i(TAG, "refreshed: ${updated.size} posters")
    }

    /** 装扮主题海报：priority 固定 1（低于运营海报），传 null 移除 */
    fun setThemePoster(poster: SplashPoster?) {
        val others = readPosters().filter { it.id != THEME_POSTER_ID }
        val updated = if (poster != null) {
            others + poster.copy(id = THEME_POSTER_ID, priority = 1)
        } else {
            others
        }
        kvStore.putString(KEY_POSTERS, json.encodeToString(updated))
    }

    private fun readPosters(): List<SplashPoster> =
        runCatching {
            json.decodeFromString<List<SplashPoster>>(kvStore.getString(KEY_POSTERS, "[]"))
        }.getOrDefault(emptyList())

    companion object {
        private const val TAG = "Splash"
        private const val KEY_POSTERS = "splash_posters_json"
        private const val THEME_POSTER_ID = "theme_pack"
    }
}
