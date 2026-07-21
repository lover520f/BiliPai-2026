package com.android.purebilibili.feature.video.ui.gesture

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.ui.isNativeMiuixEnabled
import com.android.purebilibili.feature.video.ui.section.VideoGestureMode
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.Speaker
import io.github.alexzhirkevich.cupertino.icons.filled.SpeakerSlash
import io.github.alexzhirkevich.cupertino.icons.filled.SpeakerWave2
import io.github.alexzhirkevich.cupertino.icons.filled.SunMax
import io.github.alexzhirkevich.cupertino.icons.outlined.LightMax
import io.github.alexzhirkevich.cupertino.icons.outlined.LightMin
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.VolumeOff
import top.yukonga.miuix.kmp.icon.extended.VolumeUp

/**
 * Three fully distinct volume/brightness feedback skins.
 * Layout, placement, icon family and motion all differ by style.
 */
enum class GestureLevelOverlayStyle {
    /** Material 3: centered vertical pill with bottom-up fill. */
    Md3,
    /** iOS: centered frosted capsule with SF-style glyphs. */
    Ios,
    /** MIUIX: edge vertical rail (brightness left / volume right). */
    Miuix
}

enum class GestureLevelKind {
    Brightness,
    Volume
}

data class GestureLevelOverlaySpec(
    val style: GestureLevelOverlayStyle,
    val kind: GestureLevelKind,
    val alignment: Alignment,
    val showLabel: Boolean,
    val showPercentText: Boolean,
    val verticalRail: Boolean,
    val accentColor: Color,
    val trackColor: Color,
    val fillColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val iconTint: Color,
    val textColor: Color,
    val railWidthDp: Int,
    val railHeightDp: Int,
    val capsuleMinWidthDp: Int,
    val iconSizeDp: Int
)

fun resolveGestureLevelOverlayStyle(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant = AndroidNativeVariant.MATERIAL3
): GestureLevelOverlayStyle {
    return when {
        isNativeMiuixEnabled(uiPreset, androidNativeVariant) -> GestureLevelOverlayStyle.Miuix
        uiPreset == UiPreset.IOS -> GestureLevelOverlayStyle.Ios
        else -> GestureLevelOverlayStyle.Md3
    }
}

fun resolveGestureLevelKind(mode: VideoGestureMode): GestureLevelKind? {
    return when (mode) {
        VideoGestureMode.Brightness -> GestureLevelKind.Brightness
        VideoGestureMode.Volume -> GestureLevelKind.Volume
        else -> null
    }
}

