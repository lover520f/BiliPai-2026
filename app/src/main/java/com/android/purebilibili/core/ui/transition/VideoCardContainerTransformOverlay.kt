package com.android.purebilibili.core.ui.transition

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.adaptive.resolveDeviceUiProfile
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.core.util.LocalWindowSizeClass

@Composable
internal fun VideoCardContainerTransformOverlay(
    cardTransitionEnabled: Boolean,
    sourceKey: String?,
    cardFullyVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val session = LocalVideoCardTransitionSession.current
    val windowSizeClass = LocalWindowSizeClass.current
    val motionTier = remember(windowSizeClass.widthSizeClass) {
        resolveDeviceUiProfile(windowSizeClass.widthSizeClass).motionTier
    }
    val density = LocalDensity.current
    var overlayBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    val sourceBoundsInRoot = CardPositionManager.lastClickedCardBounds
    val sourceCornerRadiusDp = remember(CardPositionManager.lastClickedVideoSourceCornerDp) {
        (CardPositionManager.lastClickedVideoSourceCornerDp ?: 12).toFloat()
    }
    val sourceKeyMatches = sourceKey != null &&
        sourceKey == CardPositionManager.lastClickedVideoSourceKey
    val transformBounds = remember(sourceBoundsInRoot, overlayBoundsInRoot) {
        overlayBoundsInRoot?.let { overlayBounds ->
            VideoCardContainerTransformBounds(
                sourceBoundsInRoot = sourceBoundsInRoot,
                overlayBoundsInRoot = overlayBounds,
                targetBoundsInOverlay = Rect(
                    left = 0f,
                    top = 0f,
                    right = overlayBounds.width,
                    bottom = overlayBounds.height
                )
            )
        }
    }
    val frame = remember(
        cardTransitionEnabled,
        sourceKeyMatches,
        cardFullyVisible,
        motionTier,
        session,
        transformBounds,
        sourceCornerRadiusDp
    ) {
        resolveVideoCardContainerTransformFrame(
            cardTransitionEnabled = cardTransitionEnabled,
            sourceKeyMatches = sourceKeyMatches,
            cardFullyVisible = cardFullyVisible,
            motionTier = motionTier,
            session = session,
            bounds = transformBounds,
            sourceCornerRadiusDp = sourceCornerRadiusDp
        )
    }
    val cornerRadiusPx = remember(frame.cornerRadiusDp, density) {
        with(density) { frame.cornerRadiusDp.dp.toPx() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayBoundsInRoot = coordinates.boundsInRoot()
            }
            .drawBehind {
                if (!frame.active) return@drawBehind
                drawRoundRect(
                    color = Color.Black.copy(alpha = frame.alpha),
                    topLeft = Offset(frame.rect.left, frame.rect.top),
                    size = Size(frame.rect.width, frame.rect.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            }
    )
}
