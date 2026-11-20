package net.coolshell.poite.shared.core.network.mock

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.mp.KoinPlatform

actual val mockAvailable: Boolean
    get() = (KoinPlatform.getKoin().get<Context>().applicationInfo.flags and
        ApplicationInfo.FLAG_DEBUGGABLE) != 0
