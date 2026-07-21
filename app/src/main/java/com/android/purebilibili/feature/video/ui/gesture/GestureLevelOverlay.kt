package com.android.purebilibili.feature.video.ui.gesture

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.feature.video.ui.components.AnimatedGesturePercentText
import com.android.purebilibili.feature.video.ui.section.VideoGestureMode
import com.android.purebilibili.feature.video.ui.section.resolveVideoGestureMotionSpec
import kotlin.math.roundToInt

/**
 * Theme-native volume / brightness feedback:
 * - MD3: centered vertical material pill
 * - iOS: centered frosted capsule
 * - MIUIX: edge vertical system-style rail
 */
@Composable
fun BoxScope.GestureLevelOverlayHost(
    visible: Boolean,
    mode: VideoGestureMode,
    percent: Float,
    modifier: Modifier = Modifier
) {
    val kind = resolveGestureLevelKind(mode) ?: return
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val style = remember(uiPreset, androidNativeVariant) {
        resolveGestureLevelOverlayStyle(uiPreset, androidNativeVariant)
    }
    val motionSpec = remember { resolveVideoGestureMotionSpec() }
    val spec = remember(style, kind, percent) {
        resolveGestureLevelOverlaySpec(style = style, kind = kind, percent = percent)
    }
    val progress by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 1f),
        animationSpec = tween(motionSpec.levelProgressDurationMillis),
        label = "gesture-level-progress"
    )
    val icon = resolveGestureLevelIcon(style = style, kind = kind, percent = percent)
    val percentInt = (percent.coerceIn(0f, 1f) * 100f).roundToInt().coerceIn(0, 100)

    AnimatedVisibility(
        visible = visible,
        modifier = modifier
            .align(spec.alignment)
            .then(
                when (style) {
                    GestureLevelOverlayStyle.Miuix -> Modifier.padding(horizontal = 22.dp)
                    else -> Modifier
                }
            )
            .zIndex(40f),
        enter = fadeIn(animationSpec = tween(motionSpec.levelOverlayEnterFadeDurationMillis)) +
            scaleIn(
                initialScale = if (style == GestureLevelOverlayStyle.Miuix) 0.92f else 0.84f,
                animationSpec = tween(motionSpec.levelOverlayEnterTransformDurationMillis)
            ) +
            slideInVertically(
                initialOffsetY = { if (style == GestureLevelOverlayStyle.Ios) it / 8 else 0 },
                animationSpec = tween(motionSpec.levelOverlayEnterTransformDurationMillis)
            ),
        exit = fadeOut(animationSpec = tween(motionSpec.levelOverlayExitDurationMillis)) +
            scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(motionSpec.levelOverlayExitDurationMillis)
            ) +
            slideOutVertically(
                targetOffsetY = { if (style == GestureLevelOverlayStyle.Ios) -it / 10 else 0 },
                animationSpec = tween(motionSpec.levelOverlayExitDurationMillis)
            )
    ) {
        when (style) {
            GestureLevelOverlayStyle.Md3 -> Md3GestureLevelRail(
                spec = spec,
                icon = icon,
                progress = progress,
                percent = percentInt
            )
            GestureLevelOverlayStyle.Ios -> IosGestureLevelCapsule(
                spec = spec,
                icon = icon,
                progress = progress,
                percent = percentInt
            )
            GestureLevelOverlayStyle.Miuix -> MiuixGestureLevelRail(
                spec = spec,
                icon = icon,
                progress = progress
            )
        }
    }
}

