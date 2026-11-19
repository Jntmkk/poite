package net.coolshell.poite.shared.feature.splash.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.koin.mp.KoinPlatform
import java.io.File

actual object SplashFiles {
    private val dir by lazy {
        File(KoinPlatform.getKoin().get<Context>().filesDir, "splash").apply { mkdirs() }
    }

    actual fun pathFor(id: String): String = File(dir, "$id.img").absolutePath
    actual fun exists(path: String): Boolean = File(path).isFile
    actual fun save(path: String, bytes: ByteArray) = File(path).writeBytes(bytes)
    actual fun delete(path: String) { File(path).delete() }
    actual fun loadBitmap(path: String): ImageBitmap? =
        runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
}
