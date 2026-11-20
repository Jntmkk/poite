package net.coolshell.poite.shared.core.network.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.coolshell.poite.shared.core.cache.KvStore

enum class MockScenario { NORMAL, ALL_500, ALL_TIMEOUT, OFFLINE, SLOW, EMPTY, BAD_JSON }

/** 网络 Mock 总控：开关 + 场景，持久化。active=false 时拦截器完全放行 */
class MockController(private val kvStore: KvStore) {
    private val _enabled = MutableStateFlow(kvStore.getBoolean(KEY_ENABLED, false))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _scenario = MutableStateFlow(readScenario())
    val scenario: StateFlow<MockScenario> = _scenario.asStateFlow()

    fun setEnabled(value: Boolean) {
        kvStore.putBoolean(KEY_ENABLED, value)
        _enabled.value = value
    }

    fun setScenario(value: MockScenario) {
        kvStore.putString(KEY_SCENARIO, value.name)
        _scenario.value = value
    }

    fun active(): Boolean = _enabled.value && _scenario.value != MockScenario.NORMAL

    private fun readScenario(): MockScenario =
        runCatching { MockScenario.valueOf(kvStore.getString(KEY_SCENARIO, MockScenario.NORMAL.name)) }
            .getOrDefault(MockScenario.NORMAL)

    private companion object {
        const val KEY_ENABLED = "mock_enabled"
        const val KEY_SCENARIO = "mock_scenario"
    }
}
