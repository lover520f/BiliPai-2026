package com.android.purebilibili.core.ui.transition

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

internal enum class VideoSharedTransitionProfile {
    COVER_ONLY,
    COVER_AND_METADATA
}

internal const val VIDEO_SHARED_COVER_ASPECT_RATIO = 16f / 10f
private const val HOME_SOURCE_ROUTE = "home"
private const val HOME_SHARED_TRANSITION_DURATION_MILLIS = 360
private const val HOME_DETAIL_REVEAL_DELAY_MILLIS = 40
private const val HOME_DETAIL_REVEAL_DURATION_MILLIS = 220
private const val HOME_DETAIL_REVEAL_SLIDE_OFFSET_DP = 14
private const val HOME_DETAIL_REVEAL_INITIAL_SCALE = 0.985f
private const val HOME_SHARED_TRANSITION_CARD_CORNER_DP = 16
private const val HOME_SHARED_TRANSITION_PLAYER_CORNER_DP = 12
private const val VIDEO_CARD_RETURN_REBOUND_START_SCALE = 0.984f
private const val VIDEO_CARD_RETURN_REBOUND_TRANSLATION_Y_DP = 2.25f
private const val VIDEO_CARD_RETURN_REBOUND_DAMPING_RATIO = 0.64f
private const val VIDEO_CARD_RETURN_REBOUND_STIFFNESS = 520f
private val VIDEO_CARD_IOS_LIKE_EASE_OUT = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

internal data class VideoSharedTransitionOwnership(
    val useCoverSharedBounds: Boolean,
    val useMetadataSharedBounds: Boolean
)

internal data class VideoDetailContentRevealMotion(
    val enabled: Boolean,
    val delayMillis: Int,
    val durationMillis: Int,
    val slideOffsetDp: Int,
    val initialScale: Float
)

internal data class VideoSharedTransitionMotionSpec(
    val enabled: Boolean,
    val durationMillis: Int,
    val contentDelayMillis: Int,
    val contentDurationMillis: Int,
    val contentSlideOffsetDp: Int,
    val contentInitialScale: Float,
    val easing: Easing
)

internal data class VideoSharedCornerSpec(
    val enabled: Boolean,
    val startCornerDp: Int,
    val endCornerDp: Int
)

internal data class VideoCardReturnReboundSpec(
    val enabled: Boolean,
    val durationMillis: Int,
    val startScale: Float,
    val startTranslationYDp: Float,
    val dampingRatio: Float,
    val stiffness: Float,
    val easing: Easing
)

internal fun resolveVideoSharedTransitionProfile(): VideoSharedTransitionProfile {
    return VideoSharedTransitionProfile.COVER_AND_METADATA
}

internal fun resolveVideoCardSharedTransitionEasing(): Easing {
    return VIDEO_CARD_IOS_LIKE_EASE_OUT
}

private fun resolveVideoSharedTransitionProfile(sourceRoute: String?): VideoSharedTransitionProfile {
    return if (sourceRoute?.substringBefore("?") == HOME_SOURCE_ROUTE) {
        VideoSharedTransitionProfile.COVER_ONLY
    } else {
        VideoSharedTransitionProfile.COVER_AND_METADATA
    }
}

internal fun shouldEnableVideoCoverSharedTransition(
    transitionEnabled: Boolean,
    hasSharedTransitionScope: Boolean,
    hasAnimatedVisibilityScope: Boolean
): Boolean {
    return transitionEnabled &&
        hasSharedTransitionScope &&
        hasAnimatedVisibilityScope
}

internal fun shouldEnableVideoMetadataSharedTransition(
    coverSharedEnabled: Boolean,
    isQuickReturnLimited: Boolean,
    useCardContainerSharedBounds: Boolean = false,
    profile: VideoSharedTransitionProfile = resolveVideoSharedTransitionProfile()
): Boolean {
    if (!coverSharedEnabled) return false
    // 卡片容器已经承载整体放大/回收时，标题、UP、统计等不要再各自抢独立 sharedBounds。
    if (useCardContainerSharedBounds) return false
    // Keep metadata linked during quick return to avoid cover-only snapback.
    if (isQuickReturnLimited && profile == VideoSharedTransitionProfile.COVER_ONLY) return false
    return profile == VideoSharedTransitionProfile.COVER_AND_METADATA
}

internal fun resolveVideoSharedTransitionOwnership(
    sourceRoute: String?,
    coverSharedEnabled: Boolean,
    isQuickReturnLimited: Boolean
): VideoSharedTransitionOwnership {
    if (!coverSharedEnabled) {
        return VideoSharedTransitionOwnership(
            useCoverSharedBounds = false,
            useMetadataSharedBounds = false
        )
    }

    val isHomeSource = sourceRoute?.substringBefore("?") == HOME_SOURCE_ROUTE
    return VideoSharedTransitionOwnership(
        useCoverSharedBounds = true,
        // 首页进入详情时封面是主锚点，元数据跟随普通内容入场，避免多个 sharedBounds 抢焦点。
        useMetadataSharedBounds = !isHomeSource &&
            shouldEnableVideoMetadataSharedTransition(
                coverSharedEnabled = true,
                isQuickReturnLimited = isQuickReturnLimited,
                profile = resolveVideoSharedTransitionProfile(sourceRoute)
            )
    )
}

