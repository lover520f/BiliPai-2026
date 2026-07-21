package com.android.purebilibili.core.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.android.purebilibili.core.store.home.HomeSettingsStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeSettingsStoreParityTest {

    @Test
    fun `home store maps defaults the same way as settings manager policy`() {
        val prefs = mutablePreferencesOf()

        assertEquals(
            mapHomeSettingsFromPreferences(prefs),
            HomeSettingsStore.mapFromPreferences(prefs)
        )
    }

    @Test
    fun `home store maps populated preferences the same way as settings manager policy`() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("display_mode") to 1,
            booleanPreferencesKey("bottom_bar_floating") to false,
            intPreferencesKey("bottom_bar_label_mode") to 2
        )

        assertEquals(
            mapHomeSettingsFromPreferences(prefs),
            HomeSettingsStore.mapFromPreferences(prefs)
        )
    }

    @Test
    fun `home settings defaults enable soft glass card badges`() {
        val result = mapHomeSettingsFromPreferences(mutablePreferencesOf())

        assertTrue(result.showHomeCoverGlassBadges)
        assertTrue(result.showHomeInfoGlassBadges)
        assertEquals(HomeCardBadgeEffectMode.SOFT_GLASS, result.homeCardBadgeEffectMode)
        assertEquals(HomeWallpaperEffectMode.SOFT_BLUR, result.homeWallpaperEffectMode)
        assertEquals(HomeWallpaperEffectScope.HOME_ONLY, result.homeWallpaperEffectScope)
        assertTrue(result.showHomeUpBadges)
        assertEquals(HomeDurationStyle.OUTSIDE_COVER, result.homeDurationStyle)
    }

    @Test
    fun `home settings honor explicit card badge effect mode`() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("home_card_badge_effect_mode") to HomeCardBadgeEffectMode.LIGHT_BLUR.value,
            intPreferencesKey("home_wallpaper_effect_mode") to HomeWallpaperEffectMode.OFF.value,
            intPreferencesKey("home_wallpaper_effect_scope") to HomeWallpaperEffectScope.GLOBAL.value,
            booleanPreferencesKey("home_up_badges_visible") to false,
            booleanPreferencesKey("home_video_duration_badges_visible") to false
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertTrue(result.showHomeCoverGlassBadges)
        assertTrue(result.showHomeInfoGlassBadges)
        assertEquals(HomeCardBadgeEffectMode.LIGHT_BLUR, result.homeCardBadgeEffectMode)
        assertEquals(HomeWallpaperEffectMode.OFF, result.homeWallpaperEffectMode)
        assertEquals(HomeWallpaperEffectScope.GLOBAL, result.homeWallpaperEffectScope)
        assertEquals(false, result.showHomeUpBadges)
        assertEquals(HomeDurationStyle.HIDDEN, result.homeDurationStyle)
    }

    @Test
    fun `home settings map legacy both glass flags off to effect off`() {
        val prefs = mutablePreferencesOf(
            booleanPreferencesKey("home_cover_glass_badges_visible") to false,
            booleanPreferencesKey("home_info_glass_badges_visible") to false
        )
        val result = mapHomeSettingsFromPreferences(prefs)
        assertEquals(HomeCardBadgeEffectMode.OFF, result.homeCardBadgeEffectMode)
        assertFalse(result.showHomeCoverGlassBadges)
        assertFalse(result.showHomeInfoGlassBadges)
    }
}
