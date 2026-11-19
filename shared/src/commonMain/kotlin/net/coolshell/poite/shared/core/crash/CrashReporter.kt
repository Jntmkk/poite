package net.coolshell.poite.shared.core.crash

/**
 * 崩溃/错误上报接口。业务代码只依赖此接口；
 * 当前为 NoOp 实现，接入 Sentry 后替换实现即可，业务代码零改动。
 */
interface CrashReporter {
    /** 上报非致命异常（捕获到的业务异常） */
    fun recordError(throwable: Throwable)

    /** 崩溃面包屑，帮助还原崩溃现场操作路径 */
    fun logBreadcrumb(message: String)

    fun setUserId(userId: String)
}

class NoOpCrashReporter : CrashReporter {
    override fun recordError(throwable: Throwable) = Unit
    override fun logBreadcrumb(message: String) = Unit
    override fun setUserId(userId: String) = Unit
}
