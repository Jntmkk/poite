package net.coolshell.poite.shared

import net.coolshell.poite.shared.core.tracking.AppEvent
import net.coolshell.poite.shared.core.tracking.Tracker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 测试用 Fake：记录事件与属性，不碰任何 SDK */
class FakeTracker : Tracker {
    val events = mutableListOf<AppEvent>()
    val userProperties = mutableMapOf<String, String>()
    val superProperties = mutableMapOf<String, Any>()
    var recordedUserId: String? = null

    override fun track(event: AppEvent) { events.add(event) }
    override fun setUserId(userId: String?) { recordedUserId = userId }
    override fun setUserProperty(key: String, value: String) { userProperties[key] = value }
    override fun setSuperProperty(key: String, value: Any) { superProperties[key] = value }
}

class TrackingTest {
    @Test
    fun eventNamesAreSnakeCaseWithParams() {
        val event = AppEvent.DemoTap(count = 42)
        assertEquals("demo_tap", event.name)
        assertEquals(mapOf("count" to 42), event.params)
        assertEquals("screen_view", AppEvent.ScreenView("HomeRoute").name)
    }

    @Test
    fun fakeRecordsEventsAndProperties() {
        val tracker = FakeTracker()
        tracker.track(AppEvent.DemoTap(1))
        tracker.setUserId("user_1")
        tracker.setSuperProperty("theme", "maltese")

        assertTrue(tracker.events.any { it is AppEvent.DemoTap })
        assertEquals("user_1", tracker.recordedUserId)
        assertEquals("maltese", tracker.superProperties["theme"])

        tracker.setUserId(null)
        assertEquals(null, tracker.recordedUserId)
    }
}
