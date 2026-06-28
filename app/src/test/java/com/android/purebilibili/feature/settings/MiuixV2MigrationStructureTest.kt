package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiuixV2MigrationStructureTest {

    @Test
    fun webDavBackupScreen_usesAdaptiveScaffold_notLargeTitleBar() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/webdav/WebDavBackupScreen.kt")
        assertTrue(source.contains("AdaptiveScaffold("))
        assertTrue(source.contains("AdaptiveTopAppBar("))
        assertFalse(source.contains("iOSLargeTitleBar("))
        assertFalse(source.contains("globalWallpaperAwareBackground("))
    }

    @Test
    fun settingsShareScreen_usesAdaptiveScaffold_notLargeTitleBar() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/share/SettingsShareScreen.kt")
        assertTrue(source.contains("AdaptiveScaffold("))
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
    fun buildGradle_pinsMiuixVersionTo092() {
        val source = loadSource("app/build.gradle.kts")
        assertTrue(source.contains("val miuixVersion = \"0.9.2\""))
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