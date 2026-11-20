package net.coolshell.poite.shared.feature.theme.data

import android.content.Context
import org.koin.mp.KoinPlatform
import java.io.File

actual object ThemeFiles {
    private val dir by lazy {
        File(KoinPlatform.getKoin().get<Context>().filesDir, "theme").apply { mkdirs() }
    }

    actual fun pathFor(packId: String, slot: String): String = File(dir, "$packId-$slot.img").absolutePath
    actual fun exists(path: String): Boolean = File(path).isFile
    actual fun save(path: String, bytes: ByteArray) = File(path).writeBytes(bytes)
    actual fun delete(path: String) { File(path).delete() }
}
