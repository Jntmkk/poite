package net.coolshell.poite.shared.di

import com.russhwolf.settings.NSUserDefaultsSettings
import net.coolshell.poite.shared.core.cache.KvStore
import net.coolshell.poite.shared.core.cache.SettingsKvStore
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule(): Module = module {
    single<KvStore> { SettingsKvStore(NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)) }
}
