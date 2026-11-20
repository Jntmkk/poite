package net.coolshell.poite.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import net.coolshell.poite.shared.core.navigation.AppNavHost
import net.coolshell.poite.shared.core.network.mock.MockController
import net.coolshell.poite.shared.core.network.mock.MockScenario
import net.coolshell.poite.shared.core.theme.AppTheme
import net.coolshell.poite.shared.feature.splash.presentation.SplashHost
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    AppTheme {
        val mock = koinInject<MockController>()
        val mockEnabled by mock.enabled.collectAsState()
        val mockScenario by mock.scenario.collectAsState()
        Box {
            SplashHost { AppNavHost() }
            if (mockEnabled && mockScenario != MockScenario.NORMAL) {
                Text(
                    "MOCK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("app-mock-banner")
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}
