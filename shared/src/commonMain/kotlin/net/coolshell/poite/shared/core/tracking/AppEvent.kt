package net.coolshell.poite.shared.core.tracking

/**
 * 全部埋点事件的唯一定义处。新增事件 = 加一个 sealed 子类；
 * 业务代码禁止裸字符串事件名（双端会跑偏）。name 用 snake_case
 */
sealed class AppEvent {
    abstract val name: String
    open val params: Map<String, Any> get() = emptyMap()

    /** 页面曝光（双端统一由 NavHost 发出） */
    data class ScreenView(val screenName: String) : AppEvent() {
        override val name = "screen_view"
        override val params = mapOf("screen_name" to screenName)
    }

    data class DemoTap(val count: Int) : AppEvent() {
        override val name = "demo_tap"
        override val params = mapOf("count" to count)
    }

    data class HeaderAction(val action: String) : AppEvent() {
        override val name = "header_action"
        override val params = mapOf("action" to action)
    }

    data class ThemeModeChanged(val mode: String) : AppEvent() {
        override val name = "theme_mode_changed"
        override val params = mapOf("mode" to mode)
    }

    data class ThemePackChanged(val packId: String) : AppEvent() {
        override val name = "theme_pack_changed"
        override val params = mapOf("pack_id" to packId)
    }
}
