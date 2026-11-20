package net.coolshell.poite.shared.feature.theme.di

import net.coolshell.poite.shared.feature.theme.data.ThemeApi
import net.coolshell.poite.shared.feature.theme.data.ThemeApiImpl
import net.coolshell.poite.shared.feature.theme.data.ThemePackRepository
import org.koin.dsl.module

val themeModule = module {
    single<ThemeApi> { ThemeApiImpl(get()) }
    single { ThemePackRepository(get(), get(), get(), get(), get()) }
}
