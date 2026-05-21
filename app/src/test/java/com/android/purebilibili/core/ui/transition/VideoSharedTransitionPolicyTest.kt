package com.android.purebilibili.core.ui.transition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoSharedTransitionPolicyTest {

    @Test
    fun coverSharedTransition_enabled_whenTransitionAndScopesAreReady() {
        assertTrue(
            shouldEnableVideoCoverSharedTransition(
                transitionEnabled = true,
                hasSharedTransitionScope = true,
                hasAnimatedVisibilityScope = true
            )
        )
        assertFalse(
            shouldEnableVideoCoverSharedTransition(
                transitionEnabled = true,
                hasSharedTransitionScope = false,
                hasAnimatedVisibilityScope = true
            )
        )
    }

    @Test
    fun metadataSharedTransition_keepsDefaultForNonHomeCallers() {
        assertEquals(VideoSharedTransitionProfile.COVER_AND_METADATA, resolveVideoSharedTransitionProfile())
        assertTrue(
            shouldEnableVideoMetadataSharedTransition(
                coverSharedEnabled = true,
                isQuickReturnLimited = false
            )
        )
    }

    @Test
    fun metadataSharedTransition_staysEnabledWhenQuickReturnLimitedForNonHomeCallers() {
        assertTrue(
            shouldEnableVideoMetadataSharedTransition(
                coverSharedEnabled = true,
                isQuickReturnLimited = true
            )
        )
    }

    @Test
    fun metadataSharedTransition_disabledWhenCardContainerOwnsSharedBounds() {
        assertFalse(
            shouldEnableVideoMetadataSharedTransition(
                coverSharedEnabled = true,
                isQuickReturnLimited = false,
                useCardContainerSharedBounds = true
            )
        )
    }

    @Test
    fun homeVideoTransition_usesCoverAsPrimaryAnchor() {
        val policy = resolveVideoSharedTransitionOwnership(
            sourceRoute = "home",
            coverSharedEnabled = true,
            isQuickReturnLimited = false
        )

        assertTrue(policy.useCoverSharedBounds)
        assertFalse(policy.useMetadataSharedBounds)
    }

    @Test
    fun videoCardShellKey_keepsSourceRouteDistinctFromCoverKey() {
        val shellKey = videoCardShellSharedElementKey(
            bvid = "BV1",
            sourceRoute = "history"
        )
        val coverKey = videoCoverSharedElementKey(
            bvid = "BV1",
            sourceRoute = "history"
        )

        assertEquals(VideoSharedElement.CARD_SHELL, shellKey.element)
        assertEquals("history", shellKey.sourceRoute)
        assertFalse(shellKey == coverKey)
    }

    @Test
    fun returnReboundOnlyRunsForMatchingVisibleSourceCard() {
        assertTrue(
            shouldPlayVideoCardReturnRebound(
                cardBvid = "BV1",
                cardSourceRoute = "history",
                returningSourceKey = "history:BV1",
                returningSourceRoute = "history",
                isReturningFromDetail = true,
                sharedTransitionReady = true
            )
        )
        assertFalse(
            shouldPlayVideoCardReturnRebound(
                cardBvid = "BV2",
                cardSourceRoute = "history",
                returningSourceKey = "history:BV1",
                returningSourceRoute = "history",
                isReturningFromDetail = true,
                sharedTransitionReady = true
            )
        )
        assertFalse(
            shouldPlayVideoCardReturnRebound(
                cardBvid = "BV1",
                cardSourceRoute = null,
                returningSourceKey = "history:BV1",
                returningSourceRoute = "history",
                isReturningFromDetail = true,
                sharedTransitionReady = true
            )
        )
    }

    @Test
    fun nonHomeVideoTransition_keepsMetadataSharedBoundsWhenAvailable() {
        val policy = resolveVideoSharedTransitionOwnership(
            sourceRoute = "search",
            coverSharedEnabled = true,
            isQuickReturnLimited = false
        )

        assertTrue(policy.useCoverSharedBounds)
        assertTrue(policy.useMetadataSharedBounds)
    }

    @Test
    fun detailContentReveal_disabledForHomeSharedTransitionShellMode() {
        // shell sharedBounds 接管整体 morph，详情内容不再单独 fade/slide。
        val motion = resolveVideoDetailContentRevealMotion(
            sourceRoute = "home",
            transitionEnabled = true
        )

        assertFalse(motion.enabled)
        assertEquals(0, motion.delayMillis)
        assertEquals(0, motion.durationMillis)
        assertEquals(0, motion.slideOffsetDp)
        assertEquals(1f, motion.initialScale, 0.0001f)
    }

    @Test
    fun detailContentReveal_disabledForAnySourceRouteUnderShellMode() {
        // 非首页源（search/dynamic 等）也走 shell sharedBounds，同样不允许二级 reveal。
        val motion = resolveVideoDetailContentRevealMotion(
            sourceRoute = "search",
            transitionEnabled = true
        )

        assertFalse(motion.enabled)
    }

    @Test
    fun homeSharedTransitionMotion_usesShortCoverPrimaryTimeline() {
        val motion = resolveHomeVideoSharedTransitionMotionSpec(
            sourceRoute = "home",
            transitionEnabled = true
        )

        assertTrue(motion.enabled)
        assertEquals(360, motion.durationMillis)
        assertEquals(40, motion.contentDelayMillis)
        assertEquals(220, motion.contentDurationMillis)
        assertEquals(14, motion.contentSlideOffsetDp)
        assertEquals(0.985f, motion.contentInitialScale, 0.0001f)
        assertTrue(motion.easing.transform(0.35f) > 0.7f)
        assertTrue(motion.easing.transform(0.75f) > 0.96f)
    }

    @Test
    fun homeSharedTransitionMotion_disabledForNonHomeSources() {
        val motion = resolveHomeVideoSharedTransitionMotionSpec(
            sourceRoute = "search",
            transitionEnabled = true
        )

        assertTrue(motion.enabled)
        assertEquals(360, motion.durationMillis)
    }

    @Test
    fun returnRebound_usesFastOutSlowFinishCurve() {
        val rebound = resolveVideoCardReturnReboundSpec(enabled = true)

        assertEquals(0.984f, rebound.startScale, 0.0001f)
        assertEquals(2.25f, rebound.startTranslationYDp, 0.0001f)
        assertEquals(0.64f, rebound.dampingRatio, 0.0001f)
        assertEquals(520f, rebound.stiffness, 0.0001f)
    }

    @Test
    fun homeSharedTransitionCornerSpec_softlyConvergesFromCardToPlayer() {
        val corner = resolveHomeVideoSharedTransitionCornerSpec(
            sourceRoute = "home",
            transitionEnabled = true
        )

        assertTrue(corner.enabled)
        assertEquals(16, corner.startCornerDp)
        assertEquals(12, corner.endCornerDp)
    }

    @Test
    fun detailContentReveal_disabledWithoutSharedTransition() {
        val motion = resolveVideoDetailContentRevealMotion(
            sourceRoute = "home",
            transitionEnabled = false
        )

        assertFalse(motion.enabled)
        assertEquals(0, motion.delayMillis)
        assertEquals(0, motion.slideOffsetDp)
        assertEquals(1f, motion.initialScale, 0.0001f)
    }

    @Test
    fun sharedCoverAspectRatio_defaultsToHomeCardSixteenByTen() {
        assertEquals(1.6f, VIDEO_SHARED_COVER_ASPECT_RATIO, 0.0001f)
    }
}
