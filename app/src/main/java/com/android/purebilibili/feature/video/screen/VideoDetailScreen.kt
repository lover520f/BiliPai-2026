// 文件路径: feature/video/screen/VideoDetailScreen.kt
package com.android.purebilibili.feature.video.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.OrientationEventListener
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.android.purebilibili.data.model.response.BgmInfo
import com.android.purebilibili.data.model.CommentFraudStatus
import com.android.purebilibili.data.repository.resolveCommentFraudLightMessage
import com.android.purebilibili.data.repository.shouldShowCommentFraudResultDialog
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.store.PortraitPlayerCollapseMode
import com.android.purebilibili.core.theme.LocalUiPreset
//  已改用 MaterialTheme.colorScheme.primary

import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.ReplyItem
import com.android.purebilibili.data.model.response.UgcSeason
import com.android.purebilibili.data.model.response.VideoTag
import com.android.purebilibili.data.model.response.ViewInfo
import com.android.purebilibili.data.model.response.ViewPoint
import com.android.purebilibili.feature.common.resolveIndexedVideoLazyKey
// Refactored UI components
import com.android.purebilibili.feature.video.ui.section.VideoTitleSection
import com.android.purebilibili.feature.video.ui.section.VideoTitleWithDesc
import com.android.purebilibili.feature.video.ui.section.UpInfoSection
import com.android.purebilibili.feature.video.ui.section.DescriptionSection
import com.android.purebilibili.feature.video.ui.section.ActionButtonsRow
import com.android.purebilibili.feature.video.ui.section.ActionButton
import com.android.purebilibili.feature.video.ui.components.RelatedVideosHeader
import com.android.purebilibili.feature.video.ui.components.RelatedVideoItem
import com.android.purebilibili.feature.video.ui.components.CoinDialog
import com.android.purebilibili.feature.video.ui.components.CollectionRow
import com.android.purebilibili.feature.video.ui.components.CollectionSheet
import com.android.purebilibili.feature.video.ui.components.PagesSelector
// Imports for moved classes
import com.android.purebilibili.feature.video.viewmodel.PlayerViewModel
import com.android.purebilibili.feature.video.viewmodel.PlayerUiState
import com.android.purebilibili.feature.video.viewmodel.QualitySwitchFailureDialogState
import com.android.purebilibili.feature.video.viewmodel.CommentUiState
import com.android.purebilibili.feature.video.viewmodel.VideoCommentViewModel
import com.android.purebilibili.feature.video.state.VideoPlayerState
import com.android.purebilibili.feature.video.state.rememberVideoPlayerState
import com.android.purebilibili.feature.video.ui.section.VideoPlayerSection
import com.android.purebilibili.feature.video.ui.section.shouldKeepVideoPlaybackAwake
import com.android.purebilibili.feature.video.ui.components.ReplyHeader
import com.android.purebilibili.feature.video.ui.components.ReplyItemView
import com.android.purebilibili.feature.video.ui.components.CommentFraudResultDialog
import com.android.purebilibili.feature.video.ui.components.VideoCommentSheetHost

import com.android.purebilibili.feature.video.viewmodel.CommentSortMode  //  新增
import com.android.purebilibili.feature.video.ui.components.LikeBurstAnimation
import com.android.purebilibili.feature.video.ui.components.TripleSuccessAnimation
import com.android.purebilibili.feature.video.ui.components.VideoDetailSkeleton
import com.android.purebilibili.feature.video.ui.components.VideoActionFeedbackHost
import com.android.purebilibili.feature.video.subtitle.SubtitleAutoPreference
import kotlin.math.roundToInt
import com.android.purebilibili.feature.video.subtitle.SubtitleDisplayMode
import com.android.purebilibili.feature.video.subtitle.resolveSubtitleDisplayModePreference
import com.android.purebilibili.feature.video.progress.PbpProgressData
import com.android.purebilibili.feature.video.usecase.playPlayerFromUserAction
import com.android.purebilibili.feature.video.usecase.seekPlayerFromUserAction
import com.android.purebilibili.feature.video.policy.reduceVideoDetailPostScroll
import com.android.purebilibili.feature.video.policy.reduceVideoDetailPreScroll
import com.android.purebilibili.feature.video.policy.resolveVideoDetailCollapseProgress
import com.android.purebilibili.feature.video.subtitle.resolveSubtitlePreferenceSession
import io.github.alexzhirkevich.cupertino.CupertinoActivityIndicator
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
//  共享元素过渡
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.shape.RoundedCornerShape
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionSession
import com.android.purebilibili.core.ui.transition.VideoCardTransitionPhase
import com.android.purebilibili.core.ui.transition.VideoCardTransitionSession
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionPlaybackIntent
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionMotionSpec
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionEasing
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionPlaybackIntent
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionSourceCornerDp
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionVisualSpec
import com.android.purebilibili.core.ui.transition.shouldEnableVideoCoverSharedTransition
import com.android.purebilibili.core.ui.transition.shouldFadePlayerSurfaceOnDetailReturn
import com.android.purebilibili.core.ui.transition.shouldUseDetailReturnCoverCrossfade
import com.android.purebilibili.core.ui.transition.videoSharedElementBoundsTransformSpec
import com.android.purebilibili.core.ui.rememberAppCollectionIcon
import com.android.purebilibili.core.ui.rememberAppDownloadIcon
import com.android.purebilibili.core.ui.rememberAppMusicIcon
import com.android.purebilibili.core.ui.rememberAppPhotoIcon
import com.android.purebilibili.core.ui.rememberAppPlayIcon
import com.android.purebilibili.feature.video.player.MiniPlayerManager
import com.android.purebilibili.feature.video.player.PlaybackService
import com.android.purebilibili.feature.video.player.PlaylistItem
import com.android.purebilibili.feature.video.player.PlaylistManager
import com.android.purebilibili.feature.video.player.PlaylistUiState
import com.android.purebilibili.feature.video.player.ExternalPlaylistSource
import com.android.purebilibili.feature.video.player.buildPipPlaybackRemoteActions
import com.android.purebilibili.core.ui.performance.TrackJankStateFlag
// 📱 [新增] 竖屏全屏
import com.android.purebilibili.feature.video.ui.overlay.PortraitFullscreenOverlay
import com.android.purebilibili.feature.video.ui.overlay.PlayerProgress
import com.android.purebilibili.feature.video.ui.components.VideoAspectRatio
import com.android.purebilibili.feature.video.danmaku.rememberDanmakuManager
import com.android.purebilibili.core.ui.blur.shouldAllowRuntimeShaderBackedHazeEffect
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.core.ui.IOSModalBottomSheet
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.core.util.FormatUtils
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import com.android.purebilibili.feature.video.ui.components.DanmakuContextMenu
import com.android.purebilibili.feature.video.ui.components.DanmakuBlockActionTarget
import com.android.purebilibili.feature.video.ui.components.resolveDanmakuBlockActionFeedbackMessage
import com.android.purebilibili.feature.video.danmaku.appendDanmakuKeywordBlockRule
import com.android.purebilibili.feature.video.danmaku.appendDanmakuUserHashBlockRule
import com.android.purebilibili.feature.video.ui.components.InteractiveChoiceOverlay
import com.android.purebilibili.feature.video.ui.feedback.VideoFeedbackAnchor
import com.android.purebilibili.feature.video.ui.feedback.TripleCelebrationPlacement
import com.android.purebilibili.feature.video.ui.feedback.resolveQualityReminderPlacement
import com.android.purebilibili.feature.video.ui.feedback.resolveTripleCelebrationPlacement
import com.android.purebilibili.feature.video.ui.feedback.resolveVideoFeedbackPlacement
import com.android.purebilibili.feature.video.ui.section.resolveForcedReturnCoverSharedElementSourceRoute
import com.android.purebilibili.feature.video.share.VideoSharePayload
import com.android.purebilibili.feature.video.share.VideoShareSheet
import com.android.purebilibili.feature.video.viewmodel.PlayerToastMessage
import com.android.purebilibili.feature.video.viewmodel.PlayerToastPresentation
import kotlin.math.abs
import kotlin.math.roundToInt

@Stable
private class InlinePortraitPlayerCollapseState {
    var offsetPx by mutableFloatStateOf(0f)
        private set

    var restoreRequested by mutableStateOf(false)
        private set

    fun updateOffset(value: Float) {
        offsetPx = value
    }

    fun reset() {
        offsetPx = 0f
        restoreRequested = false
    }

    fun beginScroll() {
        restoreRequested = false
    }

    fun restore() {
        offsetPx = 0f
        restoreRequested = true
    }
}

@Composable
private fun rememberInlinePortraitPlayerCollapseState() =
    remember { InlinePortraitPlayerCollapseState() }

internal fun shouldHandleVideoDetailDisposeAsNavigationExit(
    isNavigatingToAudioMode: Boolean,
    isNavigatingToMiniMode: Boolean,
    isMiniModeActive: Boolean,
    isChangingConfigurations: Boolean,
    isNavigatingToVideo: Boolean
): Boolean {
    return !isNavigatingToAudioMode &&
        !isNavigatingToMiniMode &&
        !isMiniModeActive &&
        !isChangingConfigurations &&
        !isNavigatingToVideo
}

internal fun resolveIsNavigatingToVideoDuringDispose(
    localNavigatingToVideo: Boolean,
    managerNavigatingToVideo: Boolean
): Boolean {
    return localNavigatingToVideo || managerNavigatingToVideo
}

internal fun shouldMarkReturningStateOnVideoDetailDispose(
    shouldHandleAsNavigationExit: Boolean
): Boolean {
    return shouldHandleAsNavigationExit
}

internal fun shouldClearStaleReturningStateOnVideoDetailEnter(
    isReturningFromDetail: Boolean
): Boolean {
    return isReturningFromDetail
}

private const val COVER_TAKEOVER_PRE_BACK_DELAY_MILLIS = 16L
internal const val VIDEO_CONTENT_COMMENT_TAB_INDEX = 1

internal fun resolveForceCoverOnlyForReturn(
    forceCoverOnlyOnReturn: Boolean,
    isReturningFromDetail: Boolean,
    isExitTransitionInProgress: Boolean,
    transitionEnabled: Boolean = true,
    detailShellSharedBoundsEnabled: Boolean = false
): Boolean {
    if (!transitionEnabled) return false
    if (detailShellSharedBoundsEnabled) return false
    return forceCoverOnlyOnReturn || isReturningFromDetail
}

internal fun shouldUseReturningVideoDetailVisualState(
    forceCoverOnlyForReturn: Boolean,
    isReturningFromDetail: Boolean,
    isExitTransitionInProgress: Boolean
): Boolean {
    return forceCoverOnlyForReturn || isReturningFromDetail
}

internal fun resolveCoverTakeoverDelayBeforeBackNavigationMillis(): Long {
    return COVER_TAKEOVER_PRE_BACK_DELAY_MILLIS
}

internal data class VideoDetailSystemBarsSnapshot(
    val statusBarColor: Int,
    val navigationBarColor: Int,
    val lightStatusBars: Boolean,
    val lightNavigationBars: Boolean,
    val systemBarsBehavior: Int
)

internal fun resolveVideoDetailSystemBarsSnapshot(
    statusBarColor: Int?,
    navigationBarColor: Int?,
    lightStatusBars: Boolean?,
    lightNavigationBars: Boolean?,
    systemBarsBehavior: Int?,
    fallbackColor: Int,
    fallbackLightBars: Boolean,
    fallbackSystemBarsBehavior: Int
): VideoDetailSystemBarsSnapshot {
    return VideoDetailSystemBarsSnapshot(
        statusBarColor = statusBarColor ?: fallbackColor,
        navigationBarColor = navigationBarColor ?: fallbackColor,
        lightStatusBars = lightStatusBars ?: fallbackLightBars,
        lightNavigationBars = lightNavigationBars ?: fallbackLightBars,
        systemBarsBehavior = systemBarsBehavior ?: fallbackSystemBarsBehavior
    )
}

internal fun shouldShowSystemBarsOnVideoDetailExit(): Boolean {
    return true
}

internal data class VideoDetailSystemBarsVisibilityPolicy(
    val hideStatusBars: Boolean,
    val hideNavigationBars: Boolean
)

internal enum class VideoDetailHiddenSystemBars {
    NONE,
    STATUS_BARS,
    SYSTEM_BARS
}

internal data class VideoDetailSystemBarsApplySpec(
    val hiddenBars: VideoDetailHiddenSystemBars,
    val systemBarsBehavior: Int,
    val statusBarColor: Int,
    val navigationBarColor: Int,
    val lightStatusBars: Boolean,
    val lightNavigationBars: Boolean
)

internal fun resolveVideoDetailSystemBarsVisibilityPolicy(
    isFullscreenMode: Boolean,
    hideVideoPageStatusBar: Boolean,
    isInPipMode: Boolean,
    isScreenActive: Boolean
): VideoDetailSystemBarsVisibilityPolicy {
    if (!isScreenActive || isInPipMode) {
        return VideoDetailSystemBarsVisibilityPolicy(
            hideStatusBars = false,
            hideNavigationBars = false
        )
    }
    if (isFullscreenMode) {
        return VideoDetailSystemBarsVisibilityPolicy(
            hideStatusBars = true,
            hideNavigationBars = true
        )
    }
    return VideoDetailSystemBarsVisibilityPolicy(
        hideStatusBars = hideVideoPageStatusBar,
        hideNavigationBars = false
    )
}

internal fun resolveVideoDetailSystemBarsApplySpec(
    visibilityPolicy: VideoDetailSystemBarsVisibilityPolicy,
    useTabletLayout: Boolean,
    isLightBackground: Boolean,
    backgroundColor: Int,
    transparentColor: Int,
    blackColor: Int,
    transientBarsBehavior: Int
): VideoDetailSystemBarsApplySpec {
    if (visibilityPolicy.hideNavigationBars) {
        return VideoDetailSystemBarsApplySpec(
            hiddenBars = VideoDetailHiddenSystemBars.SYSTEM_BARS,
            systemBarsBehavior = transientBarsBehavior,
            statusBarColor = blackColor,
            navigationBarColor = blackColor,
            lightStatusBars = false,
            lightNavigationBars = false
        )
    }

    val hiddenBars = if (visibilityPolicy.hideStatusBars) {
        VideoDetailHiddenSystemBars.STATUS_BARS
    } else {
        VideoDetailHiddenSystemBars.NONE
    }
    return if (useTabletLayout) {
        VideoDetailSystemBarsApplySpec(
            hiddenBars = hiddenBars,
            systemBarsBehavior = transientBarsBehavior,
            statusBarColor = backgroundColor,
            navigationBarColor = backgroundColor,
            lightStatusBars = isLightBackground,
            lightNavigationBars = isLightBackground
        )
    } else {
        VideoDetailSystemBarsApplySpec(
            hiddenBars = hiddenBars,
            systemBarsBehavior = transientBarsBehavior,
            statusBarColor = transparentColor,
            navigationBarColor = transparentColor,
            lightStatusBars = false,
            lightNavigationBars = false
        )
    }
}

internal fun resolveVideoDetailStableStatusBarHeightDp(
    visibleStatusBarHeightDp: Float,
    statusBarIgnoringVisibilityHeightDp: Float,
    hideStatusBars: Boolean
): Float {
    fun sanitize(value: Float): Float {
        return value.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    }

    val visibleInset = sanitize(visibleStatusBarHeightDp)
    val stableInset = sanitize(statusBarIgnoringVisibilityHeightDp)
    return if (hideStatusBars) {
        stableInset.coerceAtLeast(visibleInset)
    } else {
        visibleInset
    }
}

internal fun resolveVideoDetailPortraitPlayerTopInsetDp(
    stableStatusBarHeightDp: Float,
    hideStatusBars: Boolean
): Float {
    val stableInset = stableStatusBarHeightDp
        .takeIf { it.isFinite() }
        ?.coerceAtLeast(0f)
        ?: 0f
    return if (hideStatusBars) 0f else stableInset
}

internal fun shouldRestoreSystemBarsDuringVideoDetailExitTransition(
    isExitTransitionInProgress: Boolean,
    isActuallyLeaving: Boolean
): Boolean {
    if (!isExitTransitionInProgress) return false
    if (isActuallyLeaving) return false
    return true
}

private fun applyVideoDetailSystemBarsSpec(
    window: Window,
    insetsController: WindowInsetsControllerCompat,
    spec: VideoDetailSystemBarsApplySpec
) {
    when (spec.hiddenBars) {
        VideoDetailHiddenSystemBars.SYSTEM_BARS -> {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        VideoDetailHiddenSystemBars.STATUS_BARS -> {
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.show(WindowInsetsCompat.Type.navigationBars())
        }
        VideoDetailHiddenSystemBars.NONE -> {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    insetsController.systemBarsBehavior = spec.systemBarsBehavior
    insetsController.isAppearanceLightStatusBars = spec.lightStatusBars
    insetsController.isAppearanceLightNavigationBars = spec.lightNavigationBars
    window.statusBarColor = spec.statusBarColor
    window.navigationBarColor = spec.navigationBarColor
}

internal fun shouldShowExternalPlaylistQueueBarByPolicy(
    isExternalPlaylist: Boolean,
    externalPlaylistSource: ExternalPlaylistSource,
    playlistSize: Int
): Boolean {
    val sourceCanShowQueue = when (externalPlaylistSource) {
        ExternalPlaylistSource.WATCH_LATER,
        ExternalPlaylistSource.FAVORITE,
        ExternalPlaylistSource.SPACE -> true
        ExternalPlaylistSource.NONE,
        ExternalPlaylistSource.UNKNOWN -> false
    }
    return isExternalPlaylist &&
        sourceCanShowQueue &&
        playlistSize > 0
}

internal fun shouldShowExternalPlaylistQueueBarOnContentTab(
    queueAvailable: Boolean,
    selectedTabIndex: Int
): Boolean {
    return queueAvailable && selectedTabIndex != VIDEO_CONTENT_COMMENT_TAB_INDEX
}

internal fun resolveExternalPlaylistQueueTitle(
    externalPlaylistSource: ExternalPlaylistSource
): String {
    return when (externalPlaylistSource) {
        ExternalPlaylistSource.WATCH_LATER -> "稍后再看"
        ExternalPlaylistSource.FAVORITE -> "收藏夹"
        ExternalPlaylistSource.SPACE -> "UP主视频"
        ExternalPlaylistSource.NONE,
        ExternalPlaylistSource.UNKNOWN -> "播放队列"
    }
}

internal fun normalizePlaylistCoverUrlForUi(rawUrl: String?): String {
    val url = rawUrl?.trim().orEmpty()
    if (url.isBlank()) return ""
    return when {
        url.startsWith("//") -> "https:$url"
        url.startsWith("http://", ignoreCase = true) -> "https://${url.substring(7)}"
        else -> url
    }
}

internal fun resolveExternalPlaylistQueueListMaxHeightDp(screenHeightDp: Int): Int {
    val dynamicHeight = (screenHeightDp * 0.72f).roundToInt()
    return dynamicHeight.coerceIn(420, 680)
}

internal fun resolveExternalPlaylistQueueBottomSpacerDp(navigationBarBottomDp: Int): Int {
    return (navigationBarBottomDp + 8).coerceAtLeast(8)
}

internal enum class ExternalPlaylistQueueSheetPresentation {
    INLINE_HAZE,
    MODAL
}

internal fun resolveExternalPlaylistQueueSheetPresentation(
    requireRealtimeHaze: Boolean
): ExternalPlaylistQueueSheetPresentation {
    return if (requireRealtimeHaze) {
        ExternalPlaylistQueueSheetPresentation.INLINE_HAZE
    } else {
        ExternalPlaylistQueueSheetPresentation.MODAL
    }
}

internal fun shouldOpenCommentUrlInApp(url: String): Boolean {
    val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return false
    val scheme = uri.scheme?.lowercase().orEmpty()
    if (scheme !in setOf("http", "https", "bili", "bilibili")) return false
    val host = uri.host?.lowercase().orEmpty()
    return host.contains("bilibili.com") || host.contains("b23.tv")
}

internal sealed interface CommentUrlNavigationTarget {
    data class Video(val videoId: String) : CommentUrlNavigationTarget
    data class Search(val keyword: String) : CommentUrlNavigationTarget
    data class Space(val mid: Long) : CommentUrlNavigationTarget
}

internal fun resolveCommentUrlNavigationTarget(rawUrl: String): CommentUrlNavigationTarget? {
    val url = rawUrl.trim()
    if (url.isEmpty()) return null
    return when (val target = com.android.purebilibili.core.util.BilibiliNavigationTargetParser.parse(url)) {
        is com.android.purebilibili.core.util.BilibiliNavigationTarget.Video -> {
            target.videoId.trim()
                .takeIf { it.isNotEmpty() }
                ?.let(CommentUrlNavigationTarget::Video)
        }

        is com.android.purebilibili.core.util.BilibiliNavigationTarget.Search -> {
            target.keyword.trim()
                .takeIf { it.isNotEmpty() }
                ?.let(CommentUrlNavigationTarget::Search)
        }

        is com.android.purebilibili.core.util.BilibiliNavigationTarget.Space -> {
            target.mid.takeIf { it > 0L }?.let(CommentUrlNavigationTarget::Space)
        }

        else -> null
    }
}

internal fun resolveDanmakuDialogTopReservePx(
    isLandscape: Boolean,
    isFullscreenMode: Boolean,
    isPortraitFullscreen: Boolean,
    playerBottomPx: Int?,
    fallbackPlayerBottomPx: Int = 0
): Int {
    if (isLandscape || isFullscreenMode || isPortraitFullscreen) return 0
    return (playerBottomPx ?: fallbackPlayerBottomPx).coerceAtLeast(0)
}

internal data class VideoDetailEntryVisualFrame(
    val contentAlpha: Float,
    val scrimAlpha: Float,
    val blurRadiusPx: Float
)

internal data class VideoDetailLoadingTransitionFrame(
    val showLoadingContent: Boolean,
    val containerBackgroundAlpha: Float,
    val loadingContentAlpha: Float
)

internal data class VideoDetailRouteSheetMotion(
    val enabled: Boolean,
    val durationMillis: Int,
    val mainDurationMillis: Int,
    val settleDurationMillis: Int,
    val initialScale: Float,
    val initialTranslationYDp: Float,
    val initialCornerDp: Float,
    val initialBackgroundScrimAlpha: Float,
    val settleScaleDelta: Float,
    val settleTranslationDp: Float,
    val easing: Easing
)

internal enum class VideoDetailRouteSheetSettleDirection {
    None,
    Enter,
    Return
}

internal data class VideoDetailRouteSheetFrame(
    val scale: Float,
    val translationYDp: Float,
    val cornerDp: Float,
    val backgroundScrimAlpha: Float,
    val settleProgress: Float
)

internal data class VideoDetailMotionSpec(
    val entryPhaseDurationMillis: Int,
    val contentSwapFadeDurationMillis: Int,
    val contentRevealFadeDurationMillis: Int
)

private const val VIDEO_DETAIL_ENTRY_PHASE_MIN_DURATION_MILLIS = 120
private const val VIDEO_DETAIL_CONTENT_PHASE_MIN_DURATION_MILLIS = 180
private const val HOME_VIDEO_ROUTE_SHEET_MAIN_DURATION_MILLIS = 320
private const val HOME_VIDEO_ROUTE_SHEET_SETTLE_DURATION_MILLIS = 96
private const val HOME_VIDEO_ROUTE_SHEET_DURATION_MILLIS =
    HOME_VIDEO_ROUTE_SHEET_MAIN_DURATION_MILLIS + HOME_VIDEO_ROUTE_SHEET_SETTLE_DURATION_MILLIS
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_SCALE = 0.965f
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_TRANSLATION_Y_DP = 56f
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_CORNER_DP = 28f
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_SCRIM_ALPHA = 0.18f
private const val HOME_VIDEO_ROUTE_SHEET_SETTLE_SCALE_DELTA = 0.0015f
private const val HOME_VIDEO_ROUTE_SHEET_SETTLE_TRANSLATION_DP = 1.5f

internal fun resolveVideoDetailMotionSpec(
    transitionEnterDurationMillis: Int
): VideoDetailMotionSpec {
    return VideoDetailMotionSpec(
        entryPhaseDurationMillis = transitionEnterDurationMillis
            .coerceAtLeast(VIDEO_DETAIL_ENTRY_PHASE_MIN_DURATION_MILLIS),
        contentSwapFadeDurationMillis = transitionEnterDurationMillis
            .coerceAtLeast(VIDEO_DETAIL_CONTENT_PHASE_MIN_DURATION_MILLIS),
        contentRevealFadeDurationMillis = transitionEnterDurationMillis
            .coerceAtLeast(VIDEO_DETAIL_CONTENT_PHASE_MIN_DURATION_MILLIS)
    )
}

internal fun resolveVideoDetailRouteSheetMotion(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoDetailRouteSheetMotion {
    val isHomeSource = sourceRoute?.substringBefore("?") == com.android.purebilibili.navigation.ScreenRoutes.Home.route
    val enabled = transitionEnabled && isHomeSource
    return VideoDetailRouteSheetMotion(
        enabled = enabled,
        durationMillis = HOME_VIDEO_ROUTE_SHEET_DURATION_MILLIS,
        mainDurationMillis = HOME_VIDEO_ROUTE_SHEET_MAIN_DURATION_MILLIS,
        settleDurationMillis = HOME_VIDEO_ROUTE_SHEET_SETTLE_DURATION_MILLIS,
        initialScale = HOME_VIDEO_ROUTE_SHEET_INITIAL_SCALE,
        initialTranslationYDp = HOME_VIDEO_ROUTE_SHEET_INITIAL_TRANSLATION_Y_DP,
        initialCornerDp = HOME_VIDEO_ROUTE_SHEET_INITIAL_CORNER_DP,
        initialBackgroundScrimAlpha = HOME_VIDEO_ROUTE_SHEET_INITIAL_SCRIM_ALPHA,
        settleScaleDelta = HOME_VIDEO_ROUTE_SHEET_SETTLE_SCALE_DELTA,
        settleTranslationDp = HOME_VIDEO_ROUTE_SHEET_SETTLE_TRANSLATION_DP,
        easing = resolveVideoCardSharedTransitionEasing()
    )
}

internal fun resolveVideoDetailRouteSheetFrame(
    rawProgress: Float,
    settleProgress: Float = 0f,
    settleDirection: VideoDetailRouteSheetSettleDirection = VideoDetailRouteSheetSettleDirection.None,
    motion: VideoDetailRouteSheetMotion
): VideoDetailRouteSheetFrame {
    if (!motion.enabled) {
        return VideoDetailRouteSheetFrame(
            scale = 1f,
            translationYDp = 0f,
            cornerDp = 0f,
            backgroundScrimAlpha = 0f,
            settleProgress = 0f
        )
    }
    val progress = rawProgress.coerceIn(0f, 1f)
    val safeSettleProgress = settleProgress.coerceIn(0f, 1f)
    val settleScale = when (settleDirection) {
        VideoDetailRouteSheetSettleDirection.Enter -> motion.settleScaleDelta * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.Return -> -motion.settleScaleDelta * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.None -> 0f
    }
    val settleTranslation = when (settleDirection) {
        VideoDetailRouteSheetSettleDirection.Enter -> -motion.settleTranslationDp * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.Return -> motion.settleTranslationDp * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.None -> 0f
    }
    return VideoDetailRouteSheetFrame(
        scale = lerpVideoDetailFloat(motion.initialScale, 1f, progress) + settleScale,
        translationYDp = lerpVideoDetailFloat(motion.initialTranslationYDp, 0f, progress) + settleTranslation,
        cornerDp = lerpVideoDetailFloat(motion.initialCornerDp, 0f, progress),
        backgroundScrimAlpha = lerpVideoDetailFloat(motion.initialBackgroundScrimAlpha, 0f, progress),
        settleProgress = safeSettleProgress
    )
}

private fun lerpVideoDetailFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@Composable
private fun rememberVideoDetailRouteSheetFrame(
    motion: VideoDetailRouteSheetMotion,
    isExitTransitionInProgress: Boolean,
    sharedBoundsActive: Boolean = false
): VideoDetailRouteSheetFrame {
    // shell sharedBounds 接管整张详情壳的 morph 时，sheet 自身的 scale/translation/corner/scrim
    // 必须全部停摆——否则会与共享元素同时形变导致撕裂。等价于 motion.enabled = false。
    val effectiveMotion = if (sharedBoundsActive) motion.copy(enabled = false) else motion
    val routeSheetProgress = remember(effectiveMotion.enabled) {
        Animatable(if (effectiveMotion.enabled) 0f else 1f)
    }
    val routeSheetSettleProgress = remember(effectiveMotion.enabled) {
        Animatable(0f)
    }
    var settleDirection by remember {
        mutableStateOf(VideoDetailRouteSheetSettleDirection.None)
    }

    LaunchedEffect(
        effectiveMotion.enabled,
        effectiveMotion.mainDurationMillis,
        effectiveMotion.settleDurationMillis,
        isExitTransitionInProgress
    ) {
        if (!effectiveMotion.enabled) {
            settleDirection = VideoDetailRouteSheetSettleDirection.None
            routeSheetSettleProgress.snapTo(0f)
            routeSheetProgress.snapTo(1f)
            return@LaunchedEffect
        }

        settleDirection = VideoDetailRouteSheetSettleDirection.None
        routeSheetSettleProgress.snapTo(0f)
        val targetProgress = if (isExitTransitionInProgress) 0f else 1f
        routeSheetProgress.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(
                durationMillis = effectiveMotion.mainDurationMillis,
                easing = effectiveMotion.easing
            )
        )
        settleDirection = if (isExitTransitionInProgress) {
            VideoDetailRouteSheetSettleDirection.Return
        } else {
            VideoDetailRouteSheetSettleDirection.Enter
        }
        routeSheetSettleProgress.snapTo(1f)
        routeSheetSettleProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = effectiveMotion.settleDurationMillis,
                easing = effectiveMotion.easing
            )
        )
        settleDirection = VideoDetailRouteSheetSettleDirection.None
    }

    return remember(
        routeSheetProgress.value,
        routeSheetSettleProgress.value,
        settleDirection,
        effectiveMotion
    ) {
        resolveVideoDetailRouteSheetFrame(
            rawProgress = routeSheetProgress.value,
            settleProgress = routeSheetSettleProgress.value,
            settleDirection = settleDirection,
            motion = effectiveMotion
        )
    }
}

@Composable
private fun VideoDetailRouteSheetHost(
    frame: VideoDetailRouteSheetFrame,
    motion: VideoDetailRouteSheetMotion,
    isFullscreenMode: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    overlayContent: @Composable BoxScope.() -> Unit
) {
    val routeSheetTranslationYPx = with(LocalDensity.current) {
        frame.translationYDp.dp.toPx()
    }
    val routeSheetShape = RoundedCornerShape(frame.cornerDp.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = frame.backgroundScrimAlpha))
            .graphicsLayer {
                scaleX = frame.scale
                scaleY = frame.scale
                translationY = routeSheetTranslationYPx
                transformOrigin = TransformOrigin(0.5f, 0f)
                clip = motion.enabled && frame.cornerDp > 0.01f
                shape = routeSheetShape
            }
            .background(if (isFullscreenMode) Color.Black else backgroundColor),
    ) {
        content()
        overlayContent()
    }
}

