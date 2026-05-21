package com.android.purebilibili.navigation

import androidx.lifecycle.Lifecycle

internal fun shouldStopPlaybackEagerlyOnVideoRouteExit(
    fromRoute: String?,
    toRoute: String?
): Boolean {
    if (toRoute.isNullOrBlank()) return false
    return isVideoDetailRoute(fromRoute) &&
        !isVideoDetailRoute(toRoute) &&
        toRoute != ScreenRoutes.AudioMode.route
}

internal fun shouldDeferBottomBarRevealOnVideoReturn(
    isReturningFromDetail: Boolean,
    currentRoute: String?
): Boolean {
    if (!isReturningFromDetail || currentRoute !in setOf(ScreenRoutes.Home.route, "main_host")) return false
    return false
}

internal fun shouldClearReturningStateWhenDisposingVideoDestination(
    stillInVideoRoute: Boolean
): Boolean {
    return stillInVideoRoute
}

internal fun isVideoCardReturnTargetRoute(route: String?): Boolean {
    val routeBase = route?.substringBefore("?") ?: return false
    return routeBase == "main_host" ||
        routeBase == ScreenRoutes.Home.route ||
        routeBase == ScreenRoutes.History.route ||
        routeBase == ScreenRoutes.Favorite.route ||
        routeBase == ScreenRoutes.WatchLater.route ||
        routeBase == ScreenRoutes.Search.route ||
        routeBase == ScreenRoutes.Dynamic.route ||
        routeBase.startsWith("dynamic_detail/") ||
        routeBase == ScreenRoutes.Partition.route ||
        routeBase.startsWith("category/") ||
        routeBase.startsWith("season_series_detail/") ||
        routeBase.startsWith("space/")
}

internal fun isVideoDetailRoute(route: String?): Boolean {
    return route?.startsWith("${VideoRoute.base}/") == true
}

internal fun shouldEnableVideoDetailSharedTransition(
    cardTransitionEnabled: Boolean,
    predictiveBackAnimationEnabled: Boolean
): Boolean {
    // 预测性返回只控制返回进度，不反向关闭用户可见的共享元素动效。
    return cardTransitionEnabled
}

internal fun shouldShareAudioModeViewModelWithPreviousEntry(
    previousRoute: String?,
    previousLifecycleState: Lifecycle.State?
): Boolean {
    return previousLifecycleState?.isAtLeast(Lifecycle.State.CREATED) == true &&
        isVideoDetailRoute(previousRoute)
}

internal fun shouldNavigateAudioModeBackToCurrentVideo(
    previousVideoBvid: String?,
    currentVideoBvid: String
): Boolean {
    val normalizedCurrentBvid = currentVideoBvid.trim()
    if (normalizedCurrentBvid.isEmpty()) return false
    return previousVideoBvid?.trim() != normalizedCurrentBvid
}
