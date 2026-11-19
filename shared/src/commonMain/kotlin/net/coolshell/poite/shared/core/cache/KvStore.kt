package net.coolshell.poite.shared.core.cache

/** 轻量 KV 持久化（配置、标志位等）。结构化数据请用 Room/SQLDelight，不在这里塞。 */
interface KvStore {
    fun putString(key: String, value: String)
    fun getString(key: String, default: String = ""): String
    fun putInt(key: String, value: Int)
    fun getInt(key: String, default: Int = 0): Int
    fun putLong(key: String, value: Long)
    fun getLong(key: String, default: Long = 0L): Long
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun remove(key: String)
    fun clear()
}
