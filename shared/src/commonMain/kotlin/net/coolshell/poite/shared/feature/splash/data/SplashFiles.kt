package net.coolshell.poite.shared.feature.splash.data

import androidx.compose.ui.graphics.ImageBitmap

/** 海报文件的读写与解码，平台各自实现（沙盒目录） */
expect object SplashFiles {
    fun pathFor(id: String): String
    fun exists(path: String): Boolean
    fun save(path: String, bytes: ByteArray)
    fun delete(path: String)
    fun loadBitmap(path: String): ImageBitmap?
}