internal fun resolveVideoDetailEntryVisualFrame(
    rawProgress: Float,
    transitionEnabled: Boolean,
    fallbackBlurEnabled: Boolean = false,
    maxBlurRadiusPx: Float
): VideoDetailEntryVisualFrame {
    // 共享元素模式下，sharedBounds 已经处理视觉过渡，
    // 额外的 alpha/blur 会与共享元素动画冲突导致闪烁。
    if (transitionEnabled || !fallbackBlurEnabled) {
        return VideoDetailEntryVisualFrame(
            contentAlpha = 1f,
            scrimAlpha = 0f,
            blurRadiusPx = 0f
        )
    }
    val progress = rawProgress.coerceIn(0f, 1f)
    val maxLightBlurRadiusPx = maxBlurRadiusPx.coerceAtMost(6f).coerceAtLeast(0f)
    return VideoDetailEntryVisualFrame(
        contentAlpha = 1f,
        scrimAlpha = 0f,
        blurRadiusPx = maxLightBlurRadiusPx * (1f - progress)
    )
}

internal fun resolveVideoDetailLoadingTransitionFrame(
    session: VideoCardTransitionSession
): VideoDetailLoadingTransitionFrame {
    val expanding = session.phase == VideoCardTransitionPhase.EXPANDING &&
        session.progress < 1f - 0.001f
    return if (expanding) {
        VideoDetailLoadingTransitionFrame(
            showLoadingContent = false,
            containerBackgroundAlpha = 0f,
            loadingContentAlpha = 0f
        )
    } else {
        VideoDetailLoadingTransitionFrame(
            showLoadingContent = true,
            containerBackgroundAlpha = 1f,
            loadingContentAlpha = 1f
        )
    }
}

internal fun resolveVideoDetailContainerTransformContentAlpha(
    session: VideoCardTransitionSession
): Float {
    return if (
        session.phase == VideoCardTransitionPhase.EXPANDING &&
        session.progress < 0.92f
    ) {
        0f
    } else {
        1f
    }
}

internal fun shouldApplyPipParamsUpdate(
    pipModeEnabled: Boolean,
    modeChanged: Boolean,
    boundsChanged: Boolean,
    elapsedSinceLastUpdateMs: Long,
    minUpdateIntervalMs: Long = 400L
): Boolean {
    if (!pipModeEnabled) return false
    if (modeChanged) return true
    if (!boundsChanged) return false
    return elapsedSinceLastUpdateMs >= minUpdateIntervalMs
}

internal fun shouldDismissCommentThreadDetailForPip(
    wasInPipMode: Boolean,
    isInPipMode: Boolean,
    subReplyVisible: Boolean
): Boolean {
    return !wasInPipMode && isInPipMode && subReplyVisible
}

internal fun shouldAutoEnterAudioModeFromRoute(
    startAudioFromRoute: Boolean,
    hasAutoEnteredAudioMode: Boolean,
    isVideoLoadSuccess: Boolean
): Boolean {
    return startAudioFromRoute && !hasAutoEnteredAudioMode && isVideoLoadSuccess
}

internal fun shouldAutoEnterPortraitFullscreenFromRoute(
    autoEnterPortraitFromRoute: Boolean,
    startAudioFromRoute: Boolean,
    portraitExperienceEnabled: Boolean,
    useOfficialInlinePortraitDetailExperience: Boolean,
    allowStandalonePortraitAutoEnter: Boolean = true,
    isCurrentRouteVideoLoaded: Boolean,
    isVerticalVideo: Boolean,
    isPortraitFullscreen: Boolean,
    hasAutoEnteredPortraitFromRoute: Boolean
): Boolean {
    return autoEnterPortraitFromRoute &&
        !startAudioFromRoute &&
        portraitExperienceEnabled &&
        !useOfficialInlinePortraitDetailExperience &&
        allowStandalonePortraitAutoEnter &&
        isCurrentRouteVideoLoaded &&
        isVerticalVideo &&
        !isPortraitFullscreen &&
        !hasAutoEnteredPortraitFromRoute
}

internal fun shouldStartInPortraitFullscreenFromRouteHint(
    autoEnterPortraitFromRoute: Boolean,
    startAudioFromRoute: Boolean,
    initialVerticalFromRoute: Boolean
): Boolean {
    return autoEnterPortraitFromRoute &&
        !startAudioFromRoute &&
        initialVerticalFromRoute
}

internal fun shouldSyncMainPlayerToInternalBvid(
    isPortraitFullscreen: Boolean,
    routeBvid: String,
    currentBvid: String,
    currentBvidCid: Long,
    loadedBvid: String,
    loadedCid: Long
): Boolean {
    if (isPortraitFullscreen) return false
    if (currentBvid.isBlank()) return false
    if (loadedBvid != currentBvid && currentBvid == routeBvid) return false
    if (loadedBvid != currentBvid) return true
    val targetCid = currentBvidCid.takeIf { it > 0L } ?: return false
    val resolvedLoadedCid = loadedCid.takeIf { it > 0L } ?: return true
    return resolvedLoadedCid != targetCid
}

internal fun resolveAutoPlayOverrideForInternalBvidSync(
    forceAutoPlay: Boolean
): Boolean? {
    return if (forceAutoPlay) true else null
}

internal fun shouldSwitchCollectionVideoInsideCurrentDetailPage(
    targetBvid: String,
    currentBvid: String,
    ugcSeason: UgcSeason?
): Boolean {
    val normalizedTargetBvid = targetBvid.trim()
    if (normalizedTargetBvid.isBlank() || normalizedTargetBvid == currentBvid.trim()) {
        return false
    }
    return ugcSeason
        ?.sections
        .orEmpty()
        .flatMap { section -> section.episodes }
        .any { episode -> episode.bvid == normalizedTargetBvid } == true
}

internal data class VideoPlayerSectionTarget(
    val bvid: String,
    val entryCoverUrl: String
)

internal fun resolveVideoPlayerSectionTarget(
    routeBvid: String,
    routeCoverUrl: String,
    currentBvid: String
): VideoPlayerSectionTarget {
    val normalizedCurrentBvid = currentBvid.trim()
    val normalizedRouteBvid = routeBvid.trim()
    val resolvedBvid = normalizedCurrentBvid.ifBlank { normalizedRouteBvid }
    val resolvedCoverUrl = if (resolvedBvid == normalizedRouteBvid) {
        routeCoverUrl
    } else {
        ""
    }
    return VideoPlayerSectionTarget(
        bvid = resolvedBvid,
        entryCoverUrl = resolvedCoverUrl
    )
}

