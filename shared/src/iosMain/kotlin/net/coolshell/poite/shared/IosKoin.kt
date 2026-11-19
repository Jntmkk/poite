package net.coolshell.poite.shared

import net.coolshell.poite.shared.core.tracking.initPostHogIos
import net.coolshell.poite.shared.di.initKoin

/** Swift 侧入口（顶层函数带 Kt 前缀：IosKoinKt.startKoinIos()）。名字别用 init 开头，K/N 会加 do 前缀 */
fun startKoinIos() {
    initPostHogIos()
    initKoin()
}
