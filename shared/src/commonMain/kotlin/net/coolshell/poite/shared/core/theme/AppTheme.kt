package net.coolshell.poite.shared.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.coolshell.poite.shared.core.ui.LocalThemeAssets
import net.coolshell.poite.shared.feature.theme.data.ThemePackRepository
import net.coolshell.poite.shared.feature.theme.domain.ThemePalette
import org.koin.compose.koinInject

/** 应用统一主题入口：深浅色模式 × 装扮色板。组件一律用语义色，禁止写死颜色 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val controller = koinInject<ThemeController>()
    val mode by controller.mode.collectAsState()
    val packRepository = koinInject<ThemePackRepository>()
    val activePack by packRepository.active.collectAsState()

    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val base = if (dark) darkColorScheme() else lightColorScheme()
    val palette = activePack?.pack?.colors?.let { if (dark) it.dark else it.light }
    val scheme = if (palette == null) base else base.applyPalette(palette)

    CompositionLocalProvider(LocalThemeAssets provides (activePack?.localAssets ?: emptyMap())) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

private fun ColorScheme.applyPalette(p: ThemePalette): ColorScheme = copy(
    primary = parseHexColor(p.primary) ?: primary,
    onPrimary = parseHexColor(p.onPrimary) ?: onPrimary,
    background = parseHexColor(p.background) ?: background,
    surface = parseHexColor(p.surface) ?: surface,
    surfaceVariant = parseHexColor(p.surfaceVariant) ?: surfaceVariant,
    surfaceContainerLow = parseHexColor(p.surfaceVariant) ?: surfaceContainerLow,
    onSurface = parseHexColor(p.onSurface) ?: onSurface,
)
