package net.coolshell.poite.shared.core.network.mock

import kotlin.native.Platform

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual val mockAvailable: Boolean = Platform.isDebugBinary