@Composable
private fun Md3GestureLevelRail(
    spec: GestureLevelOverlaySpec,
    icon: ImageVector,
    progress: Float,
    percent: Int
) {
    val shape = RoundedCornerShape(28.dp)
    Surface(
        shape = shape,
        color = spec.containerColor,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, spec.borderColor)
    ) {
        Column(
            modifier = Modifier
                .width(spec.railWidthDp.dp)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GestureLevelIconSlot(
                icon = icon,
                tint = spec.iconTint,
                sizeDp = spec.iconSizeDp,
                glowColor = spec.fillColor.copy(alpha = 0.28f)
            )
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height((spec.railHeightDp - 78).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(spec.trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(progress)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    spec.fillColor.copy(alpha = 0.72f),
                                    spec.fillColor
                                )
                            )
                        )
                )
            }
            Text(
                text = "$percent",
                color = spec.textColor,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun IosGestureLevelCapsule(
    spec: GestureLevelOverlaySpec,
    icon: ImageVector,
    progress: Float,
    percent: Int
) {
    val shape = RoundedCornerShape(22.dp)
    Surface(
        shape = shape,
        color = spec.containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, spec.borderColor),
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = spec.capsuleMinWidthDp.dp, max = 188.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GestureLevelIconSlot(
                icon = icon,
                tint = spec.accentColor,
                sizeDp = spec.iconSizeDp,
                glowColor = spec.accentColor.copy(alpha = 0.34f)
            )
            if (spec.showLabel) {
                Text(
                    text = resolveGestureLevelLabel(spec.kind),
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
            AnimatedGesturePercentText(
                percent = percent,
                color = spec.textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                label = "ios-gesture-level-percent"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(spec.trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    spec.fillColor.copy(alpha = 0.7f),
                                    spec.fillColor
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun MiuixGestureLevelRail(
    spec: GestureLevelOverlaySpec,
    icon: ImageVector,
    progress: Float
) {
    val shape = RoundedCornerShape(999.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(spec.railWidthDp.dp)
                .height(spec.railHeightDp.dp)
                .clip(shape)
                .background(spec.containerColor)
                .border(1.dp, spec.borderColor, shape)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                spec.fillColor.copy(alpha = 0.78f),
                                spec.fillColor
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .size((spec.iconSizeDp + 10).dp)
                    .background(Color.Black.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = icon,
                    transitionSpec = {
                        (fadeIn(tween(120)) + scaleIn(initialScale = 0.82f, animationSpec = tween(140)))
                            .togetherWith(fadeOut(tween(100)) + scaleOut(targetScale = 1.12f, animationSpec = tween(120)))
                    },
                    label = "miuix-gesture-icon"
                ) { target ->
                    Icon(
                        imageVector = target,
                        contentDescription = null,
                        tint = spec.iconTint,
                        modifier = Modifier.size(spec.iconSizeDp.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GestureLevelIconSlot(
    icon: ImageVector,
    tint: Color,
    sizeDp: Int,
    glowColor: Color
) {
    Box(
        modifier = Modifier.size((sizeDp + 16).dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(glowColor, CircleShape)
                .graphicsLayer { alpha = 0.9f }
        )
        AnimatedContent(
            targetState = icon,
            transitionSpec = {
                (fadeIn(tween(120)) + scaleIn(initialScale = 0.8f, animationSpec = tween(150)))
                    .togetherWith(fadeOut(tween(100)) + scaleOut(targetScale = 1.15f, animationSpec = tween(130)))
            },
            label = "gesture-level-icon"
        ) { target ->
            Icon(
                imageVector = target,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(sizeDp.dp)
            )
        }
    }
}

/** Convenience for non-BoxScope hosts (fullscreen / bangumi / offline). */
@Composable
fun GestureLevelOverlayContent(
    mode: VideoGestureMode,
    percent: Float,
    style: GestureLevelOverlayStyle,
    modifier: Modifier = Modifier
) {
    val kind = resolveGestureLevelKind(mode) ?: return
    val motionSpec = remember { resolveVideoGestureMotionSpec() }
    val spec = remember(style, kind, percent) {
        resolveGestureLevelOverlaySpec(style = style, kind = kind, percent = percent)
    }
    val progress by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 1f),
        animationSpec = tween(motionSpec.levelProgressDurationMillis),
        label = "gesture-level-progress-content"
    )
    val icon = resolveGestureLevelIcon(style = style, kind = kind, percent = percent)
    val percentInt = (percent.coerceIn(0f, 1f) * 100f).roundToInt().coerceIn(0, 100)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (style) {
            GestureLevelOverlayStyle.Md3 -> Md3GestureLevelRail(
                spec = spec,
                icon = icon,
                progress = progress,
                percent = percentInt
            )
            GestureLevelOverlayStyle.Ios -> IosGestureLevelCapsule(
                spec = spec,
                icon = icon,
                progress = progress,
                percent = percentInt
            )
            GestureLevelOverlayStyle.Miuix -> MiuixGestureLevelRail(
                spec = spec,
                icon = icon,
                progress = progress
            )
        }
    }
}
