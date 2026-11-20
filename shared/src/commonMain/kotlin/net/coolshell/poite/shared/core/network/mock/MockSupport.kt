package net.coolshell.poite.shared.core.network.mock

/** Mock 能力仅 debug 构建可用（Android 读 FLAG_DEBUGGABLE，iOS 读 isDebugBinary） */
expect val mockAvailable: Boolean
