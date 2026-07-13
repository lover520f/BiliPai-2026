package com.android.purebilibili.feature.audio.screen

import com.android.purebilibili.feature.video.player.PlayMode
import kotlin.math.abs

internal data class MusicLyricFocusStyle(
    val blurRadiusDp: Int,
    val alphaPercent: Int
)

internal data class MusicGlassFallbackStyle(
    val backgroundAlphaPercent: Int,
    val borderAlphaPercent: Int
)

internal fun resolveMusicPagerIndicatorPosition(
    currentPage: Int,
    currentPageOffsetFraction: Float
): Float = (currentPage + currentPageOffsetFraction).coerceIn(0f, 1f)

internal fun resolveMusicPlayModeIndex(mode: PlayMode): Int = when (mode) {
    PlayMode.SEQUENTIAL -> 0
    PlayMode.SHUFFLE -> 1
    PlayMode.REPEAT_ONE -> 2
    PlayMode.REPEAT_ALL -> 3
}

internal fun resolveMusicPlayMode(index: Int): PlayMode = when (index) {
    1 -> PlayMode.SHUFFLE
    2 -> PlayMode.REPEAT_ONE
    3 -> PlayMode.REPEAT_ALL
    else -> PlayMode.SEQUENTIAL
}

internal fun resolveMusicGlassFallbackStyle(): MusicGlassFallbackStyle {
    return MusicGlassFallbackStyle(
        backgroundAlphaPercent = 48,
        borderAlphaPercent = 24
    )
}

internal fun resolveMusicLyricsBlurEnabled(
    sdkInt: Int,
    effectsEnabled: Boolean,
    reduceMotion: Boolean
): Boolean = sdkInt >= 31 && effectsEnabled && !reduceMotion

internal fun resolveMusicLyricFocusStyle(
    lineIndex: Int,
    currentIndex: Int,
    blurEnabled: Boolean
): MusicLyricFocusStyle {
    val distance = abs(lineIndex - currentIndex)
    val alphaPercent = when (distance) {
        0 -> 100
        1 -> 46
        2 -> 30
        else -> 20
    }
    val blurRadiusDp = if (!blurEnabled) {
        0
    } else {
        when (distance) {
            0 -> 0
            1 -> 2
            2 -> 4
            else -> 7
        }
    }
    return MusicLyricFocusStyle(blurRadiusDp, alphaPercent)
}

internal fun resolveMusicLiquidGlassEnabled(
    sdkInt: Int,
    effectsEnabled: Boolean,
    isAppInBackground: Boolean,
    reduceMotion: Boolean
): Boolean {
    return effectsEnabled &&
        sdkInt >= 33 &&
        !isAppInBackground &&
        !reduceMotion
}
