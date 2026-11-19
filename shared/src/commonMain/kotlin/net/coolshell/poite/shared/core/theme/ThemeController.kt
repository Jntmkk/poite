package net.coolshell.poite.shared.core.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.coolshell.poite.shared.core.cache.KvStore

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** 主题偏好：跟随系统/强制浅色/强制深色，持久化到 KvStore。系统深浅色切换无需重启 */
class ThemeController(private val kvStore: KvStore) {
    private val _mode = MutableStateFlow(readMode())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        kvStore.putString(KEY, mode.name)
        _mode.value = mode
    }

    fun isDark(systemDark: Boolean): Boolean = when (_mode.value) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    private fun readMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(kvStore.getString(KEY, ThemeMode.SYSTEM.name)) }
            .getOrDefault(ThemeMode.SYSTEM)

    private companion object { const val KEY = "theme_mode" }
}
