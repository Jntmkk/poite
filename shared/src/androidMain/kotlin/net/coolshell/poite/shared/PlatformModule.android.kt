package net.coolshell.poite.shared.di

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import net.coolshell.poite.shared.core.cache.KvStore
import net.coolshell.poite.shared.core.cache.SettingsKvStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<KvStore> {
        SettingsKvStore(
            SharedPreferencesSettings(
                androidContext().getSharedPreferences("poite", Context.MODE_PRIVATE)
            )
        )
    }
}
