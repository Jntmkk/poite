package net.coolshell.poite.shared.core.crash

import io.sentry.kotlin.multiplatform.Sentry

/** 在 Application/App 启动最早期调用（经 initKoin 触发）。DSN 是公开标识符，可入库。 */
fun initializeSentry() {
    Sentry.init { options ->
        options.dsn = "https://c802729aabb62ea23b9ea9f57fd5465b@o4512457172582400.ingest.us.sentry.io/4512457221603328"
        options.sendDefaultPii = true
        options.tracesSampleRate = 1.0
    }
}
