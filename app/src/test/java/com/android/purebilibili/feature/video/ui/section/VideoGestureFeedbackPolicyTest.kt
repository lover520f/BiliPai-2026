package com.android.purebilibili.feature.video.ui.section

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.feature.video.ui.components.GesturePercentTransitionDirection
import com.android.purebilibili.feature.video.ui.components.resolveGesturePercentTransitionDirection
import com.android.purebilibili.feature.video.ui.components.shouldTriggerGesturePercentHaptic
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.VolumeOff
import top.yukonga.miuix.kmp.icon.extended.VolumeUp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoGestureFeedbackPolicyTest {

    @Test
    fun `resolveOrientationSwitchHintText returns landscape copy`() {
        assertEquals("已切换到横屏", resolveOrientationSwitchHintText(isFullscreen = true))
    }

    @Test
    fun `resolveOrientationSwitchHintText returns portrait copy`() {
        assertEquals("已切换到竖屏", resolveOrientationSwitchHintText(isFullscreen = false))
    }

    @Test
    fun `resolveGestureIndicatorLabel returns brightness label`() {
        assertEquals("亮度", resolveGestureIndicatorLabel(VideoGestureMode.Brightness))
    }

    @Test
    fun `resolveGestureIndicatorLabel returns volume label`() {
        assertEquals("音量", resolveGestureIndicatorLabel(VideoGestureMode.Volume))
    }

    @Test
    fun `resolveGestureIndicatorLabel returns empty for non level mode`() {
        assertEquals("", resolveGestureIndicatorLabel(VideoGestureMode.Seek))
    }

    @Test
    fun `resolveGestureLevelIconStyle maps three distinct themes`() {
        assertEquals(
            GestureLevelIconStyle.Md3,
            resolveGestureLevelIconStyle(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertEquals(
            GestureLevelIconStyle.Ios,
            resolveGestureLevelIconStyle(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertEquals(
            GestureLevelIconStyle.Miuix,
            resolveGestureLevelIconStyle(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
    }

    @Test
    fun `resolveGestureDisplayIcon maps brightness level for md3`() {
        assertEquals(
            Icons.Filled.BrightnessLow,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Brightness,
                percent = 0.2f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
        assertEquals(
            Icons.Filled.BrightnessMedium,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Brightness,
                percent = 0.52f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
        assertEquals(
            Icons.Filled.BrightnessHigh,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Brightness,
                percent = 0.92f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
    }

    @Test
    fun `resolveGestureDisplayIcon maps volume level for md3`() {
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeOff,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Volume,
                percent = 0f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeMute,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Volume,
                percent = 0.2f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeDown,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Volume,
                percent = 0.5f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeUp,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Volume,
                percent = 0.9f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
    }

    @Test
    fun `resolveGestureDisplayIcon maps miuix volume and brightness ladders`() {
        assertEquals(
            MiuixIcons.VolumeOff,
            resolveVolumeGestureIcon(
                percent = 0f,
                iconStyle = GestureLevelIconStyle.Miuix
            )
        )
        assertEquals(
            MiuixIcons.VolumeUp,
            resolveVolumeGestureIcon(
                percent = 0.9f,
                iconStyle = GestureLevelIconStyle.Miuix
            )
        )
        assertEquals(
            Icons.Filled.Brightness4,
            resolveBrightnessGestureIcon(
                percent = 0.1f,
                iconStyle = GestureLevelIconStyle.Miuix
            )
        )
        assertEquals(
            Icons.Filled.Brightness7,
            resolveBrightnessGestureIcon(
                percent = 0.9f,
                iconStyle = GestureLevelIconStyle.Miuix
            )
        )
    }

    @Test
    fun `resolveGestureDisplayIcon falls back for unsupported mode`() {
        assertEquals(
            Icons.Filled.BrightnessHigh,
            resolveGestureDisplayIcon(
                mode = VideoGestureMode.Seek,
                percent = 0.5f,
                fallbackIcon = null,
                iconStyle = GestureLevelIconStyle.Md3
            )
        )
    }

    @Test
    fun `shouldTriggerFullscreenBySwipe follows default direction`() {
        assertEquals(
            true,
            shouldTriggerFullscreenBySwipe(
                isFullscreen = false,
                reverseGesture = false,
                totalDragDistanceY = -80f,
                thresholdPx = 50f
            )
        )
        assertEquals(
            true,
            shouldTriggerFullscreenBySwipe(
                isFullscreen = true,
                reverseGesture = false,
                totalDragDistanceY = 80f,
                thresholdPx = 50f
            )
        )
    }

    @Test
    fun `shouldTriggerFullscreenBySwipe supports reverse direction`() {
        assertEquals(
            true,
            shouldTriggerFullscreenBySwipe(
                isFullscreen = false,
                reverseGesture = true,
                totalDragDistanceY = 80f,
                thresholdPx = 50f
            )
        )
        assertEquals(
            true,
            shouldTriggerFullscreenBySwipe(
                isFullscreen = true,
                reverseGesture = true,
                totalDragDistanceY = -80f,
                thresholdPx = 50f
            )
        )
    }

    @Test
    fun `resolveVerticalGestureMode uses portrait swipe setting for upward entry`() {
        assertEquals(
            VideoGestureMode.SwipeToFullscreen,
            resolveVerticalGestureMode(
                isFullscreen = false,
                isSwipeUp = true,
                startX = 500f,
                leftZoneEnd = 300f,
                rightZoneStart = 700f,
                portraitSwipeToFullscreenEnabled = true,
                centerSwipeToFullscreenEnabled = false
            )
        )
    }

    @Test
    fun `resolveVerticalGestureMode keeps edge volume brightness ahead of portrait fullscreen`() {
        assertEquals(
            VideoGestureMode.Brightness,
            resolveVerticalGestureMode(
                isFullscreen = false,
                isSwipeUp = true,
                startX = 120f,
                leftZoneEnd = 300f,
                rightZoneStart = 700f,
                portraitSwipeToFullscreenEnabled = true,
                centerSwipeToFullscreenEnabled = false
            )
        )
        assertEquals(
            VideoGestureMode.Volume,
            resolveVerticalGestureMode(
                isFullscreen = false,
                isSwipeUp = true,
                startX = 920f,
                leftZoneEnd = 300f,
                rightZoneStart = 700f,
                portraitSwipeToFullscreenEnabled = true,
                centerSwipeToFullscreenEnabled = false
            )
        )
    }

    @Test
    fun `resolveVerticalGestureMode uses center setting in fullscreen independently`() {
        assertEquals(
            VideoGestureMode.SwipeToFullscreen,
            resolveVerticalGestureMode(
                isFullscreen = true,
                isSwipeUp = false,
                startX = 500f,
                leftZoneEnd = 300f,
                rightZoneStart = 700f,
                portraitSwipeToFullscreenEnabled = false,
                centerSwipeToFullscreenEnabled = true
            )
        )
    }

    @Test
    fun `resolveVerticalGestureMode returns none when center swipe disabled`() {
        assertEquals(
            VideoGestureMode.None,
            resolveVerticalGestureMode(
                isFullscreen = true,
                isSwipeUp = false,
                startX = 500f,
                leftZoneEnd = 300f,
                rightZoneStart = 700f,
                portraitSwipeToFullscreenEnabled = false,
                centerSwipeToFullscreenEnabled = false
            )
        )
    }

    @Test
    fun `resolveVerticalGestureMode disables brightness volume when setting off`() {
        assertEquals(
            VideoGestureMode.None,
            resolveVerticalGestureMode(
                isFullscreen = true,
                isSwipeUp = false,
                startX = 120f,
                leftZoneEnd = 300f,
                rightZoneStart = 700f,
                portraitSwipeToFullscreenEnabled = false,
                centerSwipeToFullscreenEnabled = false,
                slideVolumeBrightnessEnabled = false
            )
        )
        assertEquals(
            VideoGestureMode.None,
            resolveVerticalGestureMode(
                isFullscreen = true,
                isSwipeUp = false,
                startX = 920f,
                leftZoneEnd = 300f,
                rightZoneStart = 700f,
                portraitSwipeToFullscreenEnabled = false,
                centerSwipeToFullscreenEnabled = false,
                slideVolumeBrightnessEnabled = false
            )
        )
    }

    @Test
    fun `shouldShowDanmakuLayers respects pip no danmaku setting`() {
        assertEquals(
            true,
            shouldShowDanmakuLayers(
                isInPipMode = true,
                danmakuEnabled = true,
                isPortraitFullscreen = false,
                pipNoDanmakuEnabled = false,
                hostLifecycleStarted = true
            )
        )
        assertEquals(
            false,
            shouldShowDanmakuLayers(
                isInPipMode = true,
                danmakuEnabled = true,
                isPortraitFullscreen = false,
                pipNoDanmakuEnabled = true,
                hostLifecycleStarted = true
            )
        )
        assertEquals(
            false,
            shouldShowDanmakuLayers(
                isInPipMode = false,
                danmakuEnabled = false,
                isPortraitFullscreen = false,
                pipNoDanmakuEnabled = false,
                hostLifecycleStarted = true
            )
        )
    }

    @Test
    fun `resolveGesturePercentDigitChangeMask only animates ones for 40 to 41`() {
        assertEquals(
            listOf(false, false, true),
            resolveGesturePercentDigitChangeMask(previousPercent = 40, currentPercent = 41)
        )
    }

    @Test
    fun `resolveGesturePercentDigitChangeMask animates tens and ones for 49 to 50`() {
        assertEquals(
            listOf(false, true, true),
            resolveGesturePercentDigitChangeMask(previousPercent = 49, currentPercent = 50)
        )
    }

    @Test
    fun `resolveGesturePercentTransitionDirection follows value delta`() {
        assertEquals(
            GesturePercentTransitionDirection.Increase,
            resolveGesturePercentTransitionDirection(previousPercent = 40, currentPercent = 41)
        )
        assertEquals(
            GesturePercentTransitionDirection.Decrease,
            resolveGesturePercentTransitionDirection(previousPercent = 41, currentPercent = 40)
        )
        assertEquals(
            GesturePercentTransitionDirection.None,
            resolveGesturePercentTransitionDirection(previousPercent = 40, currentPercent = 40)
        )
    }

    @Test
    fun `shouldTriggerGesturePercentHaptic only ticks crossed steps and endpoints`() {
        assertFalse(shouldTriggerGesturePercentHaptic(previousPercent = 41, currentPercent = 42))
        assertTrue(shouldTriggerGesturePercentHaptic(previousPercent = 44, currentPercent = 45))
        assertTrue(shouldTriggerGesturePercentHaptic(previousPercent = 46, currentPercent = 45))
        assertTrue(shouldTriggerGesturePercentHaptic(previousPercent = 98, currentPercent = 100))
        assertTrue(shouldTriggerGesturePercentHaptic(previousPercent = 2, currentPercent = 0))
    }

    @Test
    fun `resolveGestureLevelOverlayVisualPolicy returns warm accent for brightness`() {
        val policy = resolveGestureLevelOverlayVisualPolicy(
            mode = VideoGestureMode.Brightness,
            percent = 0.3f
        )

        assertEquals(Color(0xFFFFD54F), policy.accentColor)
        // Scrim must stay opaque enough for white text over bright video frames.
        assertTrue(policy.containerAlpha >= 0.70f)
    }

    @Test
    fun `resolveGestureLevelOverlayVisualPolicy returns cool accent for volume`() {
        val policy = resolveGestureLevelOverlayVisualPolicy(
            mode = VideoGestureMode.Volume,
            percent = 0.8f
        )

        assertEquals(Color(0xFF80DEEA), policy.accentColor)
        assertTrue(policy.glowAlpha > 0.3f)
        assertTrue(policy.containerAlpha >= 0.70f)
    }

    @Test
    fun `resolveGestureRenderProgress clamps to safe range`() {
        assertEquals(0f, resolveGestureRenderProgress(-0.3f))
        assertEquals(0.6f, resolveGestureRenderProgress(0.6f))
        assertEquals(1f, resolveGestureRenderProgress(1.4f))
    }

    @Test
    fun `resolveVideoGestureMotionSpec keeps tuned durations for level overlay`() {
        val spec = resolveVideoGestureMotionSpec()

        assertEquals(160, spec.levelOverlayEnterFadeDurationMillis)
        assertEquals(220, spec.levelOverlayEnterTransformDurationMillis)
        assertEquals(200, spec.levelOverlayExitDurationMillis)
        assertEquals(130, spec.levelProgressDurationMillis)
        assertEquals(180, spec.levelIconScaleDurationMillis)
        assertEquals(180, spec.levelIconContentScaleDurationMillis)
        assertEquals(140, spec.levelValueScaleDurationMillis)
    }

    @Test
    fun `resolveVideoGestureMotionSpec keeps tuned durations for hints and digit transitions`() {
        val spec = resolveVideoGestureMotionSpec()

        assertEquals(16f, spec.digitInitialBlurRadiusDp)
        assertEquals(0.28f, spec.digitInitialAlpha)
        assertEquals(16, spec.digitBlurHoldDurationMillis)
        assertEquals(160, spec.digitBlurResetDurationMillis)
        assertEquals(140, spec.digitAlphaResetDurationMillis)
        assertEquals(140, spec.digitEnterFadeDurationMillis)
        assertEquals(90, spec.digitExitFadeDurationMillis)
        assertEquals(0, spec.digitScaleDurationMillis)
        assertEquals(0.86f, spec.digitSlideSpringDampingRatio)
        assertEquals(720f, spec.digitSlideSpringStiffness)
        assertEquals(150, spec.orientationHintEnterFadeDurationMillis)
        assertEquals(230, spec.orientationHintEnterTransformDurationMillis)
        assertEquals(200, spec.orientationHintExitDurationMillis)
        assertEquals(200, spec.longPressHintDurationMillis)
        assertEquals(900, spec.longPressArrowCycleDurationMillis)
        assertEquals(300, spec.longPressArrowPhaseStepDurationMillis)
    }
}
