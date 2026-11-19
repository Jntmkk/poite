package net.coolshell.poite.shared.core.cache

import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.minutes

class Cache4kMemoryCache : MemoryCache {

    private val cache = Cache.Builder<String, Any>()
        .expireAfterWrite(10.minutes)
        .build()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: String): T? = cache.get(key) as? T

    override fun <T : Any> put(key: String, value: T) = cache.put(key, value)

    override fun remove(key: String) = cache.invalidate(key)

    override fun clear() = cache.invalidateAll()
}
