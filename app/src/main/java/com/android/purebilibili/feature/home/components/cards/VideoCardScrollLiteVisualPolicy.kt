package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.transition.VIDEO_CARD_RETURN_CHROME_REVEAL_START
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.normalizeSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.resolveVideoCardReturnListCoverContract
import com.android.purebilibili.core.ui.transition.resolveVideoCardReturnSettleProgress

internal data class VideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showCoverGradientMask: Boolean,
    val showHistoryProgressBar: Boolean,
    val showCompactStatsOnCover: Boolean,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean,
    compactStatsOnCover: Boolean
): VideoCardScrollLiteVisualPolicy {
    if (scrollLiteModeEnabled) {
        return VideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showCoverGradientMask = false,
            showHistoryProgressBar = false,
            showCompactStatsOnCover = compactStatsOnCover,
            showSecondaryStatsRow = !compactStatsOnCover
        )
    }

    return VideoCardScrollLiteVisualPolicy(
        coverShadowElevationDp = 0f,
        // 统计信息移到封面外时也不需要暗渐变；保持静止和滚动状态一致，避免整批封面明暗闪烁。
        showCoverGradientMask = false,
        showHistoryProgressBar = true,
        showCompactStatsOnCover = compactStatsOnCover,
        showSecondaryStatsRow = !compactStatsOnCover
    )
}

/**
 * 列表封面是否允许 Coil crossfade。
 * 契约收口到 [resolveVideoCardReturnListCoverContract]（[VideoCardReturnTimeline]）。
 */
internal fun shouldEnableVideoCardCoverCrossfade(
    isScrollInProgress: Boolean,
    isReturningFromDetail: Boolean,
    useCoverSharedBounds: Boolean,
    isSharedReturnTarget: Boolean
): Boolean = resolveVideoCardReturnListCoverContract(
    isSharedReturnTarget = isSharedReturnTarget,
    isScrollInProgress = isScrollInProgress,
    isReturningFromDetail = isReturningFromDetail,
    useCoverSharedBounds = useCoverSharedBounds,
).enableCoilCrossfade

/**
 * shared 返回目标卡是否应钉住封面 URL/缓存键。
 * 契约收口到 [resolveVideoCardReturnListCoverContract]。
 */
internal fun shouldPinVideoCardCoverForSharedReturn(
    isSharedReturnTarget: Boolean,
): Boolean = resolveVideoCardReturnListCoverContract(
    isSharedReturnTarget = isSharedReturnTarget,
    isScrollInProgress = false,
    isReturningFromDetail = false,
    useCoverSharedBounds = true,
).pinCoverSource

/**
 * 首页卡片 → 详情页 CARD_SHELL morph 期间，源卡片封面是否让位给 overlay。
 * 始终不藏：见 [VideoCardReturnListCoverContract.hideCoverDuringShellMorph]。
 */
@Suppress("UNUSED_PARAMETER")
internal fun shouldHideHomeCardCoverDuringShellMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
): Boolean = resolveVideoCardReturnListCoverContract(
    isSharedReturnTarget = isSharedMorphSourceCard,
    isScrollInProgress = false,
    isReturningFromDetail = isReturningFromDetail,
    useCoverSharedBounds = useCardContainerSharedBounds,
).hideCoverDuringShellMorph

/**
 * 返回落位进度达到该比例后开始淡入标题等 chrome。
 * 与 [VIDEO_CARD_RETURN_CHROME_REVEAL_START] 同源。
 */
internal const val HOME_CARD_CHROME_EARLY_REVEAL_SETTLE_START =
    VIDEO_CARD_RETURN_CHROME_REVEAL_START

/**
 * 进场 OPENING：标题在进度到达此前仍保持满显，避免「一点击就挖空」。
 * 约 8% 缓冲，等封面已明显启动 morph。
 */
internal const val HOME_CARD_CHROME_OPEN_FADE_START = 0.08f

/**
 * 进场 OPENING：标题在进度达到此后完全透明，避免字叠在详情播放器上。
 * 中段 smoothstep 淡出，观感连续。
 */
internal const val HOME_CARD_CHROME_OPEN_FADE_END = 0.52f

/**
 * 源卡 chrome 在返回落位进度上的淡入曲线。
 * [settleProgress] 0=刚开始缩回，1=完全落位；[revealStart] 之前保持 0。
 */
internal fun resolveHomeCardChromeEarlyRevealAlpha(
    settleProgress: Float,
    revealStart: Float = HOME_CARD_CHROME_EARLY_REVEAL_SETTLE_START,
): Float {
    val clampedSettle = settleProgress.coerceIn(0f, 1f)
    val start = revealStart.coerceIn(0f, 1f)
    if (clampedSettle <= start) return 0f
    if (start >= 1f) return if (clampedSettle >= 1f) 1f else 0f
    return ((clampedSettle - start) / (1f - start)).coerceIn(0f, 1f)
}

