package net.coolshell.poite.shared.feature.theme.data

/** 主题素材文件（沙盒 theme 目录），平台各自实现 */
expect object ThemeFiles {
    fun pathFor(packId: String, slot: String): String
    fun exists(path: String): Boolean
    fun save(path: String, bytes: ByteArray)
    fun delete(path: String)
}
