package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.store.PredictiveBackAnimationStyle
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AnimationSettingsPolicyTest {

    @Test
    fun predictiveBackEntry_usesSelectedStyleState() {
        val aosp = resolvePredictiveBackToggleUiState(
            predictiveBackAnimationStyle = PredictiveBackAnimationStyle.AOSP
        )
        assertTrue(aosp.enabled)
        assertEquals(PredictiveBackAnimationStyle.AOSP, aosp.selectedStyle)
        assertEquals(PREDICTIVE_BACK_ANIMATION_TITLE, aosp.title)
        assertEquals("预测性返回动画", aosp.title)
        assertEquals("当前：AOSP", aosp.subtitle)

        val none = resolvePredictiveBackToggleUiState(
            predictiveBackAnimationStyle = PredictiveBackAnimationStyle.NONE
        )
        assertTrue(none.enabled)
        assertEquals(PredictiveBackAnimationStyle.NONE, none.selectedStyle)
        assertEquals("当前：无", none.subtitle)
    }

    @Test
    fun predictiveBackEntry_ignoresCardTransitionState() {
        val state = resolvePredictiveBackToggleUiState(
            predictiveBackAnimationStyle = PredictiveBackAnimationStyle.AOSP
        )

        assertTrue(state.enabled)
        assertEquals(PredictiveBackAnimationStyle.AOSP, state.selectedStyle)
        assertEquals(PREDICTIVE_BACK_ANIMATION_TITLE, state.title)
        assertEquals("当前：AOSP", state.subtitle)
    }

    @Test
    fun predictiveBackStyles_matchInstallerXDialogOrder() {
        assertEquals(
            listOf("无", "AOSP", "Miuix", "缩放", "经典"),
            PredictiveBackAnimationStyle.entries.map { it.displayName }
        )
        assertEquals(PredictiveBackAnimationStyle.AOSP, PredictiveBackAnimationStyle.Default)
    }

    @Test
    fun liquidGlassPreviewUiState_usesContinuousCopy() {
        val clear = resolveLiquidGlassPreviewUiState(progress = 0.1f)
        val frosted = resolveLiquidGlassPreviewUiState(progress = 0.9f)

        assertEquals("通透", clear.modeLabel)
        assertTrue(clear.subtitle.contains("清晰"))
        assertEquals("磨砂", frosted.modeLabel)
        assertTrue(frosted.subtitle.contains("柔和"))
        assertNotEquals("平衡", clear.modeLabel)
        assertNotEquals("平衡", frosted.modeLabel)
    }

    @Test
    fun liquidGlassPreviewUiState_clampsAndFormatsProgress() {
        val state = resolveLiquidGlassPreviewUiState(progress = 1.4f)

        assertEquals(1f, state.normalizedProgress)
        assertEquals("100%", state.strengthLabel)
    }

    @Test
    fun bottomBarLiquidGlassPresetControl_livesInAnimationSettings() {
        val animationSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"
        )
        val bottomBarSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/BottomBarSettingsScreen.kt"
        )
        val settingsManagerSource = loadSource(
            "app/src/main/java/com/android/purebilibili/core/store/SettingsManager.kt"
        )

        assertFalse(animationSource.contains("底栏液态玻璃预设"))
        assertFalse(animationSource.contains("BottomBarLiquidGlassPreset.entries"))
        assertTrue(animationSource.contains("listOf(BottomBarLiquidGlassPreset.BILIPAI_TUNED)"))
        assertTrue(settingsManagerSource.contains("TODO: 通透底栏液态玻璃已移除"))
        assertFalse(settingsManagerSource.contains("更轻的模糊、更低的遮罩和更清晰的背景折射"))
        val forbiddenExternalName = listOf("Na", "gram", "X").joinToString("")
        assertFalse(animationSource.contains(forbiddenExternalName))
        assertFalse(animationSource.contains(forbiddenExternalName.lowercase()))
        assertFalse(settingsManagerSource.contains(forbiddenExternalName))
        assertFalse(settingsManagerSource.contains(forbiddenExternalName.lowercase()))
        assertFalse(animationSource.contains("底栏跟随高光"))
        assertFalse(animationSource.contains("getBottomBarInteractiveHighlightEnabled"))
        assertFalse(animationSource.contains("setBottomBarInteractiveHighlightEnabled"))
        assertFalse(bottomBarSource.contains("底栏液态玻璃预设"))
        assertFalse(bottomBarSource.contains("BottomBarLiquidGlassPreset.entries"))
        assertFalse(bottomBarSource.contains("底栏跟随高光"))
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