@Composable
private fun PortraitInlineVideoPlayerHost(
    modifier: Modifier,
    animatedViewportWidth: Dp,
    animatedViewportHeight: Dp,
    inlinePlayerAlpha: Float,
    inlinePlayerScale: Float,
    context: Context,
    playerState: VideoPlayerState,
    uiState: PlayerUiState,
    isPipMode: Boolean,
    transitionEnabled: Boolean,
    onToggleFullscreen: () -> Unit,
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    videoPlayerSectionTarget: VideoPlayerSectionTarget,
    sponsorSegment: com.android.purebilibili.data.model.response.SponsorSegment?,
    showSponsorSkipButton: Boolean,
    sleepTimerMinutes: Int?,
    viewPoints: List<ViewPoint>,
    pbpProgressData: PbpProgressData?,
    sponsorProgressMarkers: List<com.android.purebilibili.data.model.response.SponsorProgressMarker>,
    isVerticalVideo: Boolean,
    onPortraitFullscreen: () -> Unit,
    isPortraitFullscreen: Boolean,
    onPipClick: () -> Unit,
    codecPreference: String,
    secondCodecPreference: String,
    audioQualityPreference: Int,
    onNavigateToAudioMode: () -> Unit,
    forceCoverOnly: Boolean,
    allowLivePlayerSharedElement: Boolean,
    sourceRouteForSharedElement: String?,
    suppressSubtitleOverlay: Boolean,
    subtitleDisplayModePreferenceOverride: SubtitleDisplayMode?,
    onSubtitleDisplayModePreferenceOverrideChange: (SubtitleDisplayMode) -> Unit
) {
    val successState = uiState as? PlayerUiState.Success

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedViewportHeight)
            .alpha(inlinePlayerAlpha)
            .graphicsLayer {
                scaleX = inlinePlayerScale
                scaleY = inlinePlayerScale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
    ) {
        VideoPlayerSection(
            playerState = playerState,
            uiState = uiState,
            isFullscreen = false,
            isInPipMode = isPipMode,
            transitionEnabled = transitionEnabled,
            onToggleFullscreen = onToggleFullscreen,
            onQualityChange = { qid -> viewModel.changeQuality(qid) },
            onBack = onBack,
            onHomeClick = onHomeClick,
            onDanmakuInputClick = { viewModel.showDanmakuSendDialog() },
            bvid = videoPlayerSectionTarget.bvid,
            coverUrl = videoPlayerSectionTarget.entryCoverUrl,
            onDoubleTapLike = { viewModel.toggleLike() },
            sponsorSegment = sponsorSegment,
            showSponsorSkipButton = showSponsorSkipButton,
            onSponsorSkip = { viewModel.skipCurrentSponsorSegment() },
            onSponsorDismiss = { viewModel.dismissSponsorSkipButton() },
            onReloadVideo = { viewModel.reloadVideo() },
            currentCdnIndex = successState?.currentCdnIndex ?: 0,
            cdnCount = successState?.cdnCount ?: 1,
            cdnLineDiagnostics = successState?.cdnLineDiagnostics.orEmpty(),
            isCdnProbing = successState?.isCdnProbing ?: false,
            onSwitchCdn = { viewModel.switchCdn() },
            onSwitchCdnTo = { viewModel.switchCdnTo(it) },
            onProbeCdnCandidates = { viewModel.probeCurrentCdnCandidates() },
            isAudioOnly = false,
            onAudioOnlyToggle = onNavigateToAudioMode,
            sleepTimerMinutes = sleepTimerMinutes,
            onSleepTimerChange = { viewModel.setSleepTimer(it) },
            videoshotData = successState?.videoshotData,
            viewPoints = viewPoints,
            pbpProgressData = pbpProgressData,
            sponsorMarkers = sponsorProgressMarkers,
            onUserSeek = { position -> viewModel.notifyPluginsOfExplicitSeek(position) },
            isVerticalVideo = isVerticalVideo,
            onPortraitFullscreen = onPortraitFullscreen,
            isPortraitFullscreen = isPortraitFullscreen,
            viewportWidthDpOverride = animatedViewportWidth.value.roundToInt(),
            onPipClick = onPipClick,
            currentCodec = codecPreference,
            onCodecChange = { viewModel.setVideoCodec(it) },
            currentSecondCodec = secondCodecPreference,
            onSecondCodecChange = { viewModel.setVideoSecondCodec(it) },
            currentAudioQuality = audioQualityPreference,
            onAudioQualityChange = { viewModel.setAudioQuality(it) },
            onPlaybackSpeedChange = { viewModel.applyPlaybackSpeedFromUi(it) },
            onAudioLangChange = { viewModel.changeAudioLanguage(it) },
            onSaveCover = { viewModel.saveCover(context) },
            onDownloadAudio = { viewModel.downloadAudio(context) },
            forceCoverOnly = forceCoverOnly,
            allowLivePlayerSharedElement = allowLivePlayerSharedElement,
            sourceRouteForSharedElement = sourceRouteForSharedElement,
            suppressSubtitleOverlay = suppressSubtitleOverlay,
            subtitleDisplayModePreferenceOverride = subtitleDisplayModePreferenceOverride,
            onSubtitleDisplayModePreferenceOverrideChange = onSubtitleDisplayModePreferenceOverrideChange,
            onSubtitleTrackSelected = viewModel::selectSubtitleTrack
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VideoDetailScreen(
    bvid: String,
    cid: Long = 0L,
    coverUrl: String = "",
    startInFullscreen: Boolean = false,
    startAudioFromRoute: Boolean = false,
    autoEnterPortraitFromRoute: Boolean = false,
    initialVerticalFromRoute: Boolean = false,
    resumePositionMsFromRoute: Long = 0L,
    openCommentRootRpidFromRoute: Long = 0L,
    openCommentTargetRpidFromRoute: Long = 0L,
    sourceRouteForSharedElement: String? = null,
    isReturningFromDetail: Boolean = false,
    isQuickReturningFromDetail: Boolean = false,
    onMarkReturningFromDetail: () -> Unit = {},
    onClearReturningFromDetail: () -> Unit = {},
    transitionEnabled: Boolean = false,
    fallbackEntryBlurEnabled: Boolean = false,
    transitionEnterDurationMillis: Int = 320,
    transitionMaxBlurRadiusPx: Float = 20f,
    onBack: () -> Unit,
    onHomeClick: () -> Unit = onBack,
    onNavigateToAudioMode: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onSearchKeywordClick: (String) -> Unit = {},
    onOpenBilibiliLink: ((String) -> Unit)? = null,
    onVideoClick: (String, android.os.Bundle?) -> Unit,
    onUpClick: (Long) -> Unit = {},
    miniPlayerManager: MiniPlayerManager? = null,
    isInPipMode: Boolean = false,
    isVisible: Boolean = true,
    viewModel: PlayerViewModel = viewModel(),
    commentViewModel: VideoCommentViewModel = viewModel(),
    onBgmClick: (BgmInfo) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val homeUpBadgesVisible by com.android.purebilibili.core.store.SettingsManager
        .getHomeUpBadgesVisible(context)
        .collectAsStateWithLifecycle(initialValue = true
        )
    val motionSpec = remember(transitionEnterDurationMillis) {
        resolveVideoDetailMotionSpec(transitionEnterDurationMillis)
    }
    val sharedTransitionSpeedSettings = LocalVideoSharedTransitionSpeedSettings.current
    val homeSharedTransitionMotionSpec = remember(
        sourceRouteForSharedElement,
        transitionEnabled,
        sharedTransitionSpeedSettings
    ) {
        resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = sourceRouteForSharedElement,
            transitionEnabled = transitionEnabled,
            speedSettings = sharedTransitionSpeedSettings
        )
    }
    val sharedTransitionSourceCornerDp = remember(sourceRouteForSharedElement) {
        CardPositionManager.lastClickedVideoSourceCornerDp
            ?: resolveVideoSharedTransitionSourceCornerDp(sourceRouteForSharedElement)
    }
    val videoSharedPlaybackIntent = remember(context, startAudioFromRoute) {
        resolveVideoSharedTransitionPlaybackIntent(
            clickToPlayEnabled = com.android.purebilibili.core.store.SettingsManager.getClickToPlaySync(context),
            forceImmediatePlayback = startAudioFromRoute
        )
    }
    val routeSheetMotion = remember(sourceRouteForSharedElement, transitionEnabled) {
        resolveVideoDetailRouteSheetMotion(
            sourceRoute = sourceRouteForSharedElement,
            transitionEnabled = transitionEnabled
        )
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videoCardTransitionSession = LocalVideoCardTransitionSession.current
    val detailLoadingTransitionFrame = remember(videoCardTransitionSession) {
        resolveVideoDetailLoadingTransitionFrame(videoCardTransitionSession)
    }
    val detailLoadingBackgroundAlpha by animateFloatAsState(
        targetValue = if (uiState is PlayerUiState.Loading) {
            detailLoadingTransitionFrame.containerBackgroundAlpha
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 140),
        label = "video-detail-loading-background"
    )
    val resumePlaybackSuggestion by viewModel.resumePlaybackSuggestion.collectAsStateWithLifecycle()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var isNavigatingToVideo by remember { mutableStateOf(false) }
    var isNavigatingToAudioMode by remember { mutableStateOf(false) }
    var isNavigatingToMiniMode by remember { mutableStateOf(false) }
    var hasAutoEnteredAudioMode by rememberSaveable { mutableStateOf(false) }
    var hasAutoEnteredPortraitFromRoute by rememberSaveable(bvid) { mutableStateOf(false) }
    var hasHandledCommentRootFromRoute by rememberSaveable(
        bvid,
        openCommentRootRpidFromRoute,
        openCommentTargetRpidFromRoute
    ) { mutableStateOf(false) }
    // 🔄 [Seamless Playback] Internal BVID state to support seamless switching in portrait mode
    var currentBvid by rememberSaveable(bvid) { mutableStateOf(bvid) }
    var currentBvidCid by rememberSaveable { mutableLongStateOf(0L) }

    fun markSecondaryNavigationLeave(expectedBvid: String = currentBvid) {
        miniPlayerManager?.markLeavingByNavigation(expectedBvid = expectedBvid)
    }

    val navigateToUserSpaceFromVideo: (Long) -> Unit = { mid ->
        onUpClick(mid)
    }

    val navigateToSearchFromVideo: () -> Unit = {
        onNavigateToSearch()
    }

    val navigateToSearchKeywordFromVideo: (String) -> Unit = { keyword ->
        onSearchKeywordClick(keyword)
    }

    fun switchVideoInCurrentDetailPage(
        targetBvid: String,
        targetCid: Long,
        autoPlay: Boolean = true
    ) {
        val normalizedBvid = targetBvid.trim()
        if (normalizedBvid.isBlank()) return
        val safeCid = targetCid.coerceAtLeast(0L)
        val success = uiState as? PlayerUiState.Success
        if (success?.info?.bvid == normalizedBvid && (safeCid <= 0L || success.info.cid == safeCid)) {
            return
        }
        currentBvid = normalizedBvid
        currentBvidCid = safeCid
        viewModel.loadVideo(
            bvid = normalizedBvid,
            cid = safeCid,
            autoPlay = autoPlay
        )
    }

    val navigateToRelatedVideo = remember(onVideoClick, miniPlayerManager, uiState, currentBvid) {
        { targetBvid: String, options: android.os.Bundle? ->
            val success = uiState as? PlayerUiState.Success
            val explicitCid = options?.getLong(VIDEO_NAV_TARGET_CID_KEY) ?: 0L
            val resolvedCid = resolveNavigationTargetCid(
                targetBvid = targetBvid,
                explicitCid = explicitCid,
                relatedVideos = success?.related.orEmpty(),
                ugcSeason = success?.info?.ugc_season
            )
            com.android.purebilibili.core.util.Logger.d(
                "VideoDetailScreen",
                "navigateToRelatedVideo: current=${success?.info?.bvid ?: "unknown"} target=$targetBvid explicitCid=$explicitCid resolvedCid=$resolvedCid"
            )
            if (
                shouldSwitchCollectionVideoInsideCurrentDetailPage(
                    targetBvid = targetBvid,
                    currentBvid = success?.info?.bvid ?: currentBvid,
                    ugcSeason = success?.info?.ugc_season
                )
            ) {
                miniPlayerManager?.isNavigatingToVideo = false
                switchVideoInCurrentDetailPage(
                    targetBvid = targetBvid,
                    targetCid = resolvedCid,
                    autoPlay = true
                )
            } else {
                isNavigatingToVideo = true
                miniPlayerManager?.isNavigatingToVideo = true
                markSecondaryNavigationLeave(expectedBvid = success?.info?.bvid ?: currentBvid)
                val navOptions = android.os.Bundle(options ?: android.os.Bundle.EMPTY)
                if (resolvedCid > 0L) {
                    navOptions.putLong(VIDEO_NAV_TARGET_CID_KEY, resolvedCid)
                }
                onVideoClick(targetBvid, navOptions)
            }
        }
    }

    LaunchedEffect(bvid, cid) {
        com.android.purebilibili.core.util.Logger.d(
            "VideoDetailScreen",
            "SUB_DBG screen entry args: bvid=$bvid, cid=$cid"
        )
    }

    val openCommentUrl: (String) -> Unit = openCommentUrl@{ rawUrl ->
        val url = rawUrl.trim()
        if (url.isEmpty()) return@openCommentUrl
        if (onOpenBilibiliLink != null) {
            onOpenBilibiliLink(url)
            return@openCommentUrl
        }

        when (val target = resolveCommentUrlNavigationTarget(url)) {
            is CommentUrlNavigationTarget.Video -> {
                navigateToRelatedVideo(target.videoId, null)
                return@openCommentUrl
            }

            is CommentUrlNavigationTarget.Search -> {
                navigateToSearchKeywordFromVideo(target.keyword)
                return@openCommentUrl
            }

            is CommentUrlNavigationTarget.Space -> {
                navigateToUserSpaceFromVideo(target.mid)
                return@openCommentUrl
            }

            null -> Unit
        }

        runCatching { uriHandler.openUri(url) }
    }

    // 🎭 [性能优化] 进场视觉帧 + 重型组件延迟加载
    // shell sharedBounds 接管整体 morph 时，内容必须从第一帧就处在最终布局，
    // 不能再走 isTransitionFinished 门控触发的二级 fadeIn / slide / shrink。
    val shellSharedBoundsLikely = transitionEnabled && !sourceRouteForSharedElement.isNullOrBlank()
    val entryVisualEnabled = transitionEnabled || fallbackEntryBlurEnabled
    var isTransitionFinished by remember {
        mutableStateOf(!entryVisualEnabled || shellSharedBoundsLikely)
    }
    val entryVisualProgress = remember(entryVisualEnabled) {
        Animatable(if (entryVisualEnabled) 0f else 1f)
    }

    LaunchedEffect(
        entryVisualEnabled,
        motionSpec.entryPhaseDurationMillis,
        shellSharedBoundsLikely
    ) {
        if (!entryVisualEnabled || shellSharedBoundsLikely) {
            entryVisualProgress.snapTo(1f)
            isTransitionFinished = true
            return@LaunchedEffect
        }

        entryVisualProgress.snapTo(0f)
        isTransitionFinished = false
        entryVisualProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = motionSpec.entryPhaseDurationMillis,
                easing = FastOutSlowInEasing
            )
        )
        isTransitionFinished = true
    }

    val entryVisualFrame = remember(
        entryVisualProgress.value,
        transitionEnabled,
        fallbackEntryBlurEnabled,
        transitionMaxBlurRadiusPx,
        videoCardTransitionSession
    ) {
        val baseFrame = resolveVideoDetailEntryVisualFrame(
            rawProgress = entryVisualProgress.value,
            transitionEnabled = transitionEnabled,
            fallbackBlurEnabled = fallbackEntryBlurEnabled,
            maxBlurRadiusPx = transitionMaxBlurRadiusPx
        )
        baseFrame.copy(
            contentAlpha = baseFrame.contentAlpha *
                resolveVideoDetailContainerTransformContentAlpha(videoCardTransitionSession)
        )
    }

    //  监听评论状态
    val commentState by commentViewModel.commentState.collectAsStateWithLifecycle()
    val subReplyState by commentViewModel.subReplyState.collectAsStateWithLifecycle()

    LaunchedEffect(
        openCommentRootRpidFromRoute,
        openCommentTargetRpidFromRoute,
        commentState.replies,
        commentState.isRepliesLoading,
        subReplyState.visible
    ) {
        if (openCommentRootRpidFromRoute <= 0L || hasHandledCommentRootFromRoute || subReplyState.visible) {
            return@LaunchedEffect
        }

        val rootReply = commentState.replies.firstOrNull { it.rpid == openCommentRootRpidFromRoute }
        if (rootReply != null) {
            commentViewModel.openSubReply(rootReply, openCommentTargetRpidFromRoute)
            hasHandledCommentRootFromRoute = true
        } else if (!commentState.isRepliesLoading) {
            val openStarted = commentViewModel.openSubReplyFromRoute(
                rootReplyId = openCommentRootRpidFromRoute,
                targetReplyId = openCommentTargetRpidFromRoute
            )
            if (openStarted) {
                hasHandledCommentRootFromRoute = true
            }
        }
    }
    val commentDefaultSortMode by com.android.purebilibili.core.store.SettingsManager
        .getCommentDefaultSortMode(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.SettingsManager.getCommentDefaultSortModeSync(context),
            lifecycle = lifecycleOwner.lifecycle
        )
    val commentFraudDetectionEnabled by com.android.purebilibili.core.store.SettingsManager
        .getCommentFraudDetectionEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )
    val commentMemberDecorationsEnabled by com.android.purebilibili.core.store.SettingsManager
        .getCommentMemberDecorationsEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = false,
            lifecycle = lifecycleOwner.lifecycle
        )
    val preferredCommentSortMode = remember(commentDefaultSortMode) {
        CommentSortMode.fromApiMode(commentDefaultSortMode)
    }
    LaunchedEffect(commentFraudDetectionEnabled, uiState) {
        val success = uiState as? PlayerUiState.Success ?: return@LaunchedEffect
        viewModel.commentSentEvent.collect { reply ->
            commentViewModel.onExternalCommentSent(
                aid = success.info.aid,
                newReply = reply,
                fraudDetectionEnabled = commentFraudDetectionEnabled
            )
        }
    }
    var fraudDialogStatus by remember { mutableStateOf<CommentFraudStatus?>(null) }
    LaunchedEffect(Unit) {
        commentViewModel.fraudEvent.collect { status ->
            val lightMessage = resolveCommentFraudLightMessage(status)
            if (lightMessage != null) {
                Toast.makeText(context, lightMessage, Toast.LENGTH_SHORT).show()
            } else if (shouldShowCommentFraudResultDialog(status)) {
                fraudDialogStatus = status
            }
        }
    }
    fraudDialogStatus?.let { status ->
        CommentFraudResultDialog(
            status = status,
            onDismiss = {
                fraudDialogStatus = null
                commentViewModel.dismissFraudResult()
            },
            onDeleteComment = if (status == CommentFraudStatus.SHADOW_BANNED) {
                {
                    val rpid = commentViewModel.commentState.value.fraudDetectRpid
                    if (rpid > 0L) {
                        commentViewModel.startDissolve(rpid)
                    }
                }
            } else {
                null
            }
        )
    }
    val sortPreferenceScope = rememberCoroutineScope()
    val danmakuEnabledForDetail by com.android.purebilibili.core.store.SettingsManager
        .getDanmakuEnabled(
            context,
            com.android.purebilibili.core.store.DanmakuSettingsScope.PORTRAIT
        )
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )
    val showFavoriteFolderDialog by viewModel.favoriteFolderDialogVisible.collectAsStateWithLifecycle()
    val showCommentInput by viewModel.showCommentDialog.collectAsStateWithLifecycle()
    val favoriteFolders by viewModel.favoriteFolders.collectAsStateWithLifecycle()
    val isFavoriteFoldersLoading by viewModel.isFavoriteFoldersLoading.collectAsStateWithLifecycle()
    val selectedFavoriteFolderIds by viewModel.favoriteSelectedFolderIds.collectAsStateWithLifecycle()
    val isSavingFavoriteFolders by viewModel.isSavingFavoriteFolders.collectAsStateWithLifecycle()
    val qualitySwitchFailureDialog by viewModel.qualitySwitchFailureDialog.collectAsStateWithLifecycle()
    val playerDiagnosticLoggingEnabled by com.android.purebilibili.core.store.SettingsManager
        .getPlayerDiagnosticLoggingEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )
    val qualitySwitchFailureDialogEnabled by com.android.purebilibili.core.store.SettingsManager
        .getQualitySwitchFailureDialogEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )
    val qualitySwitchFailureDialogOnceEnabled by com.android.purebilibili.core.store.SettingsManager
        .getQualitySwitchFailureDialogOnceEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = false,
            lifecycle = lifecycleOwner.lifecycle
        )
    val qualitySwitchFailureDialogShown by com.android.purebilibili.core.store.SettingsManager
        .getQualitySwitchFailureDialogShown(context)
        .collectAsStateWithLifecycle(
            initialValue = false,
            lifecycle = lifecycleOwner.lifecycle
        )
    val qualitySwitchDialogScope = rememberCoroutineScope()

    // [Blur] Haze State
    val hazeState = rememberRecoverableHazeState()

    val sponsorSegment by viewModel.currentSponsorSegment.collectAsStateWithLifecycle()
    val showSponsorSkipButton by viewModel.showSkipButton.collectAsStateWithLifecycle()

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val interactiveChoicePanel by viewModel.interactiveChoicePanel.collectAsStateWithLifecycle()

    // 📐 [大屏适配] 仅 Expanded 才启用平板分栏布局
    val windowSizeClass = com.android.purebilibili.core.util.LocalWindowSizeClass.current
    val horizontalAdaptationEnabled by com.android.purebilibili.core.store.SettingsManager
        .getHorizontalAdaptationEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = windowSizeClass.isTabletDevice,
            lifecycle = lifecycleOwner.lifecycle
        )
    val hideVideoPageStatusBar by com.android.purebilibili.core.store.SettingsManager
        .getHideVideoPageStatusBar(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.SettingsManager
                .getHideVideoPageStatusBarSync(context),
            lifecycle = lifecycleOwner.lifecycle
        )
    val useTabletLayout = shouldUseTabletVideoLayout(
        isExpandedScreen = windowSizeClass.isExpandedScreen,
        isTabletDevice = windowSizeClass.isTabletDevice
    ) && horizontalAdaptationEnabled

    // 🔧 [修复] 追踪用户是否主动请求全屏（点击全屏按钮）
    // 使用 rememberSaveable 确保状态在横竖屏切换时保持
    var userRequestedFullscreen by rememberSaveable { mutableStateOf(false) }
    var manualPortraitHoldActive by rememberSaveable { mutableStateOf(false) }
    val activity = remember { context.findActivity() }
    val isActivityInMultiWindowMode = activity?.let { host ->
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && host.isInMultiWindowMode
    } ?: false

    // 📐 全屏模式逻辑：
    // - 手机：横屏时自动进入全屏
    // - 平板：仅用户主动切换全屏
    val fullscreenMode by com.android.purebilibili.core.store.SettingsManager
        .getFullscreenMode(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.FullscreenMode.AUTO,
            lifecycle = lifecycleOwner.lifecycle
        )
    val prefersManualFullscreenMode = remember(fullscreenMode) {
        fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.NONE ||
            fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.VERTICAL
    }
    val isOrientationDrivenFullscreen = !prefersManualFullscreenMode &&
        shouldUseOrientationDrivenFullscreen(
        isCompactDevice = windowSizeClass.isCompactDevice
    )
    val isFullscreenMode = resolveVideoDetailFullscreenMode(
        isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
        isLandscape = isLandscape,
        userRequestedFullscreen = userRequestedFullscreen,
        isInMultiWindowMode = isActivityInMultiWindowMode
    )
    ManualFullscreenRequestLifecycleEffect(
        manualFullscreenRequested = userRequestedFullscreen,
        isFullscreenMode = isFullscreenMode,
        onReleaseManualFullscreenRequest = { userRequestedFullscreen = false }
    )
    val activeDanmakuScope = remember(isFullscreenMode) {
        com.android.purebilibili.core.store.resolveDanmakuSettingsScope(isLandscape = isFullscreenMode)
    }
    val activeDanmakuBlockRulesRaw by com.android.purebilibili.core.store.SettingsManager
        .getDanmakuBlockRulesRaw(context, activeDanmakuScope)
        .collectAsStateWithLifecycle(
            initialValue = "",
            lifecycle = lifecycleOwner.lifecycle
        )

    var isPipMode by remember { mutableStateOf(isInPipMode) }
    var previousPipMode by remember { mutableStateOf(isInPipMode) }
    LaunchedEffect(isInPipMode) { isPipMode = isInPipMode }
    LaunchedEffect(isPipMode, subReplyState.visible) {
        val shouldDismissThreadDetail = shouldDismissCommentThreadDetailForPip(
            wasInPipMode = previousPipMode,
            isInPipMode = isPipMode,
            subReplyVisible = subReplyState.visible
        )
        previousPipMode = isPipMode
        if (shouldDismissThreadDetail) {
            commentViewModel.closeSubReply()
        }
    }
    val openFavoriteFolders: (VideoFavoriteEntryPoint) -> Unit = { entryPoint ->
        when (resolveVideoFavoriteAction(entryPoint)) {
            VideoFavoriteAction.ToggleFavorite -> viewModel.toggleFavorite()
        }
    }

    //  [新增] 监听定时关闭状态
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()

    // 📖 [新增] 监听视频章节数据
    // 📖 [新增] 监听视频章节数据
    val viewPoints by viewModel.viewPoints.collectAsStateWithLifecycle()
    val pbpProgressData by viewModel.pbpProgressData.collectAsStateWithLifecycle()
    val sponsorProgressMarkers by viewModel.sponsorProgressMarkers.collectAsStateWithLifecycle()

    // [New] Codec & Audio Preferences
    val codecPreference by viewModel.videoCodecPreference.collectAsStateWithLifecycle()
    val secondCodecPreference by viewModel.videoSecondCodecPreference.collectAsStateWithLifecycle()
    val audioQualityPreference by viewModel.audioQualityPreference.collectAsStateWithLifecycle()

    //  [PiP修复] 记录视频播放器在屏幕上的位置，用于PiP窗口只显示视频区域
    var videoPlayerBounds by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var videoPlayerRootBottomPx by remember { mutableIntStateOf(0) }

    // 📱 [优化] isPortraitFullscreen 和 isVerticalVideo 现在从 playerState 获取（见 playerState 定义后）

    // 🔁 [优化] 合并播放队列状态订阅，减少同帧多次重组
    val playlistUiState by PlaylistManager.uiState.collectAsStateWithLifecycle(
        initialValue = PlaylistUiState(),
        lifecycle = lifecycleOwner.lifecycle
    )
    val currentPlayMode = playlistUiState.playMode
    val playlistItems = playlistUiState.playlist
    val playlistCurrentIndex = playlistUiState.currentIndex
    val isExternalPlaylist = playlistUiState.isExternalPlaylist
    val externalPlaylistSource = playlistUiState.externalPlaylistSource
    val shouldShowExternalPlaylistQueueBar = shouldShowExternalPlaylistQueueBarByPolicy(
        isExternalPlaylist = isExternalPlaylist,
        externalPlaylistSource = externalPlaylistSource,
        playlistSize = playlistItems.size
    )
    val externalPlaylistQueueTitle = resolveExternalPlaylistQueueTitle(externalPlaylistSource)
    var showExternalPlaylistQueueSheet by rememberSaveable { mutableStateOf(false) }
    var pendingVideoShare by remember { mutableStateOf<VideoSharePayload?>(null) }
    val externalPlaylistQueueSheetPresentation = remember {
        resolveExternalPlaylistQueueSheetPresentation(requireRealtimeHaze = true)
    }

    LaunchedEffect(shouldShowExternalPlaylistQueueBar) {
        if (!shouldShowExternalPlaylistQueueBar) {
            showExternalPlaylistQueueSheet = false
        }
    }

    LaunchedEffect(startAudioFromRoute, hasAutoEnteredAudioMode, uiState) {
        if (shouldAutoEnterAudioModeFromRoute(
                startAudioFromRoute = startAudioFromRoute,
                hasAutoEnteredAudioMode = hasAutoEnteredAudioMode,
                isVideoLoadSuccess = uiState is PlayerUiState.Success
            )
        ) {
            hasAutoEnteredAudioMode = true
            isNavigatingToAudioMode = true
            viewModel.setAudioMode(true)
            onNavigateToAudioMode()
        }
    }

    //  从小窗展开时自动进入全屏
    LaunchedEffect(startInFullscreen, isOrientationDrivenFullscreen, isLandscape) {
        if (startInFullscreen) {
            if (!isOrientationDrivenFullscreen) {
                userRequestedFullscreen = true
            } else {
                context.findActivity()?.let { activity ->
                    val isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                        activity.isInMultiWindowMode
                    if (!shouldApplyStartFullscreenOrientationRequest(
                            startInFullscreen = startInFullscreen,
                            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                            isLandscape = isLandscape,
                            isInMultiWindowMode = isInMultiWindowMode
                        )
                    ) {
                        if (isInMultiWindowMode) {
                            userRequestedFullscreen = true
                        }
                        return@let
                    }
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            }
        }
    }

    //  用于跟踪组件是否正在退出，防止 SideEffect 覆盖恢复操作
    var isScreenActive by rememberSaveable(currentBvid) { mutableStateOf(true) }

    //  [关键] 保存进入前的状态栏配置（在 DisposableEffect 外部定义以便复用）
    val window = remember { activity?.window }
    var entryRequestedOrientation by rememberSaveable {
        mutableIntStateOf(
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        )
    }
    val insetsController = remember {
        if (window != null && activity != null) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else null
    }
    val originalSystemBarsSnapshot = remember(window, insetsController) {
        resolveVideoDetailSystemBarsSnapshot(
            statusBarColor = window?.statusBarColor,
            navigationBarColor = window?.navigationBarColor,
            lightStatusBars = insetsController?.isAppearanceLightStatusBars,
            lightNavigationBars = insetsController?.isAppearanceLightNavigationBars,
            systemBarsBehavior = insetsController?.systemBarsBehavior,
            fallbackColor = android.graphics.Color.TRANSPARENT,
            fallbackLightBars = true,
            fallbackSystemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        )
    }

    //  [新增] 恢复状态栏的函数（可复用）
    val restoreStatusBar = remember {
        {
            if (window != null && insetsController != null) {
                if (shouldShowSystemBarsOnVideoDetailExit()) {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
                insetsController.systemBarsBehavior = originalSystemBarsSnapshot.systemBarsBehavior
                insetsController.isAppearanceLightStatusBars = originalSystemBarsSnapshot.lightStatusBars
                insetsController.isAppearanceLightNavigationBars = originalSystemBarsSnapshot.lightNavigationBars
                window.statusBarColor = originalSystemBarsSnapshot.statusBarColor
                window.navigationBarColor = originalSystemBarsSnapshot.navigationBarColor
            }
        }
    }

    //  [修复] 包装的 onBack，在导航之前立即恢复状态栏并通知小窗管理器
    val latestOnBack by rememberUpdatedState(onBack)
    val latestOnHomeClick by rememberUpdatedState(onHomeClick)
    val latestOnMarkReturningFromDetail by rememberUpdatedState(onMarkReturningFromDetail)
    val topBarActionHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var pendingTopBarActionRunnable by remember { mutableStateOf<Runnable?>(null) }
    var isActuallyLeaving by rememberSaveable(currentBvid) { mutableStateOf(false) }
    var forceCoverOnlyOnReturn by remember { mutableStateOf(false) }
    val rootAnimatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val isExitTransitionInProgress =
        rootAnimatedVisibilityScope?.transition?.targetState == EnterExitState.PostExit
    val rootSharedTransitionScope = LocalSharedTransitionScope.current
    val sharedBoundsActive = shouldEnableVideoCoverSharedTransition(
        transitionEnabled = transitionEnabled,
        hasSharedTransitionScope = rootSharedTransitionScope != null,
        hasAnimatedVisibilityScope = rootAnimatedVisibilityScope != null
    ) && !sourceRouteForSharedElement.isNullOrBlank()
    // Shell sharedBounds 不再包裹整页，仅用于 route sheet 禁用标记
    val detailShellSharedBoundsEnabled = false
    val routeSheetFrame = rememberVideoDetailRouteSheetFrame(
        motion = routeSheetMotion,
        isExitTransitionInProgress = isExitTransitionInProgress,
        sharedBoundsActive = sharedBoundsActive
    )
    // Shell sharedBounds 已移除，cover 独立 sharedBounds 处理播放器 → 封面映射
    val detailShellModifier = Modifier
    val coverTakeoverBeforeBackDelayMillis = remember {
        resolveCoverTakeoverDelayBeforeBackNavigationMillis()
    }
    val forceCoverOnlyForReturn = resolveForceCoverOnlyForReturn(
        forceCoverOnlyOnReturn = forceCoverOnlyOnReturn,
        isReturningFromDetail = isReturningFromDetail,
        isExitTransitionInProgress = isExitTransitionInProgress,
        transitionEnabled = transitionEnabled,
        detailShellSharedBoundsEnabled = detailShellSharedBoundsEnabled
    )
    val useReturningVideoDetailVisualState = shouldUseReturningVideoDetailVisualState(
        forceCoverOnlyForReturn = forceCoverOnlyForReturn,
        isReturningFromDetail = isReturningFromDetail,
        isExitTransitionInProgress = isExitTransitionInProgress
    )

    val handleTopBarAction = remember(
        miniPlayerManager,
        currentBvid,
        coverTakeoverBeforeBackDelayMillis,
        topBarActionHandler
    ) {
        action@{ action: VideoDetailTopBarAction ->
            if (isActuallyLeaving) return@action
            isActuallyLeaving = true // 标记确实是用户通过点击或返回键离开
            isScreenActive = false  // 标记页面正在退出
            forceCoverOnlyOnReturn = true
            // 进入返回流程时立即标记，确保封面优先接管。
            latestOnMarkReturningFromDetail()
            // 🎯 通知小窗管理器这是用户主动导航离开（用于控制后台音频）
            miniPlayerManager?.markLeavingByNavigation(expectedBvid = currentBvid)

            restoreStatusBar() // 立即恢复状态栏（动画开始前）
            pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
            val navigationRunnable = Runnable {
                pendingTopBarActionRunnable = null
                when (action) {
                    VideoDetailTopBarAction.BACK -> latestOnBack()
                    VideoDetailTopBarAction.HOME -> latestOnHomeClick()
                }
            }
            pendingTopBarActionRunnable = navigationRunnable
            if (coverTakeoverBeforeBackDelayMillis > 0L) {
                topBarActionHandler.postDelayed(navigationRunnable, coverTakeoverBeforeBackDelayMillis)
            } else {
                navigationRunnable.run()
            }
        }
    }
    val handleBack = remember(handleTopBarAction) {
        {
            handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = false))
        }
    }

    LaunchedEffect(isExitTransitionInProgress, isActuallyLeaving) {
        if (!shouldRestoreSystemBarsDuringVideoDetailExitTransition(
                isExitTransitionInProgress = isExitTransitionInProgress,
                isActuallyLeaving = isActuallyLeaving
            )
        ) {
            return@LaunchedEffect
        }

        isScreenActive = false
        restoreStatusBar()
    }

    LaunchedEffect(currentBvid) {
        pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
        pendingTopBarActionRunnable = null
        isScreenActive = true
        forceCoverOnlyOnReturn = false
        if (shouldClearStaleReturningStateOnVideoDetailEnter(isReturningFromDetail)) {
            onClearReturningFromDetail()
        }
    }

    DisposableEffect(lifecycleOwner, currentBvid) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
                pendingTopBarActionRunnable = null
                isScreenActive = true
                forceCoverOnlyOnReturn = false
                if (shouldClearStaleReturningStateOnVideoDetailEnter(isReturningFromDetail)) {
                    onClearReturningFromDetail()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 🔄 [新增] 自动横竖屏切换 - 跟随手机传感器方向
    val autoRotateEnabled by com.android.purebilibili.core.store.SettingsManager
        .getAutoRotateEnabled(context).collectAsStateWithLifecycle(
            initialValue = false,
            lifecycle = lifecycleOwner.lifecycle
        )
    val systemAutoRotateEnabled by rememberSystemAutoRotateEnabled(context)
    val cardAnimationEnabled by com.android.purebilibili.core.store.SettingsManager
        .getCardAnimationEnabled(context).collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )

    DisposableEffect(activity, isScreenActive) {
        if (!isScreenActive || activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onDispose { }
        } else {
            val hostWindow = activity.window
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display
            } else {
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay
            }

            if (hostWindow == null || display == null) {
                onDispose { }
            } else {
                val originalModeId = hostWindow.attributes.preferredDisplayModeId
                val currentModeId = display.mode.modeId
                val preferredModeId = resolvePreferredHighRefreshModeId(
                    currentModeId = currentModeId,
                    supportedModes = display.supportedModes.map { mode ->
                        RefreshModeCandidate(
                            modeId = mode.modeId,
                            refreshRate = mode.refreshRate,
                            width = mode.physicalWidth,
                            height = mode.physicalHeight
                        )
                    }
                )
                if (preferredModeId != null && preferredModeId != originalModeId) {
                    hostWindow.attributes = hostWindow.attributes.apply {
                        preferredDisplayModeId = preferredModeId
                    }
                }

                onDispose {
                    if (hostWindow.attributes.preferredDisplayModeId != originalModeId) {
                        hostWindow.attributes = hostWindow.attributes.apply {
                            preferredDisplayModeId = originalModeId
                        }
                    }
                }
            }
        }
    }

    // 退出重置亮度 +  屏幕常亮管理 + 状态栏恢复（作为安全网）

    DisposableEffect(Unit) {
        //  [沉浸式] 启用边到边显示，让内容延伸到状态栏下方
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        onDispose {
            pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
            pendingTopBarActionRunnable = null
            //  [关键] 标记页面正在退出，防止 SideEffect 覆盖
            isScreenActive = false

            // ⚡ [性能优化] Phase 1: 同步执行 — 仅保留影响视觉的关键操作
            val layoutParams = window?.attributes
            layoutParams?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window?.attributes = layoutParams
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            restoreStatusBar()

            // ⚡ [性能优化] Phase 1b: CardPositionManager 状态（影响首页卡片动画，必须同步）
            val shouldHandleAsNavigationExit = shouldHandleVideoDetailDisposeAsNavigationExit(
                isNavigatingToAudioMode = isNavigatingToAudioMode,
                isNavigatingToMiniMode = isNavigatingToMiniMode,
                isMiniModeActive = miniPlayerManager?.isMiniMode == true,
                isChangingConfigurations = activity?.isChangingConfigurations == true,
                isNavigatingToVideo = resolveIsNavigatingToVideoDuringDispose(
                    localNavigatingToVideo = isNavigatingToVideo,
                    managerNavigatingToVideo = miniPlayerManager?.isNavigatingToVideo == true
                )
            )
            if (shouldMarkReturningStateOnVideoDetailDispose(shouldHandleAsNavigationExit)) {
                onMarkReturningFromDetail()
            } else {
                onClearReturningFromDetail()
            }

            // ⚡ [性能优化] Phase 2: 延迟执行 — 非视觉的系统调用推迟到下一帧
            // PiP 重置、通知清理、Service 停止、屏幕方向恢复等操作不影响退出动画
            // 将它们 post 到主线程 Handler，在导航转场动画完成后再执行
            val deferredActivity = activity
            val deferredContext = context
            val deferredShouldHandleAsNavExit = shouldHandleAsNavigationExit
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                // 🔧 重置 PiP 参数
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    deferredActivity?.let { act ->
                        try {
                            val pipParams = android.app.PictureInPictureParams.Builder()
                                .setAutoEnterEnabled(false)
                                .build()
                            act.setPictureInPictureParams(pipParams)
                        } catch (_: Exception) {}
                    }
                }

                // 🔕 通知清理 + Service 停止
                if (deferredShouldHandleAsNavExit) {
                    val notificationManager = deferredContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE)
                        as android.app.NotificationManager
                    notificationManager.cancel(1001)
                    notificationManager.cancel(PlaybackService.NOTIFICATION_ID)
                    try {
                        deferredContext.startService(
                            android.content.Intent(deferredContext, PlaybackService::class.java).apply {
                                action = PlaybackService.ACTION_STOP_FOREGROUND
                            }
                        )
                    } catch (_: Exception) {}
                }

                // 恢复进入详情页前的方向请求，避免平板误横屏后退不回去。
                deferredActivity?.requestedOrientation = resolveVideoDetailExitRequestedOrientation(
                    originalRequestedOrientation = entryRequestedOrientation
                )
            }
        }
    }

    //  新增：监听消息事件（关注/收藏反馈）- 使用居中弹窗
    var popupMessage by remember { mutableStateOf<PlayerToastMessage?>(null) }
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            popupMessage = message
            // 2秒后自动隐藏
            kotlinx.coroutines.delay(2000)
            popupMessage = null
        }
    }

    //  [新增] 监听弹幕发送事件 - 将发送的弹幕显示在屏幕上
    val danmakuManager = rememberDanmakuManager()
    LaunchedEffect(Unit) {
        viewModel.danmakuSentEvent.collect { danmakuData ->
            android.util.Log.d("VideoDetailScreen", "📺 Displaying sent danmaku: ${danmakuData.text}")
            danmakuManager.addLocalDanmaku(
                text = danmakuData.text,
                color = danmakuData.color,
                mode = danmakuData.mode,
                fontSize = danmakuData.fontSize
            )
        }
    }

    //  初始化进度持久化存储
    LaunchedEffect(Unit) {
        viewModel.initWithContext(context)
        //  [埋点] 页面浏览追踪
        com.android.purebilibili.core.util.AnalyticsHelper.logScreenView("VideoDetailScreen")
    }

    //  [PiP修复] 当视频播放器位置更新时，同步更新PiP参数
    //  [修复] 只有支持系统 PiP 的模式才启用自动进入 PiP
    val pipModeEnabled = remember {
        com.android.purebilibili.core.store.SettingsManager.getMiniPlayerModeSync(context)
            .supportsSystemPip
    }
    val feedbackBottomInsetDp = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
        .value
        .roundToInt() + if (isFullscreenMode) 24 else 20
    val feedbackPlacement = resolveVideoFeedbackPlacement(
        isFullscreen = isFullscreenMode,
        isLandscape = isLandscape,
        bottomInsetDp = feedbackBottomInsetDp
    )
    val feedbackAnchorAlignment = when (feedbackPlacement.anchor) {
        VideoFeedbackAnchor.BottomCenter -> Alignment.BottomCenter
        VideoFeedbackAnchor.BottomTrailing -> Alignment.BottomEnd
        VideoFeedbackAnchor.CenterOverlay -> Alignment.Center
    }
    val isReducedActionMotion = !cardAnimationEnabled

    // 🔧 [性能优化] 记录上次设置的 PiP bounds，避免重复设置
    var lastPipBounds by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var lastPipModeEnabled by remember { mutableStateOf<Boolean?>(null) }
    var lastPipUpdateElapsedMs by remember { mutableStateOf(0L) }
    val hasMeaningfulBoundsChange = remember {
        { oldBounds: android.graphics.Rect?, newBounds: android.graphics.Rect? ->
            when {
                oldBounds == null && newBounds == null -> false
                oldBounds == null || newBounds == null -> true
                else -> {
                    abs(oldBounds.left - newBounds.left) > 3 ||
                        abs(oldBounds.top - newBounds.top) > 3 ||
                        abs(oldBounds.right - newBounds.right) > 3 ||
                        abs(oldBounds.bottom - newBounds.bottom) > 3
                }
            }
        }
    }

    LaunchedEffect(videoPlayerBounds, pipModeEnabled) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return@LaunchedEffect

        val modeChanged = lastPipModeEnabled == null || lastPipModeEnabled != pipModeEnabled
        val boundsChanged = hasMeaningfulBoundsChange(lastPipBounds, videoPlayerBounds)
        val now = android.os.SystemClock.elapsedRealtime()
        val elapsedSinceLastUpdate = now - lastPipUpdateElapsedMs
        val shouldUpdate = shouldApplyPipParamsUpdate(
            pipModeEnabled = pipModeEnabled,
            modeChanged = modeChanged,
            boundsChanged = boundsChanged,
            elapsedSinceLastUpdateMs = elapsedSinceLastUpdate
        )
        if (!shouldUpdate) return@LaunchedEffect

        lastPipBounds = videoPlayerBounds?.let { android.graphics.Rect(it) }
        lastPipModeEnabled = pipModeEnabled
        lastPipUpdateElapsedMs = now

        activity?.let { act ->
            val pipParamsBuilder = android.app.PictureInPictureParams.Builder()
                .setAspectRatio(android.util.Rational(16, 9))
                .setActions(
                    buildPipPlaybackRemoteActions(
                        context = context,
                        player = miniPlayerManager?.player
                    )
                )

            //  设置源矩形区域 - PiP只显示视频播放器区域
            videoPlayerBounds?.let { bounds ->
                pipParamsBuilder.setSourceRectHint(bounds)
            }

            // Android 12+ 支持手势自动进入 PiP -  只有 SYSTEM_PIP 模式才启用
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pipParamsBuilder.setAutoEnterEnabled(pipModeEnabled)  //  受设置控制
                pipParamsBuilder.setSeamlessResizeEnabled(pipModeEnabled)
            }

            act.setPictureInPictureParams(pipParamsBuilder.build())
            com.android.purebilibili.core.util.Logger.d(
                "VideoDetailScreen",
                " PiP参数更新: autoEnterEnabled=$pipModeEnabled, modeChanged=$modeChanged, boundsChanged=$boundsChanged"
            )
        }
    }

    // 📱 [修复] 提升竖屏全屏状态到 Screen 级别，防止 VideoPlayerState 重建时状态丢失
    var isPortraitFullscreen by rememberSaveable {
        mutableStateOf(
            shouldStartInPortraitFullscreenFromRouteHint(
                autoEnterPortraitFromRoute = autoEnterPortraitFromRoute,
                startAudioFromRoute = startAudioFromRoute,
                initialVerticalFromRoute = initialVerticalFromRoute
            )
        )
    }
    val useSharedPortraitPlayer = shouldUseSharedPlayerForPortraitFullscreen()
    val portraitPagerMotionSpec = remember {
        resolveStandalonePortraitPagerMotionSpec()
    }
    val shouldAnimatePortraitPager = remember(useSharedPortraitPlayer) {
        shouldAnimateStandalonePortraitPager(useSharedPlayer = useSharedPortraitPlayer)
    }
    val inlinePlayerAlpha by animateFloatAsState(
        targetValue = if (isPortraitFullscreen) 0f else 1f,
        animationSpec = tween(
            durationMillis = if (shouldAnimatePortraitPager) {
                portraitPagerMotionSpec.inlineReturnDurationMillis
            } else {
                0
            },
            easing = FastOutSlowInEasing
        ),
        label = "inline-player-alpha"
    )
    val inlinePlayerScale by animateFloatAsState(
        targetValue = if (isPortraitFullscreen) {
            portraitPagerMotionSpec.inlineReturnInitialScale
        } else {
            1f
        },
        animationSpec = tween(
            durationMillis = if (shouldAnimatePortraitPager) {
                portraitPagerMotionSpec.inlineReturnDurationMillis
            } else {
                0
            },
            easing = FastOutSlowInEasing
        ),
        label = "inline-player-return-scale"
    )
    var portraitSyncSnapshotBvid by rememberSaveable { mutableStateOf<String?>(null) }
    var portraitSyncSnapshotCid by remember { mutableLongStateOf(0L) }
    var portraitSyncSnapshotPositionMs by remember { mutableLongStateOf(0L) }
    var hasPendingPortraitSync by remember { mutableStateOf(false) }
    var hasDeferredPortraitRestoreAfterExternalNavigation by rememberSaveable { mutableStateOf(false) }
    var pendingMainReloadBvidAfterPortrait by rememberSaveable { mutableStateOf<String?>(null) }
    var portraitPendingSelectionBvid by rememberSaveable { mutableStateOf<String?>(null) }
    // 初始化播放器状态
    val playerState = rememberVideoPlayerState(
        context = context,
        viewModel = viewModel,
        bvid = currentBvid,
        cid = cid,
        fallbackResumePositionMs = resumePositionMsFromRoute,
        startPaused = isPortraitFullscreen && !useSharedPortraitPlayer
    )
    val shouldKeepVideoScreenAwake by produceState(
        initialValue = shouldKeepVideoPlaybackAwake(
            playWhenReady = playerState.player.playWhenReady,
            isPlaying = playerState.player.isPlaying,
            playbackState = playerState.player.playbackState
        ),
        key1 = playerState.player
    ) {
        val player = playerState.player
        fun updateAwakeState() {
            value = shouldKeepVideoPlaybackAwake(
                playWhenReady = player.playWhenReady,
                isPlaying = player.isPlaying,
                playbackState = player.playbackState
            )
        }
        updateAwakeState()
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateAwakeState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateAwakeState()
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                updateAwakeState()
            }
        }
        player.addListener(listener)
        awaitDispose {
            player.removeListener(listener)
        }
    }
    DisposableEffect(window, shouldKeepVideoScreenAwake) {
        if (shouldKeepVideoScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (shouldKeepVideoScreenAwake) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
    val isVideoPlaying by produceState(
        initialValue = playerState.player.isPlaying,
        key1 = playerState.player
    ) {
        val player = playerState.player
        value = player.isPlaying
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                value = player.isPlaying
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                value = player.isPlaying
            }
        }
        player.addListener(listener)
        awaitDispose {
            player.removeListener(listener)
        }
    }
    val routedCommentInteractionActive =
        openCommentRootRpidFromRoute > 0L &&
            (subReplyState.visible || subReplyState.isLoading)
    val commentInteractionActive =
        routedCommentInteractionActive || subReplyState.visible || showCommentInput
    LaunchedEffect(commentInteractionActive) {
        viewModel.setCommentInteractionActive(commentInteractionActive)
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.setCommentInteractionActive(false)
        }
    }
    var hasAutoPausedForRoutedComment by rememberSaveable(
        bvid,
        openCommentRootRpidFromRoute,
        openCommentTargetRpidFromRoute
    ) {
        mutableStateOf(false)
    }
    LaunchedEffect(
        openCommentRootRpidFromRoute,
        videoSharedPlaybackIntent,
        isVideoPlaying,
        hasAutoPausedForRoutedComment
    ) {
        val shouldAutoPause =
            openCommentRootRpidFromRoute > 0L &&
                videoSharedPlaybackIntent == VideoSharedTransitionPlaybackIntent.ImmediatePlayback &&
                isVideoPlaying &&
                !hasAutoPausedForRoutedComment
        if (shouldAutoPause) {
            com.android.purebilibili.feature.video.usecase.pausePlayerFromUserAction(playerState.player)
            hasAutoPausedForRoutedComment = true
        }
    }
    val isPlaybackPaused by produceState(
        initialValue = resolveIsPlaybackPausedForCollapse(
            playWhenReady = playerState.player.playWhenReady,
            playbackState = playerState.player.playbackState
        ),
        key1 = playerState.player,
        key2 = currentBvid,
        key3 = currentBvidCid
    ) {
        val player = playerState.player

        fun updatePausedState() {
            value = resolveIsPlaybackPausedForCollapse(
                playWhenReady = player.playWhenReady,
                playbackState = player.playbackState
            )
        }

        updatePausedState()
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePausedState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePausedState()
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                updatePausedState()
            }
        }
        player.addListener(listener)
        awaitDispose {
            player.removeListener(listener)
        }
    }
    val subtitleAutoPreference by com.android.purebilibili.core.store.SettingsManager
        .getSubtitleAutoPreference(context)
        .collectAsStateWithLifecycle(
            initialValue = SubtitleAutoPreference.OFF,
            lifecycle = lifecycleOwner.lifecycle
        )
    val subtitleAudioManager = remember {
        context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    }
    val subtitleAutoModeMuted = remember(playerState.player, subtitleAudioManager, currentBvid) {
        val systemMuted = runCatching {
            subtitleAudioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) <= 0
        }.getOrDefault(false)
        systemMuted || playerState.player.volume <= 0f
    }
    val subtitlePreferenceSession = remember(uiState, currentBvid, subtitleAutoPreference, subtitleAutoModeMuted) {
        val success = uiState as? PlayerUiState.Success
        if (success == null) {
            resolveSubtitlePreferenceSession(
                bvid = currentBvid,
                cid = 0L,
                primaryLanguage = null,
                secondaryLanguage = null,
                primaryTrackLikelyAi = false,
                secondaryTrackLikelyAi = false,
                hasPrimaryTrack = false,
                hasSecondaryTrack = false,
                preference = subtitleAutoPreference,
                isMuted = subtitleAutoModeMuted
            )
        } else {
            val subtitleBelongsToCurrentVideo =
                success.subtitleOwnerBvid == success.info.bvid &&
                    success.subtitleOwnerCid == success.info.cid &&
                    success.info.cid > 0L
            val hasPrimaryTrack = subtitleBelongsToCurrentVideo &&
                (!success.subtitlePrimaryTrackKey.isNullOrBlank() || !success.subtitlePrimaryLanguage.isNullOrBlank())
            val hasSecondaryTrack = subtitleBelongsToCurrentVideo &&
                (!success.subtitleSecondaryTrackKey.isNullOrBlank() || !success.subtitleSecondaryLanguage.isNullOrBlank())
            resolveSubtitlePreferenceSession(
                bvid = success.info.bvid,
                cid = success.info.cid,
                primaryLanguage = success.subtitlePrimaryLanguage,
                secondaryLanguage = success.subtitleSecondaryLanguage,
                primaryTrackLikelyAi = subtitleBelongsToCurrentVideo && success.subtitlePrimaryLikelyAi,
                secondaryTrackLikelyAi = subtitleBelongsToCurrentVideo && success.subtitleSecondaryLikelyAi,
                hasPrimaryTrack = hasPrimaryTrack,
                hasSecondaryTrack = hasSecondaryTrack,
                preference = subtitleAutoPreference,
                isMuted = subtitleAutoModeMuted
            )
        }
    }
    var subtitlePreferenceSessionKey by rememberSaveable { mutableStateOf<String?>(null) }
    var subtitleDisplayModeOverride by rememberSaveable { mutableStateOf(SubtitleDisplayMode.OFF) }
    LaunchedEffect(subtitlePreferenceSession.key) {
        subtitleDisplayModeOverride = resolveSubtitleDisplayModePreference(
            previousSessionKey = subtitlePreferenceSessionKey,
            nextSessionKey = subtitlePreferenceSession.key,
            previousMode = subtitleDisplayModeOverride,
            nextInitialMode = subtitlePreferenceSession.initialMode
        )
        subtitlePreferenceSessionKey = subtitlePreferenceSession.key
    }

    var hasAppliedInitialPageSwitch by remember(currentBvid, cid) { mutableStateOf(false) }
    LaunchedEffect(uiState, currentBvid, cid, hasAppliedInitialPageSwitch) {
        if (hasAppliedInitialPageSwitch) return@LaunchedEffect
        val success = uiState as? PlayerUiState.Success ?: return@LaunchedEffect
        if (success.info.bvid != currentBvid) return@LaunchedEffect

        val targetPageIndex = resolveInitialPageIndex(
            requestedCid = cid,
            currentCid = success.info.cid,
            pages = success.info.pages
        )
        hasAppliedInitialPageSwitch = true
        if (targetPageIndex != null) {
            viewModel.switchPage(targetPageIndex)
        }
    }

    // 🎯 [修复] 确保在 VideoPlayerState 销毁之前通知 MiniPlayerManager 页面退出
    // 必须在 playerState 之后声明此 Effect，这样它会在 playerState.onDispose 之前执行（LIFO 顺序）
    DisposableEffect(playerState) {
        onDispose {
            // 标记页面正在退出
            // 配置切换不标记离开；音频模式/小窗模式为主动保活场景，也不标记离开。
            val isChangingConfigurations = activity?.isChangingConfigurations == true
            val shouldHandleAsNavigationExit = shouldHandleVideoDetailDisposeAsNavigationExit(
                isNavigatingToAudioMode = isNavigatingToAudioMode,
                isNavigatingToMiniMode = isNavigatingToMiniMode,
                isMiniModeActive = miniPlayerManager?.isMiniMode == true,
                isChangingConfigurations = isChangingConfigurations,
                isNavigatingToVideo = resolveIsNavigatingToVideoDuringDispose(
                    localNavigatingToVideo = isNavigatingToVideo,
                    managerNavigatingToVideo = miniPlayerManager?.isNavigatingToVideo == true
                )
            )
            if (shouldHandleAsNavigationExit) {
                com.android.purebilibili.core.util.Logger.d(
                    "VideoDetailScreen",
                    "🛑 Disposing screen as navigation exit, notifying MiniPlayerManager"
                )
                miniPlayerManager?.markLeavingByNavigation(expectedBvid = currentBvid)
            } else {
                com.android.purebilibili.core.util.Logger.d(
                    "VideoDetailScreen",
                    "💤 Screen disposed without navigation-exit mark (audioMode=$isNavigatingToAudioMode, miniMode=$isNavigatingToMiniMode, changingConfig=$isChangingConfigurations)"
                )
            }
        }
    }

    //  [性能优化] 生命周期感知：进入后台时暂停播放，返回前台时继续
    //  [修复] 此处逻辑已移至 VideoPlayerState.kt 统一处理
    // 删除冗余的暂停逻辑，避免与 VideoPlayerState 中的生命周期处理冲突
    // VideoPlayerState 会检查 PiP/小窗模式来决定是否暂停

    // 📱 [优化] 竖屏视频检测已移至 VideoPlayerState 集中管理
    val isVerticalVideo by playerState.isVerticalVideo.collectAsStateWithLifecycle()
    val activeVideoSharedTransitionVisualSpec = remember(
        sourceRouteForSharedElement,
        sharedTransitionSourceCornerDp,
        videoSharedPlaybackIntent,
        startInFullscreen,
        autoEnterPortraitFromRoute,
        initialVerticalFromRoute,
        isVerticalVideo,
        useReturningVideoDetailVisualState
    ) {
        resolveVideoSharedTransitionVisualSpec(
            sourceRoute = sourceRouteForSharedElement,
            sourceCornerDp = sharedTransitionSourceCornerDp,
            playbackIntent = videoSharedPlaybackIntent,
            fullscreen = startInFullscreen,
            autoPortrait = autoEnterPortraitFromRoute,
            initialVertical = initialVerticalFromRoute,
            isVerticalVideo = isVerticalVideo,
            isReturning = useReturningVideoDetailVisualState
        )
    }
    LaunchedEffect(
        autoRotateEnabled,
        systemAutoRotateEnabled,
        fullscreenMode,
        useTabletLayout,
        isOrientationDrivenFullscreen,
        isFullscreenMode,
        windowSizeClass.isCompactDevice,
        isActivityInMultiWindowMode,
        userRequestedFullscreen,
        manualPortraitHoldActive,
        isVerticalVideo
    ) {
        val requestedOrientation = resolvePhoneVideoRequestedOrientation(
            autoRotateEnabled = autoRotateEnabled,
            systemAutoRotateEnabled = systemAutoRotateEnabled,
            fullscreenMode = fullscreenMode,
            isCompactDevice = windowSizeClass.isCompactDevice,
            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
            isFullscreenMode = isFullscreenMode,
            manualFullscreenRequested = userRequestedFullscreen,
            manualPortraitHoldActive = manualPortraitHoldActive,
            isVerticalVideo = isVerticalVideo,
            currentRequestedOrientation = activity?.requestedOrientation,
            isInMultiWindowMode = isActivityInMultiWindowMode
        ) ?: return@LaunchedEffect

        if (activity?.requestedOrientation != requestedOrientation) {
            activity?.requestedOrientation = requestedOrientation
        }
        com.android.purebilibili.core.util.Logger.d(
            "VideoDetailScreen",
            "🔄 Auto-rotate: enabled=$autoRotateEnabled, system=$systemAutoRotateEnabled, hold=$manualPortraitHoldActive, mode=$fullscreenMode, horizontal=$horizontalAdaptationEnabled, requested=$requestedOrientation, fullscreen=$isFullscreenMode, verticalVideo=$isVerticalVideo, isCompactDevice=${windowSizeClass.isCompactDevice}, multiWindow=$isActivityInMultiWindowMode"
        )
    }
    var lastPhoneAutoRotateLandscapeAppliedAtMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(
        autoRotateEnabled,
        systemAutoRotateEnabled,
        windowSizeClass.isCompactDevice,
        isOrientationDrivenFullscreen,
        fullscreenMode,
        manualPortraitHoldActive,
        isActivityInMultiWindowMode
    ) {
        if (!shouldObservePhoneAutoRotate(
                autoRotateEnabled = autoRotateEnabled,
                systemAutoRotateEnabled = systemAutoRotateEnabled,
                isCompactDevice = windowSizeClass.isCompactDevice,
                isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                fullscreenMode = fullscreenMode,
                manualPortraitHoldActive = manualPortraitHoldActive,
                isInMultiWindowMode = isActivityInMultiWindowMode
            )
        ) {
            lastPhoneAutoRotateLandscapeAppliedAtMs = null
        }
    }

    DisposableEffect(
        activity,
        autoRotateEnabled,
        systemAutoRotateEnabled,
        fullscreenMode,
        useTabletLayout,
        isOrientationDrivenFullscreen,
        manualPortraitHoldActive,
        isActivityInMultiWindowMode
    ) {
        val hostActivity = activity
        if (
            hostActivity == null ||
            !shouldObservePhoneAutoRotate(
                autoRotateEnabled = autoRotateEnabled,
                systemAutoRotateEnabled = systemAutoRotateEnabled,
                isCompactDevice = windowSizeClass.isCompactDevice,
                isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                fullscreenMode = fullscreenMode,
                manualPortraitHoldActive = manualPortraitHoldActive,
                isInMultiWindowMode = isActivityInMultiWindowMode
            ) ||
            !isOrientationDrivenFullscreen
        ) {
            return@DisposableEffect onDispose {}
        }

        val orientationListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (manualPortraitHoldActive) {
                    if (shouldReleasePhoneManualPortraitHold(orientation)) {
                        manualPortraitHoldActive = false
                    }
                    return
                }
                val isCurrentlyLandscape =
                    hostActivity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                val targetOrientation = resolvePhoneAutoRotateRequestedOrientation(
                    orientationDegrees = orientation,
                    isCurrentlyLandscape = isCurrentlyLandscape
                )
                val nowMs = SystemClock.elapsedRealtime()
                val targetToApply = resolvePhoneAutoRotateTargetToApply(
                    candidateOrientation = targetOrientation,
                    lastLandscapeAppliedAtMs = lastPhoneAutoRotateLandscapeAppliedAtMs,
                    nowMs = nowMs
                ) ?: return
                if (hostActivity.requestedOrientation != targetToApply) {
                    hostActivity.requestedOrientation = targetToApply
                }
                lastPhoneAutoRotateLandscapeAppliedAtMs =
                    if (isLandscapeRequestedOrientation(targetToApply)) nowMs else null
            }
        }

        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }

        onDispose {
            orientationListener.disable()
            lastPhoneAutoRotateLandscapeAppliedAtMs = null
        }
    }
    val portraitExperienceEnabled = shouldEnablePortraitExperience()
    val useOfficialInlinePortraitDetailExperience = shouldUseOfficialInlinePortraitDetailExperience(
        useTabletLayout = useTabletLayout,
        isVerticalVideo = isVerticalVideo,
        portraitExperienceEnabled = portraitExperienceEnabled
    )
    val allowStandalonePortraitExperience = portraitExperienceEnabled &&
        !useOfficialInlinePortraitDetailExperience
    val isCurrentRouteVideoLoaded = remember(uiState, currentBvid) {
        val success = uiState as? PlayerUiState.Success
        success?.info?.bvid == currentBvid
    }
    val enterPortraitFullscreen = {
        if (shouldActivatePortraitFullscreenState(portraitExperienceEnabled)) {
            portraitSyncSnapshotBvid = (uiState as? PlayerUiState.Success)?.info?.bvid
            portraitSyncSnapshotCid = (uiState as? PlayerUiState.Success)?.info?.cid ?: 0L
            portraitSyncSnapshotPositionMs = playerState.player.currentPosition.coerceAtLeast(0L)
            hasPendingPortraitSync = false
            isPortraitFullscreen = true
        }
    }
    LaunchedEffect(
        autoEnterPortraitFromRoute,
        startAudioFromRoute,
        portraitExperienceEnabled,
        useOfficialInlinePortraitDetailExperience,
        windowSizeClass.widthSizeClass,
        isCurrentRouteVideoLoaded,
        isVerticalVideo,
        isPortraitFullscreen,
        hasAutoEnteredPortraitFromRoute
    ) {
        if (
            shouldAutoEnterPortraitFullscreenFromRoute(
                autoEnterPortraitFromRoute = autoEnterPortraitFromRoute,
                startAudioFromRoute = startAudioFromRoute,
                portraitExperienceEnabled = portraitExperienceEnabled,
                useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
                allowStandalonePortraitAutoEnter = windowSizeClass.widthSizeClass ==
                    com.android.purebilibili.core.util.WindowWidthSizeClass.Compact,
                isCurrentRouteVideoLoaded = isCurrentRouteVideoLoaded,
                isVerticalVideo = isVerticalVideo,
                isPortraitFullscreen = isPortraitFullscreen,
                hasAutoEnteredPortraitFromRoute = hasAutoEnteredPortraitFromRoute
            )
        ) {
            enterPortraitFullscreen()
            hasAutoEnteredPortraitFromRoute = true
        }
    }
    val shouldMirrorPortraitProgressToMainPlayer = com.android.purebilibili.feature.video.ui.pager
        .shouldMirrorPortraitProgressToMainPlayer(useSharedPlayer = useSharedPortraitPlayer)

    val tryApplyPortraitProgressSync = remember(playerState, viewModel) {
        { snapshotBvid: String?, snapshotPositionMs: Long ->
            val currentSuccess = viewModel.uiState.value as? PlayerUiState.Success
            val currentBvid = currentSuccess?.info?.bvid
            val currentCid = currentSuccess?.info?.cid ?: 0L
            if (!com.android.purebilibili.feature.video.ui.pager.shouldApplyPortraitProgressSync(
                    snapshotBvid = snapshotBvid,
                    snapshotCid = portraitSyncSnapshotCid,
                    currentBvid = currentBvid,
                    currentCid = currentCid
                )
            ) {
                false
            } else {
                playerState.player.seekTo(snapshotPositionMs.coerceAtLeast(0L))
                true
            }
        }
    }

    fun applyPortraitExitRestore() {
        val target = com.android.purebilibili.feature.video.ui.pager.resolvePortraitExitRestoreTarget(
            pendingMainReloadBvidAfterPortrait = pendingMainReloadBvidAfterPortrait,
            portraitPendingSelectionBvid = portraitPendingSelectionBvid,
            portraitSyncSnapshotBvid = portraitSyncSnapshotBvid,
            portraitSyncSnapshotCid = portraitSyncSnapshotCid,
            currentBvidCid = currentBvidCid
        ) ?: return
        currentBvid = target.bvid
        currentBvidCid = target.cid
    }



    // 同步状态到 playerState (可选，用于日志或内部逻辑)
    LaunchedEffect(isPortraitFullscreen) {
        playerState.setPortraitFullscreen(isPortraitFullscreen)
        viewModel.setPortraitPlaybackSessionActive(isPortraitFullscreen)
        val shouldPauseMainPlayer = com.android.purebilibili.feature.video.ui.pager
            .shouldPauseMainPlayerOnPortraitEnter(useSharedPlayer = useSharedPortraitPlayer)
        if (isPortraitFullscreen) {
            if (shouldPauseMainPlayer) {
                playerState.player.pause()
                playerState.player.volume = 0f
                playerState.player.playWhenReady = false
            }
            portraitSyncSnapshotBvid = (uiState as? PlayerUiState.Success)?.info?.bvid
            portraitSyncSnapshotCid = (uiState as? PlayerUiState.Success)?.info?.cid ?: 0L
            portraitSyncSnapshotPositionMs = playerState.player.currentPosition.coerceAtLeast(0L)
            hasPendingPortraitSync = shouldPauseMainPlayer
        } else {
             if (shouldPauseMainPlayer) {
                 // 退出时恢复音量 (不自动播放，等待用户操作或 onResume)
                 com.android.purebilibili.core.player.PlayerVolumeController
                     .applyPreferredVolume(playerState.player)
             }
            if (!com.android.purebilibili.feature.video.ui.pager
                    .shouldApplyDeferredPortraitRestoreOnResume(
                        hasDeferredRestore = hasDeferredPortraitRestoreAfterExternalNavigation,
                        isPortraitFullscreen = isPortraitFullscreen
                    )
            ) {
                applyPortraitExitRestore()
                pendingMainReloadBvidAfterPortrait = null
                portraitPendingSelectionBvid = null
            }
        }
    }

    DisposableEffect(
        lifecycleOwner,
        isPortraitFullscreen,
        hasDeferredPortraitRestoreAfterExternalNavigation,
        pendingMainReloadBvidAfterPortrait,
        portraitPendingSelectionBvid,
        portraitSyncSnapshotBvid,
        portraitSyncSnapshotCid,
        currentBvidCid
    ) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event != androidx.lifecycle.Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            if (!com.android.purebilibili.feature.video.ui.pager
                    .shouldApplyDeferredPortraitRestoreOnResume(
                        hasDeferredRestore = hasDeferredPortraitRestoreAfterExternalNavigation,
                        isPortraitFullscreen = isPortraitFullscreen
                    )
            ) {
                return@LifecycleEventObserver
            }
            applyPortraitExitRestore()
            pendingMainReloadBvidAfterPortrait = null
            portraitPendingSelectionBvid = null
            hasDeferredPortraitRestoreAfterExternalNavigation = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.setPortraitPlaybackSessionActive(false)
        }
    }

    LaunchedEffect(
        uiState,
        hasPendingPortraitSync,
        portraitSyncSnapshotBvid,
        portraitSyncSnapshotPositionMs
    ) {
        if (hasPendingPortraitSync && tryApplyPortraitProgressSync(
                portraitSyncSnapshotBvid,
                portraitSyncSnapshotPositionMs
            )
        ) {
            hasPendingPortraitSync = false
        }
    }

    LaunchedEffect(uiState, currentBvid, currentBvidCid, isPortraitFullscreen, bvid) {
        val success = uiState as? PlayerUiState.Success ?: return@LaunchedEffect
        if (!shouldSyncMainPlayerToInternalBvid(
                isPortraitFullscreen = isPortraitFullscreen,
                routeBvid = bvid,
                currentBvid = currentBvid,
                currentBvidCid = currentBvidCid,
                loadedBvid = success.info.bvid,
                loadedCid = success.info.cid
            )
        ) {
            return@LaunchedEffect
        }
        viewModel.loadVideo(
            bvid = currentBvid,
            cid = currentBvidCid.takeIf { it > 0L } ?: 0L,
            autoPlay = resolveAutoPlayOverrideForInternalBvidSync(forceAutoPlay = false)
        )
    }

    // 📲 小窗模式（手机/平板统一逻辑）
    val handlePipClick = {
        // 使用 MiniPlayerManager 进入应用内小窗模式
        miniPlayerManager?.let { manager ->
            val stopPlaybackOnExit = com.android.purebilibili.core.store.SettingsManager
                .getStopPlaybackOnExitSync(context)
            if (stopPlaybackOnExit) {
                com.android.purebilibili.core.util.Logger.d(
                    "VideoDetailScreen",
                    "Stop-on-exit enabled, skip mini mode and leave page directly"
                )
                manager.markLeavingByNavigation(expectedBvid = currentBvid)
                onBack()
                return@let
            }

            //  [埋点] PiP 进入事件
            com.android.purebilibili.core.util.AnalyticsHelper.logPictureInPicture(
                videoId = currentBvid,
                action = "enter_mini"
            )

            // 1. 将当前播放器信息传递给小窗管理器
            val info = uiState as? PlayerUiState.Success
            manager.setVideoInfo(
                bvid = currentBvid,
                title = info?.info?.title ?: "",
                cover = info?.info?.pic ?: "",
                owner = info?.info?.owner?.name ?: "",
                cid = info?.info?.cid ?: 0L,
                aid = info?.info?.aid ?: 0L,
                externalPlayer = playerState.player
            )

            // 2. 进入小窗模式（强制，不管当前模式设置）
            manager.enterMiniMode(forced = true)

            // 3. 返回上一页（首页）
            isNavigatingToMiniMode = true
            onBack()
        } ?: run {
            // 如果 miniPlayerManager 不存在，直接返回
            com.android.purebilibili.core.util.Logger.w("VideoDetailScreen", "⚠️ miniPlayerManager 为 null，无法进入小窗")
            onBack()
        }
    }

    // 🔧 [性能优化] 记录上次缓存的 bvid，避免重复缓存 MiniPlayer 信息
    var lastCachedMiniPlayerBvid by remember { mutableStateOf<String?>(null) }

    //  核心修改：初始化评论 & 媒体中心信息
    LaunchedEffect(uiState) {
        if (uiState is PlayerUiState.Success) {
            val info = (uiState as PlayerUiState.Success).info
            val success = uiState as PlayerUiState.Success

            // 初始化评论（传入 UP 主 mid 用于筛选）- 保持在主线程
            commentViewModel.init(
                aid = info.aid,
                upMid = info.owner.mid,
                preferredSortMode = preferredCommentSortMode,
                expectedReplyCount = info.stat.reply
            )

            playerState.updateMediaMetadata(
                title = info.title,
                artist = info.owner.name,
                coverUrl = info.pic
            )

            // 📱 [双重验证] 从 API dimension 字段设置预判断值
            info.dimension?.let { dim ->
                playerState.setApiDimension(dim.width, dim.height, dim.rotate)
            }

            //  同步视频信息到小窗管理器（为小窗模式做准备）
            //  🚀 [性能优化] 将繁重的序列化和缓存操作移至后台线程，防止主线程卡顿
            // 🔧 [性能优化] 只有首次加载或视频切换时才缓存 MiniPlayer 信息
            val shouldCacheMiniPlayer = lastCachedMiniPlayerBvid != currentBvid

            if (miniPlayerManager != null && shouldCacheMiniPlayer) {
                lastCachedMiniPlayerBvid = currentBvid

                // PiP 判断依赖 MiniPlayerManager 的 active/player/playing/bvid 状态。
                // 这一步必须先于后台缓存完成，避免竖屏全屏快速进入 PiP 时拿到旧状态而暂停。
                miniPlayerManager.setVideoInfo(
                    bvid = currentBvid,
                    title = info.title,
                    cover = info.pic,
                    owner = info.owner.name,
                    cid = info.cid,  //  传递 cid 用于弹幕加载
                    aid = info.aid,
                    externalPlayer = playerState.player,
                    fromLeft = com.android.purebilibili.core.util.CardPositionManager.isCardOnLeft  //  传递入场方向
                )

                launch(Dispatchers.Default) {
                    com.android.purebilibili.core.util.Logger.d("VideoDetailScreen", "🔄 [Background] Caching MiniPlayer UI state...")

                    // 序列化缓存 (Heavy Operation)
                    miniPlayerManager.cacheUiState(success)
                    com.android.purebilibili.core.util.Logger.d("VideoDetailScreen", "✅ [Background] MiniPlayer info cached")
                }
            } else if (miniPlayerManager == null) {
                android.util.Log.w("VideoDetailScreen", " miniPlayerManager 是 null!")
            }
        } else if (uiState is PlayerUiState.Loading) {
            playerState.updateMediaMetadata(
                title = "加载中...",
                artist = "",
                coverUrl = coverUrl
            )
        }
    }

    //  弹幕加载逻辑已移至 VideoPlayerState 内部处理
    // 避免在此处重复消耗 InputStream

    // 辅助函数：切换全屏状态
    val toggleFullscreen = {
        val activity = context.findActivity()
        toggleVideoDetailFullscreen(
            activity = activity,
            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
            isLandscape = isLandscape,
            isFullscreenMode = isFullscreenMode,
            isCompactDevice = windowSizeClass.isCompactDevice,
            fullscreenMode = fullscreenMode,
            isVerticalVideo = isVerticalVideo,
            portraitExperienceEnabled = portraitExperienceEnabled,
            onEnterPortraitFullscreen = { enterPortraitFullscreen() },
            onUserRequestedFullscreenChange = { requested -> userRequestedFullscreen = requested },
            onManualPortraitHoldActiveChange = { active -> manualPortraitHoldActive = active }
        )
    }

    //  拦截系统返回键：如果是全屏模式，则先退出全屏
    BackHandler(enabled = isFullscreenMode) {
        toggleFullscreen()
    }

    // 📱 拦截系统返回键：如果是竖屏全屏模式，则先退出竖屏全屏
    BackHandler(enabled = isPortraitFullscreen) {
        isPortraitFullscreen = false
    }

    // 以下 BackHandler 会阻止 Compose Navigation 的返回路由动画，由根导航统一处理。
    // 显式点击返回时由 handleBack 提前标记 returning，系统路径仍由 onDispose 兜底标记。
    // BackHandler(enabled = !isFullscreenMode && !isPortraitFullscreen, onBack = handleBack)


    // 清理逻辑（markLeavingByNavigation、restoreStatusBar）已移至 DisposableEffect.onDispose

    // 沉浸式状态栏控制
    val backgroundColor = MaterialTheme.colorScheme.background
    val isLightBackground = remember(backgroundColor) { backgroundColor.luminance() > 0.5f }
    val systemBarsVisibilityPolicy = remember(
        isFullscreenMode,
        hideVideoPageStatusBar,
        isPipMode,
        isScreenActive
    ) {
        resolveVideoDetailSystemBarsVisibilityPolicy(
            isFullscreenMode = isFullscreenMode,
            hideVideoPageStatusBar = hideVideoPageStatusBar,
            isInPipMode = isPipMode,
            isScreenActive = isScreenActive
        )
    }
    val systemBarsApplySpec = remember(
        systemBarsVisibilityPolicy,
        useTabletLayout,
        isLightBackground,
        backgroundColor
    ) {
        resolveVideoDetailSystemBarsApplySpec(
            visibilityPolicy = systemBarsVisibilityPolicy,
            useTabletLayout = useTabletLayout,
            isLightBackground = isLightBackground,
            backgroundColor = backgroundColor.toArgb(),
            transparentColor = Color.Transparent.toArgb(),
            blackColor = Color.Black.toArgb(),
            transientBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        )
    }

    //  iOS风格：竖屏时状态栏黑色背景（与播放器融为一体）
    //  只在页面活跃时修改状态栏，避免退出时覆盖恢复操作
    LaunchedEffect(view, window, insetsController, isScreenActive, systemBarsApplySpec) {
        if (view.isInEditMode || !isScreenActive || window == null || insetsController == null) {
            return@LaunchedEffect
        }
        applyVideoDetailSystemBarsSpec(
            window = window,
            insetsController = insetsController,
            spec = systemBarsApplySpec
        )
    }

    val uiSuccessState = uiState as? PlayerUiState.Success
    val videoPlayerSectionTarget = remember(bvid, coverUrl, currentBvid) {
        resolveVideoPlayerSectionTarget(
            routeBvid = bvid,
            routeCoverUrl = coverUrl,
            currentBvid = currentBvid
        )
    }
    val shouldSuppressSubtitleOverlay = useSharedPortraitPlayer &&
        !isPortraitFullscreen &&
        pendingMainReloadBvidAfterPortrait != null &&
        (
            pendingMainReloadBvidAfterPortrait != uiSuccessState?.info?.bvid ||
                (portraitSyncSnapshotCid > 0L && portraitSyncSnapshotCid != (uiSuccessState?.info?.cid ?: 0L))
            )
    var selectedVideoContentTabIndex by rememberSaveable(currentBvid) { mutableIntStateOf(0) }

    // Android 16 ART 曾拒绝校验由 VideoDetailRouteSheetHost 尾随 lambda 生成的超大合成方法
    // （VerifyError: VideoDetailScreen$lambda$N(...BoxScope, Composer, int) 参数过多）。
    // 主布局与覆盖层必须使用两个内容槽，单个局部函数仍会捕获全部状态并生成百参数方法。
    // 分槽后 Compose 编译器会分别生成合成方法，避免一个方法聚合整页状态。
    @Composable
    fun BoxScope.VideoDetailRouteSheetMainContent() {
            // 📐 [平板适配] 全屏模式过渡动画（只有手机横屏才进入全屏）
        if (isFullscreenMode) {
                val showDanmakuDialog by viewModel.showDanmakuDialog.collectAsStateWithLifecycle()
                val isSendingDanmaku by viewModel.isSendingDanmaku.collectAsStateWithLifecycle()
                val composerDrafts by viewModel.composerDrafts.collectAsStateWithLifecycle()
                val useInlineDanmakuComposer =
                    com.android.purebilibili.feature.video.ui.components.shouldUseInlineDanmakuComposer(
                        isFullscreenMode = isFullscreenMode
                    )
                val danmakuSendPreferenceScope = rememberCoroutineScope()
                val rememberedDanmakuSendColor by com.android.purebilibili.core.store.SettingsManager
                    .getDanmakuSendColor(context)
                    .collectAsStateWithLifecycle(initialValue = 16777215)
                val rememberedDanmakuSendMode by com.android.purebilibili.core.store.SettingsManager
                    .getDanmakuSendMode(context)
                    .collectAsStateWithLifecycle(initialValue = 1)
                val rememberedDanmakuSendFontSize by com.android.purebilibili.core.store.SettingsManager
                    .getDanmakuSendFontSize(context)
                    .collectAsStateWithLifecycle(initialValue = 25)
                VideoPlayerSection(
                    playerState = playerState,
                    uiState = uiState,
                    isFullscreen = true,
                    isInPipMode = isPipMode,
                    transitionEnabled = transitionEnabled,
                    onToggleFullscreen = { toggleFullscreen() },
                    onQualityChange = { qid -> viewModel.changeQuality(qid) },
                    onBack = { toggleFullscreen() },
                    onHomeClick = {
                        handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
                    },
                    onDanmakuInputClick = { viewModel.showDanmakuSendDialog() },
                    danmakuComposerVisible = showDanmakuDialog && useInlineDanmakuComposer,
                    onDismissDanmakuComposer = { viewModel.hideDanmakuSendDialog() },
                    onSendDanmakuComposer = { message, color, mode, fontSize, encourage ->
                        viewModel.sendDanmaku(message, color, mode, fontSize, encourage)
                    },
                    isSendingDanmakuComposer = isSendingDanmaku,
                    danmakuComposerInitialText = composerDrafts.danmaku.text,
                    danmakuComposerInitialAttentionCommand = composerDrafts.danmaku.attentionCommand,
                    danmakuComposerInitialColor = rememberedDanmakuSendColor,
                    danmakuComposerInitialMode = rememberedDanmakuSendMode,
                    danmakuComposerInitialFontSize = rememberedDanmakuSendFontSize,
                    onDanmakuComposerDraftChange = viewModel::updateDanmakuDraft,
                    onDanmakuComposerSelectionChange = { color, mode, fontSize ->
                        danmakuSendPreferenceScope.launch {
                            com.android.purebilibili.core.store.SettingsManager.setDanmakuSendColor(context, color)
                            com.android.purebilibili.core.store.SettingsManager.setDanmakuSendMode(context, mode)
                            com.android.purebilibili.core.store.SettingsManager.setDanmakuSendFontSize(context, fontSize)
                        }
                    },
                    // 🔗 [新增] 分享功能
                    bvid = videoPlayerSectionTarget.bvid,
                    coverUrl = videoPlayerSectionTarget.entryCoverUrl,
                    //  实验性功能：双击点赞
                    onDoubleTapLike = { viewModel.toggleLike() },
                    sponsorSegment = sponsorSegment,
                    showSponsorSkipButton = showSponsorSkipButton,
                    onSponsorSkip = { viewModel.skipCurrentSponsorSegment() },
                    onSponsorDismiss = { viewModel.dismissSponsorSkipButton() },
                    //  [新增] 重载视频
                    onReloadVideo = { viewModel.reloadVideo() },
                    //  [新增] CDN 线路切换
                    cdnCount = (uiState as? PlayerUiState.Success)?.cdnCount ?: 1,
                    cdnLineDiagnostics = (uiState as? PlayerUiState.Success)?.cdnLineDiagnostics.orEmpty(),
                    isCdnProbing = (uiState as? PlayerUiState.Success)?.isCdnProbing ?: false,
                    onSwitchCdn = { viewModel.switchCdn() },
                    onSwitchCdnTo = { viewModel.switchCdnTo(it) },
                    onProbeCdnCandidates = { viewModel.probeCurrentCdnCandidates() },

                    // [New] Codec & Audio (Fullscreen)
                    currentCodec = codecPreference,
                    onCodecChange = { viewModel.setVideoCodec(it) },
                    currentSecondCodec = secondCodecPreference,
                    onSecondCodecChange = { viewModel.setVideoSecondCodec(it) },
                    currentAudioQuality = audioQualityPreference,
                    onAudioQualityChange = { viewModel.setAudioQuality(it) },
                    onPlaybackSpeedChange = { viewModel.applyPlaybackSpeedFromUi(it) },
                    // [New] Audio Language
                    onAudioLangChange = { viewModel.changeAudioLanguage(it) },

                    //  [新增] 音频模式
                    isAudioOnly = false, // 全屏模式只有视频
                    onAudioOnlyToggle = {
                        viewModel.setAudioMode(true)
                        isNavigatingToAudioMode = true // [Fix] Set flag to prevent notification cancellation
                        onNavigateToAudioMode()
                    },

                    //  [新增] 定时关闭
                    sleepTimerMinutes = sleepTimerMinutes,
                    onSleepTimerChange = { viewModel.setSleepTimer(it) },

                    // 🖼️ [新增] 视频预览图数据
                        videoshotData = (uiState as? PlayerUiState.Success)?.videoshotData,

                    // 📖 [新增] 视频章节数据
                        viewPoints = viewPoints,
                        pbpProgressData = pbpProgressData,
                        sponsorMarkers = sponsorProgressMarkers,
                        onUserSeek = { position -> viewModel.notifyPluginsOfExplicitSeek(position) },
                    // 📱 [新增] 竖屏全屏模式
                    isVerticalVideo = isVerticalVideo && allowStandalonePortraitExperience,
                    isPortraitFullscreen = isPortraitFullscreen,
                    onPortraitFullscreen = {
                        if (allowStandalonePortraitExperience) {
                            if (!isPortraitFullscreen) {
                                if (isFullscreenMode) {
                                    toggleFullscreen()
                                }
                                enterPortraitFullscreen()
                            } else {
                                isPortraitFullscreen = false
                            }
                        }
                    },
                    // 🔁 [新增] 播放模式
                    currentPlayMode = currentPlayMode,
                    onPlayModeClick = { com.android.purebilibili.feature.video.player.PlaylistManager.togglePlayMode() },

                    // [New Actions]
                    onSaveCover = { viewModel.saveCover(context) },
                    onDownloadAudio = { viewModel.downloadAudio(context) },

                    // [新增] 侧边栏抽屉数据与交互
                    relatedVideos = (uiState as? PlayerUiState.Success)?.related ?: emptyList(),
                    ugcSeason = (uiState as? PlayerUiState.Success)?.info?.ugc_season,
                    isFollowed = (uiState as? PlayerUiState.Success)?.isFollowing ?: false,
                    isLiked = (uiState as? PlayerUiState.Success)?.isLiked ?: false,
                    isCoined = (uiState as? PlayerUiState.Success)?.coinCount?.let { it > 0 } ?: false,
                    isFavorited = (uiState as? PlayerUiState.Success)?.isFavorited ?: false,
                    onToggleFollow = { viewModel.toggleFollow() },
                    onToggleLike = { viewModel.toggleLike() },
                    onDislike = { viewModel.markVideoNotInterested() },
                    onCoin = { viewModel.showCoinDialog() },
                    onToggleFavorite = {
                        openFavoriteFolders(VideoFavoriteEntryPoint.FullscreenOverlay)
                    },
                    onTriple = { viewModel.doTripleAction() },
                    onRelatedVideoClick = navigateToRelatedVideo,
                    onPageSelect = { viewModel.switchPage(it) },
                    forceCoverOnly = forceCoverOnlyForReturn,
                    allowLivePlayerSharedElement = true,
                    sourceRouteForSharedElement = sourceRouteForSharedElement,
                    suppressSubtitleOverlay = shouldSuppressSubtitleOverlay,
                    subtitleDisplayModePreferenceOverride = subtitleDisplayModeOverride,
                    onSubtitleDisplayModePreferenceOverrideChange = { subtitleDisplayModeOverride = it },
                    onSubtitleTrackSelected = viewModel::selectSubtitleTrack
                )
            } else {
                    //  沉浸式布局：视频延伸到状态栏 + 内容区域
                    //  📐 [大屏适配] 仅 Expanded 使用分栏布局

                    //  📐 [大屏适配] 根据设备类型选择布局
                    if (useTabletLayout) {
                        // 🖥️ 平板：左右分栏布局（视频+信息 | 评论/推荐）
                        TabletCinemaLayout(
                            playerState = playerState,
                            uiState = uiState,
                            commentState = commentState,
                            viewModel = viewModel,
                            commentViewModel = commentViewModel,
                            configuration = configuration,
                            isVerticalVideo = isVerticalVideo,
                            sleepTimerMinutes = sleepTimerMinutes,

                            viewPoints = viewPoints,
                            pbpProgressData = pbpProgressData,
                            bvid = bvid,
                            coverUrl = coverUrl,
                            onBack = {
                                com.android.purebilibili.core.util.Logger.d(
                                    "VideoDetailScreen",
                                    "📱 Calling handleBack()"
                                )
                                handleBack()
                            },
                            onUpClick = navigateToUserSpaceFromVideo,
                            onBgmClick = onBgmClick,
                            onNavigateToAudioMode = {
                                isNavigatingToAudioMode = true // [Fix] Set flag to prevent notification cancellation
                                onNavigateToAudioMode()
                            },
                            onToggleFullscreen = { toggleFullscreen() },  // 📺 平板全屏切换
                            isInPipMode = isPipMode,
                            onPipClick = handlePipClick,
                            isPortraitFullscreen = isPortraitFullscreen,
                            onHomeClick = {
                                handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
                            },

                            transitionEnabled = transitionEnabled,  //  传递过渡动画开关
                            // [New] Codec & Audio
                            currentCodec = codecPreference,
                            onCodecChange = { viewModel.setVideoCodec(it) },
                            currentSecondCodec = secondCodecPreference,
                            onSecondCodecChange = { viewModel.setVideoSecondCodec(it) },
                            currentAudioQuality = audioQualityPreference,
                            onAudioQualityChange = { viewModel.setAudioQuality(it) },
                            onRelatedVideoClick = navigateToRelatedVideo,
                            showUpBadge = homeUpBadgesVisible,
                            onSearchKeywordClick = navigateToSearchKeywordFromVideo,
                            onOpenBilibiliLink = onOpenBilibiliLink,
                            // 🔁 [新增] 播放模式
                            currentPlayMode = currentPlayMode,
                            onPlayModeClick = { com.android.purebilibili.feature.video.player.PlaylistManager.togglePlayMode() },
                            forceCoverOnlyOnReturn = forceCoverOnlyForReturn
                        )
                    } else {
                        // 📱 手机竖屏：原有单列布局
                        val stableStatusBarHeight = resolveVideoDetailStableStatusBarHeightDp(
                            visibleStatusBarHeightDp = WindowInsets.statusBars
                                .asPaddingValues()
                                .calculateTopPadding()
                                .value,
                            statusBarIgnoringVisibilityHeightDp = WindowInsets.statusBarsIgnoringVisibility
                                .asPaddingValues()
                                .calculateTopPadding()
                                .value,
                            hideStatusBars = systemBarsVisibilityPolicy.hideStatusBars
                        ).dp
                        val playerTopInset = resolveVideoDetailPortraitPlayerTopInsetDp(
                            stableStatusBarHeightDp = stableStatusBarHeight.value,
                            hideStatusBars = systemBarsVisibilityPolicy.hideStatusBars
                        ).dp
                        val screenWidthDp = configuration.screenWidthDp.dp
                        val screenHeightDp = configuration.screenHeightDp.dp
                        val videoHeight = screenWidthDp * 9f / 16f  // 16:9 比例
                        val uiPreset = LocalUiPreset.current
                        val videoContentTabSwitchAnimationSpec = remember(uiPreset) {
                            resolveVideoContentTabSwitchAnimationSpec(uiPreset)
                        }

                        //  读取竖屏播放器滚动缩小模式
                        val portraitPlayerCollapseMode by com.android.purebilibili.core.store.SettingsManager
                            .getPortraitPlayerCollapseMode(context)
                            .collectAsStateWithLifecycle(initialValue = PortraitPlayerCollapseMode.OFF
            )
                        val inlinePortraitScrollEnabled = shouldEnableInlinePortraitScrollTransform(
                            collapseMode = portraitPlayerCollapseMode,
                            selectedTabIndex = selectedVideoContentTabIndex,
                            isVerticalVideo = isVerticalVideo,
                            isPlaybackPaused = isPlaybackPaused
                        )
                        // 父层只关心折叠阈值，避免列表每个像素的滚动都触发整页重组。
                        var introScrollPastCollapseThreshold by remember { mutableStateOf(false) }
                        val inlinePlayerCollapseState = rememberInlinePortraitPlayerCollapseState()
                        val compactInlinePlayerForCommentTab =
                            shouldUseCompactInlinePortraitPlayerForCommentTab(
                                useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
                                selectedTabIndex = selectedVideoContentTabIndex,
                                isPortraitFullscreen = isPortraitFullscreen,
                                isCommentThreadVisible = subReplyState.visible,
                                collapseMode = portraitPlayerCollapseMode,
                                isVerticalVideo = isVerticalVideo,
                                isPlaybackPaused = isPlaybackPaused
                            )
                        val compactInlinePlayerForIntroScroll =
                            shouldUseCompactInlinePortraitPlayerForIntroScroll(
                                useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
                                selectedTabIndex = selectedVideoContentTabIndex,
                                isPortraitFullscreen = isPortraitFullscreen,
                                firstVisibleItemIndex = if (introScrollPastCollapseThreshold) 1 else 0,
                                firstVisibleItemScrollOffset = 0,
                                collapseMode = portraitPlayerCollapseMode,
                                isVerticalVideo = isVerticalVideo,
                                isPlaybackPaused = isPlaybackPaused
                            )

                        // 📏 [Collapsing Player] 上滑隐藏播放器逻辑
                        val expandedPortraitInlineSpec = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
                            resolvePortraitInlinePlayerLayoutSpec(
                                screenWidthDp = configuration.screenWidthDp.toFloat(),
                                screenHeightDp = configuration.screenHeightDp.toFloat(),
                                isCollapsed = false
                            )
                        }
                        val collapsedPortraitInlineSpec = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
                            resolvePortraitInlinePlayerLayoutSpec(
                                screenWidthDp = configuration.screenWidthDp.toFloat(),
                                screenHeightDp = configuration.screenHeightDp.toFloat(),
                                isCollapsed = true
                            )
                        }
                        val collapseRangePx = with(LocalDensity.current) {
                            if (useOfficialInlinePortraitDetailExperience) {
                                (expandedPortraitInlineSpec.heightDp.dp - collapsedPortraitInlineSpec.heightDp.dp)
                                    .toPx()
                                    .coerceAtLeast(0f)
                            } else {
                                videoHeight.toPx()
                            }
                        }
                        LaunchedEffect(
                            selectedVideoContentTabIndex,
                            compactInlinePlayerForCommentTab,
                            compactInlinePlayerForIntroScroll,
                            portraitPlayerCollapseMode
                        ) {
                            if (!compactInlinePlayerForCommentTab && !compactInlinePlayerForIntroScroll) {
                                inlinePlayerCollapseState.reset()
                            }
                        }
                        TrackJankStateFlag(
                            stateName = "video_detail:player_swipe_collapse",
                            isActive = inlinePortraitScrollEnabled &&
                                abs(inlinePlayerCollapseState.offsetPx) > 0.5f
                        )
                        val isPlayerCollapsed by remember(inlinePortraitScrollEnabled, collapseRangePx) {
                            derivedStateOf {
                                resolveIsPlayerCollapsed(
                                    swipeHidePlayerEnabled = inlinePortraitScrollEnabled,
                                    playerHeightOffsetPx = inlinePlayerCollapseState.offsetPx,
                                    videoHeightPx = collapseRangePx
                                )
                            }
                        }

                        // 当设置关闭时，重置高度
                        LaunchedEffect(inlinePortraitScrollEnabled) {
                            if (!inlinePortraitScrollEnabled) inlinePlayerCollapseState.reset()
                        }

                        val nestedScrollConnection = remember(
                            inlinePortraitScrollEnabled,
                            isPortraitFullscreen,
                            inlinePlayerCollapseState
                        ) {
                            object : NestedScrollConnection {
                                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                    if (available.y != 0f) inlinePlayerCollapseState.beginScroll()
                                    val scrollUpdate = reduceVideoDetailPreScroll(
                                        currentOffsetPx = inlinePlayerCollapseState.offsetPx,
                                        deltaPx = available.y,
                                        minOffsetPx = -collapseRangePx,
                                        inlinePortraitScrollEnabled = inlinePortraitScrollEnabled,
                                        isPortraitFullscreen = isPortraitFullscreen
                                    ) ?: return Offset.Zero
                                    inlinePlayerCollapseState.updateOffset(scrollUpdate.nextOffsetPx)
                                    return Offset(0f, scrollUpdate.consumedDeltaPx)
                                }

                                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                                    if (available.y != 0f) inlinePlayerCollapseState.beginScroll()
                                    val scrollUpdate = reduceVideoDetailPostScroll(
                                        currentOffsetPx = inlinePlayerCollapseState.offsetPx,
                                        deltaPx = available.y,
                                        minOffsetPx = -collapseRangePx,
                                        inlinePortraitScrollEnabled = inlinePortraitScrollEnabled,
                                        isPortraitFullscreen = isPortraitFullscreen
                                    ) ?: return Offset.Zero
                                    inlinePlayerCollapseState.updateOffset(scrollUpdate.nextOffsetPx)
                                    return Offset(0f, scrollUpdate.consumedDeltaPx)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                        ) {

                        //  播放器隐藏状态（用于动画）
                        //  播放器隐藏状态（用于动画）
                        //  当 playerHeightOffsetPx 为 -videoHeightPx 时，高度只剩 statusBarHeight
                        //  [Fix] 竖屏全屏模式下强制高度不受偏移影响
                        val playerHeightOffset = if (isPortraitFullscreen) {
                            0f
                        } else {
                            inlinePlayerCollapseState.offsetPx
                        }
                        val collapseProgress = resolveVideoDetailCollapseProgress(
                            playerHeightOffsetPx = playerHeightOffset,
                            collapseRangePx = collapseRangePx,
                            isPortraitFullscreen = isPortraitFullscreen
                        )
                        val commentTabCollapseProgress by animateFloatAsState(
                            targetValue = if (compactInlinePlayerForCommentTab || compactInlinePlayerForIntroScroll) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = resolveInlinePortraitPlayerCommentCollapseDurationMillis(
                                    videoContentTabSwitchAnimationSpec
                                ),
                                easing = FastOutSlowInEasing
                            ),
                            label = "inline_portrait_comment_tab_collapse"
                        )
                        val effectiveCollapseProgress = resolveInlinePortraitPlayerCollapseProgress(
                            manualCollapseProgress = collapseProgress,
                            compactForCommentTabProgress = commentTabCollapseProgress,
                            restoreRequested = inlinePlayerCollapseState.restoreRequested
                        )
                        val expandedViewportHeight = if (useOfficialInlinePortraitDetailExperience) {
                            expandedPortraitInlineSpec.heightDp.dp
                        } else {
                            videoHeight
                        }
                        val collapsedViewportHeight = if (useOfficialInlinePortraitDetailExperience) {
                            collapsedPortraitInlineSpec.heightDp.dp
                        } else {
                            0.dp
                        }
                        val animatedViewportHeight = lerp(
                            expandedViewportHeight,
                            collapsedViewportHeight,
                            effectiveCollapseProgress
                        )
                        val expandedViewportWidth = if (useOfficialInlinePortraitDetailExperience) {
                            expandedPortraitInlineSpec.widthDp.dp
                        } else {
                            screenWidthDp
                        }
                        val collapsedViewportWidth = if (useOfficialInlinePortraitDetailExperience) {
                            collapsedPortraitInlineSpec.widthDp.dp
                        } else {
                            screenWidthDp
                        }
                        val animatedViewportWidth = lerp(
                            expandedViewportWidth,
                            collapsedViewportWidth,
                            effectiveCollapseProgress
                        )
                        val animatedPlayerHeight = animatedViewportHeight + playerTopInset

                        //  注意：移除了状态栏黑色 Spacer
                        // 播放器将延伸到状态栏下方，共享元素过渡更流畅

                        //  注意：移除了状态栏黑色 Spacer
                        // 播放器将延伸到状态栏下方，共享元素过渡更流畅

                        //  视频播放器区域：状态栏可见时避让，隐藏时让画面沉浸到顶部。
                        //  尝试获取共享元素作用域
                        val sharedTransitionScope = LocalSharedTransitionScope.current
                        val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
                        val coverSharedElementSourceRoute = resolveForcedReturnCoverSharedElementSourceRoute(
                            sourceRouteForSharedElement
                        )

                        //  为播放器容器添加共享元素标记（封面 ↔ 播放器区域映射）
                        val isFullscreenTarget = activeVideoSharedTransitionVisualSpec.fillTargetViewport
                        val playerContainerModifier = if (
                            shouldEnableVideoCoverSharedTransition(
                                transitionEnabled = transitionEnabled,
                                hasSharedTransitionScope = sharedTransitionScope != null,
                                hasAnimatedVisibilityScope = animatedVisibilityScope != null
                            ) &&
                            activeVideoSharedTransitionVisualSpec.useCoverSharedBounds &&
                            videoSharedPlaybackIntent == VideoSharedTransitionPlaybackIntent.ImmediatePlayback &&
                            !forceCoverOnlyForReturn
                        ) {
                            with(requireNotNull(sharedTransitionScope)) {
                                Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            key = com.android.purebilibili.core.ui.transition.videoCoverSharedElementKey(
                                                bvid,
                                                sourceRoute = coverSharedElementSourceRoute
                                            )
                                        ),
                                        animatedVisibilityScope = requireNotNull(animatedVisibilityScope),
                                        boundsTransform = { _, _ ->
                                            if (homeSharedTransitionMotionSpec.enabled) {
                                                val duration = if (isFullscreenTarget) {
                                                    homeSharedTransitionMotionSpec.fullscreenDurationMillis
                                                } else {
                                                    homeSharedTransitionMotionSpec.durationMillis
                                                }
                                                tween(
                                                    durationMillis = duration,
                                                    easing = homeSharedTransitionMotionSpec.easing
                                                )
                                            } else {
                                                videoSharedElementBoundsTransformSpec(homeSharedTransitionMotionSpec)
                                            }
                                        },
                                        clipInOverlayDuringTransition = OverlayClip(
                                            RoundedCornerShape(activeVideoSharedTransitionVisualSpec.targetCornerDp.dp)
                                        )
                                    )
                            }
                        } else {
                            Modifier
                        }

                        val isLeaving = useReturningVideoDetailVisualState
                        val returnAlphaDurationMillis = if (homeSharedTransitionMotionSpec.enabled) {
                            homeSharedTransitionMotionSpec.durationMillis
                        } else {
                            0
                        }
                        val coverCrossfadeAlpha = animateFloatAsState(
                            targetValue = if (
                                shouldUseDetailReturnCoverCrossfade(
                                    isLeaving = isLeaving,
                                    playbackIntent = videoSharedPlaybackIntent
                                )
                            ) {
                                1f
                            } else {
                                0f
                            },
                            animationSpec = tween(
                                durationMillis = returnAlphaDurationMillis,
                                easing = homeSharedTransitionMotionSpec.easing
                            ),
                            label = "coverCrossfade"
                        )
                        val playerFadeAlpha = animateFloatAsState(
                            targetValue = if (
                                shouldFadePlayerSurfaceOnDetailReturn(
                                    isLeaving = isLeaving,
                                    playbackIntent = videoSharedPlaybackIntent
                                )
                            ) {
                                0f
                            } else {
                                1f
                            },
                            animationSpec = tween(
                                durationMillis = returnAlphaDurationMillis,
                                easing = homeSharedTransitionMotionSpec.easing
                            ),
                            label = "playerFade"
                        )
                        val crossfadeCoverUrl = remember(coverUrl) {
                            if (coverUrl.isNotBlank()) {
                                val url = coverUrl.trim()
                                when {
                                    url.startsWith("https://") -> url
                                    url.startsWith("http://") -> url.replace("http://", "https://")
                                    url.startsWith("//") -> "https:$url"
                                    else -> url
                                }
                            } else {
                                ""
                            }
                        }

                        //  播放器容器按当前顶部避让高度计算，避免隐藏状态栏后留下黑边。
                        //  [修复] 始终保持播放器在 Composition 中，避免隐藏时重新创建导致重载
                        Box(
                            modifier = playerContainerModifier
                                .fillMaxWidth()
                                .height(animatedPlayerHeight)  //  使用动画高度（包含0高度）
                                .background(Color.Black)  // 黑色背景
                                .clipToBounds()
                                //  [PiP修复] 捕获视频播放器在屏幕上的位置
                                .onGloballyPositioned { layoutCoordinates ->
                                    val position = layoutCoordinates.positionInWindow()
                                    val rootPosition = layoutCoordinates.positionInRoot()
                                    val size = layoutCoordinates.size
                                    val nextBounds = android.graphics.Rect(
                                        position.x.toInt(),
                                        position.y.toInt(),
                                        position.x.toInt() + size.width,
                                        position.y.toInt() + size.height
                                    )
                                    val nextRootBottomPx = (rootPosition.y + size.height).roundToInt()
                                    if (
                                        videoPlayerRootBottomPx == 0 ||
                                        abs(videoPlayerRootBottomPx - nextRootBottomPx) > 3
                                    ) {
                                        videoPlayerRootBottomPx = nextRootBottomPx
                                    }
                                    if (!hasMeaningfulBoundsChange(videoPlayerBounds, nextBounds)) {
                                        return@onGloballyPositioned
                                    }
                                    videoPlayerBounds = nextBounds
                                }
                        ) {
                            // 前台常驻封面叠层，返回时只改 alpha，避免临时解码/淡入打断共享元素动画。
                            if (crossfadeCoverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(crossfadeCoverUrl)
                                        .crossfade(false)
                                        .build(),
                                    contentDescription = "cover",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = coverCrossfadeAlpha.value },
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = playerTopInset)
                                    .graphicsLayer { alpha = playerFadeAlpha.value }
                            ) {
                            PortraitInlineVideoPlayerHost(
                                modifier = Modifier.align(Alignment.TopCenter),
                                animatedViewportWidth = animatedViewportWidth,
                                animatedViewportHeight = animatedViewportHeight,
                                inlinePlayerAlpha = inlinePlayerAlpha,
                                inlinePlayerScale = inlinePlayerScale,
                                context = context,
                                playerState = playerState,
                                uiState = uiState,
                                isPipMode = isPipMode,
                                transitionEnabled = transitionEnabled,
                                onToggleFullscreen = { toggleFullscreen() },
                                viewModel = viewModel,
                                onBack = handleBack,
                                onHomeClick = {
                                    handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
                                },
                                videoPlayerSectionTarget = videoPlayerSectionTarget,
                                sponsorSegment = sponsorSegment,
                                showSponsorSkipButton = showSponsorSkipButton,
                                sleepTimerMinutes = sleepTimerMinutes,
                                viewPoints = viewPoints,
                                pbpProgressData = pbpProgressData,
                                sponsorProgressMarkers = sponsorProgressMarkers,
                                isVerticalVideo = isVerticalVideo && (allowStandalonePortraitExperience || useOfficialInlinePortraitDetailExperience),
                                onPortraitFullscreen = {
                                    when (
                                        resolvePortraitFullscreenButtonAction(
                                            useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience
                                        )
                                    ) {
                                        PortraitFullscreenButtonAction.ENTER_PORTRAIT_FULLSCREEN -> {
                                            enterPortraitFullscreen()
                                        }
                                    }
                                },
                                isPortraitFullscreen = isPortraitFullscreen,
                                onPipClick = handlePipClick,
                                codecPreference = codecPreference,
                                secondCodecPreference = secondCodecPreference,
                                audioQualityPreference = audioQualityPreference,
                                onNavigateToAudioMode = {
                                    viewModel.setAudioMode(true)
                                    isNavigatingToAudioMode = true
                                    onNavigateToAudioMode()
                                },
                                forceCoverOnly = forceCoverOnlyForReturn,
                                allowLivePlayerSharedElement = true,
                                sourceRouteForSharedElement = sourceRouteForSharedElement,
                                suppressSubtitleOverlay = shouldSuppressSubtitleOverlay,
                                subtitleDisplayModePreferenceOverride = subtitleDisplayModeOverride,
                                onSubtitleDisplayModePreferenceOverrideChange = { subtitleDisplayModeOverride = it }
                            )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.background.copy(
                                        alpha = detailLoadingBackgroundAlpha
                                    )
                                )
                                // .nestedScroll(nestedScrollConnection) // [Remove] 移除嵌套滚动，确保 Tabs 正常滑动
                        ) {
                            when (uiState) {
                                is PlayerUiState.Loading -> {
                                    val loadingState = uiState as PlayerUiState.Loading
                                    val loadingContentAlpha by animateFloatAsState(
                                        targetValue = detailLoadingTransitionFrame.loadingContentAlpha,
                                        animationSpec = tween(durationMillis = 160),
                                        label = "video-detail-loading-content"
                                    )
                                    if (detailLoadingTransitionFrame.showLoadingContent || loadingContentAlpha > 0.01f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer { alpha = loadingContentAlpha }
                                        ) {
                                            //  显示重试进度
                                            if (loadingState.retryAttempt > 0) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        //  iOS 风格加载
                                                        CupertinoActivityIndicator()
                                                        Spacer(Modifier.height(16.dp))
                                                        Text(
                                                            text = "正在重试 ${loadingState.retryAttempt}/${loadingState.maxAttempts}...",
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            } else {
                                                VideoDetailSkeleton()
                                            }
                                        }
                                    }
                                }

                                is PlayerUiState.Success -> {
                                    val success = uiState as PlayerUiState.Success
                                    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
                                    VideoDetailPhoneSuccessContentLayer(
                                        success = success,
                                        commentState = commentState,
                                        commentMemberDecorationsEnabled = commentMemberDecorationsEnabled,
                                        viewModel = viewModel,
                                        commentViewModel = commentViewModel,
                                        context = context,
                                        sortPreferenceScope = sortPreferenceScope,
                                        playerState = playerState,
                                        motionSpec = motionSpec,
                                        entryVisualFrame = entryVisualFrame,
                                        hazeState = hazeState,
                                        isTransitionFinished = isTransitionFinished,
                                        isLeaving = isLeaving,
                                        shouldShowExternalPlaylistQueueBar = shouldShowExternalPlaylistQueueBar,
                                        selectedVideoContentTabIndex = selectedVideoContentTabIndex,
                                        useTabletLayout = useTabletLayout,
                                        isFullscreenMode = isFullscreenMode,
                                        isPortraitFullscreen = isPortraitFullscreen,
                                        showCommentInput = showCommentInput,
                                        isCommentThreadVisible = subReplyState.visible,
                                        showFavoriteFolderDialog = showFavoriteFolderDialog,
                                        downloadProgress = downloadProgress,
                                        danmakuEnabledForDetail = danmakuEnabledForDetail,
                                        isQuickReturnLimitedForSharedElements =
                                            isReturningFromDetail && isQuickReturningFromDetail,
                                        transitionEnabled = transitionEnabled,
                                        sourceRouteForSharedElement = sourceRouteForSharedElement,
                                        favoriteFolders = favoriteFolders,
                                        isFavoriteFoldersLoading = isFavoriteFoldersLoading,
                                        selectedFavoriteFolderIds = selectedFavoriteFolderIds,
                                        isSavingFavoriteFolders = isSavingFavoriteFolders,
                                        isPlayerCollapsed = isPlayerCollapsed,
                                        onRestorePlayer = inlinePlayerCollapseState::restore,
                                        onBgmClick = onBgmClick,
                                        homeUpBadgesVisible = homeUpBadgesVisible,
                                        isVideoPlaying = isVideoPlaying,
                                        onSelectedTabChange = { selectedVideoContentTabIndex = it },
                                        onIntroScrollThresholdChange = {
                                            introScrollPastCollapseThreshold = it
                                        },
                                        openFavoriteFolders = openFavoriteFolders,
                                        navigateToUserSpaceFromVideo = navigateToUserSpaceFromVideo,
                                        navigateToRelatedVideo = navigateToRelatedVideo,
                                        openCommentUrl = openCommentUrl,
                                        onOpenBilibiliLink = onOpenBilibiliLink,
                                        onShareVideo = { payload -> pendingVideoShare = payload },
                                        externalPlaylistQueueTitle = externalPlaylistQueueTitle,
                                        playlistItems = playlistItems,
                                        onShowExternalPlaylistQueueSheet = {
                                            showExternalPlaylistQueueSheet = true
                                        }
                                    )
                            } // End of Success block

                                is PlayerUiState.Error -> {
                                    val errorState = uiState as PlayerUiState.Error
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(32.dp)
                                        ) {
                                            //  根据错误类型显示不同图标
                                            Text(
                                                text = when (errorState.error) {
                                                    is com.android.purebilibili.data.model.VideoLoadError.NetworkError -> "📡"
                                                    is com.android.purebilibili.data.model.VideoLoadError.VideoNotFound -> "🔍"
                                                    is com.android.purebilibili.data.model.VideoLoadError.RegionRestricted -> "🌐"
                                                    is com.android.purebilibili.data.model.VideoLoadError.RateLimited -> "⏳"
                                                    is com.android.purebilibili.data.model.VideoLoadError.GlobalCooldown -> ""
                                                    is com.android.purebilibili.data.model.VideoLoadError.PlayUrlEmpty -> "⚡"
                                                    else -> ""
                                                },
                                                fontSize = 48.sp
                                            )
                                            Spacer(Modifier.height(16.dp))
                                            Text(
                                                text = errorState.msg,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 16.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )

                                            //  针对风控错误显示额外建议
                                            when (errorState.error) {
                                                is com.android.purebilibili.data.model.VideoLoadError.GlobalCooldown,
                                                is com.android.purebilibili.data.model.VideoLoadError.PlayUrlEmpty -> {
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        text = " 建议：切换 WiFi/移动数据 或 清除缓存后重试",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 13.sp,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                                is com.android.purebilibili.data.model.VideoLoadError.RateLimited -> {
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        text = " 该视频可能暂时不可用，请尝试其他视频",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 13.sp,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                                else -> {}
                                            }

                                            //  只有可重试的错误才显示重试按钮（或者风控错误允许强制重试）
                                            val showRetryButton = errorState.canRetry ||
                                                errorState.error is com.android.purebilibili.data.model.VideoLoadError.RateLimited ||
                                                errorState.error is com.android.purebilibili.data.model.VideoLoadError.PlayUrlEmpty
                                            if (showRetryButton) {
                                                Spacer(Modifier.height(24.dp))
                                                Button(
                                                    onClick = { viewModel.retry() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    )
                                                ) {
                                                    Text(
                                                        text = when (errorState.error) {
                                                            is com.android.purebilibili.data.model.VideoLoadError.RateLimited -> "强制重试"
                                                            is com.android.purebilibili.data.model.VideoLoadError.GlobalCooldown -> "清除冷却并重试"
                                                            else -> "重试"
                                                        }
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                    }
                    }  // 📱 手机竖屏布局结束（Column）
                    }  // Box with nested scroll
                }  // else shouldUseSplitLayout
            }  // else targetIsLandscape
    }

    @Composable
    fun BoxScope.VideoDetailRouteSheetOverlayContent() {
        // 📱 [新增] 竖屏全屏覆盖层
        // [修复] 在 Loading 状态时也保持竖屏全屏，使用上一个成功状态的数据
        // [修复] 移除 !isLandscape 限制，允许用户强制进入（例如在平板或特殊设备上）
        val showPortraitFullscreen = shouldShowStandalonePortraitPager(
            portraitExperienceEnabled = portraitExperienceEnabled,
            isPortraitFullscreen = isPortraitFullscreen,
            useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
            hasPlayableState = uiState is PlayerUiState.Success || uiState is PlayerUiState.Loading
        )

        // 缓存上一个成功状态以在 Loading 时使用
        var cachedSuccess by remember { mutableStateOf<PlayerUiState.Success?>(null) }
        LaunchedEffect(uiState) {
            if (uiState is PlayerUiState.Success) {
                cachedSuccess = uiState as PlayerUiState.Success
            }
        }



        // 获取当前或缓存的成功状态
        val success = when {
            uiState is PlayerUiState.Success -> uiState as PlayerUiState.Success
            uiState is PlayerUiState.Loading && cachedSuccess != null -> cachedSuccess!!
            else -> null
        }

        val isLoadingNewVideo = uiState is PlayerUiState.Loading

        // Diagnostic Log
        LaunchedEffect(isPortraitFullscreen, showPortraitFullscreen, success) {
            com.android.purebilibili.core.util.Logger.d("VideoDetailScreen",
                "Portrait Mode Check: requested=$isPortraitFullscreen, shown=$showPortraitFullscreen, " +
                "success=${success != null}, isLandscape=$isLandscape")
        }

        AnimatedVisibility(
            visible = showPortraitFullscreen && success != null,
            enter = if (shouldAnimatePortraitPager) {
                fadeIn(
                    animationSpec = tween(portraitPagerMotionSpec.enterDurationMillis, easing = com.android.purebilibili.core.ui.motion.AppMotionEasing.EmphasizedEnter)
                )
            } else {
                androidx.compose.animation.EnterTransition.None
            },
            exit = if (shouldAnimatePortraitPager) {
                val exitEasing = com.android.purebilibili.core.ui.motion.AppMotionEasing.EmphasizedExit
                val exitSpec = tween<Float>(
                    durationMillis = portraitPagerMotionSpec.exitDurationMillis,
                    easing = exitEasing
                )
                fadeOut(
                    animationSpec = exitSpec
                ) + scaleOut(
                    targetScale = portraitPagerMotionSpec.exitScaleTarget,
                    animationSpec = exitSpec,
                    transformOrigin = TransformOrigin(0.5f, 0f)
                ) + slideOutVertically(
                    animationSpec = tween(
                        durationMillis = portraitPagerMotionSpec.exitDurationMillis,
                        easing = exitEasing
                    ),
                    targetOffsetY = {
                        -(it * portraitPagerMotionSpec.exitTranslateUpFraction).roundToInt()
                    }
                )
            } else {
                androidx.compose.animation.ExitTransition.None
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (success != null) {
                val portraitInitialBvid = pendingMainReloadBvidAfterPortrait ?: success.info.bvid
                // 竖屏全屏模式：使用 Pager 实现无缝滑动 (TikTok Style)
                com.android.purebilibili.feature.video.ui.pager.PortraitVideoPager(
                    initialBvid = portraitInitialBvid,
                    initialInfo = success.info,
                    recommendations = success.related,
                    onBack = { isPortraitFullscreen = false },
                    onHomeClick = {
                        isPortraitFullscreen = false
                        handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
                    },
                    onVideoChange = { newBvid ->
                        // 高频滑动期间不重载主播放器，避免与竖屏播放器抢焦点导致暂停。
                        // 仅记录竖屏会话内当前浏览目标，真正退出时再提交给主播放器。
                        portraitPendingSelectionBvid = newBvid
                    },
                    viewModel = viewModel,
                    sharedPlayer = if (useSharedPortraitPlayer) playerState.player else null,
                    // [新增] 进度同步
                    initialStartPositionMs = portraitSyncSnapshotPositionMs,
                    onProgressUpdate = { bvid, pos, cidSnapshot ->
                        portraitPendingSelectionBvid = bvid
                        portraitSyncSnapshotBvid = bvid
                        portraitSyncSnapshotCid = cidSnapshot
                        portraitSyncSnapshotPositionMs = pos.coerceAtLeast(0L)
                        if (shouldMirrorPortraitProgressToMainPlayer) {
                            hasPendingPortraitSync = true
                            if (tryApplyPortraitProgressSync(bvid, portraitSyncSnapshotPositionMs)) {
                                hasPendingPortraitSync = false
                            }
                        }
                    },
                    onExitSnapshot = { bvid, pos, cidSnapshot ->
                        currentBvid = bvid
                        currentBvidCid = cidSnapshot
                        portraitPendingSelectionBvid = bvid
                        portraitSyncSnapshotBvid = bvid
                        portraitSyncSnapshotCid = cidSnapshot
                        portraitSyncSnapshotPositionMs = pos.coerceAtLeast(0L)
                        pendingMainReloadBvidAfterPortrait = bvid
                        if (shouldMirrorPortraitProgressToMainPlayer) {
                            hasPendingPortraitSync = true
                            if (tryApplyPortraitProgressSync(bvid, portraitSyncSnapshotPositionMs)) {
                                hasPendingPortraitSync = false
                            }
                        }
                    },
                    onSearchClick = {
                        hasDeferredPortraitRestoreAfterExternalNavigation =
                            com.android.purebilibili.feature.video.ui.pager
                                .shouldDeferPortraitRestoreUntilForegroundResume(
                                    isPortraitFullscreen = isPortraitFullscreen,
                                    isExternalNavigation = true
                                )
                        if (com.android.purebilibili.feature.video.ui.pager
                                .shouldExitPortraitForExternalNavigation(isPortraitFullscreen)
                        ) {
                            isPortraitFullscreen = false
                        }
                        navigateToSearchFromVideo()
                    },
                    onUserClick = { mid ->
                        val anchorBvid = portraitPendingSelectionBvid
                            ?: pendingMainReloadBvidAfterPortrait
                            ?: portraitSyncSnapshotBvid
                            ?: (uiState as? PlayerUiState.Success)?.info?.bvid
                        if (!anchorBvid.isNullOrBlank()) {
                            currentBvid = anchorBvid
                            currentBvidCid = if (anchorBvid == portraitSyncSnapshotBvid) {
                                portraitSyncSnapshotCid
                            } else {
                                0L
                            }
                            pendingMainReloadBvidAfterPortrait = anchorBvid
                        }
                        hasDeferredPortraitRestoreAfterExternalNavigation =
                            com.android.purebilibili.feature.video.ui.pager
                                .shouldDeferPortraitRestoreUntilForegroundResume(
                                    isPortraitFullscreen = isPortraitFullscreen,
                                    isExternalNavigation = true
                                )
                        if (com.android.purebilibili.feature.video.ui.pager
                                .shouldExitPortraitForUserSpaceNavigation(isPortraitFullscreen)
                        ) {
                            isPortraitFullscreen = false
                        }
                        navigateToUserSpaceFromVideo(mid)
                    },
                    onRotateToLandscape = {
                        isPortraitFullscreen = false
                        val activity = context.findActivity()
                        val targetOrientation = resolvePortraitRotateTargetOrientation(
                            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                            manualPortraitHoldActive = manualPortraitHoldActive
                        )
                        if (activity != null && targetOrientation != null) {
                            userRequestedFullscreen = true
                            manualPortraitHoldActive = false
                            activity.requestedOrientation = targetOrientation
                        } else {
                            toggleFullscreen()
                        }
                    }
                )
            }
        }

        InteractiveChoiceOverlay(
            state = interactiveChoicePanel,
            onSelectChoice = { edgeId, targetCid ->
                viewModel.selectInteractiveChoice(edgeId = edgeId, cid = targetCid)
            },
            onDismiss = { viewModel.dismissInteractiveChoicePanel() }
        )

        //  [新增] 投币对话框
        val coinDialogVisible by viewModel.coinDialogVisible.collectAsStateWithLifecycle()
        val currentCoinCount = (uiState as? PlayerUiState.Success)?.coinCount ?: 0
        val userBalance by viewModel.userCoinBalance.collectAsStateWithLifecycle()
        CoinDialog(
            visible = coinDialogVisible,
            currentCoinCount = currentCoinCount,
            userBalance = userBalance,
            onDismiss = { viewModel.closeCoinDialog() },
            onConfirm = { count, alsoLike -> viewModel.doCoin(count, alsoLike) }
        )

        VideoDetailFollowGroupDialog(viewModel = viewModel)

        ExternalPlaylistQueueSheet(
            visible = shouldShowExternalPlaylistQueueBar && showExternalPlaylistQueueSheet,
            title = externalPlaylistQueueTitle,
            playlist = playlistItems,
            currentIndex = playlistCurrentIndex,
            hazeState = hazeState,
            presentation = externalPlaylistQueueSheetPresentation,
            onDismiss = { showExternalPlaylistQueueSheet = false },
            onVideoSelected = { index, item ->
                PlaylistManager.playAt(index)
                showExternalPlaylistQueueSheet = false
                switchVideoInCurrentDetailPage(
                    targetBvid = item.bvid,
                    targetCid = 0L,
                    autoPlay = true
                )
            }
        )

        pendingVideoShare?.let { payload ->
            VideoShareSheet(
                payload = payload,
                onDismiss = { pendingVideoShare = null }
            )
        }

        VideoDetailPlaybackEndedDialog(
            viewModel = viewModel,
            player = playerState.player
        )

        //  [新增] 弹幕发送对话框
        val showDanmakuDialog by viewModel.showDanmakuDialog.collectAsStateWithLifecycle()
        val isSendingDanmaku by viewModel.isSendingDanmaku.collectAsStateWithLifecycle()
        val fallbackPlayerBottomPx = with(LocalDensity.current) {
            val fallbackPlayerHeight = configuration.screenWidthDp.dp * 9f / 16f
            val fallbackStableStatusBar = resolveVideoDetailStableStatusBarHeightDp(
                visibleStatusBarHeightDp = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding()
                    .value,
                statusBarIgnoringVisibilityHeightDp = WindowInsets.statusBarsIgnoringVisibility
                    .asPaddingValues()
                    .calculateTopPadding()
                    .value,
                hideStatusBars = systemBarsVisibilityPolicy.hideStatusBars
            )
            val fallbackPlayerTopInset = resolveVideoDetailPortraitPlayerTopInsetDp(
                stableStatusBarHeightDp = fallbackStableStatusBar,
                hideStatusBars = systemBarsVisibilityPolicy.hideStatusBars
            ).dp
            (fallbackPlayerHeight + fallbackPlayerTopInset).toPx().roundToInt()
        }
        val danmakuDialogTopReservePx = remember(
            isLandscape,
            isFullscreenMode,
            isPortraitFullscreen,
            videoPlayerRootBottomPx,
            fallbackPlayerBottomPx
        ) {
            resolveDanmakuDialogTopReservePx(
                isLandscape = isLandscape,
                isFullscreenMode = isFullscreenMode,
                isPortraitFullscreen = isPortraitFullscreen,
                playerBottomPx = videoPlayerRootBottomPx.takeIf { it > 0 },
                fallbackPlayerBottomPx = fallbackPlayerBottomPx
            )
        }
        val screenHeightPx = with(LocalDensity.current) {
            configuration.screenHeightDp.dp.roundToPx()
        }
        val danmakuDialogTopReserveDp = with(LocalDensity.current) { danmakuDialogTopReservePx.toDp() }
        val danmakuSendPreferenceScope = rememberCoroutineScope()
        val rememberedDanmakuSendColor by com.android.purebilibili.core.store.SettingsManager
            .getDanmakuSendColor(context)
            .collectAsStateWithLifecycle(initialValue = 16777215
        )
        val rememberedDanmakuSendMode by com.android.purebilibili.core.store.SettingsManager
            .getDanmakuSendMode(context)
            .collectAsStateWithLifecycle(initialValue = 1
        )
        val rememberedDanmakuSendFontSize by com.android.purebilibili.core.store.SettingsManager
            .getDanmakuSendFontSize(context)
            .collectAsStateWithLifecycle(initialValue = 25
        )
        val composerDrafts by viewModel.composerDrafts.collectAsStateWithLifecycle()
        val useInlineDanmakuComposer =
            com.android.purebilibili.feature.video.ui.components.shouldUseInlineDanmakuComposer(
                isFullscreenMode = isFullscreenMode
            )
        com.android.purebilibili.feature.video.ui.components.DanmakuSendDialog(
            visible = showDanmakuDialog && !useInlineDanmakuComposer,
            onDismiss = { viewModel.hideDanmakuSendDialog() },
            onSend = { message, color, mode, fontSize, encourage ->
                android.util.Log.d("VideoDetailScreen", "📤 Sending danmaku: $message")
                viewModel.sendDanmaku(message, color, mode, fontSize, encourage)
            },
            isSending = isSendingDanmaku,
            initialColor = rememberedDanmakuSendColor,
            initialMode = rememberedDanmakuSendMode,
            initialFontSize = rememberedDanmakuSendFontSize,
            initialText = composerDrafts.danmaku.text,
            initialAttentionCommand = composerDrafts.danmaku.attentionCommand,
            onDraftChange = viewModel::updateDanmakuDraft,
            onSelectionChange = { color, mode, fontSize ->
                danmakuSendPreferenceScope.launch {
                    com.android.purebilibili.core.store.SettingsManager.setDanmakuSendColor(context, color)
                    com.android.purebilibili.core.store.SettingsManager.setDanmakuSendMode(context, mode)
                    com.android.purebilibili.core.store.SettingsManager.setDanmakuSendFontSize(context, fontSize)
                }
            },
            topReservedSpace = danmakuDialogTopReserveDp
        )

        //  [新增] 评论输入对话框
        val isSendingComment by viewModel.isSendingComment.collectAsStateWithLifecycle() // 暂时复用 ViewModel 状态?
        val replyingToComment by viewModel.replyingToComment.collectAsStateWithLifecycle()
        val emotePackages by viewModel.emotePackages.collectAsStateWithLifecycle() // [新增]
        val mentionSearchState by viewModel.commentMentionSearchState.collectAsStateWithLifecycle()
        val commentDraftKey = com.android.purebilibili.feature.video.viewmodel
            .commentComposerDraftKey(replyingToComment?.rpid)
        val commentDraft = composerDrafts.comments[commentDraftKey]
            ?: com.android.purebilibili.feature.video.viewmodel.CommentComposerDraft()

        com.android.purebilibili.feature.video.ui.components.CommentInputDialog(
            visible = showCommentInput,
            onDismiss = { viewModel.hideCommentInputDialog() },
            isSending = isSendingComment,
            replyToName = replyingToComment?.member?.uname,
            inputHint = if (replyingToComment != null) commentState.childInputHint else commentState.rootInputHint,
            canUploadImage = commentState.canUploadImage,
            canInputComment = commentState.canInputComment,
            emotePackages = emotePackages, // [新增]
            mentionUsers = mentionSearchState.users,
            isMentionSearching = mentionSearchState.isLoading,
            mentionSearchError = mentionSearchState.errorMessage,
            onMentionSearchQueryChange = viewModel::searchCommentMentionUsers,
            initialText = commentDraft.text,
            initialImageUris = commentDraft.imageUris,
            initialSyncToDynamic = commentDraft.syncToDynamic,
            onDraftChange = viewModel::updateCommentDraft,
            currentVideoPositionMsProvider = { playerState.player.currentPosition.coerceAtLeast(0L) },
            onSend = { message, imageUris, syncToDynamic ->
                viewModel.sendComment(message, imageUris, syncToDynamic)
                viewModel.hideCommentInputDialog()
            }
        )

        //  [新增] 下载选项菜单 & 画质选择
        val showDownloadDialog by viewModel.showDownloadDialog.collectAsStateWithLifecycle()
        val successForDownload = uiState as? PlayerUiState.Success
        val downloadTasks by com.android.purebilibili.feature.download.DownloadManager.tasks.collectAsStateWithLifecycle()

        // 本地状态控制画质选择弹窗
        var showQualitySelection by remember { mutableStateOf(false) }
        var showBatchDownloadDialog by remember { mutableStateOf(false) }

        if (showDownloadDialog && successForDownload != null) {
            val batchDownloadCandidates = remember(successForDownload.info) {
                com.android.purebilibili.feature.download.resolveBatchDownloadCandidates(
                    successForDownload.info
                )
            }
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeDownloadDialog() },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "下载选项",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // 1. 缓存视频
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 检查任务状态
                                val existingTask = com.android.purebilibili.feature.download.DownloadManager.getVideoTask(successForDownload.info.bvid, successForDownload.info.cid)
                                if (existingTask != null && !existingTask.isFailed) {
                                    if (existingTask.isComplete) viewModel.toast("视频已缓存")
                                    else viewModel.toast("正在下载中...")
                                    viewModel.closeDownloadDialog()
                                } else {
                                    // 打开画质选择
                                    showQualitySelection = true
                                    viewModel.closeDownloadDialog()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = rememberAppDownloadIcon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "缓存视频",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "选择画质缓存当前视频",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (batchDownloadCandidates.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBatchDownloadDialog = true
                                    viewModel.closeDownloadDialog()
                                }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = rememberAppCollectionIcon(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "批量缓存",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "选择多个分P或合集条目统一加入下载",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 2. 下载音频
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val task = com.android.purebilibili.feature.download.DownloadTask(
                                    bvid = successForDownload.info.bvid,
                                    cid = successForDownload.info.cid,
                                    title = successForDownload.info.title,
                                    cover = successForDownload.info.pic,
                                    ownerName = successForDownload.info.owner.name,
                                    ownerFace = successForDownload.info.owner.face,
                                    duration = 0, // 音频不需要 duration?
                                    quality = 0,
                                    qualityDesc = "音频",
                                    videoUrl = "",
                                    audioUrl = successForDownload.audioUrl ?: "",
                                    isAudioOnly = true,
                                    isVerticalVideo = false
                                )
                                if (task.audioUrl.isNotEmpty()) {
                                    val started = com.android.purebilibili.feature.download.DownloadManager.addTask(task)
                                    if (started) viewModel.toast("已开始下载音频")
                                    else viewModel.toast("该任务已在下载中或已完成")
                                } else {
                                    viewModel.toast("无法获取音频地址")
                                }
                                viewModel.closeDownloadDialog()
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = rememberAppMusicIcon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "下载音频",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "仅保存音频文件",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. 保存封面
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current // 获取 Context

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val coverUrl = successForDownload.info.pic
                                val title = successForDownload.info.title
                                if (coverUrl.isNotEmpty()) {
                                    scope.launch {
                                        val success = com.android.purebilibili.feature.download.DownloadManager.saveImageToGallery(
                                            context,
                                            coverUrl,
                                            title
                                        )
                                        // Toast 已经在 saveImageToGallery 内部或者需要外部调用?
                                        // VideoPlayerOverlay 是自己调用的。
                                        // context 是必要的。
                                        if (success) viewModel.toast("封面已保存到相册")
                                        else viewModel.toast("保存失败")
                                    }
                                } else {
                                    viewModel.toast("无法获取封面地址")
                                }
                                viewModel.closeDownloadDialog()
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = rememberAppPhotoIcon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "保存封面",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "保存当前视频封面到相册",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 缓存视频 - 画质选择弹窗 (当 showQualitySelection 为 true 时显示)
        if (showQualitySelection && successForDownload != null) {
            val sortedQualityOptions = successForDownload.qualityIds
                .zip(successForDownload.qualityLabels)
                .sortedByDescending { it.first }
            val highestQuality = sortedQualityOptions.firstOrNull()?.first ?: successForDownload.currentQuality

            com.android.purebilibili.feature.download.DownloadQualityDialog(
                title = successForDownload.info.title,
                qualityOptions = sortedQualityOptions,
                currentQuality = highestQuality,
                onQualitySelected = { quality, options ->
                    viewModel.downloadWithQuality(quality, options)
                    showQualitySelection = false
                },
                onDismiss = { showQualitySelection = false }
            )
        }

        if (showBatchDownloadDialog && successForDownload != null) {
            val batchDownloadCandidates = remember(successForDownload.info) {
                com.android.purebilibili.feature.download.resolveBatchDownloadCandidates(
                    successForDownload.info
                )
            }
            val downloadedCandidateIds = remember(downloadTasks) {
                downloadTasks.values
                    .filter { !it.isFailed && !it.isAudioOnly }
                    .map { "${it.bvid}#${it.cid}" }
                    .toSet()
            }
            val sortedQualityOptions = successForDownload.qualityIds
                .zip(successForDownload.qualityLabels)
                .sortedByDescending { it.first }
            val highestQuality = sortedQualityOptions.firstOrNull()?.first ?: successForDownload.currentQuality

            com.android.purebilibili.feature.download.BatchDownloadDialog(
                title = successForDownload.info.title,
                candidates = batchDownloadCandidates,
                qualityOptions = sortedQualityOptions,
                currentQuality = highestQuality,
                downloadedIds = downloadedCandidateIds,
                onConfirm = { quality, options, selectedCandidates ->
                    viewModel.downloadBatchWithQuality(
                        qualityId = quality,
                        options = options,
                        candidates = selectedCandidates
                    )
                    showBatchDownloadDialog = false
                },
                onDismiss = { showBatchDownloadDialog = false }
            )
        }

        val successState = uiState as? PlayerUiState.Success
        DetachedVideoCommentThreadHost(
            visible = shouldShowDetachedVideoCommentThreadHost(useTabletLayout = useTabletLayout),
            successState = successState,
            commentState = commentState,
            commentViewModel = commentViewModel,
            forceInitialize = shouldForceInitializeDetachedCommentThreadHostForRoute(
                routeCommentRootRpid = openCommentRootRpidFromRoute,
                aid = successState?.info?.aid ?: 0L,
                hasHandledRouteComment = hasHandledCommentRootFromRoute
            ),
            viewModel = viewModel,
            onUpClick = navigateToUserSpaceFromVideo,
            onNavigateToRelatedVideo = { targetVideoId ->
                navigateToRelatedVideo(targetVideoId, null)
            },
            onSearchKeywordClick = navigateToSearchKeywordFromVideo,
            onOpenBilibiliLink = onOpenBilibiliLink,
            screenHeightPx = screenHeightPx,
            topReservedPx = danmakuDialogTopReservePx,
            onTimestampClick = { positionMs ->
                seekPlayerFromUserAction(playerState.player, positionMs)
                commentViewModel.closeSubReply()
            }
        )

        // 📁 收藏夹选择弹窗
        if (showFavoriteFolderDialog) {
            com.android.purebilibili.feature.video.ui.components.FavoriteFolderSheet(
                folders = favoriteFolders,
                isLoading = isFavoriteFoldersLoading,
                selectedFolderIds = selectedFavoriteFolderIds,
                isSaving = isSavingFavoriteFolders,
                onFolderToggle = { folder -> viewModel.toggleFavoriteFolderSelection(folder) },
                onSaveClick = { viewModel.saveFavoriteFolderSelection() },
                onDismissRequest = { viewModel.dismissFavoriteFolderDialog() },
                onCreateFolder = { title, intro, isPrivate ->
                    viewModel.createFavoriteFolder(title, intro, isPrivate)
                }
            )
        }

        // 🎉 点赞成功爆裂动画
        val likeBurstVisible by viewModel.likeBurstVisible.collectAsStateWithLifecycle()
        if (likeBurstVisible) {
            Box(
                modifier = Modifier
                    .align(feedbackAnchorAlignment)
                    .padding(
                        end = if (feedbackPlacement.anchor == VideoFeedbackAnchor.BottomTrailing) feedbackPlacement.sideInsetDp.dp else 0.dp,
                        bottom = (feedbackPlacement.bottomInsetDp + 56).dp
                    )
            ) {
                LikeBurstAnimation(
                    visible = true,
                    reducedMotion = isReducedActionMotion,
                    onAnimationEnd = { viewModel.dismissLikeBurst() }
                )
            }
        }

        // 🎉 三连成功庆祝动画
        val tripleCelebrationVisible by viewModel.tripleCelebrationVisible.collectAsStateWithLifecycle()
        val tripleCelebrationPlacement = resolveTripleCelebrationPlacement(
            isFullscreen = isFullscreenMode,
            isLandscape = isLandscape
        )
        if (tripleCelebrationVisible) {
            Box(
                modifier = Modifier.align(
                    when (tripleCelebrationPlacement) {
                        TripleCelebrationPlacement.CenterOverlay -> Alignment.Center
                    }
                )
            ) {
                TripleSuccessAnimation(
                    visible = true,
                    isCompact = false,
                    reducedMotion = isReducedActionMotion,
                    onAnimationEnd = { viewModel.dismissTripleCelebration() }
                )
            }
        }

        val activeFeedbackPlacement = if (popupMessage?.presentation == PlayerToastPresentation.CenteredHighlight) {
            resolveQualityReminderPlacement()
        } else {
            feedbackPlacement
        }

        VideoActionFeedbackHost(
            message = popupMessage?.message,
            visible = popupMessage != null,
            placement = activeFeedbackPlacement,
            hazeState = hazeState
        )

        resumePlaybackSuggestion?.let { suggestion ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissResumePlaybackSuggestion() },
                title = { Text("继续播放") },
                text = {
                    Text(
                        text = "检测到上次播放到 ${suggestion.targetLabel}（${FormatUtils.formatDuration(suggestion.positionMs)}），是否跳转继续播放？"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.continueResumePlaybackSuggestion() }) {
                        Text("跳转")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissResumePlaybackSuggestion() }) {
                        Text("稍后")
                    }
                }
            )
        }

        VideoDetailQualitySwitchFailureDialog(
            context = context,
            viewModel = viewModel,
            qualitySwitchFailureDialog = qualitySwitchFailureDialog,
            qualitySwitchFailureDialogEnabled = qualitySwitchFailureDialogEnabled,
            qualitySwitchFailureDialogOnceEnabled = qualitySwitchFailureDialogOnceEnabled,
            qualitySwitchFailureDialogShown = qualitySwitchFailureDialogShown,
            playerDiagnosticLoggingEnabled = playerDiagnosticLoggingEnabled,
            qualitySwitchDialogScope = qualitySwitchDialogScope
        )

        VideoDetailDanmakuContextMenu(
            context = context,
            viewModel = viewModel,
            activeDanmakuBlockRulesRaw = activeDanmakuBlockRulesRaw,
            activeDanmakuScope = activeDanmakuScope,
            sortPreferenceScope = sortPreferenceScope
        )

        // 🔗 绑定弹幕点击监听器
        LaunchedEffect(danmakuManager) {
            danmakuManager.setOnDanmakuClickListener { text, dmid, userHash, isSelf ->
                android.util.Log.d("VideoDetailScreen", "👆 Danmaku clicked: $text")
                viewModel.showDanmakuMenu(dmid, text, userHash, isSelf)
            }
        }
    }

    VideoDetailRouteSheetHost(
        frame = routeSheetFrame,
        motion = routeSheetMotion,
        isFullscreenMode = isFullscreenMode,
        backgroundColor = MaterialTheme.colorScheme.background,
        modifier = detailShellModifier,
        content = { VideoDetailRouteSheetMainContent() },
        overlayContent = { VideoDetailRouteSheetOverlayContent() }
    )
}

