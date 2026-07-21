package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.store.HomeCardBadgeEffectMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeVideoGlassBadgeStylePolicyTest {

    @Test
    fun softGlass_usesGlassStyleWithoutBlurFlag() {
        val visual = resolveHomeCardBadgeEffectVisual(
            mode = HomeCardBadgeEffectMode.SOFT_GLASS,
            scrollLiteModeEnabled = false
        )
        assertEquals(HomeVideoBadgeStyle.GLASS, visual.coverStyle)
        assertEquals(HomeVideoBadgeStyle.GLASS, visual.infoStyle)
        assertTrue(visual.glassEnabled)
        assertFalse(visual.blurEnabled)
        assertEquals(HomeCardBadgeEffectMode.SOFT_GLASS, visual.effectiveMode)
    }

    @Test
    fun lightBlur_degradesToSoftGlassWhileScrolling() {
        val idle = resolveHomeCardBadgeEffectVisual(
            mode = HomeCardBadgeEffectMode.LIGHT_BLUR,
            scrollLiteModeEnabled = false
        )
        val scrolling = resolveHomeCardBadgeEffectVisual(
            mode = HomeCardBadgeEffectMode.LIGHT_BLUR,
            scrollLiteModeEnabled = true
        )
        assertTrue(idle.blurEnabled)
        assertEquals(HomeCardBadgeEffectMode.LIGHT_BLUR, idle.effectiveMode)
        assertFalse(scrolling.blurEnabled)
        assertTrue(scrolling.glassEnabled)
        assertEquals(HomeCardBadgeEffectMode.SOFT_GLASS, scrolling.effectiveMode)
    }

    @Test
    fun off_usesPlainBadges() {
        val visual = resolveHomeCardBadgeEffectVisual(
            mode = HomeCardBadgeEffectMode.OFF,
            scrollLiteModeEnabled = false
        )
        assertEquals(HomeVideoBadgeStyle.PLAIN, visual.coverStyle)
        assertEquals(HomeVideoBadgeStyle.PLAIN, visual.infoStyle)
        assertFalse(visual.glassEnabled)
        assertFalse(visual.blurEnabled)
    }

    @Test
    fun legacyBooleanPolicy_mapsToSoftGlassOrOff() {
        val on = resolveHomeVideoGlassBadgeStylePolicy(
            showCoverGlassBadges = true,
            showInfoGlassBadges = true
        )
        val off = resolveHomeVideoGlassBadgeStylePolicy(
            showCoverGlassBadges = false,
            showInfoGlassBadges = false
        )
        assertEquals(HomeVideoBadgeStyle.GLASS, on.coverStyle)
        assertEquals(HomeVideoBadgeStyle.GLASS, on.infoStyle)
        assertEquals(HomeVideoBadgeStyle.PLAIN, off.coverStyle)
        assertEquals(HomeVideoBadgeStyle.PLAIN, off.infoStyle)
    }
}
