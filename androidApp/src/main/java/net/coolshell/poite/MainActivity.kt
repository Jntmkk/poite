package net.coolshell.poite

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.coolshell.poite.shared.App
import net.coolshell.poite.shared.core.theme.ThemeController
import org.koin.mp.KoinPlatform

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val theme = KoinPlatform.getKoin().get<ThemeController>()

        fun applySystemBars() {
            val systemDark = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { theme.isDark(systemDark) }
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        }
        applySystemBars()
        lifecycleScope.launch { theme.mode.collect { applySystemBars() } }

        setContent {
            App()
        }
    }
}