internal fun resolveHomeVideoSharedTransitionMotionSpec(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoSharedTransitionMotionSpec {
    val enabled = transitionEnabled &&
        !sourceRoute?.substringBefore("?").isNullOrBlank()
    if (!enabled) {
        return VideoSharedTransitionMotionSpec(
            enabled = false,
            durationMillis = 0,
            contentDelayMillis = 0,
            contentDurationMillis = 0,
            contentSlideOffsetDp = 0,
            contentInitialScale = 1f,
            easing = VIDEO_CARD_IOS_LIKE_EASE_OUT
        )
    }

    return VideoSharedTransitionMotionSpec(
        enabled = true,
        durationMillis = HOME_SHARED_TRANSITION_DURATION_MILLIS,
        contentDelayMillis = HOME_DETAIL_REVEAL_DELAY_MILLIS,
        contentDurationMillis = HOME_DETAIL_REVEAL_DURATION_MILLIS,
        contentSlideOffsetDp = HOME_DETAIL_REVEAL_SLIDE_OFFSET_DP,
        contentInitialScale = HOME_DETAIL_REVEAL_INITIAL_SCALE,
        easing = VIDEO_CARD_IOS_LIKE_EASE_OUT
    )
}

internal fun resolveHomeVideoSharedTransitionCornerSpec(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoSharedCornerSpec {
    val enabled = transitionEnabled &&
        !sourceRoute?.substringBefore("?").isNullOrBlank()
    return if (enabled) {
        VideoSharedCornerSpec(
            enabled = true,
            startCornerDp = HOME_SHARED_TRANSITION_CARD_CORNER_DP,
            endCornerDp = HOME_SHARED_TRANSITION_PLAYER_CORNER_DP
        )
    } else {
        VideoSharedCornerSpec(
            enabled = false,
            startCornerDp = 0,
            endCornerDp = 0
        )
    }
}

internal fun resolveVideoDetailContentRevealMotion(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoDetailContentRevealMotion {
    // shell sharedBounds 接管整张卡片 ↔ 详情页的整体 morph；
    // 详情内容不再单独做 fade/slide，避免与共享元素形变抢戏导致撕裂。
    // 非共享元素路径（无 sourceRoute / 禁用过渡）也无需 reveal —— 由调用方自身的 fallback 转场处理。
    return VideoDetailContentRevealMotion(
        enabled = false,
        delayMillis = 0,
        durationMillis = 0,
        slideOffsetDp = 0,
        initialScale = 1f
    )
}

internal fun shouldPlayVideoCardReturnRebound(
    cardBvid: String,
    cardSourceRoute: String?,
    returningSourceKey: String?,
    returningSourceRoute: String?,
    isReturningFromDetail: Boolean,
    sharedTransitionReady: Boolean
): Boolean {
    val normalizedBvid = cardBvid.trim()
    val normalizedCardRoute = cardSourceRoute?.substringBefore("?")?.takeIf { it.isNotBlank() }
    val normalizedReturnRoute = returningSourceRoute?.substringBefore("?")?.takeIf { it.isNotBlank() }
    if (!isReturningFromDetail || !sharedTransitionReady) return false
    if (normalizedBvid.isEmpty() || normalizedCardRoute == null) return false
    if (normalizedCardRoute != normalizedReturnRoute) return false
    return returningSourceKey == "$normalizedCardRoute:$normalizedBvid"
}

internal fun resolveVideoCardReturnReboundSpec(
    enabled: Boolean
): VideoCardReturnReboundSpec {
    return if (enabled) {
        VideoCardReturnReboundSpec(
            enabled = true,
            durationMillis = 150,
            startScale = VIDEO_CARD_RETURN_REBOUND_START_SCALE,
            startTranslationYDp = VIDEO_CARD_RETURN_REBOUND_TRANSLATION_Y_DP,
            dampingRatio = VIDEO_CARD_RETURN_REBOUND_DAMPING_RATIO,
            stiffness = VIDEO_CARD_RETURN_REBOUND_STIFFNESS,
            easing = VIDEO_CARD_IOS_LIKE_EASE_OUT
        )
    } else {
        VideoCardReturnReboundSpec(
            enabled = false,
            durationMillis = 0,
            startScale = 1f,
            startTranslationYDp = 0f,
            dampingRatio = 1f,
            stiffness = 0f,
            easing = VIDEO_CARD_IOS_LIKE_EASE_OUT
        )
    }
}
