package net.coolshell.poite.shared.core.storage

/** 目录递归大小 / 单文件大小 / 清空目录内容（保留目录本身） */
expect fun directorySizeBytes(path: String): Long
expect fun fileSizeBytes(path: String): Long
expect fun clearDirectoryContents(path: String)
