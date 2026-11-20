package net.coolshell.poite.shared.core.storage

import android.content.Context
import org.koin.mp.KoinPlatform
import java.io.File

actual fun kvStoreFileBytes(): Long {
    val context = KoinPlatform.getKoin().get<Context>()
    return fileSizeBytes(File(context.filesDir.parent, "shared_prefs/poite.xml").absolutePath)
}
