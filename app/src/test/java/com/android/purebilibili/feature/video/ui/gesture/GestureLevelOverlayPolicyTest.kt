package com.android.purebilibili.feature.video.ui.gesture

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.ui.Alignment
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.SpeakerSlash
import io.github.alexzhirkevich.cupertino.icons.filled.SpeakerWave2
import io.github.alexzhirkevich.cupertino.icons.filled.SunMax
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.VolumeOff
import top.yukonga.miuix.kmp.icon.extended.VolumeUp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GestureLevelOverlayPolicyTest {

    @Test
    fun overlayStyle_mapsUiPresetAndAndroidVariant() {
        assertEquals(
            GestureLevelOverlayStyle.Md3,
            resolveGestureLevelOverlayStyle(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        )
        assertEquals(
            GestureLevelOverlayStyle.Ios,
            resolveGestureLevelOverlayStyle(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        )
        assertEquals(
            GestureLevelOverlayStyle.Miuix,
            resolveGestureLevelOverlayStyle(UiPreset.MD3, AndroidNativeVariant.MIUIX)
        )
        // iOS preset wins over miuix native variant.
        assertEquals(
            GestureLevelOverlayStyle.Ios,
            resolveGestureLevelOverlayStyle(UiPreset.IOS, AndroidNativeVariant.MIUIX)
        )
    }

    @Test
    fun overlaySpec_differsByThemeLayout() {
        val md3 = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Md3,
            kind = GestureLevelKind.Volume,
            percent = 0.5f
        )
        val ios = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Ios,
            kind = GestureLevelKind.Volume,
            percent = 0.5f
        )
        val miuixVolume = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Miuix,
            kind = GestureLevelKind.Volume,
            percent = 0.5f
        )
        val miuixBrightness = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Miuix,
            kind = GestureLevelKind.Brightness,
            percent = 0.5f
        )

        assertTrue(md3.verticalRail)
        assertFalse(ios.verticalRail)
        assertTrue(miuixVolume.verticalRail)
        assertEquals(Alignment.Center, md3.alignment)
        assertEquals(Alignment.Center, ios.alignment)
        assertEquals(Alignment.CenterEnd, miuixVolume.alignment)
        assertEquals(Alignment.CenterStart, miuixBrightness.alignment)
        assertTrue(ios.showLabel)
        assertFalse(md3.showLabel)
        assertFalse(miuixVolume.showPercentText)
    }

    @Test
    fun icons_useDistinctFamiliesPerTheme() {
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeOff,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Volume, 0f)
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeUp,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Volume, 1f)
        )
        assertEquals(
            CupertinoIcons.Filled.SpeakerSlash,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Ios, GestureLevelKind.Volume, 0f)
        )
        assertEquals(
            CupertinoIcons.Filled.SpeakerWave2,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Ios, GestureLevelKind.Volume, 1f)
        )
        assertEquals(
            MiuixIcons.VolumeOff,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Miuix, GestureLevelKind.Volume, 0f)
        )
        assertEquals(
            MiuixIcons.VolumeUp,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Miuix, GestureLevelKind.Volume, 1f)
        )
        assertEquals(
            Icons.Filled.BrightnessLow,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Brightness, 0.2f)
        )
        assertEquals(
            CupertinoIcons.Filled.SunMax,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Ios, GestureLevelKind.Brightness, 0.5f)
        )
        assertEquals(
            Icons.Filled.BrightnessHigh,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Brightness, 0.9f)
        )
    }
}
