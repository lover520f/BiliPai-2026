package com.android.purebilibili.feature.video.screen

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoDetailShellEnterMorphPolicyTest {

    @Test
    fun suppressSkeleton_onlyWhileShellEnterMorphActive() {
        assertTrue(
            shouldSuppressDetailSkeletonDuringShellEnterMorph(
                detailShellSharedBoundsEnabled = true,
                isSharedTransitionActive = true,
                isExitTransitionInProgress = false,
            )
        )
        assertFalse(
            shouldSuppressDetailSkeletonDuringShellEnterMorph(
                detailShellSharedBoundsEnabled = true,
                isSharedTransitionActive = true,
                isExitTransitionInProgress = true,
            )
        )
        assertFalse(
            shouldSuppressDetailSkeletonDuringShellEnterMorph(
                detailShellSharedBoundsEnabled = true,
                isSharedTransitionActive = false,
                isExitTransitionInProgress = false,
            )
        )
        assertFalse(
            shouldSuppressDetailSkeletonDuringShellEnterMorph(
                detailShellSharedBoundsEnabled = false,
                isSharedTransitionActive = true,
                isExitTransitionInProgress = false,
            )
        )
    }
}
