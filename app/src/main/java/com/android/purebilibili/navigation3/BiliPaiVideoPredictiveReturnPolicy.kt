package com.android.purebilibili.navigation3

import androidx.compose.ui.geometry.Rect
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.NavigationEventTransitionState.InProgress
import com.android.purebilibili.core.store.PredictiveBackAnimationStyle

internal data class BiliPaiPredictiveBackGestureState(
    val active: Boolean = false,
    val progress: Float = 0f
)

internal fun resolveBiliPaiPredictiveBackGestureState(
    transitionState: NavigationEventTransitionState?
): BiliPaiPredictiveBackGestureState {
    val inProgress = transitionState as? InProgress ?: return BiliPaiPredictiveBackGestureState()
    return BiliPaiPredictiveBackGestureState(
        active = true,
        progress = inProgress.latestEvent.progress.coerceIn(0f, 1f)
    )
}

internal fun shouldEnableVideoPredictiveReturnToCard(
    currentKey: BiliPaiNavKey?,
    previousKey: BiliPaiNavKey?,
    predictiveBackAnimationStyle: PredictiveBackAnimationStyle,
    sourceMetadata: BiliPaiNavSourceMetadata,
    sourceBounds: Rect?
): Boolean {
    val videoKey = currentKey as? BiliPaiNavKey.VideoDetail ?: return false
    val normalizedSourceRoute = sourceMetadata.sourceRoute?.substringBefore("?")
    val normalizedVideoSourceRoute = videoKey.sourceRoute?.substringBefore("?")
    return predictiveBackAnimationStyle.usesPredictiveBack &&
        previousKey == BiliPaiNavKey.Home &&
        normalizedSourceRoute == "home" &&
        normalizedVideoSourceRoute == "home" &&
        sourceMetadata.sharedTransitionReady &&
        sourceMetadata.sourceKey == "home:${videoKey.bvid}" &&
        sourceBounds != null
}
