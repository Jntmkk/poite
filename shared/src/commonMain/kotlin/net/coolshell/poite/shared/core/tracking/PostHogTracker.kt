package net.coolshell.poite.shared.core.tracking

import com.posthog.kmp.PostHog

class PostHogTracker : Tracker {
    override fun track(event: AppEvent) {
        PostHog.capture(event.name, properties = event.params)
    }

    override fun setUserId(userId: String?) {
        if (userId == null) PostHog.reset() else PostHog.identify(userId)
    }

    override fun setUserProperty(key: String, value: String) {
        PostHog.setPersonProperties(userPropertiesToSet = mapOf(key to value))
    }

    override fun setSuperProperty(key: String, value: Any) {
        PostHog.register(key, value)
    }
}
