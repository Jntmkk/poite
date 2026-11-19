package net.coolshell.poite.shared.feature.splash.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.coolshell.poite.shared.feature.splash.data.SplashFiles
import net.coolshell.poite.shared.feature.splash.data.SplashRepository
import net.coolshell.poite.shared.feature.splash.domain.SplashPoster
import org.koin.compose.koinInject

private const val MIN_SPLASH_MS = 500L

/**
 * 主内容始终在底层组合；有生效海报时上层盖 Splash。
 * 切换条件：主内容首帧就绪 && 至少展示 500ms（防闪屏）；点击立即跳过。
 * 无海报则零延迟直进内容。
 */
@Composable
fun SplashHost(content: @Composable () -> Unit) {
    val repository = koinInject<SplashRepository>()
    val poster = remember { repository.activePoster() }
    var contentReady by remember { mutableStateOf(false) }
    var minElapsed by remember { mutableStateOf(false) }
    var skipped by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.refreshAsync()
        delay(MIN_SPLASH_MS)
        minElapsed = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.onGloballyPositioned { contentReady = true }) {
            content()
        }
        if (poster != null && !skipped && !(contentReady && minElapsed)) {
            SplashScreen(poster, onSkip = { skipped = true })
        }
    }
}

@Composable
private fun SplashScreen(poster: SplashPoster, onSkip: () -> Unit) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(poster.id) {
        bitmap = withContext(Dispatchers.Default) { SplashFiles.loadBitmap(poster.localPath) }
        if (bitmap == null) onSkip()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onSkip),
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
