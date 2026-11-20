package net.coolshell.poite.shared.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.coolshell.poite.shared.Greeting
import net.coolshell.poite.shared.core.cache.KvStore
import net.coolshell.poite.shared.core.logging.AppLogger
import net.coolshell.poite.shared.core.tracking.AppEvent
import net.coolshell.poite.shared.core.tracking.Tracker
import net.coolshell.poite.shared.core.ui.AppHeader
import net.coolshell.poite.shared.core.ui.LocalThemeAssets
import net.coolshell.poite.shared.core.ui.ThemeAssetImage
import net.coolshell.poite.shared.daysPhrase
import net.coolshell.poite.shared.feature.theme.domain.ThemeSlot
import net.coolshell.poite.shared.generated.resources.Res
import net.coolshell.poite.shared.generated.resources.compose_multiplatform
import org.koin.compose.koinInject

@Composable
fun HomeScreen() {
    val logger = koinInject<AppLogger>()
    val kvStore = koinInject<KvStore>()
    val tracker = koinInject<Tracker>()
    var taps by remember { mutableIntStateOf(kvStore.getInt("demo_tap_count")) }

    val hasThemeBackground = LocalThemeAssets.current.containsKey(ThemeSlot.BACKGROUND)
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasThemeBackground) {
            ThemeAssetImage(
                slot = ThemeSlot.BACKGROUND,
                defaultRes = Res.drawable.compose_multiplatform,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f,
            )
        }
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppHeader(
                    onAdd = { tracker.track(AppEvent.HeaderAction("add")) },
                    onSearch = { tracker.track(AppEvent.HeaderAction("search")) },
                )
            },
        ) { padding ->
        Column(
            modifier = Modifier
                .background(if (hasThemeBackground) Color.Transparent else MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "poite",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Kotlin Multiplatform · 共享 UI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ThemeAssetImage(
                        slot = ThemeSlot.HOME_ILLUSTRATION,
                        defaultRes = Res.drawable.compose_multiplatform,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(140.dp).testTag("home-illustration-image"),
                    )
                    Text(
                        Greeting().greet(),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        daysPhrase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Button(
                onClick = {
                    taps++
                    kvStore.putInt("demo_tap_count", taps)
                    logger.i("Demo", "tapped, total=$taps")
                    tracker.track(AppEvent.DemoTap(taps))
                },
                modifier = Modifier.testTag("home-click-button"),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    if (taps == 0) "Click me!" else "Clicked $taps times",
                    modifier = Modifier.testTag("home-counter-text"),
                )
            }

            Spacer(Modifier.height(120.dp))
        }
        }
    }
}
