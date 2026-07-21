package com.android.purebilibili.feature.video.ui.pager

import com.android.purebilibili.data.model.response.Owner
import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.Stat
import com.android.purebilibili.data.model.response.UgcEpisode
import com.android.purebilibili.data.model.response.ViewInfo

/**
 * Follow-up segments for multi-P and UGC season inside portrait immersive.
 * These are injected after the current video so swipe / auto-continue stays in-collection.
 */
internal fun resolvePortraitCollectionFollowUps(
    info: ViewInfo,
    currentCid: Long = info.cid
): List<RelatedVideo> {
    val multiPageFollowUps = resolvePortraitMultiPageFollowUps(
        info = info,
        currentCid = currentCid
    )
    if (multiPageFollowUps.isNotEmpty()) {
        return multiPageFollowUps
    }
    return resolvePortraitSeasonFollowUps(
        info = info,
        currentCid = currentCid
    )
}

internal fun resolvePortraitMultiPageFollowUps(
    info: ViewInfo,
    currentCid: Long = info.cid
): List<RelatedVideo> {
    val pages = info.pages.filter { it.cid > 0L }
    if (pages.size <= 1) return emptyList()

    val currentIndex = pages.indexOfFirst { it.cid == currentCid }
        .takeIf { it >= 0 }
        ?: pages.indexOfFirst { it.cid == info.cid }.coerceAtLeast(0)

    return pages.drop(currentIndex + 1).map { page ->
        RelatedVideo(
            aid = info.aid,
            bvid = info.bvid,
            cid = page.cid,
            title = page.part.ifBlank { "P${page.page.coerceAtLeast(1)}" },
            pic = info.pic,
            owner = info.owner,
            stat = info.stat,
            duration = page.duration.toInt().coerceAtLeast(0)
        )
    }
}

internal fun resolvePortraitSeasonFollowUps(
    info: ViewInfo,
    currentCid: Long = info.cid
): List<RelatedVideo> {
    val season = info.ugc_season ?: return emptyList()
    val episodes = season.sections.flatMap { section -> section.episodes }
        .filter { episode ->
            episode.cid > 0L || episode.bvid.isNotBlank() || episode.aid > 0L
        }
    if (episodes.size <= 1) return emptyList()

    val currentIndex = resolvePortraitSeasonEpisodeIndex(
        episodes = episodes,
        currentBvid = info.bvid,
        currentCid = currentCid.takeIf { it > 0L } ?: info.cid
    )
    if (currentIndex < 0) return emptyList()

    return episodes.drop(currentIndex + 1).map { episode ->
        toRelatedVideoFromUgcEpisode(
            episode = episode,
            fallbackOwner = info.owner,
            fallbackCover = season.cover.ifBlank { info.pic }
        )
    }
}

internal fun resolvePortraitSeasonEpisodeIndex(
    episodes: List<UgcEpisode>,
    currentBvid: String,
    currentCid: Long
): Int {
    val normalizedBvid = currentBvid.trim()
    if (currentCid > 0L) {
        val byCid = episodes.indexOfFirst { it.cid == currentCid }
        if (byCid >= 0) return byCid
    }
    if (normalizedBvid.isNotEmpty()) {
        val byBvid = episodes.indexOfFirst { it.bvid.trim() == normalizedBvid }
        if (byBvid >= 0) return byBvid
    }
    return -1
}

/**
 * Whether auto-continue (CONTINUE_CURRENT_LOGIC) should advance to [nextItem].
 * True for multi-P (same bvid) or season episode follow-ups; false for plain related feed.
 */
