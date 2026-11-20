package net.coolshell.poite.shared

import androidx.compose.runtime.Composable
import net.coolshell.poite.shared.core.navigation.AppNavHost
import net.coolshell.poite.shared.core.theme.AppTheme
import net.coolshell.poite.shared.feature.splash.presentation.SplashHost
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme { SplashHost { AppNavHost() } }
}
