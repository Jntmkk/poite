package net.coolshell.poite.shared.core.tracking

import com.posthog.kmp.PostHogConfig

/** projectToken 是公开客户端标识（仅能写入事件），可入库。debug 在发布前关掉。 */
internal fun postHogConfig() = PostHogConfig(
    projectToken = "phc_ykCnZZ3CvfhZoEdYnUiSQwu8y2uyvBvADRWfLiuZ6xj5",
    host = PostHogConfig.HOST_US,
    debug = true,
    captureScreenViews = false,
)
