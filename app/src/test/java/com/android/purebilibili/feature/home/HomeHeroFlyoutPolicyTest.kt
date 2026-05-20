package com.android.purebilibili.feature.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeHeroFlyoutPolicyTest {

    @Test
    fun gridVideoRequest_runsHeroFlyoutBeforeNavigation() {
        val request = HomeVideoClickRequest(
            bvid = "BV1hero",
            cid = 12L,
            coverUrl = "cover",
            source = HomeVideoClickSource.GRID
        )

        assertTrue(shouldRunHomeHeroFlyoutBeforeNavigation(request))
    }

    @Test
    fun nonGridOrDynamicPlaceholder_skipsHeroFlyout() {
        assertFalse(
            shouldRunHomeHeroFlyoutBeforeNavigation(
                HomeVideoClickRequest(
                    bvid = "BV1today",
                    source = HomeVideoClickSource.TODAY_WATCH
                )
            )
        )
        assertFalse(
            shouldRunHomeHeroFlyoutBeforeNavigation(
                HomeVideoClickRequest(
                    bvid = "DYN_123",
                    dynamicId = "123",
                    source = HomeVideoClickSource.GRID
                )
            )
        )
    }

    @Test
    fun flyoutFrame_movesWholeCardOutClearly() {
        val start = resolveHomeHeroFlyoutFrame(progress = 0f)
        val end = resolveHomeHeroFlyoutFrame(progress = 1f)

        assertEquals(1f, start.alpha, 0.0001f)
        assertEquals(1f, start.scale, 0.0001f)
        assertEquals(0f, start.translationYDp, 0.0001f)
        assertEquals(0f, end.alpha, 0.0001f)
        assertEquals(1.08f, end.scale, 0.0001f)
        assertEquals(-56f, end.translationYDp, 0.0001f)
        assertEquals(180, resolveHomeHeroFlyoutDurationMillis())
        assertEquals(180L, resolveHomeHeroFlyoutNavigationDelayMillis())
    }
}
