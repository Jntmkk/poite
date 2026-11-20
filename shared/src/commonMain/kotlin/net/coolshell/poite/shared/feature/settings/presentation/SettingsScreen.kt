package net.coolshell.poite.shared.feature.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import net.coolshell.poite.shared.core.network.mock.MockController
import net.coolshell.poite.shared.core.network.mock.MockScenario
import net.coolshell.poite.shared.core.network.mock.mockAvailable
import net.coolshell.poite.shared.core.storage.StorageFault
import net.coolshell.poite.shared.core.storage.StorageFaultController
import net.coolshell.poite.shared.core.storage.StorageManager
import net.coolshell.poite.shared.core.storage.formatBytes
import net.coolshell.poite.shared.core.theme.ThemeController
import net.coolshell.poite.shared.core.tracking.AppEvent
import net.coolshell.poite.shared.core.tracking.Tracker
import net.coolshell.poite.shared.core.theme.ThemeMode
import net.coolshell.poite.shared.core.theme.parseHexColor
import net.coolshell.poite.shared.core.ui.AppHeader
import net.coolshell.poite.shared.core.ui.NetworkImage
import net.coolshell.poite.shared.feature.theme.data.ThemePackRepository
import net.coolshell.poite.shared.generated.resources.Res
import net.coolshell.poite.shared.generated.resources.mock_network
import net.coolshell.poite.shared.generated.resources.mock_scenario_all_500
import net.coolshell.poite.shared.generated.resources.mock_scenario_all_timeout
import net.coolshell.poite.shared.generated.resources.mock_scenario_bad_json
import net.coolshell.poite.shared.generated.resources.mock_scenario_empty
import net.coolshell.poite.shared.generated.resources.mock_scenario_normal
import net.coolshell.poite.shared.generated.resources.mock_scenario_offline
import net.coolshell.poite.shared.generated.resources.mock_scenario_slow
import net.coolshell.poite.shared.generated.resources.action_cancel
import net.coolshell.poite.shared.generated.resources.action_confirm
import net.coolshell.poite.shared.generated.resources.action_ok
import net.coolshell.poite.shared.generated.resources.fault_corrupt
import net.coolshell.poite.shared.generated.resources.fault_enospc
import net.coolshell.poite.shared.generated.resources.fault_normal
import net.coolshell.poite.shared.generated.resources.fault_read_fail
import net.coolshell.poite.shared.generated.resources.fault_write_fail
import net.coolshell.poite.shared.generated.resources.settings_about
import net.coolshell.poite.shared.generated.resources.settings_storage
import net.coolshell.poite.shared.generated.resources.storage_clear
import net.coolshell.poite.shared.generated.resources.storage_cache
import net.coolshell.poite.shared.generated.resources.storage_clear_failed
import net.coolshell.poite.shared.generated.resources.storage_data
import net.coolshell.poite.shared.generated.resources.storage_fault
import net.coolshell.poite.shared.generated.resources.storage_unavailable
import net.coolshell.poite.shared.generated.resources.settings_developer
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
    val tracker = koinInject<Tracker>()
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
                    onSelect = {
                        theme.setMode(it)
                        tracker.track(AppEvent.ThemeModeChanged(it.name))
                    },
                    testTagFor = { "settings-theme-${it.name.lowercase()}-option" },
                )
            }
            SettingsGroup(label = stringResource(Res.string.settings_decoration)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ThemePackCard(
                        name = stringResource(Res.string.theme_default),
                        color = Color(0xFF6750A4),
                        selected = activePack == null,
                        loading = false,
                        onClick = {
                            packRepository.activateAsync(null)
                            tracker.track(AppEvent.ThemePackChanged("default"))
                        },
                        testTag = "settings-pack-default-card",
                    )
                    summaries.forEach { summary ->
                        ThemePackCard(
                            name = summary.name,
                            color = parseHexColor(summary.previewColor) ?: Color.LightGray,
                            imageUrl = summary.previewImage,
                            selected = activePack?.pack?.id == summary.id,
                            loading = downloading && activePack?.pack?.id != summary.id,
                            onClick = {
                                packRepository.activateAsync(summary.id)
                                tracker.track(AppEvent.ThemePackChanged(summary.id))
                            },
                            testTag = "settings-pack-${summary.id}-card",
                        )
                    }
                }
            }
            if (mockAvailable) {
                val mock = koinInject<MockController>()
                val mockEnabled by mock.enabled.collectAsState()
                val mockScenario by mock.scenario.collectAsState()
                SettingsGroup(label = stringResource(Res.string.settings_developer)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(Res.string.mock_network), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = mockEnabled,
                            onCheckedChange = mock::setEnabled,
                            modifier = Modifier.testTag("settings-mock-switch"),
                        )
                    }
                    if (mockEnabled) {
                        Spacer(Modifier.height(8.dp))
                        scenarioRows.forEach { row ->
                            SegmentedSelector(
                                options = row.map { it to stringResource(it.labelRes()) },
                                selected = mockScenario,
                                onSelect = mock::setScenario,
                                testTagFor = { "settings-mock-${it.name.lowercase().replace('_', '-')}-option" },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    val faultController = koinInject<StorageFaultController>()
                    val faultEnabled by faultController.enabled.collectAsState()
                    val fault by faultController.fault.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(Res.string.storage_fault), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = faultEnabled,
                            onCheckedChange = faultController::setEnabled,
                            modifier = Modifier.testTag("settings-storage-mock-switch"),
                        )
                    }
                    if (faultEnabled) {
                        Spacer(Modifier.height(8.dp))
                        faultRows.forEach { row ->
                            SegmentedSelector(
                                options = row.map { it to stringResource(it.labelRes()) },
                                selected = fault,
                                onSelect = faultController::setFault,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
            SettingsGroup(label = stringResource(Res.string.settings_storage)) {
                val storage = koinInject<StorageManager>()
                var usage by remember { mutableStateOf(storage.usage()) }
                var showClearDialog by remember { mutableStateOf(false) }
                var showClearError by remember { mutableStateOf(false) }

                SettingsRow(
                    title = stringResource(Res.string.storage_cache),
                    trailing = if (usage.error) stringResource(Res.string.storage_unavailable) else formatBytes(usage.cacheBytes),
                )
                SettingsRow(
                    title = stringResource(Res.string.storage_data),
                    trailing = if (usage.error) stringResource(Res.string.storage_unavailable) else formatBytes(usage.dataBytes),
                )
                TextButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("settings-clear-cache-button"),
                ) {
                    Text(stringResource(Res.string.storage_clear), color = MaterialTheme.colorScheme.error)
                }

                if (showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        title = { Text(stringResource(Res.string.storage_clear)) },
                        confirmButton = {
                            TextButton(onClick = {
                                if (storage.clearCache()) {
                                    usage = storage.usage()
                                } else {
                                    showClearError = true
                                }
                                showClearDialog = false
                            }) {
                                Text(stringResource(Res.string.action_confirm))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text(stringResource(Res.string.action_cancel))
                            }
                        },
                    )
                }
                if (showClearError) {
                    AlertDialog(
                        onDismissRequest = { showClearError = false },
                        title = { Text(stringResource(Res.string.storage_clear_failed)) },
                        confirmButton = {
                            TextButton(onClick = { showClearError = false }) {
                                Text(stringResource(Res.string.action_ok))
                            }
                        },
                    )
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
    testTag: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).testTag(testTag),
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

private val scenarioRows = listOf(
    listOf(MockScenario.NORMAL, MockScenario.ALL_500, MockScenario.ALL_TIMEOUT, MockScenario.OFFLINE),
    listOf(MockScenario.SLOW, MockScenario.EMPTY, MockScenario.BAD_JSON),
)

private fun MockScenario.labelRes(): StringResource = when (this) {
    MockScenario.NORMAL -> Res.string.mock_scenario_normal
    MockScenario.ALL_500 -> Res.string.mock_scenario_all_500
    MockScenario.ALL_TIMEOUT -> Res.string.mock_scenario_all_timeout
    MockScenario.OFFLINE -> Res.string.mock_scenario_offline
    MockScenario.SLOW -> Res.string.mock_scenario_slow
    MockScenario.EMPTY -> Res.string.mock_scenario_empty
    MockScenario.BAD_JSON -> Res.string.mock_scenario_bad_json
}

private val faultRows = listOf(
    listOf(StorageFault.NORMAL, StorageFault.READ_FAIL, StorageFault.WRITE_FAIL),
    listOf(StorageFault.ENOSPC, StorageFault.CORRUPT_IMAGE),
)

private fun StorageFault.labelRes(): StringResource = when (this) {
    StorageFault.NORMAL -> Res.string.fault_normal
    StorageFault.READ_FAIL -> Res.string.fault_read_fail
    StorageFault.WRITE_FAIL -> Res.string.fault_write_fail
    StorageFault.ENOSPC -> Res.string.fault_enospc
    StorageFault.CORRUPT_IMAGE -> Res.string.fault_corrupt
}
