package net.coolshell.poite.shared.feature.splash.di

import net.coolshell.poite.shared.feature.splash.data.SplashApi
import net.coolshell.poite.shared.feature.splash.data.SplashApiImpl
import net.coolshell.poite.shared.feature.splash.data.SplashRepository
import org.koin.dsl.module

val splashModule = module {
    single<SplashApi> { SplashApiImpl(get()) }
    single { SplashRepository(get(), get(), get(), get()) }
}
