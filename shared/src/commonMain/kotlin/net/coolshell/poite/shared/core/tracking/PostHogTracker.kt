package net.coolshell.poite.shared.core.tracking

import com.posthog.kmp.PostHog

class PostHogTracker : Tracker {
    override fun track(event: String, params: Map<String, Any?>) {
        val properties = params.mapNotNull { (key, value) -> value?.let { key to it } }.toMap()
        PostHog.capture(event, properties = properties)
    }
}
