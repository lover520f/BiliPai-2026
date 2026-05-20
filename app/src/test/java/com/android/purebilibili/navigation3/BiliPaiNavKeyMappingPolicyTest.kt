package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation.ScreenRoutes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BiliPaiNavKeyMappingPolicyTest {

    @Test
    fun topLevelRoutes_mapToNavigation3Keys() {
        assertEquals(BiliPaiNavKey.Home, legacyRouteToBiliPaiNavKey(ScreenRoutes.Home.route))
        assertEquals(BiliPaiNavKey.Dynamic, legacyRouteToBiliPaiNavKey(ScreenRoutes.Dynamic.route))
        assertEquals(BiliPaiNavKey.Search, legacyRouteToBiliPaiNavKey(ScreenRoutes.Search.route))
        assertEquals(BiliPaiNavKey.Profile, legacyRouteToBiliPaiNavKey(ScreenRoutes.Profile.route))
    }

    @Test
    fun videoRoute_preservesNavigationArguments() {
        val route = "video/BV1xx411c7mD?cid=123&cover=https%3A%2F%2Fexample.com%2Fcover.jpg" +
            "&startAudio=true&autoPortrait=true&fullscreen=true&resumePositionMs=456&commentRootRpid=789"

        val key = assertIs<BiliPaiNavKey.VideoDetail>(legacyRouteToBiliPaiNavKey(route))

        assertEquals("BV1xx411c7mD", key.bvid)
        assertEquals(123L, key.cid)
        assertEquals("https://example.com/cover.jpg", key.coverUrl)
        assertEquals(true, key.startAudio)
        assertEquals(true, key.autoPortrait)
        assertEquals(true, key.fullscreen)
        assertEquals(456L, key.resumePositionMs)
        assertEquals(789L, key.commentRootRpid)
    }

    @Test
    fun navKey_roundTripsToLegacyRouteForCurrentBridge() {
        val key = BiliPaiNavKey.Space(mid = 42L)

        assertEquals(ScreenRoutes.Space.createRoute(42L), key.toLegacyRoute())
        assertEquals(key, legacyRouteToBiliPaiNavKey(key.toLegacyRoute()))
    }

    @Test
    fun settingsSecondaryRoutes_mapToNavigation3Keys() {
        assertEquals(BiliPaiNavKey.OpenSourceLicenses, legacyRouteToBiliPaiNavKey(ScreenRoutes.OpenSourceLicenses.route))
        assertEquals(BiliPaiNavKey.AppearanceSettings, legacyRouteToBiliPaiNavKey(ScreenRoutes.AppearanceSettings.route))
        assertEquals(BiliPaiNavKey.IconSettings, legacyRouteToBiliPaiNavKey(ScreenRoutes.IconSettings.route))
        assertEquals(BiliPaiNavKey.AnimationSettings, legacyRouteToBiliPaiNavKey(ScreenRoutes.AnimationSettings.route))
        assertEquals(BiliPaiNavKey.PlaybackSettings, legacyRouteToBiliPaiNavKey(ScreenRoutes.PlaybackSettings.route))
        assertEquals(BiliPaiNavKey.PermissionSettings, legacyRouteToBiliPaiNavKey(ScreenRoutes.PermissionSettings.route))
        assertEquals(BiliPaiNavKey.PluginsSettings(), legacyRouteToBiliPaiNavKey(ScreenRoutes.PluginsSettings.createRoute()))
        val pluginImportRoute = "plugins_settings?importUrl=https%3A%2F%2Fexample.com%2Fa.bpplugin"
        assertEquals(
            BiliPaiNavKey.PluginsSettings(importUrl = "https://example.com/a.bpplugin"),
            legacyRouteToBiliPaiNavKey(pluginImportRoute)
        )
        assertEquals(BiliPaiNavKey.BottomBarSettings, legacyRouteToBiliPaiNavKey(ScreenRoutes.BottomBarSettings.route))
        assertEquals(BiliPaiNavKey.SettingsShare, legacyRouteToBiliPaiNavKey(ScreenRoutes.SettingsShare.route))
        assertEquals(BiliPaiNavKey.WebDavBackup, legacyRouteToBiliPaiNavKey(ScreenRoutes.WebDavBackup.route))
        assertEquals(BiliPaiNavKey.TipsSettings, legacyRouteToBiliPaiNavKey(ScreenRoutes.TipsSettings.route))
    }

    @Test
    fun cardReturnTargets_matchExistingSharedElementDestinations() {
        assertEquals(true, isCardReturnTargetNavKey(BiliPaiNavKey.Home))
        assertEquals(true, isCardReturnTargetNavKey(BiliPaiNavKey.Search))
        assertEquals(true, isCardReturnTargetNavKey(BiliPaiNavKey.Space(42L)))
        assertEquals(false, isCardReturnTargetNavKey(BiliPaiNavKey.VideoDetail("BV1")))
        assertEquals(false, isCardReturnTargetNavKey(BiliPaiNavKey.Settings))
    }
}
