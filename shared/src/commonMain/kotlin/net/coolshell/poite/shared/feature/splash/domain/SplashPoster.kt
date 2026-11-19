package net.coolshell.poite.shared.feature.splash.domain

import kotlinx.serialization.Serializable

/** 启动海报。startAt/endAt 为 epoch 毫秒，[startAt, endAt) 区间；localPath 为空表示未下载 */
@Serializable
data class SplashPoster(
    val id: String,
    val imageUrl: String,
    val startAt: Long,
    val endAt: Long,
    val priority: Int = 0,
    val localPath: String = "",
)

@Serializable
data class SplashConfig(val posters: List<SplashPoster> = emptyList())

/** 只选「已下载且当前生效」的，取优先级最高 */
fun selectActivePoster(posters: List<SplashPoster>, now: Long): SplashPoster? =
    posters.filter { it.localPath.isNotEmpty() && now >= it.startAt && now < it.endAt }
        .maxByOrNull { it.priority }
