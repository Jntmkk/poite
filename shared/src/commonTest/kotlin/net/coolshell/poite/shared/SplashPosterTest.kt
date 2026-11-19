package net.coolshell.poite.shared

import kotlinx.serialization.json.Json
import net.coolshell.poite.shared.feature.splash.domain.SplashPoster
import net.coolshell.poite.shared.feature.splash.domain.selectActivePoster
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SplashPosterTest {
    private val now = 1_800_000_000_000L

    private fun poster(
        id: String,
        priority: Int = 0,
        startAt: Long = now - 1000,
        endAt: Long = now + 1000,
        downloaded: Boolean = true,
    ) = SplashPoster(
        id = id,
        imageUrl = "https://example.com/$id.png",
        startAt = startAt,
        endAt = endAt,
        priority = priority,
        localPath = if (downloaded) "/tmp/$id.img" else "",
    )

    @Test
    fun emptyListReturnsNull() {
        assertNull(selectActivePoster(emptyList(), now))
    }

    @Test
    fun filtersByTimeRange() {
        val notStarted = poster("future", startAt = now + 10_000, endAt = now + 20_000)
        val expired = poster("past", startAt = now - 20_000, endAt = now - 10_000)
        val active = poster("now")
        assertEquals("now", selectActivePoster(listOf(notStarted, expired, active), now)?.id)
    }

    @Test
    fun filtersOutNotDownloaded() {
        assertNull(selectActivePoster(listOf(poster("a", downloaded = false)), now))
    }

    @Test
    fun highestPriorityWins() {
        val low = poster("low", priority = 1)
        val high = poster("high", priority = 10)
        val mid = poster("mid", priority = 5)
        assertEquals("high", selectActivePoster(listOf(low, high, mid), now)?.id)
    }

    @Test
    fun startInclusiveEndExclusive() {
        val p = poster("edge")
        assertEquals("edge", selectActivePoster(listOf(p), p.startAt)?.id)
        assertNull(selectActivePoster(listOf(p), p.endAt))
    }

    @Test
    fun jsonRoundTrip() {
        val json = Json { ignoreUnknownKeys = true }
        val original = listOf(poster("a", priority = 3), poster("b", downloaded = false))
        val restored = json.decodeFromString<List<SplashPoster>>(json.encodeToString(original))
        assertEquals(original, restored)
    }
}
