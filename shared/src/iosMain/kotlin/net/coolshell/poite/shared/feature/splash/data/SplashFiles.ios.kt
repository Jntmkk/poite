package net.coolshell.poite.shared.feature.splash.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
actual object SplashFiles {
    private val dir: String by lazy {
        val path = NSHomeDirectory() + "/Documents/splash"
        NSFileManager.defaultManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
        path
    }

    actual fun pathFor(id: String): String = "$dir/$id.img"
    actual fun rootPath(): String = dir
    actual fun exists(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    actual fun save(path: String, bytes: ByteArray) {
        net.coolshell.poite.shared.core.storage.StorageFaults.checkWrite()
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

    actual fun loadBitmap(path: String): ImageBitmap? = runCatching {
        val bytes = readBytes(path) ?: return null
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    }.getOrNull()

    private fun readBytes(path: String): ByteArray? {
        val file = fopen(path, "rb") ?: return null
        return try {
            fseek(file, 0, SEEK_END)
            val size = ftell(file)
            fseek(file, 0, SEEK_SET)
            if (size <= 0) return null
            val bytes = ByteArray(size.toInt())
            bytes.usePinned { fread(it.addressOf(0), 1uL, size.toULong(), file) }
            bytes
        } finally {
            fclose(file)
        }
    }
}
