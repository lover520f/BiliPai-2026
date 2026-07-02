package com.android.purebilibili.core.ui.transition

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Rect
import com.android.purebilibili.core.ui.adaptive.MotionTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow

internal enum class VideoCardTransitionPhase {
    IDLE,
    EXPANDING,
    EXPANDED,
    COLLAPSING
}

internal data class VideoCardTransitionSession(
    val phase: VideoCardTransitionPhase = VideoCardTransitionPhase.IDLE,
    val progress: Float = 0f,
    val skipBackdropEffects: Boolean = false
)

internal enum class VideoCardTransitionDirection {
    EXPAND,
    COLLAPSE
}

internal data class VideoCardTransitionBackdropFrame(
    val active: Boolean,
    val scale: Float,
    val blurRadiusDp: Float,
    val scrimAlpha: Float
)

internal data class VideoCardContainerTransformBounds(
    val sourceBoundsInRoot: Rect?,
    val overlayBoundsInRoot: Rect,
    val targetBoundsInOverlay: Rect
)

internal data class VideoCardContainerTransformFrame(
    val active: Boolean,
    val rect: Rect,
    val cornerRadiusDp: Float,
    val alpha: Float
)

internal val LocalVideoCardTransitionSession = compositionLocalOf { VideoCardTransitionSession() }

internal const val VIDEO_CARD_TRANSITION_MIN_SCALE = 0.94f
private const val VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA = 0.10f
private const val VIDEO_CARD_TRANSITION_PROGRESS_EPSILON = 0.001f
private const val VIDEO_CARD_TRANSITION_COLLAPSE_BLUR_POWER = 1.8f
private const val VIDEO_CARD_TRANSITION_EXPAND_BLUR_POWER = 1.35f
private const val VIDEO_CARD_TRANSITION_EXPAND_BLUR_PEAK_PROGRESS = 0.35f
private const val VIDEO_CARD_TRANSITION_BLUR_EFFECT_MIN_RADIUS_DP = 0.5f
private const val VIDEO_CARD_CONTAINER_TARGET_CORNER_DP = 0f
private const val VIDEO_CARD_CONTAINER_ALPHA = 1f

internal enum class VideoTransitionBackdropBlurMode {
    RENDER_EFFECT,
    COMPOSE_BLUR_FALLBACK,
    DISABLED
}

internal fun shouldDriveVideoCardTransitionBackdrop(
    cardTransitionEnabled: Boolean,
    sharedTransitionReady: Boolean
): Boolean {
    return cardTransitionEnabled && sharedTransitionReady
}

internal fun shouldApplyVideoCardTransitionBackdropToEntry(
    cardTransitionEnabled: Boolean,
    session: VideoCardTransitionSession,
    entryInvolvesVideoDetail: Boolean,
    entryIsUnderlyingSource: Boolean
): Boolean {
    return cardTransitionEnabled &&
        entryInvolvesVideoDetail &&
        entryIsUnderlyingSource &&
        session.phase != VideoCardTransitionPhase.IDLE &&
        session.progress > VIDEO_CARD_TRANSITION_PROGRESS_EPSILON
}

internal fun resolveVideoCardTransitionBackdropFrame(
    session: VideoCardTransitionSession,
    direction: VideoCardTransitionDirection,
    skipBackdropEffects: Boolean,
    motionTier: MotionTier,
    maxBlurRadiusDp: Float,
    sdkInt: Int = Build.VERSION.SDK_INT
): VideoCardTransitionBackdropFrame {
    val progress = session.progress.coerceIn(0f, 1f)
    if (
        session.phase == VideoCardTransitionPhase.IDLE ||
        progress <= VIDEO_CARD_TRANSITION_PROGRESS_EPSILON ||
        skipBackdropEffects
    ) {
        return inactiveVideoCardTransitionBackdropFrame()
    }

    val allowBlur = motionTier != MotionTier.Reduced && maxBlurRadiusDp > 0f

    val scale = resolveVideoCardTransitionScale(progress)
    val effectStrength = resolveVideoCardTransitionEffectStrength(
        progress = progress,
        direction = direction
    )
    val blurRadiusDp = if (allowBlur) {
        maxBlurRadiusDp * effectStrength
    } else {
        0f
    }
    val scrimAlpha = VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA * effectStrength

    return VideoCardTransitionBackdropFrame(
        active = blurRadiusDp > 0f || scrimAlpha > 0f || scale < 0.999f,
        scale = scale,
        blurRadiusDp = blurRadiusDp,
        scrimAlpha = scrimAlpha
    )
}

internal fun inactiveVideoCardTransitionBackdropFrame(): VideoCardTransitionBackdropFrame {
    return VideoCardTransitionBackdropFrame(
        active = false,
        scale = 1f,
        blurRadiusDp = 0f,
        scrimAlpha = 0f
    )
}

