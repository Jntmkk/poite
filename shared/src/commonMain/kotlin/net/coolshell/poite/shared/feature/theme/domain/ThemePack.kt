package net.coolshell.poite.shared.feature.theme.domain

import kotlinx.serialization.Serializable

/** 资源槽位：头像/首页插画/背景/tab 图标/启动海报 */
object ThemeSlot {
    const val AVATAR = "avatar"
    const val HOME_ILLUSTRATION = "homeIllustration"
    const val BACKGROUND = "background"
    const val TAB_HOME = "tabHome"
    const val TAB_SETTINGS = "tabSettings"
    const val SPLASH_POSTER = "splashPoster"
}

@Serializable
data class ThemePackSummary(
    val id: String,
    val name: String,
    val version: Int = 1,
    val previewColor: String = "",
    val previewImage: String? = null,
)

@Serializable
data class ThemePalette(
    val primary: String? = null,
    val onPrimary: String? = null,
    val background: String? = null,
    val surface: String? = null,
    val surfaceVariant: String? = null,
    val onSurface: String? = null,
)

@Serializable
data class ThemePack(
    val id: String,
    val name: String,
    val version: Int = 1,
    val previewColor: String = "",
    val colors: ThemePalettes = ThemePalettes(),
    val assets: Map<String, String> = emptyMap(),
)

@Serializable
data class ThemePalettes(
    val light: ThemePalette = ThemePalette(),
    val dark: ThemePalette = ThemePalette(),
)

/** 已下载到本地的主题包：pack 元数据 + slot → 本地文件路径 */
@Serializable
data class LoadedThemePack(
    val pack: ThemePack,
    val localAssets: Map<String, String> = emptyMap(),
)
