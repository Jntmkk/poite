package net.coolshell.poite.shared

import com.russhwolf.settings.MapSettings
import net.coolshell.poite.shared.core.cache.SettingsKvStore
import net.coolshell.poite.shared.core.storage.StorageFault
import net.coolshell.poite.shared.core.storage.StorageFaultController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StorageFaultControllerTest {
    @Test
    fun inactiveByDefault() {
        assertFalse(StorageFaultController(SettingsKvStore(MapSettings())).active())
    }

    @Test
    fun faultActiveAndPersisted() {
        val store = SettingsKvStore(MapSettings())
        val controller = StorageFaultController(store)
        controller.setEnabled(true)
        controller.setFault(StorageFault.ENOSPC)
        assertTrue(controller.active())
        assertTrue(StorageFaultController(store).active())
        assertEquals(StorageFault.ENOSPC, StorageFaultController(store).fault.value)
        controller.setFault(StorageFault.NORMAL)
        assertFalse(controller.active())
    }

    @Test
    fun faultWithoutEnableIsInactive() {
        val controller = StorageFaultController(SettingsKvStore(MapSettings()))
        controller.setFault(StorageFault.READ_FAIL)
        assertFalse(controller.active())
    }
}