internal fun resolveVideoCardTransitionScale(progress: Float): Float {
    val clamped = progress.coerceIn(0f, 1f)
    return 1f - ((1f - VIDEO_CARD_TRANSITION_MIN_SCALE) * clamped)
}

internal fun resolveVideoCardTransitionEffectStrength(
    progress: Float,
    direction: VideoCardTransitionDirection
): Float {
    val clamped = progress.coerceIn(0f, 1f)
    return when (direction) {
        VideoCardTransitionDirection.EXPAND -> {
            val normalized = (clamped / VIDEO_CARD_TRANSITION_EXPAND_BLUR_PEAK_PROGRESS)
                .coerceIn(0f, 1f)
            normalized.pow(VIDEO_CARD_TRANSITION_EXPAND_BLUR_POWER)
        }
        VideoCardTransitionDirection.COLLAPSE -> {
            clamped.pow(VIDEO_CARD_TRANSITION_COLLAPSE_BLUR_POWER)
        }
    }
}

internal fun resolveVideoTransitionBackdropBlurMode(
    blurRadiusDp: Float,
    motionTier: MotionTier,
    sdkInt: Int = Build.VERSION.SDK_INT
): VideoTransitionBackdropBlurMode {
    if (motionTier == MotionTier.Reduced || blurRadiusDp <= VIDEO_CARD_TRANSITION_BLUR_EFFECT_MIN_RADIUS_DP) {
        return VideoTransitionBackdropBlurMode.DISABLED
    }
    return if (sdkInt >= Build.VERSION_CODES.S) {
        VideoTransitionBackdropBlurMode.RENDER_EFFECT
    } else {
        VideoTransitionBackdropBlurMode.COMPOSE_BLUR_FALLBACK
    }
}

internal fun resolveVideoCardContainerTransformFrame(
    cardTransitionEnabled: Boolean,
    sourceKeyMatches: Boolean,
    cardFullyVisible: Boolean,
    motionTier: MotionTier,
    session: VideoCardTransitionSession,
    bounds: VideoCardContainerTransformBounds?,
    sourceCornerRadiusDp: Float
): VideoCardContainerTransformFrame {
    val inactiveFrame = inactiveVideoCardContainerTransformFrame()
    val sourceBoundsInRoot = bounds?.sourceBoundsInRoot ?: return inactiveFrame
    if (
        !cardTransitionEnabled ||
        !sourceKeyMatches ||
        !cardFullyVisible ||
        motionTier == MotionTier.Reduced ||
        session.phase == VideoCardTransitionPhase.IDLE ||
        session.phase == VideoCardTransitionPhase.EXPANDED
    ) {
        return inactiveFrame
    }

    val progress = session.progress.coerceIn(0f, 1f)
    val overlayBounds = bounds.overlayBoundsInRoot
    val sourceBoundsInOverlay = Rect(
        left = sourceBoundsInRoot.left - overlayBounds.left,
        top = sourceBoundsInRoot.top - overlayBounds.top,
        right = sourceBoundsInRoot.right - overlayBounds.left,
        bottom = sourceBoundsInRoot.bottom - overlayBounds.top
    )
    val targetBounds = bounds.targetBoundsInOverlay
    val rect = Rect(
        left = lerpFloat(sourceBoundsInOverlay.left, targetBounds.left, progress),
        top = lerpFloat(sourceBoundsInOverlay.top, targetBounds.top, progress),
        right = lerpFloat(sourceBoundsInOverlay.right, targetBounds.right, progress),
        bottom = lerpFloat(sourceBoundsInOverlay.bottom, targetBounds.bottom, progress)
    )

    return VideoCardContainerTransformFrame(
        active = true,
        rect = rect,
        cornerRadiusDp = lerpFloat(
            sourceCornerRadiusDp.coerceAtLeast(0f),
            VIDEO_CARD_CONTAINER_TARGET_CORNER_DP,
            progress
        ),
        alpha = VIDEO_CARD_CONTAINER_ALPHA
    )
}

internal fun inactiveVideoCardContainerTransformFrame(): VideoCardContainerTransformFrame {
    return VideoCardContainerTransformFrame(
        active = false,
        rect = Rect(0f, 0f, 0f, 0f),
        cornerRadiusDp = 0f,
        alpha = 0f
    )
}

internal fun resolveVideoCardTransitionExpandedFractionFromPredictiveGestureProgress(
    gestureProgress: Float
): Float {
    return (1f - gestureProgress).coerceIn(0f, 1f)
}

