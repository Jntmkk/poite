package net.coolshell.poite.shared.core.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.coolshell.poite.shared.core.image.decodeImageBitmap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** 当前装扮主题的资源（slot → 本地路径），空 map = 默认主题，AppTheme 提供 */
val LocalThemeAssets = compositionLocalOf { emptyMap<String, String>() }

/** 主题资源图片：有装扮素材用本地文件，否则回退内置默认图 */
@Composable
fun ThemeAssetImage(
    slot: String,
    defaultRes: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f,
) {
    val path = LocalThemeAssets.current[slot]
    if (path == null) {
        Image(painterResource(defaultRes), contentDescription, modifier, contentScale = contentScale, alpha = alpha)
        return
    }
    var bitmap by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = withContext(Dispatchers.Default) { decodeImageBitmap(path) }
    }
    bitmap?.let {
        Image(it, contentDescription, modifier, contentScale = contentScale, alpha = alpha)
    } ?: Image(painterResource(defaultRes), contentDescription, modifier, contentScale = contentScale, alpha = alpha)
}
