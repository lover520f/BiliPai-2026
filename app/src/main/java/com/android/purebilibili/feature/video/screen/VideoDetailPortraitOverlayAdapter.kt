package com.android.purebilibili.feature.video.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.feature.video.ui.pager.PortraitVideoPager
import com.android.purebilibili.feature.video.viewmodel.VideoEngagementViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackUiState
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackViewModel
import kotlin.math.roundToInt

@Composable
internal fun VideoDetailPortraitOverlayAdapter(
    uiState: VideoPlaybackUiState,
    portraitExperienceEnabled: Boolean,
    isPortraitFullscreen: Boolean,
    useOfficialInlinePortraitDetailExperience: Boolean,
    isLandscape: Boolean,
    shouldAnimatePortraitPager: Boolean,
    motionSpec: StandalonePortraitPagerMotionSpec,
    initialBvidOverride: String?,
    initialStartPositionMs: Long,
    entryCoverUrl: String = "",
    playbackViewModel: VideoPlaybackViewModel,
    engagementViewModel: VideoEngagementViewModel,
    sharedPlayer: ExoPlayer?,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onVideoChange: (String) -> Unit,
    onProgressUpdate: (String, Long, Long) -> Unit,
    onExitSnapshot: (String, Long, Long) -> Unit,
    onSearchClick: () -> Unit,
    onUserClick: (Long) -> Unit,
    onRotateToLandscape: () -> Unit,
) {
    val showPortraitFullscreen = shouldShowStandalonePortraitPager(
        portraitExperienceEnabled = portraitExperienceEnabled,
        isPortraitFullscreen = isPortraitFullscreen,
        useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
        hasPlayableState = uiState is VideoPlaybackUiState.Success || uiState is VideoPlaybackUiState.Loading,
    )
    var cachedSuccess by remember { mutableStateOf<VideoPlaybackUiState.Success?>(null) }
    LaunchedEffect(uiState) {
        if (uiState is VideoPlaybackUiState.Success) cachedSuccess = uiState
    }
    val success = when {
        uiState is VideoPlaybackUiState.Success -> uiState
        uiState is VideoPlaybackUiState.Loading -> cachedSuccess
        else -> null
    }
    val showEntryCoverPlaceholder = shouldShowPortraitEntryCoverPlaceholder(
        showPortraitFullscreen = showPortraitFullscreen,
        hasPlayableSuccess = success != null,
        entryCoverUrl = entryCoverUrl,
    )
    LaunchedEffect(isPortraitFullscreen, showPortraitFullscreen, success, isLandscape) {
        com.android.purebilibili.core.util.Logger.d(
            "VideoDetailScreen",
            "Portrait Mode Check: requested=$isPortraitFullscreen, shown=$showPortraitFullscreen, " +
                "success=${success != null}, isLandscape=$isLandscape",
        )
    }
    if (showEntryCoverPlaceholder) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = entryCoverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
    // Enter: optional fade (disabled for direct-portrait morph so card shell is the only motion).
    // Exit: always soft scale/slide so manual leave still feels polished.
    val portraitExitTransition = remember(motionSpec) {
        val exitSpec = tween<Float>(
            durationMillis = motionSpec.exitDurationMillis,
            easing = AppMotionEasing.EmphasizedExit,
        )
        fadeOut(exitSpec) + scaleOut(
            targetScale = motionSpec.exitScaleTarget,
            animationSpec = exitSpec,
            transformOrigin = TransformOrigin(0.5f, 0f),
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = motionSpec.exitDurationMillis,
                easing = AppMotionEasing.EmphasizedExit,
            ),
            targetOffsetY = { -(it * motionSpec.exitTranslateUpFraction).roundToInt() },
        )
    }
    AnimatedVisibility(
        visible = showPortraitFullscreen && success != null,
        enter = if (shouldAnimatePortraitPager) {
            fadeIn(tween(motionSpec.enterDurationMillis, easing = AppMotionEasing.EmphasizedEnter))
        } else {
            EnterTransition.None
        },
        exit = portraitExitTransition,
        modifier = Modifier.fillMaxSize(),
    ) {
        success?.let { playableState ->
            PortraitVideoPager(
                initialBvid = initialBvidOverride ?: playableState.info.bvid,
                initialInfo = playableState.info,
                recommendations = playableState.related,
                onBack = onBack,
                onHomeClick = onHomeClick,
                onVideoChange = onVideoChange,
                viewModel = playbackViewModel,
                engagementViewModel = engagementViewModel,
                sharedPlayer = sharedPlayer,
                initialStartPositionMs = initialStartPositionMs,
                onProgressUpdate = onProgressUpdate,
                onExitSnapshot = onExitSnapshot,
                onSearchClick = onSearchClick,
                onUserClick = onUserClick,
                onRotateToLandscape = onRotateToLandscape,
            )
        }
    }
}

internal fun shouldShowPortraitEntryCoverPlaceholder(
    showPortraitFullscreen: Boolean,
    hasPlayableSuccess: Boolean,
    entryCoverUrl: String,
): Boolean {
    return showPortraitFullscreen && !hasPlayableSuccess && entryCoverUrl.isNotBlank()
}
