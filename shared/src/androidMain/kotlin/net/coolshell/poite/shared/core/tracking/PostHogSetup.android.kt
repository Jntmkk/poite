package net.coolshell.poite.shared.core.tracking

import android.app.Application
import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogContext

/** 必须在 Application.onCreate 调用（PostHogContext 需要 Application，不能用 Activity）。 */
fun initPostHog(application: Application) {
    PostHog.setup(config = postHogConfig(), context = PostHogContext(application))
}
