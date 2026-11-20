package net.coolshell.poite.shared

import androidx.compose.ui.graphics.Color
import net.coolshell.poite.shared.core.theme.parseHexColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ColorHexTest {
    @Test
    fun sixDigitParsesWithOpaqueAlpha() {
        assertEquals(Color(0xFFF5E6C8), parseHexColor("#F5E6C8"))
        assertEquals(Color(0xFF8C6D4F), parseHexColor("#8C6D4F"))
    }

    @Test
    fun eightDigitKeepsAlpha() {
        assertEquals(Color(0x80F5E6C8L), parseHexColor("#80F5E6C8"))
    }

    @Test
    fun invalidReturnsNull() {
        assertNull(parseHexColor("bad"))
        assertNull(parseHexColor(null))
        assertNull(parseHexColor("#12345"))
    }
}
