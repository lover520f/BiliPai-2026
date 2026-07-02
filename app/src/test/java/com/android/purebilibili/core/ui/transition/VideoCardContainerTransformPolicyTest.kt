package com.android.purebilibili.core.ui.transition

import androidx.compose.ui.geometry.Rect
import com.android.purebilibili.core.ui.adaptive.MotionTier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoCardContainerTransformPolicyTest {

    private val sourceRootBounds = Rect(left = 20f, top = 100f, right = 180f, bottom = 220f)
    private val overlayRootBounds = Rect(left = 0f, top = 50f, right = 400f, bottom = 850f)
    private val targetOverlayBounds = Rect(left = 0f, top = 0f, right = 400f, bottom = 800f)

    @Test
    fun progressZero_usesSourceBoundsTranslatedIntoOverlayCoordinates() {
        val frame = activeFrame(progress = 0f)

        assertTrue(frame.active)
        assertEquals(Rect(left = 20f, top = 50f, right = 180f, bottom = 170f), frame.rect)
        assertEquals(12f, frame.cornerRadiusDp)
        assertEquals(1f, frame.alpha)
    }

    @Test
    fun progressOne_usesFullscreenTargetBoundsAndZeroCorner() {
        val frame = activeFrame(progress = 1f)

        assertTrue(frame.active)
        assertEquals(targetOverlayBounds, frame.rect)
        assertEquals(0f, frame.cornerRadiusDp)
        assertEquals(1f, frame.alpha)
    }

    @Test
    fun midProgress_interpolatesBoundsAndCorner() {
        val frame = activeFrame(progress = 0.5f)

        assertTrue(frame.active)
        assertEquals(Rect(left = 10f, top = 25f, right = 290f, bottom = 485f), frame.rect)
        assertEquals(6f, frame.cornerRadiusDp)
    }

    @Test
    fun inactiveWhenSourceMissingMismatchedInvisibleReducedOrDisabled() {
        val baseBounds = VideoCardContainerTransformBounds(
            sourceBoundsInRoot = sourceRootBounds,
            overlayBoundsInRoot = overlayRootBounds,
            targetBoundsInOverlay = targetOverlayBounds
        )

        assertFalse(
            resolveVideoCardContainerTransformFrame(
                cardTransitionEnabled = false,
                sourceKeyMatches = true,
                cardFullyVisible = true,
                motionTier = MotionTier.Normal,
                session = VideoCardTransitionSession(VideoCardTransitionPhase.EXPANDING, 0.4f),
                bounds = baseBounds,
                sourceCornerRadiusDp = 12f
            ).active
        )
        assertFalse(
            resolveVideoCardContainerTransformFrame(
                cardTransitionEnabled = true,
                sourceKeyMatches = false,
                cardFullyVisible = true,
                motionTier = MotionTier.Normal,
                session = VideoCardTransitionSession(VideoCardTransitionPhase.EXPANDING, 0.4f),
                bounds = baseBounds,
                sourceCornerRadiusDp = 12f
            ).active
        )
        assertFalse(
            resolveVideoCardContainerTransformFrame(
                cardTransitionEnabled = true,
                sourceKeyMatches = true,
                cardFullyVisible = false,
                motionTier = MotionTier.Normal,
                session = VideoCardTransitionSession(VideoCardTransitionPhase.EXPANDING, 0.4f),
                bounds = baseBounds,
                sourceCornerRadiusDp = 12f
            ).active
        )
        assertFalse(
            resolveVideoCardContainerTransformFrame(
                cardTransitionEnabled = true,
                sourceKeyMatches = true,
                cardFullyVisible = true,
                motionTier = MotionTier.Reduced,
                session = VideoCardTransitionSession(VideoCardTransitionPhase.EXPANDING, 0.4f),
                bounds = baseBounds,
                sourceCornerRadiusDp = 12f
            ).active
        )
        assertFalse(
            resolveVideoCardContainerTransformFrame(
                cardTransitionEnabled = true,
                sourceKeyMatches = true,
                cardFullyVisible = true,
                motionTier = MotionTier.Normal,
                session = VideoCardTransitionSession(VideoCardTransitionPhase.EXPANDING, 0.4f),
                bounds = baseBounds.copy(sourceBoundsInRoot = null),
                sourceCornerRadiusDp = 12f
            ).active
        )
    }

    @Test
    fun predictiveGestureProgressMapsToExpandedFraction() {
        assertEquals(0.75f, resolveVideoCardTransitionExpandedFractionFromPredictiveGestureProgress(0.25f))
        assertEquals(1f, resolveVideoCardTransitionExpandedFractionFromPredictiveGestureProgress(-0.2f))
        assertEquals(0f, resolveVideoCardTransitionExpandedFractionFromPredictiveGestureProgress(1.4f))
    }

    private fun activeFrame(progress: Float): VideoCardContainerTransformFrame {
        return resolveVideoCardContainerTransformFrame(
            cardTransitionEnabled = true,
            sourceKeyMatches = true,
            cardFullyVisible = true,
            motionTier = MotionTier.Normal,
            session = VideoCardTransitionSession(VideoCardTransitionPhase.EXPANDING, progress),
            bounds = VideoCardContainerTransformBounds(
                sourceBoundsInRoot = sourceRootBounds,
                overlayBoundsInRoot = overlayRootBounds,
                targetBoundsInOverlay = targetOverlayBounds
            ),
            sourceCornerRadiusDp = 12f
        )
    }
}
