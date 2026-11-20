package net.coolshell.poite.shared.feature.theme.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
actual object ThemeFiles {
    private val dir: String by lazy {
        val path = NSHomeDirectory() + "/Documents/theme"
        NSFileManager.defaultManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
        path
    }

    actual fun pathFor(packId: String, slot: String): String = "$dir/$packId-$slot.img"
    actual fun exists(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    actual fun save(path: String, bytes: ByteArray) {
        val file = fopen(path, "wb") ?: return
        try {
            bytes.usePinned { fwrite(it.addressOf(0), 1uL, bytes.size.toULong(), file) }
        } finally {
            fclose(file)
        }
    }

    actual fun delete(path: String) {
        NSFileManager.defaultManager.removeItemAtPath(path, null)
    }
}
