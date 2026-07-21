package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiuixV2MigrationStructureTest {

    @Test
    fun webDavBackupScreen_usesSettingsPageScaffold_notLargeTitleBar() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/webdav/WebDavBackupScreen.kt")
        assertTrue(source.contains("SettingsPageScaffold("))
        assertFalse(source.contains("iOSLargeTitleBar("))
        assertFalse(source.contains("globalWallpaperAwareBackground("))
    }

    @Test
    fun settingsShareScreen_usesSettingsPageScaffold_notLargeTitleBar() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/share/SettingsShareScreen.kt")
        assertTrue(source.contains("SettingsPageScaffold("))
        assertFalse(source.contains("iOSLargeTitleBar("))
    }

    @Test
    fun iosSectionTitle_usesMiuixSmallTitleOnMiuixBranch() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/components/iOSListComponents.kt")
        assertTrue(source.contains("SmallTitle("))
        assertTrue(source.contains("androidNativeVariant == AndroidNativeVariant.MIUIX"))
    }

    @Test
    fun iosAlertDialog_routesMiuixVariantToOverlayDialog() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/iOSDialogComponents.kt")
        assertTrue(source.contains("OverlayDialog("))
        assertTrue(source.contains("androidNativeVariant == AndroidNativeVariant.MIUIX"))
    }

    @Test
    fun appSurfaceTokens_exposesMiuixSemanticColors() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/AppSurfaceTokens.kt")
        assertTrue(source.contains("fun onSurfaceVariantSummary()"))
        assertTrue(source.contains("fun onSurfaceVariantActions()"))
        assertTrue(source.contains("MiuixTheme.colorScheme.onSurfaceVariantSummary"))
    }

    @Test
    fun buildGradle_pinsMiuixVersionTo093() {
        val source = loadSource("app/build.gradle.kts")
        assertTrue(source.contains("val miuixVersion = \"0.9.3\""))
    }

    @Test
    fun buildGradle_includesMiuixShaderArtifact() {
        val source = loadSource("app/build.gradle.kts")
        assertTrue(source.contains("miuix-shader-android"))
    }

    @Test
    fun featureLayer_avoidsDirectMiuixThemeColorSchemeReads() {
        val allowed = setOf(
            "app/src/main/java/com/android/purebilibili/core/theme/Theme.kt",
            "app/src/main/java/com/android/purebilibili/core/ui/AppSurfaceTokens.kt",
            "app/src/main/java/com/android/purebilibili/core/ui/components/iOSListComponents.kt"
        )
        val offenders = listOf(
            "app/src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt",
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt",
            "app/src/main/java/com/android/purebilibili/feature/message/InboxScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/message/feed/MessageFeedCommon.kt",
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/VideoSettingsPanel.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/IOSSlidingSegmentedControl.kt"
        ).filter { path ->
            loadSource(path).contains("MiuixTheme.colorScheme")
        }
        assertTrue(
            offenders.isEmpty(),
            "Direct MiuixTheme.colorScheme reads should route through AppSurfaceTokens:\n" +
                offenders.joinToString("\n")
        )
    }

    @Test
    fun buildGradle_includesMiuixSquircleArtifact() {
        val source = loadSource("app/build.gradle.kts")
        assertTrue(source.contains("miuix-squircle-android"))
    }

    @Test
    fun buildGradle_includesMiuixIconsArtifact() {
        val source = loadSource("app/build.gradle.kts")
        assertTrue(source.contains("miuix-icons-android"))
    }

    @Test
    fun md3SegmentedControl_routesMiuixVariantToTabRow() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/IOSSlidingSegmentedControl.kt")
        assertTrue(source.contains("resolveMd3SegmentedControlRenderer("))
        assertTrue(source.contains("MiuixTabRowSegmentedControl("))
        assertTrue(source.contains("MiuixTabRow("))
    }

    @Test
    fun adaptivePullToRefreshBox_routesMiuixVariantToMiuixPullToRefresh() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/AdaptivePullToRefreshBox.kt")
        assertTrue(source.contains("MiuixPullToRefresh("))
        assertTrue(source.contains("PresetPrimitiveRenderer.MIUIX_BRIDGED"))
        assertTrue(source.contains("ComfortablePullToRefreshBox("))
        assertTrue(source.contains("indicatorTopInset"))
        assertTrue(source.contains("mergedContentPadding"))
    }

    @Test
    fun homeScreen_usesAdaptivePullToRefreshBox() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        assertTrue(source.contains("AdaptivePullToRefreshBox("))
        assertFalse(source.contains("ComfortablePullToRefreshBox("))
    }

    @Test
    fun ioSearchBar_miuixBranchUsesOfficialInputField() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/components/iOSListComponents.kt")
        assertTrue(source.contains("InputField("))
        assertTrue(source.contains("shouldUseNativeMiuixSearchBar("))
    }

    @Test
    fun iosClickableItem_routesThroughAdaptiveListItemPolicy() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/components/iOSListComponents.kt")
        assertTrue(source.contains("resolveIosClickableItemRenderer("))
        assertTrue(source.contains("IosClickableItemRenderer.MIUIX_ARROW"))
        assertTrue(source.contains("IosClickableItemRenderer.MIUIX_BASIC"))
        assertTrue(source.contains("shouldRouteIosSwitchItemToMiuixSwitchPreference("))
        assertTrue(source.contains("shouldRouteIosSliderPreferenceToMiuixSliderPreference("))
        assertTrue(source.contains("MiuixSliderPreference("))
    }

    @Test
    fun themeController_remembersUserThemeInputsWithMiuixBridgeColors() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/theme/Theme.kt")
        assertTrue(source.contains("ThemeController("))
        assertTrue(source.contains("customPrimaryColor,"))
        assertTrue(source.contains("themeRoleOverrides,"))
        assertTrue(source.contains("amoledDarkTheme,"))
        assertTrue(source.contains("resolveMiuixColorsFromMaterialBridge("))
    }

    @Test
    fun miuixDockedBottomBar_usesOfficialNavigationBarItemForStandardItems() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")
        assertTrue(source.contains("MiuixNavigationBarItem("))
        assertTrue(source.contains("shouldUseMiuixOfficialNavigationBarItem("))
    }

    @Test
    fun searchTopBar_routesMiuixVariantToInputField() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt")
        assertTrue(source.contains("InputField("))
        assertTrue(source.contains("shouldUseNativeMiuixSearchBar("))
    }

    @Test
    fun homePullRefreshPolicy_routesMiuixToNativeIndicator() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomePullRefreshUiPolicy.kt")
        assertTrue(source.contains("HomePullRefreshIndicatorStyle.MIUIX_NATIVE"))
    }

    @Test
    fun adaptiveScaffold_miuixPathMountsPopupHostForOverlayDialogs() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/AdaptiveChrome.kt")
        assertTrue(source.contains("resolveAdaptiveScaffoldRenderer("))
        assertTrue(source.contains("MiuixPopupUtils.MiuixPopupHost()"))
        assertTrue(source.contains("popupHost ="))
    }

    @Test
    fun adaptiveScaffoldPolicy_requiresMiuixPopupHostOnMiuixVariant() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/AdaptiveScaffoldPolicy.kt")
        assertTrue(source.contains("shouldMountMiuixPopupHostOnAdaptiveScaffold("))
        assertTrue(source.contains("MIUIX_SCAFFOLD_WITH_POPUP_HOST"))
    }

    @Test
    fun featureLayer_doesNotCallIosLargeTitleBarDirectly() {
        val featureRoot = File("app/src/main/java/com/android/purebilibili/feature")
        val offenders = featureRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains("iOSLargeTitleBar(") }
            .map { it.path }
            .toList()
        assertTrue(
            offenders.isEmpty(),
            "iOSLargeTitleBar should remain iOS-only chrome; migrate feature screens to AdaptiveScaffold:\n" +
                offenders.joinToString("\n")
        )
    }

    @Test
    fun iosLargeTitleBar_routesMd3AndMiuixToAdaptiveTopAppBar() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/iOSLargeTitleBar.kt")
        assertTrue(source.contains("resolveLargeTitleBarRenderer("))
        assertTrue(source.contains("IOSLargeTitleBarRenderer.ADAPTIVE_TOP_APP_BAR"))
        assertTrue(source.contains("AdaptiveTopAppBar("))
    }

    @Test
    fun md3SegmentedControl_usesAdaptiveSquircleBackground() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/IOSSlidingSegmentedControl.kt"
        )
        assertTrue(source.contains("adaptiveSquircleBackground("))
    }

    @Test
    fun appSurfaceTokens_exposesFullMiuixSemanticPalette() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/ui/AppSurfaceTokens.kt")
        listOf(
            "fun background()",
            "fun surface()",
            "fun surfaceContainer()",
            "fun surfaceContainerHigh()",
            "fun onSecondaryContainer()",
            "fun onSurfaceContainerHigh()",
            "fun onSurfaceContainerHighest()",
            "fun primary()",
            "fun resolveMiuixSemanticColor("
        ).forEach { token ->
            assertTrue(source.contains(token), "Missing token: $token")
        }
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}