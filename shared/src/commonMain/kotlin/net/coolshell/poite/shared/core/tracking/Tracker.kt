package net.coolshell.poite.shared.core.tracking

/**
 * 曝光/行为埋点接口。业务代码只依赖此接口；
 * 当前为 NoOp 实现，接入 PostHog/Amplitude 后替换实现即可，业务代码零改动。
 */
interface Tracker {
    fun track(event: String, params: Map<String, Any?> = emptyMap())
}

class NoOpTracker : Tracker {
    override fun track(event: String, params: Map<String, Any?>) = Unit
}
