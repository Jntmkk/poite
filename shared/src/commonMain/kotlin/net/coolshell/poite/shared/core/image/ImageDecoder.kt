package net.coolshell.poite.shared.core.image

import androidx.compose.ui.graphics.ImageBitmap

/** 本地图片文件解码为 Bitmap，双端共用（splash/装扮等） */
expect fun decodeImageBitmap(path: String): ImageBitmap?
