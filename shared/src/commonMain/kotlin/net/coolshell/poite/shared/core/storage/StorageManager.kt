package net.coolshell.poite.shared.core.storage

import net.coolshell.poite.shared.core.image.ImageDiskCache
import net.coolshell.poite.shared.feature.splash.data.SplashFiles
import net.coolshell.poite.shared.feature.theme.data.ThemeFiles
import net.coolshell.poite.shared.feature.theme.data.ThemePackRepository
import kotlin.math.round

data class StorageUsage(val cacheBytes: Long, val dataBytes: Long, val error: Boolean = false)

/**
 * 存储统一收口。缓存 = 可重新下载/生成（图片缓存、海报、主题素材）；
 * 数据 = KvStore 配置等，永不在 clearCache 里动。读取失败时 usage.error=true
 */
class StorageManager(private val themePackRepository: ThemePackRepository) {
    fun usage(): StorageUsage = runCatching {
        StorageUsage(
            cacheBytes = cacheDirs().sumOf { directorySizeBytes(it) },
            dataBytes = kvStoreFileBytes(),
        )
    }.getOrDefault(StorageUsage(cacheBytes = 0, dataBytes = 0, error = true))

    fun clearCache(): Boolean = runCatching {
        cacheDirs().forEach { clearDirectoryContents(it) }
        themePackRepository.onCacheCleared()
    }.isSuccess

    private fun cacheDirs(): List<String> = listOf(
        SplashFiles.rootPath(),
        ThemeFiles.rootPath(),
        ImageDiskCache.rootPath(),
    )
}

fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${round(bytes / 1024.0 / 1024.0 * 10) / 10.0} MB"
}
