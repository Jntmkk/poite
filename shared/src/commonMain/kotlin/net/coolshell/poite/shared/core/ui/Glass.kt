package net.coolshell.poite.shared.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials

/** 全局背景模糊源，AppNavHost 根部 provide，胶囊组件消费 */
val LocalHazeState = compositionLocalOf { HazeState() }

/** Liquid Glass 风格：裁形 + 对背后内容做模糊 + 超薄半透明材质 */
@Composable
fun Modifier.glass(shape: Shape): Modifier =
    this.clip(shape).hazeEffect(LocalHazeState.current, style = HazeMaterials.ultraThin())
