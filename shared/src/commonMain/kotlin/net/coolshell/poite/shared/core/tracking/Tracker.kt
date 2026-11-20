package net.coolshell.poite.shared.core.tracking

/**
 * 埋点接口。业务代码只依赖此接口 + AppEvent；换 Provider 只换 Koin 绑定。
 * 事件在事件回调/ViewModel 里发，不要在 Composable 组合体里发（重组会双发）
 */
interface Tracker {
    fun track(event: AppEvent)

    /** 登录后设置稳定用户 ID；登出传 null（会 reset 匿名身份与 super properties） */
    fun setUserId(userId: String?)
    fun setUserProperty(key: String, value: String)

    /** 公共属性：之后每个事件都携带（PostHog register） */
    fun setSuperProperty(key: String, value: Any)
}

class NoOpTracker : Tracker {
    override fun track(event: AppEvent) = Unit
    override fun setUserId(userId: String?) = Unit
    override fun setUserProperty(key: String, value: String) = Unit
    override fun setSuperProperty(key: String, value: Any) = Unit
}
