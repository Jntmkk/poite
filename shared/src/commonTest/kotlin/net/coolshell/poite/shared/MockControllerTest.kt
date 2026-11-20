package net.coolshell.poite.shared

import com.russhwolf.settings.MapSettings
import net.coolshell.poite.shared.core.cache.SettingsKvStore
import net.coolshell.poite.shared.core.network.mock.MockController
import net.coolshell.poite.shared.core.network.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockControllerTest {
    @Test
    fun inactiveByDefault() {
        assertFalse(MockController(SettingsKvStore(MapSettings())).active())
    }

    @Test
    fun enabledWithNormalScenarioStillInactive() {
        val controller = MockController(SettingsKvStore(MapSettings()))
        controller.setEnabled(true)
        assertFalse(controller.active())
    }

    @Test
    fun enabledWithScenarioIsActiveAndPersisted() {
        val store = SettingsKvStore(MapSettings())
        val controller = MockController(store)
        controller.setEnabled(true)
        controller.setScenario(MockScenario.OFFLINE)
        assertTrue(controller.active())
        assertTrue(MockController(store).active())
        assertEquals(MockScenario.OFFLINE, MockController(store).scenario.value)
    }
}
