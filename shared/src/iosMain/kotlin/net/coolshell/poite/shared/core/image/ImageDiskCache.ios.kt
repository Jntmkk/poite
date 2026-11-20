package net.coolshell.poite.shared.core.image

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
actual object ImageDiskCache {
    private val dir: String by lazy {
        val base = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).first() as String
        val path = "$base/images"
        NSFileManager.defaultManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
        path
    }

    private fun file(url: String) = "$dir/${url.hashCode().toString(16)}"

    actual fun rootPath(): String = dir

    actual fun cachedPath(url: String): String? =
        file(url).takeIf { NSFileManager.defaultManager.fileExistsAtPath(it) }

    actual fun save(url: String, bytes: ByteArray): String {
        net.coolshell.poite.shared.core.storage.StorageFaults.checkWrite()
        val payload = if (net.coolshell.poite.shared.core.storage.StorageFaults.corruptOnWrite()) {
            ByteArray(16)
        } else {
            bytes
        }
        val path = file(url)
        val file = fopen(path, "wb")
        if (file != null) {
            try {
                payload.usePinned { fwrite(it.addressOf(0), 1uL, payload.size.toULong(), file) }
            } finally {
                fclose(file)
            }
        }
        return path
    }
}
