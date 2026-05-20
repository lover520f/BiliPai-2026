package com.android.purebilibili.navigation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppNavigationNavigation3BridgeStructureTest {

    @Test
    fun appNavigationMirrorsLegacyBackStackIntoNavigation3Keys() {
        val source = appNavigationSource()

        assertTrue(source.contains("navigation3BackStack"))
        assertTrue(source.contains("resolveBiliPaiNavKeyForLegacyBackStackEntry"))
        assertTrue(source.contains("pushBiliPaiNavKey"))
        assertTrue(source.contains("popBiliPaiNavKey"))
    }

    @Test
    fun videoReturnRouteLayerUsesNavigation3MotionDecision() {
        val source = appNavigationSource()

        assertTrue(source.contains("resolveBiliPaiNavMotionDecision"))
        assertTrue(source.contains("BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT"))
        assertTrue(source.contains("shouldInterceptSystemBackForNavigation3"))
    }

    @Test
    fun appNavigationMirrorsReturnStateIntoNavigation3SessionBeforeLegacyFallback() {
        val source = appNavigationSource()

        assertTrue(source.contains("BiliPaiReturnSessionState"))
        assertTrue(source.contains("navigation3ReturnSession"))
        assertTrue(source.contains("resolveBiliPaiNavSourceMetadata"))
        assertTrue(source.contains("navigation3ReturnSession.markReturning"))
        assertTrue(source.contains("navigation3ReturnSession.isQuickReturnFromDetail"))
        assertFalse(source.contains("isQuickReturnFromDetail = CardPositionManager.isQuickReturnFromDetail"))
    }

    @Test
    fun cardPositionManagerKeepsOnlyGeometryFallbackState() {
        val source = productionSourceExceptCardPositionManager()

        assertFalse(source.contains("CardPositionManager.isReturningFromDetail"))
        assertFalse(source.contains("CardPositionManager.isQuickReturnFromDetail"))
        assertFalse(source.contains("CardPositionManager.lastVideoSourceRoute"))
        assertFalse(source.contains("CardPositionManager.shouldLimitSharedElementsForQuickReturn()"))
        assertFalse(source.contains("CardPositionManager.markReturning()"))
        assertFalse(source.contains("CardPositionManager.clearReturning()"))
        assertFalse(source.contains("CardPositionManager.recordVideoSourceRoute("))
    }

    @Test
    fun appNavigationUsesNavDisplayAsSingleMainChain() {
        val source = appNavigationSource()

        assertTrue(source.contains("BiliPaiNavDisplayHost("))
        assertTrue(source.contains("sharedTransitionScope = LocalSharedTransitionScope.current"))
        assertTrue(source.contains("resolveBiliPaiNavEntryContentRole"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.HOME ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.DYNAMIC ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.SEARCH ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.PROFILE ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.VIDEO_DETAIL ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.HISTORY ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.WATCH_LATER ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.FAVORITE ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.LOGIN ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.STORY ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.PARTITION ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.CATEGORY ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.SPACE ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.WEB ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.DYNAMIC_DETAIL ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.ARTICLE_DETAIL ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.LIVE ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.BANGUMI_DETAIL ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.OPEN_SOURCE_LICENSES ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.APPEARANCE_SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.ICON_SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.ANIMATION_SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.PLAYBACK_SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.PERMISSION_SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.PLUGINS_SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.BOTTOM_BAR_SETTINGS ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.SETTINGS_SHARE ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.WEB_DAV_BACKUP ->"))
        assertTrue(source.contains("BiliPaiNavEntryContentRole.TIPS_SETTINGS ->"))
        assertTrue(source.contains("onBack = { performSystemBackAction() }"))
        assertFalse(source.contains("shouldUseBiliPaiNavDisplayMainChain()"))
        assertFalse(source.contains("else NavHost("))
    }

    private fun appNavigationSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"),
            File("src/main/java/com/android/purebilibili/navigation/AppNavigation.kt")
        ).first { it.exists() }.readText()
    }

    private fun productionSourceExceptCardPositionManager(): String {
        val root = listOf(
            File("app/src/main/java"),
            File("src/main/java")
        ).first { it.exists() }

        return root
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name != "CardPositionManager.kt" }
            .joinToString(separator = "\n") { it.readText() }
    }
}
