package net.coolshell.poite.shared.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import net.coolshell.poite.shared.core.ui.LocalThemeAssets
import net.coolshell.poite.shared.core.ui.ThemeAssetImage
import net.coolshell.poite.shared.core.ui.glass
import net.coolshell.poite.shared.feature.theme.domain.ThemeSlot
import net.coolshell.poite.shared.generated.resources.Res
import net.coolshell.poite.shared.generated.resources.compose_multiplatform
import net.coolshell.poite.shared.generated.resources.home_title
import net.coolshell.poite.shared.generated.resources.settings_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class Tab(
    val labelRes: StringResource,
    val icon: ImageVector,
    val slot: String,
    val testTag: String,
    val isSelected: (NavController) -> Boolean,
    val onClick: (NavController) -> Unit,
)

/** 悬浮胶囊底部 Tab：圆角浮起、选中项药丸高亮。新 tab 在 tabs 加一项 + AppNavHost 注册 */
@Composable
fun AppBottomBar(navController: NavController) {
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination

    val tabs = listOf(
        Tab(
            labelRes = Res.string.home_title,
            icon = Icons.Filled.Home,
            slot = ThemeSlot.TAB_HOME,
            testTag = "bottombar-home-tab",
            isSelected = { destination?.hasRoute<HomeRoute>() == true },
            onClick = { it.navigateTab(HomeRoute) },
        ),
        Tab(
            labelRes = Res.string.settings_title,
            icon = Icons.Filled.Settings,
            slot = ThemeSlot.TAB_SETTINGS,
            testTag = "bottombar-settings-tab",
            isSelected = { destination?.hasRoute<SettingsRoute>() == true },
            onClick = { it.navigateTab(SettingsRoute) },
        ),
    )

    Surface(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .glass(RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tabs.forEach { tab ->
                val selected = tab.isSelected(navController)
                val contentColor = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    onClick = { tab.onClick(navController) },
                    modifier = Modifier.weight(1f).testTag(tab.testTag),
                    shape = RoundedCornerShape(24.dp),
                    color = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (LocalThemeAssets.current.containsKey(tab.slot)) {
                            ThemeAssetImage(
                                slot = tab.slot,
                                defaultRes = Res.drawable.compose_multiplatform,
                                contentDescription = stringResource(tab.labelRes),
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Icon(
                                tab.icon,
                                contentDescription = stringResource(tab.labelRes),
                                tint = contentColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** tab 切换标准行为：回到根、单实例、保存/恢复各 tab 状态 */
private fun NavController.navigateTab(route: Any) {
    navigate(route) {
        popUpTo(HomeRoute::class) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
