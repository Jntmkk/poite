package net.coolshell.poite.shared.feature.theme.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.coolshell.poite.shared.core.cache.KvStore
import net.coolshell.poite.shared.core.logging.AppLogger
import net.coolshell.poite.shared.feature.splash.data.SplashRepository
import net.coolshell.poite.shared.feature.splash.domain.SplashPoster
import net.coolshell.poite.shared.feature.theme.domain.LoadedThemePack
import net.coolshell.poite.shared.feature.theme.domain.ThemePackSummary
import net.coolshell.poite.shared.feature.theme.domain.ThemeSlot

/**
 * 装扮主题仓库。激活的包与素材路径持久化在 KvStore，启动同步读取（无网络）；
 * 默认主题 = active 为 null，完全内置。
 */
class ThemePackRepository(
    private val api: ThemeApi,
    private val client: HttpClient,
    private val kvStore: KvStore,
    private val logger: AppLogger,
    private val splashRepository: SplashRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _active = MutableStateFlow(readActive())
    val active: StateFlow<LoadedThemePack?> = _active.asStateFlow()

    val summaries = MutableStateFlow<List<ThemePackSummary>>(emptyList())
    val downloading = MutableStateFlow(false)

    fun loadSummariesAsync() {
        scope.launch {
            runCatching { summaries.value = api.listPacks() }
                .onFailure { logger.w(TAG, "list failed: ${it.message}") }
        }
    }

    fun activateAsync(id: String?) {
        scope.launch {
            runCatching { activate(id) }
                .onFailure { logger.w(TAG, "activate failed: ${it.message}") }
        }
    }

    /** 本地已下载的包直接应用（零网络），并后台校验版本；未下载才走网络 */
    suspend fun activate(id: String?) {
        if (id == null) {
            _active.value = null
            kvStore.remove(KEY_ACTIVE)
            splashRepository.setThemePoster(null)
            return
        }
        val cached = readPackCache(id)
        if (cached != null) {
            applyActive(cached)
            scope.launch { runCatching { refreshPack(id, cached.pack.version) } }
            return
        }
        downloading.value = true
        try {
            applyActive(downloadPack(id))
        } finally {
            downloading.value = false
        }
    }

    private suspend fun downloadPack(id: String): LoadedThemePack {
        val pack = api.getPack(id)
        val localAssets = coroutineScope {
            pack.assets.map { (slot, url) ->
                async {
                    val path = ThemeFiles.pathFor(id, slot)
                    if (!ThemeFiles.exists(path)) {
                        ThemeFiles.save(path, client.get(url).bodyAsBytes())
                    }
                    slot to path
                }
            }.awaitAll().toMap()
        }
        return LoadedThemePack(pack, localAssets)
    }

    private suspend fun refreshPack(id: String, currentVersion: Int) {
        val pack = api.getPack(id)
        if (pack.version > currentVersion) {
            applyActive(downloadPack(id))
        }
    }

    private fun applyActive(loaded: LoadedThemePack) {
        kvStore.putString(KEY_ACTIVE, json.encodeToString(loaded))
        kvStore.putString(KEY_PACK_CACHE_PREFIX + loaded.pack.id, json.encodeToString(loaded))
        _active.value = loaded
        loaded.localAssets[ThemeSlot.SPLASH_POSTER]?.let { path ->
            splashRepository.setThemePoster(
                SplashPoster(
                    id = "theme_pack",
                    imageUrl = loaded.pack.assets[ThemeSlot.SPLASH_POSTER].orEmpty(),
                    startAt = 0,
                    endAt = Long.MAX_VALUE,
                    localPath = path,
                )
            )
        }
    }

    private fun readPackCache(id: String): LoadedThemePack? =
        runCatching {
            json.decodeFromString<LoadedThemePack>(kvStore.getString(KEY_PACK_CACHE_PREFIX + id))
        }.getOrNull()?.takeIf { loaded -> loaded.localAssets.values.all { ThemeFiles.exists(it) } }

    /** 缓存被外部清理后调用：素材已删则回退默认显示；包选择保留在 KV，重新激活即重下 */
    fun onCacheCleared() {
        _active.value = readActive()
    }

    private fun readActive(): LoadedThemePack? =
        runCatching { json.decodeFromString<LoadedThemePack>(kvStore.getString(KEY_ACTIVE)) }.getOrNull()
            ?.takeIf { loaded -> loaded.localAssets.values.all { ThemeFiles.exists(it) } }

    private companion object {
        const val TAG = "Theme"
        const val KEY_ACTIVE = "theme_active_pack"
        const val KEY_PACK_CACHE_PREFIX = "theme_pack_cache_"
    }
}
