package net.coolshell.poite.shared.core.image

import androidx.compose.ui.graphics.ImageBitmap

/** 内存图片字节解码（网络图/资源图共用） */
expect fun decodeImageBytes(bytes: ByteArray): ImageBitmap?
