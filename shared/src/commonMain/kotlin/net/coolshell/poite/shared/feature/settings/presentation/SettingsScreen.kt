package net.coolshell.poite.shared.feature.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.coolshell.poite.shared.core.theme.ThemeController
import net.coolshell.poite.shared.core.theme.ThemeMode
import net.coolshell.poite.shared.core.theme.parseHexColor
import net.coolshell.poite.shared.core.ui.AppHeader
import net.coolshell.poite.shared.core.ui.NetworkImage
import net.coolshell.poite.shared.feature.theme.data.ThemePackRepository
import net.coolshell.poite.shared.generated.resources.Res
import net.coolshell.poite.shared.generated.resources.settings_about
import net.coolshell.poite.shared.generated.resources.settings_appearance
import net.coolshell.poite.shared.generated.resources.settings_decoration
import net.coolshell.poite.shared.generated.resources.settings_version
import net.coolshell.poite.shared.generated.resources.theme_dark
import net.coolshell.poite.shared.generated.resources.theme_default
import net.coolshell.poite.shared.generated.resources.theme_light
import net.coolshell.poite.shared.generated.resources.theme_system
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SettingsScreen() {
    val theme = koinInject<ThemeController>()
    val mode by theme.mode.collectAsState()
    val packRepository = koinInject<ThemePackRepository>()
    val activePack by packRepository.active.collectAsState()
    val summaries by packRepository.summaries.collectAsState()
    val downloading by packRepository.downloading.collectAsState()

    LaunchedEffect(Unit) { packRepository.loadSummariesAsync() }

    Scaffold(topBar = { AppHeader() }) { padding ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            SettingsGroup(label = stringResource(Res.string.settings_appearance)) {
                SegmentedSelector(
                    options = ThemeMode.entries.map { it to stringResource(it.labelRes()) },
                    selected = mode,
                    onSelect = theme::setMode,
                )
            }
            SettingsGroup(label = stringResource(Res.string.settings_decoration)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ThemePackCard(
                        name = stringResource(Res.string.theme_default),
                        color = Color(0xFF6750A4),
                        selected = activePack == null,
                        loading = false,
                        onClick = { packRepository.activateAsync(null) },
                    )
                    summaries.forEach { summary ->
                        ThemePackCard(
                            name = summary.name,
                            color = parseHexColor(summary.previewColor) ?: Color.LightGray,
                            imageUrl = summary.previewImage,
                            selected = activePack?.pack?.id == summary.id,
                            loading = downloading && activePack?.pack?.id != summary.id,
                            onClick = { packRepository.activateAsync(summary.id) },
                        )
                    }
                }
            }
            SettingsGroup(label = stringResource(Res.string.settings_about)) {
                SettingsRow(
                    title = stringResource(Res.string.settings_version),
                    trailing = "1.0 (1)",
                )
            }
        }
    }
}

@Composable
private fun ThemePackCard(
    name: String,
    color: Color,
    selected: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    imageUrl: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color,
            border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    imageUrl != null -> NetworkImage(
                        url = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    )
                    selected -> Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(16.dp))
                }
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
        }
        Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
    }
}

private fun ThemeMode.labelRes(): StringResource = when (this) {
    ThemeMode.SYSTEM -> Res.string.theme_system
    ThemeMode.LIGHT -> Res.string.theme_light
    ThemeMode.DARK -> Res.string.theme_dark
}
