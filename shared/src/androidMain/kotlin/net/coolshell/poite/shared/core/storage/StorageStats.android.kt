package net.coolshell.poite.shared.core.storage

import java.io.File

actual fun directorySizeBytes(path: String): Long {
    StorageFaults.checkRead()
    val dir = File(path)
    if (!dir.exists()) return 0
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

actual fun fileSizeBytes(path: String): Long {
    StorageFaults.checkRead()
    return File(path).takeIf { it.isFile }?.length() ?: 0
}

actual fun clearDirectoryContents(path: String) {
    File(path).listFiles()?.forEach { it.deleteRecursively() }
}
