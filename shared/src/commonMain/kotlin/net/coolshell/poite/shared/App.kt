package net.coolshell.poite.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.coolshell.poite.shared.core.cache.KvStore
import net.coolshell.poite.shared.core.crash.CrashReporter
import net.coolshell.poite.shared.core.logging.AppLogger
import net.coolshell.poite.shared.core.theme.AppTheme
import net.coolshell.poite.shared.feature.splash.presentation.SplashHost
import net.coolshell.poite.shared.core.tracking.Tracker
import net.coolshell.poite.shared.generated.resources.Res
import net.coolshell.poite.shared.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    AppTheme { SplashHost { HomeContent() } }
}

@Composable
private fun HomeContent() {
        val logger = koinInject<AppLogger>()
        val kvStore = koinInject<KvStore>()
        val tracker = koinInject<Tracker>()
        val crashReporter = koinInject<CrashReporter>()
        var taps by remember { mutableIntStateOf(kvStore.getInt("demo_tap_count")) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize()
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
                    Image(
                        painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
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
                    tracker.track("demo_tap", mapOf("count" to taps))
                },
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(if (taps == 0) "Click me!" else "Clicked $taps times")
            }

        }
}
