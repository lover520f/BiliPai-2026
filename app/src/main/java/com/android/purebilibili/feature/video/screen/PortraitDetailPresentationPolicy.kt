package com.android.purebilibili.feature.video.screen

import com.android.purebilibili.core.store.PortraitPlayerCollapseMode
import kotlin.math.max
import kotlin.math.min

internal data class PortraitInlinePlayerLayoutSpec(
    val widthDp: Float,
    val heightDp: Float
)

internal data class StandalonePortraitPagerMotionSpec(
    val enterDurationMillis: Int,
    val exitDurationMillis: Int,
    val exitScaleTarget: Float,
    val exitTranslateUpFraction: Float,
    val inlineReturnDurationMillis: Int,
    val inlineReturnInitialScale: Float
)

internal enum class PortraitFullscreenButtonAction {
    ENTER_PORTRAIT_FULLSCREEN
}

internal fun shouldUseOfficialInlinePortraitDetailExperience(
    useTabletLayout: Boolean,
    isVerticalVideo: Boolean,
    portraitExperienceEnabled: Boolean
): Boolean {
    return portraitExperienceEnabled && !useTabletLayout && isVerticalVideo
}

internal fun shouldUseSharedPlayerForPortraitFullscreen(): Boolean {
    return true
}

internal fun shouldShowStandalonePortraitPager(
    portraitExperienceEnabled: Boolean,
    isPortraitFullscreen: Boolean,
    useOfficialInlinePortraitDetailExperience: Boolean,
    hasPlayableState: Boolean
): Boolean {
    return portraitExperienceEnabled &&
        isPortraitFullscreen &&
        hasPlayableState
}

internal fun shouldActivatePortraitFullscreenState(
    portraitExperienceEnabled: Boolean
): Boolean {
    return portraitExperienceEnabled
}

internal fun resolveStandalonePortraitPagerMotionSpec(): StandalonePortraitPagerMotionSpec {
    return StandalonePortraitPagerMotionSpec(
        enterDurationMillis = 220,
        exitDurationMillis = 220,
        exitScaleTarget = 0.96f,
        exitTranslateUpFraction = 0.08f,
        inlineReturnDurationMillis = 240,
        inlineReturnInitialScale = 0.985f
    )
}

internal fun shouldEnableInlinePortraitScrollTransform(
    collapseMode: PortraitPlayerCollapseMode,
    selectedTabIndex: Int,
    isVerticalVideo: Boolean = true,
    isPlaybackPaused: Boolean = false
): Boolean {
    if (!collapseMode.enablesVideoOrientation(isVerticalVideo)) return false
    if (collapseMode == PortraitPlayerCollapseMode.PAUSED_ONLY && !isPlaybackPaused) return false
    return when (selectedTabIndex) {
        0 -> collapseMode.enablesIntro
        1 -> collapseMode.enablesComment
        else -> true
    }
}

internal fun shouldAnimateStandalonePortraitPager(useSharedPlayer: Boolean): Boolean {
    return true
}

internal fun resolvePortraitFullscreenButtonAction(
    useOfficialInlinePortraitDetailExperience: Boolean
): PortraitFullscreenButtonAction {
    return PortraitFullscreenButtonAction.ENTER_PORTRAIT_FULLSCREEN
}

internal fun shouldUseCompactInlinePortraitPlayerForCommentTab(
    useOfficialInlinePortraitDetailExperience: Boolean,
    selectedTabIndex: Int,
    isPortraitFullscreen: Boolean,
    isCommentThreadVisible: Boolean = false,
    collapseMode: PortraitPlayerCollapseMode = PortraitPlayerCollapseMode.BOTH,
    isVerticalVideo: Boolean = true,
    isPlaybackPaused: Boolean = false
): Boolean {
    if (!useOfficialInlinePortraitDetailExperience || isPortraitFullscreen) return false
    if (!collapseMode.enablesVideoOrientation(isVerticalVideo)) return false
    if (!collapseMode.enablesComment) return false
    if (collapseMode == PortraitPlayerCollapseMode.PAUSED_ONLY) return false
    if (isCommentThreadVisible) return true
    return selectedTabIndex == 1
}

internal fun shouldUseCompactInlinePortraitPlayerForIntroScroll(
    useOfficialInlinePortraitDetailExperience: Boolean,
    selectedTabIndex: Int,
    isPortraitFullscreen: Boolean,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    collapseMode: PortraitPlayerCollapseMode = PortraitPlayerCollapseMode.BOTH,
    isVerticalVideo: Boolean = true,
    isPlaybackPaused: Boolean = false,
    introScrollThresholdPx: Int = 56
): Boolean {
    if (!useOfficialInlinePortraitDetailExperience || isPortraitFullscreen) return false
    if (!collapseMode.enablesVideoOrientation(isVerticalVideo)) return false
    if (collapseMode == PortraitPlayerCollapseMode.PAUSED_ONLY && !isPlaybackPaused) return false
    if (!collapseMode.enablesIntro) return false
    if (selectedTabIndex != 0) return false
    return isVideoDetailIntroScrollPastCollapseThreshold(
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
        thresholdPx = introScrollThresholdPx
    )
}

internal fun isVideoDetailIntroScrollPastCollapseThreshold(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    thresholdPx: Int = 56
): Boolean {
    return firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset >= thresholdPx
}

internal fun resolveInlinePortraitPlayerCollapseProgress(
    manualCollapseProgress: Float,
    compactForCommentTabProgress: Float,
    restoreRequested: Boolean = false
): Float {
    if (restoreRequested) return 0f
    return manualCollapseProgress
        .coerceIn(0f, 1f)
        .coerceAtLeast(compactForCommentTabProgress.coerceIn(0f, 1f))
}

internal fun resolveInlinePortraitPlayerCommentCollapseDurationMillis(
    tabSwitchAnimationSpec: VideoContentTabSwitchAnimationSpec
): Int {
    return tabSwitchAnimationSpec.durationMs
}

/**
 * Inline portrait detail player size.
 *
 * PiliPlus parity for phone: expanded ≈ max(longestSide * 0.65, shortestSide),
 * so vertical videos get a tall preview without becoming full-screen cards.
 * Wide foldable portrait windows stay capped so intro/comment remain reachable.
 */
internal fun resolvePortraitInlinePlayerLayoutSpec(
    screenWidthDp: Float,
    screenHeightDp: Float,
    isCollapsed: Boolean
): PortraitInlinePlayerLayoutSpec {
    val width = screenWidthDp
    val collapsedHeight = screenWidthDp * 9f / 16f
    if (isCollapsed) {
        return PortraitInlinePlayerLayoutSpec(
            widthDp = width,
            heightDp = collapsedHeight
        )
    }

    val shortestSide = min(screenWidthDp, screenHeightDp)
    val longestSide = max(screenWidthDp, screenHeightDp)
    val isWidePortraitWindow = screenWidthDp >= 600f && screenHeightDp > screenWidthDp
    val expandedHeight = if (isWidePortraitWindow) {
        // 折叠屏内屏竖屏窗口不能按手机竖屏体验撑满首屏，否则详情区入口会被播放器挤出。
        min(max(screenHeightDp * 0.52f, collapsedHeight), screenWidthDp)
    } else {
        max(longestSide * 0.65f, shortestSide)
    }
    return PortraitInlinePlayerLayoutSpec(
        widthDp = width,
        heightDp = expandedHeight
    )
}
