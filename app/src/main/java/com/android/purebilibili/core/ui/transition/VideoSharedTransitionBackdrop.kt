package com.android.purebilibili.core.ui.transition

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.adaptive.resolveDeviceUiProfile
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.navigation3.BiliPaiNavKey
import com.android.purebilibili.navigation3.BiliPaiNavRouteTransition

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.videoCardShellSharedBoundsOrEmpty(
    enabled: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    bvid: String,
    sourceRoute: String?,
    motionSpec: VideoSharedTransitionMotionSpec,
    clipShape: Shape
): Modifier {
    if (!enabled || sharedTransitionScope == null || animatedVisibilityScope == null || bvid.isBlank()) {
        return this
    }
    return then(
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = videoCoverSharedElementKey(
                        bvid = bvid,
                        sourceRoute = sourceRoute
                    )
                ),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    if (motionSpec.enabled) {
                        tween(
                            durationMillis = motionSpec.durationMillis,
                            easing = motionSpec.easing
                        )
                    } else {
                        com.android.purebilibili.core.ui.motion.AppMotionTokens.spatialSpec()
                    }
                },
                clipInOverlayDuringTransition = OverlayClip(clipShape)
            )
        }
    )
}

@Composable
internal fun VideoSharedTransitionBackdropHost(
    cardTransitionEnabled: Boolean,
    entryKey: BiliPaiNavKey,
    topKey: BiliPaiNavKey?,
    maxBlurRadiusDp: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val session = LocalVideoCardTransitionSession.current
    val entryInvolvesVideoDetail = topKey is BiliPaiNavKey.VideoDetail
    val entryIsUnderlyingSource = entryKey !is BiliPaiNavKey.VideoDetail && entryKey != topKey
    val direction = remember(session.phase) {
        when (session.phase) {
            VideoCardTransitionPhase.EXPANDING -> VideoCardTransitionDirection.EXPAND
            VideoCardTransitionPhase.COLLAPSING -> VideoCardTransitionDirection.COLLAPSE
            VideoCardTransitionPhase.EXPANDED,
            VideoCardTransitionPhase.IDLE -> VideoCardTransitionDirection.COLLAPSE
        }
    }
    val windowSizeClass = LocalWindowSizeClass.current
    val motionTier = remember(windowSizeClass.widthSizeClass) {
        resolveDeviceUiProfile(windowSizeClass.widthSizeClass).motionTier
    }
    val frame = remember(
        cardTransitionEnabled,
        session,
        entryInvolvesVideoDetail,
        entryIsUnderlyingSource,
        maxBlurRadiusDp,
        direction,
        motionTier
    ) {
        if (!shouldApplyVideoCardTransitionBackdropToEntry(
                cardTransitionEnabled = cardTransitionEnabled,
                session = session,
                entryInvolvesVideoDetail = entryInvolvesVideoDetail,
                entryIsUnderlyingSource = entryIsUnderlyingSource
            )
        ) {
            inactiveVideoCardTransitionBackdropFrame()
        } else {
            resolveVideoCardTransitionBackdropFrame(
                session = session,
                direction = direction,
                skipBackdropEffects = session.skipBackdropEffects,
                motionTier = motionTier,
                maxBlurRadiusDp = maxBlurRadiusDp
            )
        }
    }
    VideoSharedTransitionBackdropDecoration(
        frame = frame,
        blurMode = resolveVideoTransitionBackdropBlurMode(
            blurRadiusDp = frame.blurRadiusDp,
            motionTier = motionTier
        ),
        modifier = modifier,
        content = content
    )
}

@Composable
internal fun VideoSharedTransitionBackdropDecoration(
    frame: VideoCardTransitionBackdropFrame,
    blurMode: VideoTransitionBackdropBlurMode,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val blurRadiusPx = remember(frame.blurRadiusDp, density) {
        with(density) { frame.blurRadiusDp.dp.toPx() }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = frame.scale
                    scaleY = frame.scale
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    renderEffect =
                        if (
                            blurMode == VideoTransitionBackdropBlurMode.RENDER_EFFECT &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ) {
                            RenderEffect.createBlurEffect(
                                blurRadiusPx,
                                blurRadiusPx,
                                Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        } else {
                            null
                        }
                }
                .then(
                    if (blurMode == VideoTransitionBackdropBlurMode.COMPOSE_BLUR_FALLBACK) {
                        Modifier.blur(frame.blurRadiusDp.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }
        if (frame.scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = frame.scrimAlpha))
            )
        }
    }
}

internal fun isVideoSharedElementRouteTransition(
    routeTransition: BiliPaiNavRouteTransition
): Boolean {
    return routeTransition == BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT
}
