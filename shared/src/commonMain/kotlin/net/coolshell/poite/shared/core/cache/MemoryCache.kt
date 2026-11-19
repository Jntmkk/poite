package net.coolshell.poite.shared.core.cache

/** 进程内 TTL 缓存，用于接口响应等短生命周期对象。跨重启持久化请用 KvStore 或数据库。 */
interface MemoryCache {
    fun <T : Any> get(key: String): T?
    fun <T : Any> put(key: String, value: T)
    fun remove(key: String)
    fun clear()
}