internal fun resolveVideoCardTransitionSessionFromExpandedFraction(
    expandedFraction: Float
): VideoCardTransitionSession {
    val clamped = expandedFraction.coerceIn(0f, 1f)
    return when {
        clamped <= VIDEO_CARD_TRANSITION_PROGRESS_EPSILON ->
            VideoCardTransitionSession(VideoCardTransitionPhase.IDLE, 0f)
        clamped >= 1f - VIDEO_CARD_TRANSITION_PROGRESS_EPSILON ->
            VideoCardTransitionSession(VideoCardTransitionPhase.EXPANDED, 1f)
        else ->
            VideoCardTransitionSession(VideoCardTransitionPhase.COLLAPSING, clamped)
    }
}

@Stable
internal class VideoCardTransitionController(
    private val scope: CoroutineScope,
    private val easing: Easing,
    private val durationMillis: Int,
    private val enabled: Boolean
) {
    var session by mutableStateOf(VideoCardTransitionSession())
        private set

    private val progressAnimatable = Animatable(0f)
    private var runningJob: Job? = null
    private var progressObserverJob: Job? = null

    fun beginExpand() {
        if (!enabled) return
        cancelRunningAnimation()
        runningJob = scope.launch {
            startProgressObservation(VideoCardTransitionPhase.EXPANDING)
            try {
                progressAnimatable.snapTo(0f)
                publishSession(VideoCardTransitionPhase.EXPANDING, 0f)
                progressAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = durationMillis, easing = easing)
                )
                publishSession(VideoCardTransitionPhase.EXPANDED, 1f)
            } finally {
                stopProgressObservation()
            }
        }
    }

    fun beginCollapse(skipBackdropEffects: Boolean) {
        if (!enabled) {
            reset()
            return
        }
        cancelRunningAnimation()
        runningJob = scope.launch {
            val startProgress = progressAnimatable.value.coerceIn(0f, 1f)
            startProgressObservation(
                phase = VideoCardTransitionPhase.COLLAPSING,
                skipBackdropEffects = skipBackdropEffects
            )
            try {
                publishSession(
                    phase = VideoCardTransitionPhase.COLLAPSING,
                    progress = startProgress,
                    skipBackdropEffects = skipBackdropEffects
                )
                progressAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = durationMillis, easing = easing)
                )
                publishSession(VideoCardTransitionPhase.IDLE, 0f)
            } finally {
                stopProgressObservation()
            }
        }
    }

    fun applyPredictiveBackdropFraction(expandedFraction: Float) {
        if (!enabled) return
        cancelRunningAnimation()
        val clamped = expandedFraction.coerceIn(0f, 1f)
        runningJob = scope.launch {
            progressAnimatable.snapTo(clamped)
            session = resolveVideoCardTransitionSessionFromExpandedFraction(clamped)
        }
    }

    fun restoreExpandedBackdrop() {
        if (!enabled) return
        cancelRunningAnimation()
        runningJob = scope.launch {
            progressAnimatable.snapTo(1f)
            publishSession(VideoCardTransitionPhase.EXPANDED, 1f)
        }
    }

    fun reset() {
        cancelRunningAnimation()
        runningJob = scope.launch {
            progressAnimatable.snapTo(0f)
            publishSession(VideoCardTransitionPhase.IDLE, 0f)
        }
    }

    private fun startProgressObservation(
        phase: VideoCardTransitionPhase,
        skipBackdropEffects: Boolean = false
    ) {
        stopProgressObservation()
        progressObserverJob = scope.launch {
            snapshotFlow { progressAnimatable.value }.collect { value ->
                if (!isActive) return@collect
                publishSession(
                    phase = phase,
                    progress = value.coerceIn(0f, 1f),
                    skipBackdropEffects = skipBackdropEffects
                )
            }
        }
    }

    private fun stopProgressObservation() {
        progressObserverJob?.cancel()
        progressObserverJob = null
    }

    private fun publishSession(
        phase: VideoCardTransitionPhase,
        progress: Float,
        skipBackdropEffects: Boolean = false
    ) {
        session = VideoCardTransitionSession(
            phase = phase,
            progress = progress.coerceIn(0f, 1f),
            skipBackdropEffects = skipBackdropEffects
        )
    }

    private fun cancelRunningAnimation() {
        runningJob?.cancel()
        runningJob = null
        stopProgressObservation()
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}

@Composable
internal fun rememberVideoCardTransitionController(
    enabled: Boolean,
    speedSettings: VideoSharedTransitionSpeedSettings
): VideoCardTransitionController {
    val scope = rememberCoroutineScope()
    val easing = remember { resolveVideoCardSharedTransitionEasing() }
    val durationMillis = remember(speedSettings) {
        resolveVideoSharedTransitionDurationMillis(speedSettings)
    }
    return remember(enabled, durationMillis) {
        VideoCardTransitionController(
            scope = scope,
            easing = easing,
            durationMillis = durationMillis,
            enabled = enabled
        )
    }
}
