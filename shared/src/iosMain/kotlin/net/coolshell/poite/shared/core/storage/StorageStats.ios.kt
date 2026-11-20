@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package net.coolshell.poite.shared.core.storage

import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber

actual fun directorySizeBytes(path: String): Long {
    StorageFaults.checkRead()
    val manager = NSFileManager.defaultManager
    val enumerator = manager.enumeratorAtPath(path) ?: return 0
    var total = 0L
    while (true) {
        val item = enumerator.nextObject() as? String ?: break
        val attrs = manager.attributesOfItemAtPath("$path/$item", error = null)
        val size = attrs?.get(NSFileSize) as? NSNumber
        total += size?.longValue ?: 0
    }
    return total
}

actual fun fileSizeBytes(path: String): Long {
    StorageFaults.checkRead()
    val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
    return (attrs?.get(NSFileSize) as? NSNumber)?.longValue ?: 0
}

actual fun clearDirectoryContents(path: String) {
    val manager = NSFileManager.defaultManager
    manager.contentsOfDirectoryAtPath(path, error = null)?.forEach { item ->
        manager.removeItemAtPath("$path/$item", error = null)
    }
}
