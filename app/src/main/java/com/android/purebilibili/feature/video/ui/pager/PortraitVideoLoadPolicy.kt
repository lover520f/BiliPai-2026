package com.android.purebilibili.feature.video.ui.pager

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.player.PlaybackMediaCache
import com.android.purebilibili.core.util.MediaUtils
import com.android.purebilibili.data.model.response.DashAudio
import com.android.purebilibili.data.model.response.DashVideo
import com.android.purebilibili.data.model.response.PlayUrlData
import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.ViewInfo
import com.android.purebilibili.data.model.response.getBestAudio
import com.android.purebilibili.data.model.response.getBestVideo
import com.android.purebilibili.feature.plugin.PlaybackCdnPlugin
import com.android.purebilibili.feature.video.viewmodel.buildPlaybackAudioUrlCandidates
import kotlin.math.min

/**
 * Fallback when no user preference is available (e.g. unit tests / cold path).
 * Live portrait playback should pass the detail-page playable default quality instead.
 */
internal const val PORTRAIT_PLAYBACK_TARGET_QUALITY = 64
internal const val PORTRAIT_SWIPE_PREFETCH_OFFSET_THRESHOLD = 0.25f
internal const val PORTRAIT_EARLY_PLAYBACK_OFFSET_THRESHOLD = 0.58f

internal data class PortraitPagePlaybackIdentity(
    val bvid: String,
    val aid: Long,
    val cid: Long
)

internal data class PortraitPlaybackStreamUrls(
    val videoUrl: String,
    val audioUrl: String?
)

/**
 * Resolve the playurl qn for portrait pager / Story.
 *
 * Prefer the same playable default used by video detail so Wi‑Fi 1080P / VIP 4K-HDR
 * settings are honored. Cap invalid values to the safe fallback.
 */
internal fun resolvePortraitPlaybackTargetQuality(
    preferredQuality: Int? = null
): Int {
    val quality = preferredQuality ?: return PORTRAIT_PLAYBACK_TARGET_QUALITY
    return quality.takeIf { it > 0 } ?: PORTRAIT_PLAYBACK_TARGET_QUALITY
}

/**
 * Short label for the portrait chrome quality chip.
 * Auto-highest (marker ≥ 127) stays generic because actual track is per-video.
 */
internal fun resolvePortraitQualityLabel(qualityId: Int): String {
    return when {
        qualityId >= 127 -> "自动"
        qualityId >= 126 -> "杜比"
        qualityId >= 125 -> "HDR"
        qualityId >= 120 -> "4K"
        qualityId >= 116 -> "1080P60"
        qualityId >= 112 -> "1080P+"
        qualityId >= 80 -> "1080P"
        qualityId >= 74 -> "720P60"
        qualityId >= 64 -> "720P"
        qualityId >= 32 -> "480P"
        qualityId >= 16 -> "360P"
        else -> "高清"
    }
}

/**
 * Qualities shown in the portrait quality menu.
 * Prefer DASH track ids (switchable now); union accept_quality so higher tiers remain requestable.
 */
internal fun resolvePortraitAvailableQualityIds(
    acceptQualities: List<Int>,
    dashVideoIds: List<Int>
): List<Int> {
    return (dashVideoIds + acceptQualities)
        .filter { it > 0 }
        .distinct()
        .sortedDescending()
}

internal fun resolvePortraitQualityMenuLabels(qualityIds: List<Int>): List<String> {
    return qualityIds.map(::resolvePortraitQualityLabel)
}

/**
 * After a quality switch or reload, pick the label to show from the actual track when possible.
 */
internal fun resolvePortraitDisplayedQualityId(
    requestedQuality: Int,
    returnedQuality: Int,
    dashVideoIds: List<Int>
): Int {
    if (requestedQuality > 0 && requestedQuality in dashVideoIds) return requestedQuality
    if (returnedQuality > 0) return returnedQuality
    return dashVideoIds.maxOrNull() ?: requestedQuality.takeIf { it > 0 } ?: PORTRAIT_PLAYBACK_TARGET_QUALITY
}

internal fun shouldUsePortraitParallelPlaybackBootstrap(
    bvid: String,
    requestedCid: Long
): Boolean = bvid.trim().startsWith("BV", ignoreCase = true) && requestedCid > 0L

internal fun resolvePortraitPagePlaybackIdentity(item: Any): PortraitPagePlaybackIdentity? {
    return when (item) {
        is ViewInfo -> {
            val bvid = item.bvid.trim()
            if (bvid.isEmpty()) null
            else PortraitPagePlaybackIdentity(
                bvid = bvid,
                aid = item.aid,
                cid = item.cid
            )
        }

        is RelatedVideo -> {
            val bvid = item.bvid.trim()
            if (bvid.isEmpty()) null
            else PortraitPagePlaybackIdentity(
                bvid = bvid,
                aid = item.aid,
                cid = item.cid
            )
        }

        else -> null
    }
}