/**
 * 进场时源卡标题/UP 的淡出曲线。
 *
 * [openProgress] 0=刚点开（景深/morph 起点），1=进场结束。
 * 前段保持可读，中段 smoothstep 收干净，避免硬切成黑块。
 */
internal fun resolveHomeCardChromeOpenFadeAlpha(
    openProgress: Float,
    fadeStart: Float = HOME_CARD_CHROME_OPEN_FADE_START,
    fadeEnd: Float = HOME_CARD_CHROME_OPEN_FADE_END,
): Float {
    val p = openProgress.coerceIn(0f, 1f)
    val start = fadeStart.coerceIn(0f, 1f)
    val end = fadeEnd.coerceIn(start, 1f)
    if (p <= start) return 1f
    if (p >= end) return 0f
    val t = ((p - start) / (end - start)).coerceIn(0f, 1f)
    // Hermite smoothstep：比线性更「先慢后快再收」，点击不跳、中段不拖泥带水
    val smooth = t * t * (3f - 2f * t)
    return (1f - smooth).coerceIn(0f, 1f)
}

/**
 * 进场 OPENING 进度 → 用于 chrome 淡出的 openProgress。
 * HELD 视为已完全进场（字应收干净，直到 morph 结束再恢复）。
 */
internal fun resolveHomeCardChromeOpenProgress(
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    transitionBackgroundProgress: Float,
): Float {
    return when (transitionBackgroundPhase) {
        VideoCardTransitionBackgroundPhase.HELD -> 1f
        VideoCardTransitionBackgroundPhase.OPENING ->
            transitionBackgroundProgress.coerceIn(0f, 1f)
        // IDLE / RETURNING 不走进场淡出；由上层分支处理
        else -> transitionBackgroundProgress.coerceIn(0f, 1f)
    }
}

/**
 * shell morph 期间源卡 **chrome**（标题/UP/信息区）的 alpha。
 *
 * - morph 未进行：始终 1，避免相关推荐等落位后标题空白
 * - 进场飞向详情：随 OPENING 进度 **柔和淡出**（不再硬切 0），封面先动、字后收
 * - 返回落位：按 settle 进度在末段淡入，与封面同步出现，且中段不盖住实时画面
 * - 快速返回：直接 1
 */
internal fun resolveHomeCardChromeAlphaDuringShellReturnMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
): Float {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard) return 1f
    if (isQuickReturnFromDetail) return 1f

    val morphActive = isSharedTransitionActive || isVideoCardReturnGestureInProgress
    // morph 已结束：立刻恢复（哪怕景深 phase 仍短暂停留在 RETURNING）
    if (!morphActive) return 1f

    val isReturnMorph = isReturningFromDetail ||
        isVideoCardReturnGestureInProgress ||
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.RETURNING
    if (!isReturnMorph) {
        // 进场：标题已不在 shell 内（仅封面 morph），信息区全程保留，避免与详情骨架抢戏。
        // 叠字问题由封面 sharedBounds 单独承担，不再藏标题。
        return 1f
    }

    // 返回：与详情 content yield 共用 settle 解析（景深 1→0 → settle 0→1）。
    val settleProgress = resolveVideoCardReturnSettleProgress(
        depthBlurProgress = transitionBackgroundProgress,
    )
    return resolveHomeCardChromeEarlyRevealAlpha(settleProgress = settleProgress)
}

/**
 * 兼容旧布尔语义：chrome 尚未完全露出版视为仍在抑制。
 */
internal fun shouldSuppressHomeCardVisualDuringShellReturnMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
): Boolean {
    return resolveHomeCardChromeAlphaDuringShellReturnMorph(
        useCardContainerSharedBounds = useCardContainerSharedBounds,
        isSharedMorphSourceCard = isSharedMorphSourceCard,
        isReturningFromDetail = isReturningFromDetail,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
        isQuickReturnFromDetail = isQuickReturnFromDetail,
    ) < 1f
}

internal fun normalizeVideoCardSourceRouteForSharedKey(sourceRoute: String?): String? {
    return normalizeSharedElementSourceRoute(sourceRoute)
}

internal fun resolveVideoCardSharedReturnTargetKey(
    bvid: String,
    sourceRoute: String?,
): String? {
    val normalizedBvid = bvid.trim()
    val normalizedRoute = normalizeVideoCardSourceRouteForSharedKey(sourceRoute) ?: return null
    if (normalizedBvid.isEmpty()) return null
    return "$normalizedRoute:$normalizedBvid"
}

internal fun isVideoCardSharedReturnTarget(
    bvid: String,
    sourceRoute: String?,
    lastClickedVideoSourceKey: String?,
): Boolean {
    val key = resolveVideoCardSharedReturnTargetKey(bvid, sourceRoute) ?: return false
    return key == lastClickedVideoSourceKey
}

internal data class StoryVideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveStoryVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean
): StoryVideoCardScrollLiteVisualPolicy {
    return if (scrollLiteModeEnabled) {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    } else {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    }
}
