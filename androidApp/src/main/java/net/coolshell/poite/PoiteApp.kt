package net.coolshell.poite

import android.app.Application
import net.coolshell.poite.shared.core.tracking.initPostHog
import net.coolshell.poite.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class PoiteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initPostHog(this@PoiteApp)
        initKoin {
            androidContext(this@PoiteApp)
        }
    }
}
