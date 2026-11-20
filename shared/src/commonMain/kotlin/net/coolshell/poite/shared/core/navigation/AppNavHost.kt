package net.coolshell.poite.shared.core.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.serialization.Serializable
import net.coolshell.poite.shared.core.ui.LocalHazeState
import net.coolshell.poite.shared.feature.home.presentation.HomeScreen
import net.coolshell.poite.shared.feature.settings.presentation.SettingsScreen

@Serializable
data object HomeRoute

@Serializable
data object SettingsRoute

/**
 * 全局导航：根 Scaffold 带悬浮胶囊 Tab。顶部 inset 由各页面 AppHeader 消费（含挖孔），
 * 内容不消费根 padding，延伸到胶囊下方形成磨砂
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val hazeState = remember { HazeState() }
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Scaffold(bottomBar = { AppBottomBar(navController) }) { _ ->
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
                modifier = Modifier.hazeSource(hazeState),
            ) {
                composable<HomeRoute> { HomeScreen() }
                composable<SettingsRoute> { SettingsScreen() }
            }
        }
    }
}
