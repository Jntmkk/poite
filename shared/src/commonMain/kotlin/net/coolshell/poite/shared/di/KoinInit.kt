package net.coolshell.poite.shared.di

import net.coolshell.poite.shared.core.cache.Cache4kMemoryCache
import net.coolshell.poite.shared.core.cache.MemoryCache
import net.coolshell.poite.shared.core.crash.CrashReporter
import net.coolshell.poite.shared.core.crash.SentryCrashReporter
import net.coolshell.poite.shared.core.crash.initializeSentry
import net.coolshell.poite.shared.core.logging.AppLogger
import net.coolshell.poite.shared.core.logging.KermitAppLogger
import net.coolshell.poite.shared.core.network.createHttpClient
import net.coolshell.poite.shared.core.network.mock.MockController
import net.coolshell.poite.shared.core.theme.ThemeController
import net.coolshell.poite.shared.feature.splash.di.splashModule
import net.coolshell.poite.shared.feature.theme.di.themeModule
import net.coolshell.poite.shared.core.tracking.PostHogTracker
import net.coolshell.poite.shared.core.tracking.Tracker
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    initializeSentry()
    return startKoin {
        appDeclaration()
        modules(platformModule(), coreModule, splashModule, themeModule)
    }
}

typealias KoinAppDeclaration = KoinApplication.() -> Unit

/** 平台相关绑定（Settings 的 Context 等），各自 source set 实现 */
expect fun platformModule(): Module

val coreModule = module {
    single<AppLogger> { KermitAppLogger() }
    single<MemoryCache> { Cache4kMemoryCache() }
    single<Tracker> { PostHogTracker() }
    single { createHttpClient() }
    single { ThemeController(get()) }
    single { MockController(get()) }
    single<CrashReporter> { SentryCrashReporter() }
}
