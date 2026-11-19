package net.coolshell.poite.shared.core.crash

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.User

class SentryCrashReporter : CrashReporter {

    override fun recordError(throwable: Throwable) {
        Sentry.captureException(throwable)
    }

    override fun logBreadcrumb(message: String) {
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                this.message = message
                level = SentryLevel.INFO
            }
        )
    }

    override fun setUserId(userId: String) {
        Sentry.setUser(User().apply { id = userId })
    }
}
