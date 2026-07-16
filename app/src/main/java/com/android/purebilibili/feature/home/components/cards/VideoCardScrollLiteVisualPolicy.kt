package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase

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
 *
 * 关键：快速/普通返回结束后 [isReturningFromDetail] 会被 clear，若此时把 crossfade
 * 从 false 拨回 true，ImageRequest 重建会触发一次假加载淡入 → **落位后再闪一下**。
 * 因此只要仍是 shared 返回目标卡（lastClicked），就持续关闭 crossfade，直到用户点了别的卡。
 */
internal fun shouldEnableVideoCardCoverCrossfade(
    isScrollInProgress: Boolean,
    isReturningFromDetail: Boolean,
    useCoverSharedBounds: Boolean,
    isSharedReturnTarget: Boolean
): Boolean {
    if (isScrollInProgress) return false
    // 返回目标：全程禁用 Coil 淡入（含 clearReturning 之后）。
    if (useCoverSharedBounds && isSharedReturnTarget) return false
    // 返回会话中非 shell 路径的兜底
    if (isReturningFromDetail && isSharedReturnTarget) return false
    return true
}

/**
 * 首页卡片 → 详情页 CARD_SHELL morph 期间，源卡片封面是否让位给 overlay。
 *
 * 隐藏时机（仅这些）：
 * - OPENING：冻结 record 前藏封面，减「冻结清晰封面 + overlay」重影
 * - 预测返回跟手：详情仍是 live 全屏，藏列表封面
 *
 * 提交返回（isReturningFromDetail / RETURNING）：**不藏**列表封面。
 * 详情侧会立刻叠 handoff 封面；列表封面同时可见可避免「整段收回只有黑底，落位才出图」。
 * 返回目标已关 Coil crossfade，落位不二次淡入。
 */
internal fun shouldHideHomeCardCoverDuringShellMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    isSharedTransitionActive: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
): Boolean {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard) {
        return false
    }
    // 返回会话中：保持列表封面可见（承接落位 + morph 过程可见封面）。
    if (isReturningFromDetail) {
        return false
    }
    if (transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.OPENING) {
        return true
    }
    if (isVideoCardReturnGestureInProgress) {
        return true
    }
    // 进场 shared 尚未标 OPENING 的短窗口
    if (isSharedTransitionActive &&
        transitionBackgroundPhase != VideoCardTransitionBackgroundPhase.RETURNING
    ) {
        return true
    }
    return false
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
