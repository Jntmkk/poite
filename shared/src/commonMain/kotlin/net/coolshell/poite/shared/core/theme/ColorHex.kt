package net.coolshell.poite.shared.core.theme

import androidx.compose.ui.graphics.Color

/** "#RRGGBB" / "#AARRGGBB" 解析，非法返回 null */
fun parseHexColor(hex: String?): Color? {
    val raw = hex?.removePrefix("#") ?: return null
    if (raw.length != 6 && raw.length != 8) return null
    val value = raw.toLongOrNull(16) ?: return null
    return Color(if (raw.length == 6) value or 0xFF000000 else value)
}
