package net.coolshell.poite.shared

import com.russhwolf.settings.MapSettings
import net.coolshell.poite.shared.core.cache.Cache4kMemoryCache
import net.coolshell.poite.shared.core.cache.SettingsKvStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CoreInfraTest {

    @Test
    fun kvStoreRoundTrip() {
        val store = SettingsKvStore(MapSettings())
        store.putString("name", "poite")
        store.putInt("count", 42)
        store.putBoolean("enabled", true)

        assertEquals("poite", store.getString("name"))
        assertEquals(42, store.getInt("count"))
        assertEquals(true, store.getBoolean("enabled"))
        assertEquals("default", store.getString("missing", "default"))

        store.remove("name")
        assertEquals("", store.getString("name"))
    }

    @Test
    fun memoryCachePutGetRemove() {
        val cache = Cache4kMemoryCache()
        cache.put("user", "ywb")

        assertEquals("ywb", cache.get<String>("user"))
        assertNull(cache.get<String>("missing"))

        cache.remove("user")
        assertNull(cache.get<String>("user"))
    }
}