internal fun shouldPortraitAutoContinueToNextItem(
    currentItem: Any?,
    nextItem: Any?,
    currentLoadedInfo: ViewInfo? = null
): Boolean {
    val current = currentItem?.let(::resolvePortraitPagePlaybackIdentity) ?: return false
    val next = nextItem?.let(::resolvePortraitPagePlaybackIdentity) ?: return false
    if (current.bvid.isBlank() || next.bvid.isBlank()) return false

    // Multi-P: same bvid, different cid.
    if (current.bvid == next.bvid && next.cid > 0L && next.cid != current.cid) {
        return true
    }

    // Season: next episode listed in current video's ugc_season.
    val season = currentLoadedInfo?.ugc_season ?: return false
    if (currentLoadedInfo.bvid.trim() != current.bvid) return false
    val episodes = season.sections.flatMap { it.episodes }
    return episodes.any { episode ->
        val episodeBvid = episode.bvid.trim().ifBlank {
            if (episode.aid > 0L) "av${episode.aid}" else ""
        }
        episodeBvid == next.bvid || (next.cid > 0L && episode.cid == next.cid)
    }
}

/**
 * Insert collection follow-ups immediately after [currentPage], before unrelated feed items.
 * Skips bvids/cids already present later in the list.
 */
internal fun resolvePortraitCollectionInjectionPlan(
    pageItems: List<Any>,
    currentPage: Int,
    followUps: List<RelatedVideo>
): List<RelatedVideo> {
    if (followUps.isEmpty()) return emptyList()
    if (currentPage !in pageItems.indices) return emptyList()

    val existingKeys = pageItems.mapNotNull { item ->
        resolvePortraitPagePlaybackIdentity(item)?.let { identity ->
            portraitCollectionIdentityKey(identity.bvid, identity.cid)
        }
    }.toMutableSet()

    return followUps.filter { candidate ->
        val bvid = candidate.bvid.trim()
        if (bvid.isEmpty()) return@filter false
        val key = portraitCollectionIdentityKey(bvid, candidate.cid)
        if (key in existingKeys) return@filter false
        existingKeys += key
        true
    }
}

internal fun portraitCollectionIdentityKey(bvid: String, cid: Long): String {
    val normalized = bvid.trim()
    return if (cid > 0L) "$normalized#$cid" else normalized
}

/**
 * Find a pager index for a multi-P / season selection from the detail sheet.
 * Prefer exact bvid+cid; fall back to bvid-only when cid is unknown.
 */
internal fun resolvePortraitCollectionPageIndex(
    pageItems: List<Any>,
    targetBvid: String,
    targetCid: Long
): Int {
    val normalizedBvid = targetBvid.trim()
    if (normalizedBvid.isEmpty()) return -1
    if (targetCid > 0L) {
        val exact = pageItems.indexOfFirst { candidate ->
            val identity = resolvePortraitPagePlaybackIdentity(candidate) ?: return@indexOfFirst false
            identity.bvid == normalizedBvid && identity.cid == targetCid
        }
        if (exact >= 0) return exact
    }
    return pageItems.indexOfFirst { candidate ->
        val identity = resolvePortraitPagePlaybackIdentity(candidate) ?: return@indexOfFirst false
        identity.bvid == normalizedBvid
    }
}

private fun toRelatedVideoFromUgcEpisode(
    episode: UgcEpisode,
    fallbackOwner: Owner,
    fallbackCover: String
): RelatedVideo {
    val bvid = episode.bvid.trim().ifBlank {
        if (episode.aid > 0L) "av${episode.aid}" else ""
    }
    val title = episode.title.ifBlank { episode.arc?.title.orEmpty() }
    val cover = episode.arc?.pic?.takeIf { it.isNotBlank() } ?: fallbackCover
    val duration = episode.arc?.duration?.coerceAtLeast(0) ?: 0
    val stat = episode.arc?.stat ?: Stat()
    return RelatedVideo(
        aid = episode.aid.takeIf { it > 0L } ?: episode.arc?.aid ?: 0L,
        bvid = bvid,
        cid = episode.cid,
        title = title,
        pic = cover,
        owner = fallbackOwner,
        stat = stat,
        duration = duration
    )
}
