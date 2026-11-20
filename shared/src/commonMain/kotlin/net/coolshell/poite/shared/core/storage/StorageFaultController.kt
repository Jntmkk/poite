package net.coolshell.poite.shared.core.storage

import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.coolshell.poite.shared.core.cache.KvStore
import org.koin.mp.KoinPlatform

enum class StorageFault { NORMAL, READ_FAIL, WRITE_FAIL, ENOSPC, CORRUPT_IMAGE }

/** 存储故障注入：开关 + 场景，KvStore 持久化，实时生效（debug 限定同网络 Mock） */
class StorageFaultController(private val kvStore: KvStore) {
    private val _enabled = MutableStateFlow(kvStore.getBoolean(KEY_ENABLED, false))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _fault = MutableStateFlow(readFault())
    val fault: StateFlow<StorageFault> = _fault.asStateFlow()

    fun setEnabled(value: Boolean) {
        kvStore.putBoolean(KEY_ENABLED, value)
        _enabled.value = value
    }

    fun setFault(value: StorageFault) {
        kvStore.putString(KEY_FAULT, value.name)
        _fault.value = value
    }

    fun active(): Boolean = _enabled.value && _fault.value != StorageFault.NORMAL

    private fun readFault(): StorageFault =
        runCatching { StorageFault.valueOf(kvStore.getString(KEY_FAULT, StorageFault.NORMAL.name)) }
            .getOrDefault(StorageFault.NORMAL)

    private companion object {
        const val KEY_ENABLED = "storage_fault_enabled"
        const val KEY_FAULT = "storage_fault"
    }
}

/** 各存储操作入口的故障检查（Koin 运行时取控制器，业务无感） */
object StorageFaults {
    private fun current(): StorageFault? =
        KoinPlatform.getKoin().get<StorageFaultController>()
            .takeIf { it.active() }?.fault?.value

    fun checkRead() {
        if (current() == StorageFault.READ_FAIL) throw IOException("Mock storage read fail")
    }

    fun checkWrite() {
        when (current()) {
            StorageFault.WRITE_FAIL -> throw IOException("Mock storage write fail")
            StorageFault.ENOSPC -> throw IOException("Mock storage ENOSPC")
            else -> Unit
        }
    }

    fun corruptOnWrite(): Boolean = current() == StorageFault.CORRUPT_IMAGE
}