fun resolveGestureLevelOverlaySpec(
    style: GestureLevelOverlayStyle,
    kind: GestureLevelKind,
    percent: Float
): GestureLevelOverlaySpec {
    val progress = percent.coerceIn(0f, 1f)
    val isVolume = kind == GestureLevelKind.Volume
    return when (style) {
        GestureLevelOverlayStyle.Md3 -> GestureLevelOverlaySpec(
            style = style,
            kind = kind,
            alignment = Alignment.Center,
            showLabel = false,
            showPercentText = true,
            verticalRail = true,
            accentColor = if (isVolume) Color(0xFF80CBC4) else Color(0xFFFFCC80),
            trackColor = Color.White.copy(alpha = 0.16f),
            fillColor = if (isVolume) Color(0xFF4DB6AC) else Color(0xFFFFB74D),
            containerColor = Color(0xFF1C1B1F).copy(alpha = 0.88f),
            borderColor = Color.White.copy(alpha = 0.08f),
            iconTint = Color.White,
            textColor = Color.White,
            railWidthDp = 58,
            railHeightDp = 168,
            capsuleMinWidthDp = 58,
            iconSizeDp = 26
        )
        GestureLevelOverlayStyle.Ios -> GestureLevelOverlaySpec(
            style = style,
            kind = kind,
            alignment = Alignment.Center,
            showLabel = true,
            showPercentText = true,
            verticalRail = false,
            accentColor = if (isVolume) Color(0xFF64D2FF) else Color(0xFFFFD60A),
            trackColor = Color.White.copy(alpha = 0.22f),
            fillColor = if (isVolume) Color(0xFF64D2FF) else Color(0xFFFFD60A),
            containerColor = Color.Black.copy(alpha = 0.52f + progress * 0.08f),
            borderColor = Color.White.copy(alpha = 0.28f),
            iconTint = Color.White,
            textColor = Color.White,
            railWidthDp = 0,
            railHeightDp = 0,
            capsuleMinWidthDp = 148,
            iconSizeDp = 34
        )
        GestureLevelOverlayStyle.Miuix -> GestureLevelOverlaySpec(
            style = style,
            kind = kind,
            // MIUI-like: brightness rail on left, volume on right.
            alignment = if (isVolume) Alignment.CenterEnd else Alignment.CenterStart,
            showLabel = false,
            showPercentText = false,
            verticalRail = true,
            accentColor = if (isVolume) Color(0xFF0D84FF) else Color(0xFFFFC107),
            trackColor = Color.White.copy(alpha = 0.18f),
            fillColor = if (isVolume) Color(0xFF0D84FF) else Color(0xFFFFC107),
            containerColor = Color(0xE61A1A1A),
            borderColor = Color.White.copy(alpha = 0.06f),
            iconTint = Color.White,
            textColor = Color.White,
            railWidthDp = 40,
            railHeightDp = 186,
            capsuleMinWidthDp = 40,
            iconSizeDp = 22
        )
    }
}

fun resolveGestureLevelIcon(
    style: GestureLevelOverlayStyle,
    kind: GestureLevelKind,
    percent: Float
): ImageVector {
    val p = percent.coerceIn(0f, 1f)
    return when (kind) {
        GestureLevelKind.Volume -> when (style) {
            GestureLevelOverlayStyle.Md3 -> when {
                p < 0.01f -> Icons.AutoMirrored.Filled.VolumeOff
                p < 0.34f -> Icons.AutoMirrored.Filled.VolumeMute
                p < 0.67f -> Icons.AutoMirrored.Filled.VolumeDown
                else -> Icons.AutoMirrored.Filled.VolumeUp
            }
            GestureLevelOverlayStyle.Ios -> when {
                p < 0.01f -> CupertinoIcons.Filled.SpeakerSlash
                p < 0.45f -> CupertinoIcons.Filled.Speaker
                else -> CupertinoIcons.Filled.SpeakerWave2
            }
            GestureLevelOverlayStyle.Miuix -> when {
                p < 0.01f -> MiuixIcons.VolumeOff
                p < 0.34f -> Icons.AutoMirrored.Filled.VolumeMute
                p < 0.67f -> Icons.AutoMirrored.Filled.VolumeDown
                else -> MiuixIcons.VolumeUp
            }
        }
        GestureLevelKind.Brightness -> when (style) {
            GestureLevelOverlayStyle.Md3 -> when {
                p < 0.34f -> Icons.Filled.BrightnessLow
                p < 0.67f -> Icons.Filled.BrightnessMedium
                else -> Icons.Filled.BrightnessHigh
            }
            GestureLevelOverlayStyle.Ios -> when {
                p < 0.34f -> CupertinoIcons.Outlined.LightMin
                p < 0.67f -> CupertinoIcons.Filled.SunMax
                else -> CupertinoIcons.Outlined.LightMax
            }
            GestureLevelOverlayStyle.Miuix -> when {
                p < 0.20f -> Icons.Filled.Brightness4
                p < 0.40f -> Icons.Filled.Brightness5
                p < 0.70f -> Icons.Filled.Brightness6
                else -> Icons.Filled.Brightness7
            }
        }
    }
}

fun resolveGestureLevelLabel(kind: GestureLevelKind): String {
    return when (kind) {
        GestureLevelKind.Brightness -> "亮度"
        GestureLevelKind.Volume -> "音量"
    }
}
