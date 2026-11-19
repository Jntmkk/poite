package net.coolshell.poite.shared.core.tracking

import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogContext

fun initPostHogIos() {
    PostHog.setup(config = postHogConfig(), context = PostHogContext())
}
