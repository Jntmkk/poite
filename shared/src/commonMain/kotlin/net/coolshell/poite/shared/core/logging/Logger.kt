package net.coolshell.poite.shared.core.logging

/** 业务代码只依赖此接口，实现可换（Kermit / Sentry 日志转发等）。禁止记敏感数据。 */
interface AppLogger {
    fun v(tag: String, message: String)
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
