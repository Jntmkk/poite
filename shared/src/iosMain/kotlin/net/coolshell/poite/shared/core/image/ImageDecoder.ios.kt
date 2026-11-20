package net.coolshell.poite.shared.core.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell

@OptIn(ExperimentalForeignApi::class)
actual fun decodeImageBitmap(path: String): ImageBitmap? = runCatching {
    val file = fopen(path, "rb") ?: return null
    val bytes = try {
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
    Image.makeFromEncoded(bytes).toComposeImageBitmap()
}.getOrNull()
