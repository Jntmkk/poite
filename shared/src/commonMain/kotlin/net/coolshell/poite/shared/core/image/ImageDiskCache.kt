package net.coolshell.poite.shared.core.image

/** 网络图磁盘缓存：url 哈希为文件名，命中即离线可用 */
expect object ImageDiskCache {
    fun rootPath(): String
    fun cachedPath(url: String): String?
    fun save(url: String, bytes: ByteArray): String
}
