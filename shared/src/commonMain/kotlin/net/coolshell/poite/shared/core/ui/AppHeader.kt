package net.coolshell.poite.shared.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import net.coolshell.poite.shared.feature.theme.domain.ThemeSlot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.coolshell.poite.shared.generated.resources.Res
import net.coolshell.poite.shared.generated.resources.action_add
import net.coolshell.poite.shared.generated.resources.action_back
import net.coolshell.poite.shared.generated.resources.action_search
import net.coolshell.poite.shared.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.stringResource

/**
 * 统一大标题 header：左头像（下钻页为返回键）+ 右侧胶囊操作组 + 下方大标题。
 * 根页面传 onAdd/onSearch 显示胶囊；下钻页传 onBack。
 */
@Composable
fun AppHeader(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("header-back-button")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.action_back),
                    )
                }
            } else {
                ThemeAssetImage(
                    slot = ThemeSlot.AVATAR,
                    defaultRes = Res.drawable.compose_multiplatform,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape).testTag("header-avatar-image"),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(Modifier.weight(1f))
            if (onAdd != null || onSearch != null) {
                Surface(
                    modifier = Modifier.glass(RoundedCornerShape(50)),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                ) {
                    Row {
                        onAdd?.let {
                            IconButton(onClick = it, modifier = Modifier.testTag("header-add-button")) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = stringResource(Res.string.action_add),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        onSearch?.let {
                            IconButton(onClick = it, modifier = Modifier.testTag("header-search-button")) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = stringResource(Res.string.action_search),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        title?.let {
            Text(
                it,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
        }
    }
}