internal fun resolvePortraitPlaybackStreamUrls(
    playData: PlayUrlData,
    targetQuality: Int = PORTRAIT_PLAYBACK_TARGET_QUALITY,
    isHevcSupported: Boolean = MediaUtils.isHevcSupported(),
    isAv1Supported: Boolean = MediaUtils.isAv1Supported()
): PortraitPlaybackStreamUrls? {
    val dash = playData.dash
    if (dash != null) {
        val dashVideo = dash.getBestVideo(
            targetQn = targetQuality,
            isHevcSupported = isHevcSupported,
            isAv1Supported = isAv1Supported
        )
        val dashAudio = dash.getBestAudio()
        val videoUrl = dashVideo?.getValidUrl()?.takeIf { it.isNotEmpty() }
            ?: playData.durl?.firstOrNull()?.url?.takeIf { it.isNotEmpty() }
        if (videoUrl.isNullOrEmpty()) return null
        val audioUrl = dashAudio?.getValidUrl()?.takeIf { it.isNotEmpty() }
        return PortraitPlaybackStreamUrls(
            videoUrl = videoUrl,
            audioUrl = audioUrl
        )
    }

    val progressiveUrl = playData.durl?.firstOrNull()?.url?.takeIf { it.isNotEmpty() }
        ?: return null
    return PortraitPlaybackStreamUrls(
        videoUrl = progressiveUrl,
        audioUrl = null
    )
}

internal fun resolvePortraitPlaybackCdnUrls(
    streamUrls: PortraitPlaybackStreamUrls,
    cachedDashVideos: List<DashVideo>,
    cachedDashAudios: List<DashAudio>,
    targetQuality: Int = PORTRAIT_PLAYBACK_TARGET_QUALITY,
    cdnPlugin: PlaybackCdnPlugin?
): PortraitPlaybackStreamUrls {
    val rawVideoUrls = buildList {
        add(streamUrls.videoUrl)
        cachedDashVideos
            .find { it.id == targetQuality }
            ?.backupUrl
            .orEmpty()
            .filter { it.isNotBlank() }
            .let(::addAll)
    }.distinct()

    val rawAudioUrls = buildPlaybackAudioUrlCandidates(
        audioUrl = streamUrls.audioUrl,
        cachedDashAudios = cachedDashAudios
    )
    val rewrite = cdnPlugin?.rewritePlaybackCandidates(rawVideoUrls, rawAudioUrls)
    return PortraitPlaybackStreamUrls(
        videoUrl = rewrite?.videoUrls?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: streamUrls.videoUrl,
        audioUrl = rewrite?.audioUrls?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: streamUrls.audioUrl
    )
}

internal fun resolvePortraitSwipePrefetchTargetPage(
    isScrollInProgress: Boolean,
    currentPage: Int,
    currentPageOffsetFraction: Float,
    lastPageIndex: Int,
    prefetchThreshold: Float = PORTRAIT_SWIPE_PREFETCH_OFFSET_THRESHOLD
): Int? {
    if (!isScrollInProgress) return null
    return when {
        currentPageOffsetFraction <= -prefetchThreshold -> {
            val targetPage = currentPage + 1
            targetPage.takeIf { it <= lastPageIndex }
        }

        currentPageOffsetFraction >= prefetchThreshold -> {
            val targetPage = currentPage - 1
            targetPage.takeIf { it >= 0 }
        }

        else -> null
    }
}

internal fun resolvePortraitEarlyPlaybackPage(
    isScrollInProgress: Boolean,
    currentPage: Int,
    currentPageOffsetFraction: Float,
    lastPageIndex: Int,
    earlyPlaybackThreshold: Float = PORTRAIT_EARLY_PLAYBACK_OFFSET_THRESHOLD
): Int? {
    if (!isScrollInProgress) return null
    return when {
        currentPageOffsetFraction <= -earlyPlaybackThreshold -> {
            val targetPage = currentPage + 1
            targetPage.takeIf { it <= lastPageIndex }
        }

        currentPageOffsetFraction >= earlyPlaybackThreshold -> {
            val targetPage = currentPage - 1
            targetPage.takeIf { it >= 0 }
        }

        else -> null
    }
}

internal fun resolvePortraitPlayUrlPreloadCount(
    prefetchVideoEnabled: Boolean,
    isWifi: Boolean,
    availableTargets: Int
): Int {
    if (availableTargets <= 0 || !isWifi) return 0
    val maxCount = if (prefetchVideoEnabled) 2 else 1
    return min(availableTargets, maxCount)
}

internal fun resolvePortraitPlayUrlPreloadTargets(
    committedPage: Int,
    pageItems: List<Any>,
    preloadCount: Int
): List<PortraitPagePlaybackIdentity> {
    if (preloadCount <= 0 || pageItems.isEmpty()) return emptyList()
    val targets = mutableListOf<PortraitPagePlaybackIdentity>()
    var pageIndex = committedPage + 1
    while (targets.size < preloadCount && pageIndex < pageItems.size) {
        resolvePortraitPagePlaybackIdentity(pageItems[pageIndex])?.let { identity ->
            if (targets.none { it.bvid == identity.bvid }) {
                targets += identity
            }
        }
        pageIndex += 1
    }
    return targets
}

internal fun buildPortraitPlaybackHttpHeaders(): Map<String, String> {
    return mapOf(
        "Referer" to "https://www.bilibili.com",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    )
}

@UnstableApi
internal fun buildPortraitCachedMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
    val headers = buildPortraitPlaybackHttpHeaders()
    val upstreamFactory = OkHttpDataSource.Factory(NetworkModule.playbackOkHttpClient)
        .setDefaultRequestProperties(headers)
    val dataSourceFactory: DataSource.Factory =
        PlaybackMediaCache.buildCachedDataSourceFactory(context, upstreamFactory)
    return DefaultMediaSourceFactory(context)
        .setDataSourceFactory(dataSourceFactory)
}
