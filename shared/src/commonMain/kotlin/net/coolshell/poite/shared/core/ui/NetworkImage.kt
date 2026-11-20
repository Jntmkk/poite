package net.coolshell.poite.shared.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.coolshell.poite.shared.core.image.ImageDiskCache
import net.coolshell.poite.shared.core.image.decodeImageBitmap
import org.koin.compose.koinInject

private val memoryCache = mutableMapOf<String, ImageBitmap>()

/**
 * 网络图片：内存 → 磁盘 → 网络 三级查找，请求过的图离线可用。
 * 仅用于预览图等轻量场景，列表大图请换 Coil 并配置缓存策略
 */
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val client = koinInject<HttpClient>()
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(memoryCache[url]) }

    LaunchedEffect(url) {
        if (bitmap != null) return@LaunchedEffect

        ImageDiskCache.cachedPath(url)?.let { path ->
            val cached = withContext(Dispatchers.Default) { decodeImageBitmap(path) }
            if (cached != null) {
                memoryCache[url] = cached
                bitmap = cached
                return@LaunchedEffect
            }
        }

        runCatching {
            val bytes = client.get(url).bodyAsBytes()
            val path = ImageDiskCache.save(url, bytes)
            val decoded = withContext(Dispatchers.Default) { decodeImageBitmap(path) }
            if (decoded != null) {
                memoryCache[url] = decoded
                bitmap = decoded
            }
        }
    }

    bitmap?.let {
        Image(it, contentDescription, modifier, contentScale = contentScale)
    } ?: Box(modifier)
}
