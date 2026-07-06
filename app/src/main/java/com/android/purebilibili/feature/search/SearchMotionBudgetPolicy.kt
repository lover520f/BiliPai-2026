package com.android.purebilibili.feature.search

internal enum class SearchMotionBudget {
    FULL,
    REDUCED
}

internal fun resolveSearchMotionBudget(
    hasQuery: Boolean,
    isSearching: Boolean,
    isScrolling: Boolean
): SearchMotionBudget {
    return if (isSearching || (hasQuery && isScrolling)) {
        SearchMotionBudget.REDUCED
    } else {
        SearchMotionBudget.FULL
    }
}

internal fun shouldEnableSearchHazeSource(
    isSearching: Boolean,
    startupSettled: Boolean = true
): Boolean = startupSettled && !isSearching

internal fun resolveEffectiveSearchMotionBudget(
    startupSettled: Boolean,
    baseBudget: SearchMotionBudget
): SearchMotionBudget {
    return if (startupSettled) baseBudget else SearchMotionBudget.REDUCED
}

/**
 * 搜索结果卡片是否启用共享元素过渡。
 *
 * 与首页的差异在于：首页无条件跟随 [cardTransitionEnabled]，而搜索页额外受 motion budget 门控。
 * 结果列表 settle/加载/滚动等瞬态会把 budget 降为 [SearchMotionBudget.REDUCED]，若此时正处于
 * 从视频详情返回的 morph 过程中，来源卡片会卸载 sharedBounds，导致返回退化为"无过渡"(与首页不一致)。
 * 因此返回详情期间强制启用，保证来源卡片在整个返回动画中保持 sharedBounds，其余场景仍尊重 budget。
 */
internal fun resolveEffectiveSearchCardTransitionEnabled(
    cardTransitionEnabled: Boolean,
    motionBudget: SearchMotionBudget,
    isReturningFromVideoDetail: Boolean
): Boolean {
    if (!cardTransitionEnabled) return false
    if (isReturningFromVideoDetail) return true
    return motionBudget == SearchMotionBudget.FULL
}

internal fun shouldBootstrapSearchLandingData(
    startupSettled: Boolean,
    showResults: Boolean,
    query: String
): Boolean {
    return startupSettled && !showResults && query.isBlank()
}

internal fun shouldAutoFocusSearchField(
    startupSettled: Boolean,
    query: String
): Boolean {
    return startupSettled && query.isBlank()
}

internal fun shouldForceLowBudgetSearchHeaderBlur(
    isSearching: Boolean,
    isScrollingResults: Boolean
): Boolean {
    return isSearching && !isScrollingResults
}
