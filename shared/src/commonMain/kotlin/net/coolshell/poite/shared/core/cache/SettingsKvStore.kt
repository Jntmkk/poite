package net.coolshell.poite.shared.core.cache

import com.russhwolf.settings.Settings

class SettingsKvStore(private val settings: Settings) : KvStore {

    override fun putString(key: String, value: String) = settings.putString(key, value)

    override fun getString(key: String, default: String): String = settings.getString(key, default)

    override fun putInt(key: String, value: Int) = settings.putInt(key, value)

    override fun getInt(key: String, default: Int): Int = settings.getInt(key, default)

    override fun putLong(key: String, value: Long) = settings.putLong(key, value)

    override fun getLong(key: String, default: Long): Long = settings.getLong(key, default)

    override fun putBoolean(key: String, value: Boolean) = settings.putBoolean(key, value)

    override fun getBoolean(key: String, default: Boolean): Boolean = settings.getBoolean(key, default)

    override fun remove(key: String) = settings.remove(key)

    override fun clear() = settings.clear()
}
