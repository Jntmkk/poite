package net.coolshell.poite.shared.core.image

import android.content.Context
import org.koin.mp.KoinPlatform
import java.io.File

actual object ImageDiskCache {
    private val dir by lazy {
        File(KoinPlatform.getKoin().get<Context>().cacheDir, "images").apply { mkdirs() }
    }

    private fun file(url: String) = File(dir, url.hashCode().toString(16))

    actual fun cachedPath(url: String): String? = file(url).takeIf { it.isFile }?.absolutePath
    actual fun save(url: String, bytes: ByteArray): String =
        file(url).apply { writeBytes(bytes) }.absolutePath
}
