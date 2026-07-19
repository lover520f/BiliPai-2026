package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationSettingsScreenStructureTest {

    @Test
    fun animationSettingsScreen_controlsGlobalPredictivePreviewIndependently() {
        val source = animationSettingsSource()

        assertTrue(source.contains("title = \"预测性返回手势\""))
        assertTrue(source.contains("SettingsManager.setPredictiveBackEnabled(context, enabled)"))
        val predictiveItem = source
            .substringAfter("title = \"预测性返回手势\"")
            .substringBefore("IOSDivider()")
        assertFalse(predictiveItem.contains("enabled = state.cardTransitionEnabled"))
        assertFalse(source.contains("setPredictiveBackAnimationStyle"))
        assertFalse(source.contains("setPredictiveBackExitDirection"))
        assertFalse(source.contains("resolvePredictiveBackStyleOptions"))
        assertFalse(source.contains("resolvePredictiveBackExitDirectionOptions"))
    }

    @Test
    fun animationSettingsScreen_exposesRealtimeTransitionBlurToggle() {
        val source = animationSettingsSource()

        assertTrue(source.contains("title = \"过渡动画实时模糊\""))
        assertTrue(source.contains("checked = videoTransitionRealtimeBlurEnabled"))
        assertTrue(source.contains("toggleVideoTransitionRealtimeBlur"))
    }

    private fun animationSettingsSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
        ).first { it.exists() }.readText()
    }
}
