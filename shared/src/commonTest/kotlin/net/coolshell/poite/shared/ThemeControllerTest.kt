package net.coolshell.poite.shared

import com.russhwolf.settings.MapSettings
import net.coolshell.poite.shared.core.cache.SettingsKvStore
import net.coolshell.poite.shared.core.theme.ThemeController
import net.coolshell.poite.shared.core.theme.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeControllerTest {
    @Test
    fun defaultsToSystem() {
        val controller = ThemeController(SettingsKvStore(MapSettings()))
        assertEquals(ThemeMode.SYSTEM, controller.mode.value)
        assertTrue(controller.isDark(systemDark = true))
        assertFalse(controller.isDark(systemDark = false))
    }

    @Test
    fun forcedModesOverrideSystem() {
        val controller = ThemeController(SettingsKvStore(MapSettings()))
        controller.setMode(ThemeMode.DARK)
        assertTrue(controller.isDark(systemDark = false))
        controller.setMode(ThemeMode.LIGHT)
        assertFalse(controller.isDark(systemDark = true))
    }

    @Test
    fun modeIsPersistedAcrossInstances() {
        val store = SettingsKvStore(MapSettings())
        ThemeController(store).setMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, ThemeController(store).mode.value)
    }
}
