package net.coolshell.poite.shared.core.image

import android.content.Context
import org.koin.mp.KoinPlatform
import java.io.File

actual object ImageDiskCache {
    private val dir by lazy {
        File(KoinPlatform.getKoin().get<Context>().cacheDir, "images").apply { mkdirs() }
    }

    private fun file(url: String) = File(dir, url.hashCode().toString(16))

    actual fun rootPath(): String = dir.absolutePath

    actual fun cachedPath(url: String): String? = file(url).takeIf { it.isFile }?.absolutePath
    actual fun save(url: String, bytes: ByteArray): String {
        net.coolshell.poite.shared.core.storage.StorageFaults.checkWrite()
        val payload = if (net.coolshell.poite.shared.core.storage.StorageFaults.corruptOnWrite()) {
            ByteArray(16)
        } else {
            bytes
        }
        return file(url).apply { writeBytes(payload) }.absolutePath
    }
}