@Composable
private fun VideoDetailFollowGroupDialog(
    viewModel: PlayerViewModel
) {
    val followGroupDialogVisible by viewModel.followGroupDialogVisible.collectAsStateWithLifecycle(
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    val followGroupTags by viewModel.followGroupTags.collectAsStateWithLifecycle(
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    val followGroupSelectedTagIds by viewModel.followGroupSelectedTagIds.collectAsStateWithLifecycle(
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    val isFollowGroupsLoading by viewModel.isFollowGroupsLoading.collectAsStateWithLifecycle(
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    val isSavingFollowGroups by viewModel.isSavingFollowGroups.collectAsStateWithLifecycle(
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    if (!followGroupDialogVisible) return

    AlertDialog(
        onDismissRequest = {
            if (!isSavingFollowGroups) viewModel.dismissFollowGroupDialog()
        },
        title = { Text("设置关注分组") },
        text = {
            if (isFollowGroupsLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CupertinoActivityIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (followGroupTags.isEmpty()) {
                        Text(
                            text = "暂无可用分组（不勾选即为默认分组）",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    } else {
                        followGroupTags.forEach { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleFollowGroupSelection(tag.tagid) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = followGroupSelectedTagIds.contains(tag.tagid),
                                    onCheckedChange = { viewModel.toggleFollowGroupSelection(tag.tagid) }
                                )
                                Text(
                                    text = "${tag.name} (${tag.count})",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Text(
                        text = "可多选，确定后覆盖原分组设置。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.saveFollowGroupSelection() },
                enabled = !isFollowGroupsLoading && !isSavingFollowGroups
            ) {
                if (isSavingFollowGroups) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("确定")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.dismissFollowGroupDialog() },
                enabled = !isSavingFollowGroups
            ) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun VideoDetailPlaybackEndedDialog(
    viewModel: PlayerViewModel,
    player: Player
) {
    val showPlaybackEndedDialog by viewModel.showPlaybackEndedDialog.collectAsStateWithLifecycle(
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    if (!showPlaybackEndedDialog) return

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { viewModel.dismissPlaybackEndedDialog() }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "播放完成",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择接下来的操作",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        viewModel.dismissPlaybackEndedDialog()
                        player.seekTo(0)
                        playPlayerFromUserAction(player)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("🔄 重播当前视频")
                }
                Button(
                    onClick = {
                        viewModel.dismissPlaybackEndedDialog()
                        viewModel.playNextRecommended()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("▶️ 播放下一个视频")
                }
                TextButton(
                    onClick = { viewModel.dismissPlaybackEndedDialog() }
                ) {
                    Text("暂不操作")
                }
            }
        }
    }
}

@Composable
private fun VideoDetailQualitySwitchFailureDialog(
    context: Context,
    viewModel: PlayerViewModel,
    qualitySwitchFailureDialog: QualitySwitchFailureDialogState?,
    qualitySwitchFailureDialogEnabled: Boolean,
    qualitySwitchFailureDialogOnceEnabled: Boolean,
    qualitySwitchFailureDialogShown: Boolean,
    playerDiagnosticLoggingEnabled: Boolean,
    qualitySwitchDialogScope: CoroutineScope
) {
    LaunchedEffect(
        qualitySwitchFailureDialog?.requestedQualityId,
        qualitySwitchFailureDialogEnabled,
        qualitySwitchFailureDialogOnceEnabled,
        qualitySwitchFailureDialogShown
    ) {
        val dialog = qualitySwitchFailureDialog ?: return@LaunchedEffect
        val shouldSuppressDialog = !qualitySwitchFailureDialogEnabled ||
            (qualitySwitchFailureDialogOnceEnabled && qualitySwitchFailureDialogShown)
        if (shouldSuppressDialog) {
            viewModel.dismissQualitySwitchFailureDialog()
        }
    }

    qualitySwitchFailureDialog
        ?.takeIf {
            qualitySwitchFailureDialogEnabled &&
                !(qualitySwitchFailureDialogOnceEnabled && qualitySwitchFailureDialogShown)
        }
        ?.let { dialog ->
            fun dismissQualitySwitchFailureDialogAfterUserChoice() {
                qualitySwitchDialogScope.launch {
                    if (qualitySwitchFailureDialogOnceEnabled) {
                        com.android.purebilibili.core.store.SettingsManager
                            .markQualitySwitchFailureDialogShown(context)
                    }
                    viewModel.dismissQualitySwitchFailureDialog()
                }
            }

            AlertDialog(
                onDismissRequest = { dismissQualitySwitchFailureDialogAfterUserChoice() },
                title = { Text(dialog.title) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(dialog.message)
                        TextButton(
                            onClick = {
                                qualitySwitchDialogScope.launch {
                                    com.android.purebilibili.core.store.SettingsManager
                                        .setPlayerDiagnosticLoggingEnabled(
                                            context,
                                            !playerDiagnosticLoggingEnabled
                                        )
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                if (playerDiagnosticLoggingEnabled) {
                                    "关闭诊断日志"
                                } else {
                                    "开启诊断日志"
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    qualitySwitchDialogScope.launch {
                                        val nextValue = !qualitySwitchFailureDialogOnceEnabled
                                        com.android.purebilibili.core.store.SettingsManager
                                            .setQualitySwitchFailureDialogOnceEnabled(context, nextValue)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = qualitySwitchFailureDialogOnceEnabled,
                                onCheckedChange = { checked ->
                                    qualitySwitchDialogScope.launch {
                                        com.android.purebilibili.core.store.SettingsManager
                                            .setQualitySwitchFailureDialogOnceEnabled(context, checked)
                                    }
                                }
                            )
                            Text("仅提示一次")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            com.android.purebilibili.core.util.LogCollector.exportAndShare(context)
                            dismissQualitySwitchFailureDialogAfterUserChoice()
                        }
                    ) {
                        Text("导出日志")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dismissQualitySwitchFailureDialogAfterUserChoice() }) {
                        Text("关闭")
                    }
                }
            )
        }
}

@Composable
private fun VideoDetailDanmakuContextMenu(
    context: Context,
    viewModel: PlayerViewModel,
    activeDanmakuBlockRulesRaw: String,
    activeDanmakuScope: com.android.purebilibili.core.store.DanmakuSettingsScope,
    sortPreferenceScope: CoroutineScope
) {
    val danmakuMenuState by viewModel.danmakuMenuState.collectAsStateWithLifecycle(
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    if (!danmakuMenuState.visible) return

    DanmakuContextMenu(
        text = danmakuMenuState.text,
        onDismiss = { viewModel.hideDanmakuMenu() },
        onLike = { viewModel.likeDanmaku(danmakuMenuState.dmid) },
        onRecall = { viewModel.recallDanmaku(danmakuMenuState.dmid) },
        onReport = { reason ->
            viewModel.reportDanmaku(danmakuMenuState.dmid, reason)
        },
        voteCount = danmakuMenuState.voteCount,
        hasLiked = danmakuMenuState.hasLiked,
        voteLoading = danmakuMenuState.voteLoading,
        canVote = danmakuMenuState.canVote,
        canRecall = danmakuMenuState.isSelf,
        canBlockKeyword = danmakuMenuState.text.isNotBlank(),
        onBlockKeyword = {
            val updatedRules = appendDanmakuKeywordBlockRule(
                rawRules = activeDanmakuBlockRulesRaw,
                keyword = danmakuMenuState.text
            )
            val changed = updatedRules != activeDanmakuBlockRulesRaw
            sortPreferenceScope.launch {
                com.android.purebilibili.core.store.SettingsManager.setDanmakuBlockRulesRaw(
                    context,
                    updatedRules,
                    activeDanmakuScope
                )
            }
            viewModel.toast(
                resolveDanmakuBlockActionFeedbackMessage(
                    target = DanmakuBlockActionTarget.KEYWORD,
                    changed = changed
                )
            )
        },
        canBlockUser = danmakuMenuState.userHash.isNotBlank(),
        onBlockUser = {
            val userHash = danmakuMenuState.userHash
            if (userHash.isBlank()) {
                viewModel.toast("该弹幕缺少发送者标识")
            } else {
                val updatedRules = appendDanmakuUserHashBlockRule(
                    rawRules = activeDanmakuBlockRulesRaw,
                    userHash = userHash
                )
                val changed = updatedRules != activeDanmakuBlockRulesRaw
                sortPreferenceScope.launch {
                    com.android.purebilibili.core.store.SettingsManager.setDanmakuBlockRulesRaw(
                        context,
                        updatedRules,
                        activeDanmakuScope
                    )
                }
                viewModel.toast(
                    resolveDanmakuBlockActionFeedbackMessage(
                        target = DanmakuBlockActionTarget.USER,
                        changed = changed
                    )
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
private fun ExternalPlaylistQueueSheet(
    visible: Boolean,
    title: String,
    playlist: List<PlaylistItem>,
    currentIndex: Int,
    hazeState: HazeState,
    presentation: ExternalPlaylistQueueSheetPresentation,
    onDismiss: () -> Unit,
    onVideoSelected: (Int, PlaylistItem) -> Unit
) {
    if (!visible) return

    val configuration = LocalConfiguration.current
    val listMaxHeight = resolveExternalPlaylistQueueListMaxHeightDp(configuration.screenHeightDp).dp
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomSpacerHeight = resolveExternalPlaylistQueueBottomSpacerDp(
        navigationBarBottomPadding.value.roundToInt()
    ).dp
    val sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    when (presentation) {
        ExternalPlaylistQueueSheetPresentation.INLINE_HAZE -> {
            val interactionSource = remember { MutableInteractionSource() }
            val useHazeEffect = shouldAllowRuntimeShaderBackedHazeEffect(Build.VERSION.SDK_INT)
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onDismiss() }
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(sheetShape)
                        .then(
                            if (useHazeEffect) {
                                Modifier.hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.ultraThin()
                                )
                            } else {
                                Modifier
                            }
                        ),
                    shape = sheetShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                    tonalElevation = 0.dp,
                    border = BorderStroke(
                        width = 0.6.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    ExternalPlaylistQueueSheetContent(
                        title = title,
                        playlist = playlist,
                        currentIndex = currentIndex,
                        listMaxHeight = listMaxHeight,
                        bottomSpacerHeight = bottomSpacerHeight,
                        onVideoSelected = onVideoSelected
                    )
                }
            }
        }
        ExternalPlaylistQueueSheetPresentation.MODAL -> {
            IOSModalBottomSheet(
                onDismissRequest = onDismiss,
                containerColor = Color.Transparent,
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(sheetShape)
                        .unifiedBlur(hazeState = hazeState, shape = sheetShape),
                    shape = sheetShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                    tonalElevation = 0.dp,
                    border = BorderStroke(
                        width = 0.6.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    ExternalPlaylistQueueSheetContent(
                        title = title,
                        playlist = playlist,
                        currentIndex = currentIndex,
                        listMaxHeight = listMaxHeight,
                        bottomSpacerHeight = bottomSpacerHeight,
                        onVideoSelected = onVideoSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun ExternalPlaylistQueueSheetContent(
    title: String,
    playlist: List<PlaylistItem>,
    currentIndex: Int,
    listMaxHeight: androidx.compose.ui.unit.Dp,
    bottomSpacerHeight: androidx.compose.ui.unit.Dp,
    onVideoSelected: (Int, PlaylistItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${playlist.size}个视频",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = listMaxHeight),
            contentPadding = PaddingValues(bottom = bottomSpacerHeight)
        ) {
            items(
                playlist.size,
                key = { index ->
                    val item = playlist[index]
                    resolveIndexedVideoLazyKey(
                        namespace = "video_playlist",
                        index = index,
                        bvid = item.bvid
                    )
                }
            ) { index ->
                val item = playlist[index]
                val selected = index == currentIndex
                val normalizedCoverUrl = normalizePlaylistCoverUrlForUi(item.cover)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onVideoSelected(index, item) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        if (normalizedCoverUrl.isNotEmpty()) {
                            AsyncImage(
                                model = normalizedCoverUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "无封面",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.title,
                            maxLines = 1,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.owner,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    if (selected) {
                        Icon(
                            imageVector = rememberAppPlayIcon(),
                            contentDescription = "当前播放",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

private fun toggleVideoDetailFullscreen(
    activity: Activity?,
    isOrientationDrivenFullscreen: Boolean,
    isLandscape: Boolean,
    isFullscreenMode: Boolean,
    isCompactDevice: Boolean,
    fullscreenMode: com.android.purebilibili.core.store.FullscreenMode,
    isVerticalVideo: Boolean,
    portraitExperienceEnabled: Boolean,
    onEnterPortraitFullscreen: () -> Unit,
    onUserRequestedFullscreenChange: (Boolean) -> Unit,
    onManualPortraitHoldActiveChange: (Boolean) -> Unit
) {
    if (activity == null) return

    val isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode
    val isInPictureInPictureMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        activity.isInPictureInPictureMode
    if (shouldUseInWindowFullscreenForSystemMultiWindow(
            isInMultiWindowMode = isInMultiWindowMode,
            isInPictureInPictureMode = isInPictureInPictureMode,
            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
            isFullscreenMode = isFullscreenMode
        )
    ) {
        onUserRequestedFullscreenChange(true)
        onManualPortraitHoldActiveChange(false)
        return
    }

    if (isOrientationDrivenFullscreen && isInMultiWindowMode && isFullscreenMode) {
        onUserRequestedFullscreenChange(false)
        onManualPortraitHoldActiveChange(false)
        return
    }

    if (!isOrientationDrivenFullscreen) {
        val nextRequestedFullscreen = !isFullscreenMode
        onUserRequestedFullscreenChange(nextRequestedFullscreen)
        if (!nextRequestedFullscreen &&
            isCompactDevice &&
            fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.VERTICAL
        ) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        return
    }

    if (isLandscape) {
        onUserRequestedFullscreenChange(false)
        onManualPortraitHoldActiveChange(true)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return
    }

    val targetOrientation = resolvePhoneFullscreenEnterOrientation(
        fullscreenMode = fullscreenMode,
        isVerticalVideo = isVerticalVideo
    ) ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

    if (shouldEnterPortraitFullscreenOnFullscreenToggle(
            targetOrientation = targetOrientation,
            portraitExperienceEnabled = portraitExperienceEnabled
        )
    ) {
        onUserRequestedFullscreenChange(false)
        onManualPortraitHoldActiveChange(false)
        onEnterPortraitFullscreen()
        return
    }

    onUserRequestedFullscreenChange(true)
    onManualPortraitHoldActiveChange(false)
    activity.requestedOrientation = targetOrientation
}

internal fun resolveNextPlayerHeightOffset(
    currentOffsetPx: Float,
    deltaPx: Float,
    minOffsetPx: Float,
    maxOffsetPx: Float = 0f,
    minUpdateDeltaPx: Float = 0.75f
): Float? {
    if (abs(deltaPx) < minUpdateDeltaPx) return null
    val nextOffset = (currentOffsetPx + deltaPx).coerceIn(minOffsetPx, maxOffsetPx)
    return if (abs(nextOffset - currentOffsetPx) < minUpdateDeltaPx) {
        null
    } else {
        nextOffset
    }
}

internal fun resolveIsPlayerCollapsed(
    swipeHidePlayerEnabled: Boolean,
    playerHeightOffsetPx: Float,
    videoHeightPx: Float,
    collapseTolerancePx: Float = 10f
): Boolean {
    if (!swipeHidePlayerEnabled) return false
    return playerHeightOffsetPx <= (-videoHeightPx + collapseTolerancePx)
}

internal fun resolveIsPlaybackPausedForCollapse(
    playWhenReady: Boolean,
    playbackState: Int
): Boolean {
    // 这里按用户暂停意图判断，而不是按 isPlaying，避免缓冲态误判为“暂停时可缩小”。
    return !playWhenReady && playbackState != Player.STATE_ENDED
}

internal fun shouldUseTabletVideoLayout(
    isExpandedScreen: Boolean,
    isTabletDevice: Boolean
): Boolean {
    return isExpandedScreen && isTabletDevice
}

internal fun shouldUseOrientationDrivenFullscreen(
    isCompactDevice: Boolean
): Boolean {
    return isCompactDevice
}

internal fun shouldRotateToPortraitOnSplitBack(
    useTabletLayout: Boolean,
    isCompactDevice: Boolean,
    orientation: Int
): Boolean {
    return useTabletLayout && isCompactDevice && orientation == Configuration.ORIENTATION_LANDSCAPE
}

internal fun shouldShowDetachedVideoCommentThreadHost(
    useTabletLayout: Boolean
): Boolean {
    return !useTabletLayout
}

internal fun resolveVideoDetailCommentThreadHostMainSheetVisible(
    useEmbeddedPresentation: Boolean,
    subReplyVisible: Boolean
): Boolean {
    return useEmbeddedPresentation && subReplyVisible
}

internal fun shouldForceInitializeDetachedCommentThreadHostForRoute(
    routeCommentRootRpid: Long,
    aid: Long,
    hasHandledRouteComment: Boolean
): Boolean {
    return routeCommentRootRpid > 0L && aid > 0L && !hasHandledRouteComment
}

internal fun shouldApplyPhoneAutoRotatePolicy(
    isCompactDevice: Boolean
): Boolean {
    return isCompactDevice
}

internal fun resolveVideoDetailFullscreenMode(
    isOrientationDrivenFullscreen: Boolean,
    isLandscape: Boolean,
    userRequestedFullscreen: Boolean,
    isInMultiWindowMode: Boolean
): Boolean {
    if (!isOrientationDrivenFullscreen) return userRequestedFullscreen
    return isLandscape || (isInMultiWindowMode && userRequestedFullscreen)
}

internal fun shouldApplyStartFullscreenOrientationRequest(
    startInFullscreen: Boolean,
    isOrientationDrivenFullscreen: Boolean,
    isLandscape: Boolean,
    isInMultiWindowMode: Boolean
): Boolean {
    if (!startInFullscreen) return false
    if (!isOrientationDrivenFullscreen) return false
    if (isLandscape) return false
    // 系统小窗/分屏内强写 requestedOrientation 会让部分 ROM 在横竖窗口间反复重建。
    if (isInMultiWindowMode) return false
    return true
}

internal fun resolvePhoneFullscreenEnterOrientation(
    fullscreenMode: com.android.purebilibili.core.store.FullscreenMode,
    isVerticalVideo: Boolean
): Int? {
    return when (fullscreenMode) {
        com.android.purebilibili.core.store.FullscreenMode.NONE -> null
        com.android.purebilibili.core.store.FullscreenMode.VERTICAL -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        com.android.purebilibili.core.store.FullscreenMode.HORIZONTAL -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        com.android.purebilibili.core.store.FullscreenMode.AUTO -> {
            if (isVerticalVideo) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }
}

internal fun shouldKeepManualFullscreenRequest(
    manualFullscreenRequested: Boolean,
    hasEnteredFullscreenDuringRequest: Boolean,
    isFullscreenMode: Boolean
): Boolean {
    if (!manualFullscreenRequested) return false
    if (isFullscreenMode) return true
    return !hasEnteredFullscreenDuringRequest
}

@Composable
private fun ManualFullscreenRequestLifecycleEffect(
    manualFullscreenRequested: Boolean,
    isFullscreenMode: Boolean,
    onReleaseManualFullscreenRequest: () -> Unit
) {
    var hasEnteredFullscreenDuringRequest by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(manualFullscreenRequested, isFullscreenMode) {
        if (manualFullscreenRequested && isFullscreenMode) {
            hasEnteredFullscreenDuringRequest = true
            return@LaunchedEffect
        }

        if (
            !shouldKeepManualFullscreenRequest(
                manualFullscreenRequested = manualFullscreenRequested,
                hasEnteredFullscreenDuringRequest = hasEnteredFullscreenDuringRequest,
                isFullscreenMode = isFullscreenMode
            )
        ) {
            if (manualFullscreenRequested) {
                onReleaseManualFullscreenRequest()
            }
            hasEnteredFullscreenDuringRequest = false
        }
    }
}

internal fun resolvePhoneVideoRequestedOrientation(
    autoRotateEnabled: Boolean,
    systemAutoRotateEnabled: Boolean = true,
    fullscreenMode: com.android.purebilibili.core.store.FullscreenMode,
    isCompactDevice: Boolean,
    isOrientationDrivenFullscreen: Boolean,
    isFullscreenMode: Boolean,
    manualFullscreenRequested: Boolean = false,
    manualPortraitHoldActive: Boolean = false,
    isVerticalVideo: Boolean = false,
    currentRequestedOrientation: Int? = null,
    isInMultiWindowMode: Boolean = false
): Int? {
    if (isInMultiWindowMode) {
        return null
    }
    if (!shouldApplyPhoneAutoRotatePolicy(isCompactDevice)) {
        return if (isFullscreenMode || manualFullscreenRequested) {
            resolvePhoneFullscreenEnterOrientation(
                fullscreenMode = fullscreenMode,
                isVerticalVideo = isVerticalVideo
            )
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    if (fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.NONE) {
        return null
    }
    if (fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.VERTICAL) {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    if (!isOrientationDrivenFullscreen) {
        return null
    }
    if (manualPortraitHoldActive) {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    if (resolveEffectivePhoneAutoRotateEnabled(
            autoRotateEnabled = autoRotateEnabled,
            systemAutoRotateEnabled = systemAutoRotateEnabled,
            manualPortraitHoldActive = manualPortraitHoldActive
        )
    ) {
        return when {
            manualFullscreenRequested -> {
                resolvePhoneFullscreenEnterOrientation(
                    fullscreenMode = fullscreenMode,
                    isVerticalVideo = isVerticalVideo
                ) ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            isFullscreenMode -> resolveCurrentExactLandscapeOrientation(currentRequestedOrientation)
                ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    if (autoRotateEnabled && !systemAutoRotateEnabled && !manualFullscreenRequested) {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    return if (isFullscreenMode) {
        resolvePhoneFullscreenEnterOrientation(
            fullscreenMode = fullscreenMode,
            isVerticalVideo = isVerticalVideo
        ) ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

internal fun resolvePhoneAutoRotateRequestedOrientation(
    orientationDegrees: Int,
    isCurrentlyLandscape: Boolean,
    portraitSnapDegrees: Int = 25,
    landscapeEnterMinDegrees: Int = 60,
    landscapeEnterMaxDegrees: Int = 120,
    landscapeKeepMinDegrees: Int = 40,
    landscapeKeepMaxDegrees: Int = 140
): Int? {
    if (orientationDegrees == OrientationEventListener.ORIENTATION_UNKNOWN) return null
    val normalized = ((orientationDegrees % 360) + 360) % 360

    val portraitStable = isPhoneOrientationPortraitStable(
        orientationDegrees = normalized,
        portraitSnapDegrees = portraitSnapDegrees
    )
    val exactLandscapeEntry = resolveExactLandscapeOrientation(
        orientationDegrees = normalized,
        minLeftSideTopDegrees = landscapeEnterMinDegrees,
        maxLeftSideTopDegrees = landscapeEnterMaxDegrees,
        minRightSideTopDegrees = 240,
        maxRightSideTopDegrees = 300
    )
    val exactLandscapeKeep = resolveExactLandscapeOrientation(
        orientationDegrees = normalized,
        minLeftSideTopDegrees = landscapeKeepMinDegrees,
        maxLeftSideTopDegrees = landscapeKeepMaxDegrees,
        minRightSideTopDegrees = 220,
        maxRightSideTopDegrees = 320
    )

    return when {
        isCurrentlyLandscape && exactLandscapeKeep != null -> exactLandscapeKeep
        portraitStable -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        !isCurrentlyLandscape && exactLandscapeEntry != null -> exactLandscapeEntry
        else -> null
    }
}

internal const val PHONE_AUTO_ROTATE_LANDSCAPE_SETTLE_MS = 500L

internal fun resolvePhoneAutoRotateTargetToApply(
    candidateOrientation: Int?,
    lastLandscapeAppliedAtMs: Long?,
    nowMs: Long,
    landscapeSettleMs: Long = PHONE_AUTO_ROTATE_LANDSCAPE_SETTLE_MS
): Int? {
    if (candidateOrientation == null) return null
    // 系统配置切到横屏有延迟；按最近一次横屏写入时间保护，避免刚进横屏又被残留竖屏角度拉回。
    if (
        candidateOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT &&
        lastLandscapeAppliedAtMs != null &&
        nowMs - lastLandscapeAppliedAtMs < landscapeSettleMs
    ) {
        return null
    }
    return candidateOrientation
}

private fun resolveCurrentExactLandscapeOrientation(currentRequestedOrientation: Int?): Int? {
    return when (currentRequestedOrientation) {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> currentRequestedOrientation
        else -> null
    }
}

private fun resolveExactLandscapeOrientation(
    orientationDegrees: Int,
    minLeftSideTopDegrees: Int,
    maxLeftSideTopDegrees: Int,
    minRightSideTopDegrees: Int,
    maxRightSideTopDegrees: Int
): Int? {
    return when {
        withinWrappedRange(
            orientationDegrees,
            minLeftSideTopDegrees,
            maxLeftSideTopDegrees
        ) -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        withinWrappedRange(
            orientationDegrees,
            minRightSideTopDegrees,
            maxRightSideTopDegrees
        ) -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else -> null
    }
}

private fun isLandscapeRequestedOrientation(requestedOrientation: Int): Boolean {
    return requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
        requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
}

internal fun shouldEnterPortraitFullscreenOnFullscreenToggle(
    targetOrientation: Int,
    portraitExperienceEnabled: Boolean
): Boolean {
    return portraitExperienceEnabled && targetOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

internal fun shouldUseInWindowFullscreenForSystemMultiWindow(
    isInMultiWindowMode: Boolean,
    isInPictureInPictureMode: Boolean,
    isOrientationDrivenFullscreen: Boolean,
    isFullscreenMode: Boolean
): Boolean {
    if (!isOrientationDrivenFullscreen) return false
    if (isFullscreenMode) return false
    if (isInPictureInPictureMode) return false
    return isInMultiWindowMode
}

internal fun resolvePortraitRotateTargetOrientation(
    isOrientationDrivenFullscreen: Boolean,
    manualPortraitHoldActive: Boolean = false
): Int? {
    return if (isOrientationDrivenFullscreen && !manualPortraitHoldActive) {
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
        null
    }
}

internal fun resolveEffectivePhoneAutoRotateEnabled(
    autoRotateEnabled: Boolean,
    systemAutoRotateEnabled: Boolean,
    manualPortraitHoldActive: Boolean
): Boolean {
    return autoRotateEnabled && systemAutoRotateEnabled && !manualPortraitHoldActive
}

internal fun shouldObservePhoneAutoRotate(
    autoRotateEnabled: Boolean,
    systemAutoRotateEnabled: Boolean,
    isCompactDevice: Boolean,
    isOrientationDrivenFullscreen: Boolean,
    fullscreenMode: com.android.purebilibili.core.store.FullscreenMode,
    manualPortraitHoldActive: Boolean,
    isInMultiWindowMode: Boolean = false
): Boolean {
    if (!autoRotateEnabled) return false
    if (!systemAutoRotateEnabled) return false
    if (isInMultiWindowMode) return false
    if (!shouldApplyPhoneAutoRotatePolicy(isCompactDevice)) return false
    if (!isOrientationDrivenFullscreen) return false
    if (fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.NONE) return false
    if (fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.VERTICAL) return false
    return true
}

internal fun shouldReleasePhoneManualPortraitHold(
    orientationDegrees: Int,
    portraitSnapDegrees: Int = 25
): Boolean {
    if (orientationDegrees == OrientationEventListener.ORIENTATION_UNKNOWN) return false
    return isPhoneOrientationPortraitStable(
        orientationDegrees = orientationDegrees,
        portraitSnapDegrees = portraitSnapDegrees
    )
}

private fun isPhoneOrientationPortraitStable(
    orientationDegrees: Int,
    portraitSnapDegrees: Int
): Boolean {
    val normalized = ((orientationDegrees % 360) + 360) % 360
    return withinWrappedRange(normalized, 0, portraitSnapDegrees) ||
        withinWrappedRange(normalized, 180 - portraitSnapDegrees, 180 + portraitSnapDegrees) ||
        withinWrappedRange(normalized, 360 - portraitSnapDegrees, 359)
}

private fun withinWrappedRange(
    value: Int,
    min: Int,
    max: Int
): Boolean {
    return if (min <= max) {
        value in min..max
    } else {
        value >= min || value <= max
    }
}

@Composable
private fun rememberSystemAutoRotateEnabled(context: Context): State<Boolean> {
    return produceState(initialValue = readSystemAutoRotateEnabled(context), context) {
        val contentResolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = readSystemAutoRotateEnabled(context)
            }
        }
        value = readSystemAutoRotateEnabled(context)
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            false,
            observer
        )
        awaitDispose {
            contentResolver.unregisterContentObserver(observer)
        }
    }
}

private fun readSystemAutoRotateEnabled(context: Context): Boolean {
    return runCatching {
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION
        ) == 1
    }.getOrDefault(true)
}

@Composable
private fun DetachedVideoCommentThreadHost(
    visible: Boolean,
    successState: PlayerUiState.Success?,
    commentState: CommentUiState,
    commentViewModel: VideoCommentViewModel,
    forceInitialize: Boolean,
    viewModel: PlayerViewModel,
    onUpClick: (Long) -> Unit,
    onNavigateToRelatedVideo: (String) -> Unit,
    onSearchKeywordClick: (String) -> Unit,
    onOpenBilibiliLink: ((String) -> Unit)?,
    screenHeightPx: Int,
    topReservedPx: Int,
    onTimestampClick: (Long) -> Unit
) {
    if (!visible) return

    val subReplyState by commentViewModel.subReplyState.collectAsStateWithLifecycle()

    VideoCommentSheetHost(
        mainSheetVisible = resolveVideoDetailCommentThreadHostMainSheetVisible(
            useEmbeddedPresentation = com.android.purebilibili.feature.video.ui.pager
                .shouldUseEmbeddedVideoSubReplyPresentation(),
            subReplyVisible = subReplyState.visible
        ),
        onDismiss = { commentViewModel.closeSubReply() },
        commentViewModel = commentViewModel,
        aid = successState?.info?.aid ?: 0L,
        upMid = commentState.upMid,
        expectedReplyCount = commentState.replyCount,
        emoteMap = successState?.emoteMap ?: emptyMap(),
        onRootCommentClick = { viewModel.openRootCommentComposer() },
        onReplyClick = { replyItem ->
            android.util.Log.d("VideoDetailScreen", "📝 Reply to: ${replyItem.member.uname}")
            viewModel.setReplyingTo(replyItem)
            viewModel.showCommentInputDialog()
        },
        onUserClick = onUpClick,
        onVideoClick = onNavigateToRelatedVideo,
        onSearchKeywordClick = onSearchKeywordClick,
        onOpenBilibiliLink = onOpenBilibiliLink,
        screenHeightPx = screenHeightPx,
        topReservedPx = topReservedPx,
        onTimestampClick = onTimestampClick,
        maxTimestampMs = successState?.videoDurationMs?.takeIf { it > 0L },
        forceInitialize = forceInitialize,
        handleFraudEvents = false
    )
}

internal fun resolveVideoDetailExitRequestedOrientation(
    originalRequestedOrientation: Int?
): Int {
    return originalRequestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

internal fun shouldEnablePortraitExperience(): Boolean {
    return true
}

internal fun shouldShowVideoDetailBottomInteractionBar(
    useTabletLayout: Boolean,
    selectedTabIndex: Int,
    isFullscreenMode: Boolean,
    isPortraitFullscreen: Boolean,
    isCommentInputVisible: Boolean,
    isCommentThreadVisible: Boolean,
    isFavoriteFolderDialogVisible: Boolean,
    isExternalPlaylistQueueBarVisible: Boolean
): Boolean {
    return !useTabletLayout &&
        selectedTabIndex == VIDEO_CONTENT_COMMENT_TAB_INDEX &&
        !isFullscreenMode &&
        !isPortraitFullscreen &&
        !isCommentInputVisible &&
        !isCommentThreadVisible &&
        !isFavoriteFolderDialogVisible &&
        !isExternalPlaylistQueueBarVisible
}

internal fun shouldShowVideoDetailActionButtons(): Boolean {
    return true
}

internal data class RefreshModeCandidate(
    val modeId: Int,
    val refreshRate: Float,
    val width: Int,
    val height: Int
)

internal fun resolvePreferredHighRefreshModeId(
    currentModeId: Int,
    supportedModes: List<RefreshModeCandidate>,
    minRefreshRate: Float = 90f
): Int? {
    if (supportedModes.isEmpty()) return null
    val currentMode = supportedModes.firstOrNull { it.modeId == currentModeId } ?: return null
    val candidates = supportedModes.filter {
        it.refreshRate >= minRefreshRate &&
            it.width == currentMode.width &&
            it.height == currentMode.height
    }
    if (candidates.isEmpty()) return null

    return candidates.maxWithOrNull(
        compareBy<RefreshModeCandidate> { it.refreshRate }
            .thenBy { if (it.modeId == currentModeId) 1 else 0 }
    )?.modeId
}

// VideoContentSection 已提取到 VideoContentSection.kt
// VideoTagsRow 和 VideoTagChip 也已提取到 VideoContentSection.kt
