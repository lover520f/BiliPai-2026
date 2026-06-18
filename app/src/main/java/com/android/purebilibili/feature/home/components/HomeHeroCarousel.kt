package com.android.purebilibili.feature.home.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.feature.home.HomeHeroCarouselCardTransform
import com.android.purebilibili.feature.home.HOME_HERO_CAROUSEL_SIDE_PEEK_DP
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselAspectRatio
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselCardTransform
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselItemKey
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselItemOrNull
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselPreviewAlpha
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselWidthDp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeHeroCarousel(
    videos: List<VideoItem>,
    autoplayEnabled: Boolean,
    onVideoClick: (VideoItem) -> Unit,
    onGetPreviewUrl: suspend (String, Long) -> String?,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    val pagerState = rememberPagerState { videos.size }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val sidePeek = HOME_HERO_CAROUSEL_SIDE_PEEK_DP.dp
        val carouselWidth = resolveHomeHeroCarouselWidthDp(maxWidth.value).dp
        val pageWidth = (carouselWidth - sidePeek * 2).coerceAtLeast(0.dp)
        val aspectRatio = resolveHomeHeroCarouselAspectRatio(carouselWidth.value)
        HorizontalPager(
            state = pagerState,
            key = { page ->
                resolveHomeHeroCarouselItemKey(videos, page, VideoItem::bvid)
            },
            pageSize = PageSize.Fixed(pageWidth),
            pageSpacing = 0.dp,
            contentPadding = PaddingValues(horizontal = sidePeek),
            modifier = Modifier
                .width(carouselWidth)
                .align(Alignment.Center)
        ) { page ->
            val video = resolveHomeHeroCarouselItemOrNull(videos, page)
                ?: return@HorizontalPager
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).coerceIn(-1f, 1f)
            val transform = resolveHomeHeroCarouselCardTransform(pageOffset)
            val activeForPlayback = autoplayEnabled &&
                pagerState.currentPage == page &&
                pageOffset.absoluteValue < 0.12f
            HomeHeroCarouselCard(
                video = video,
                transform = transform,
                activeForPlayback = activeForPlayback,
                aspectRatio = aspectRatio,
                onVideoClick = { onVideoClick(video) },
                onGetPreviewUrl = onGetPreviewUrl
            )
        }

        Row(
            modifier = Modifier
                .width(carouselWidth)
                .align(Alignment.BottomCenter)
                .padding(start = 28.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            videos.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 11.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.46f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeHeroCarouselCard(
    video: VideoItem,
    transform: HomeHeroCarouselCardTransform,
    activeForPlayback: Boolean,
    aspectRatio: Float,
    onVideoClick: () -> Unit,
    onGetPreviewUrl: suspend (String, Long) -> String?
) {
    var previewUrl by remember(video.bvid, video.cid) { mutableStateOf<String?>(null) }
    LaunchedEffect(activeForPlayback, video.bvid, video.cid) {
        if (activeForPlayback && previewUrl == null && video.bvid.isNotBlank() && video.cid > 0L) {
            previewUrl = onGetPreviewUrl(video.bvid, video.cid)
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = (transform.shadowElevationFraction * 10f).dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .zIndex(transform.zIndex)
            .graphicsLayer {
                transformOrigin = TransformOrigin(transform.pivotFractionX, 0.5f)
                translationX = transform.translationXFraction * size.width
                scaleX = transform.scale
                scaleY = transform.scale
                alpha = transform.alpha
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onVideoClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val normalizedCoverUrl = remember(video.pic) { FormatUtils.fixImageUrl(video.pic) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = transform.contentParallaxFraction * size.width
                        scaleX = transform.contentScale
                        scaleY = transform.contentScale
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(normalizedCoverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (activeForPlayback && previewUrl != null) {
                    MutedHeroVideoPlayer(url = previewUrl.orEmpty())
                }
            }
            if (transform.edgeShadeAlpha > 0.001f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (transform.edgeShadeStartFromLeft) {
                                Brush.horizontalGradient(
                                    0f to Color.Black.copy(alpha = transform.edgeShadeAlpha),
                                    0.48f to Color.Transparent,
                                    1f to Color.Transparent
                                )
                            } else {
                                Brush.horizontalGradient(
                                    0f to Color.Transparent,
                                    0.52f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = transform.edgeShadeAlpha)
                                )
                            }
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.54f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.76f)
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, end = 28.dp, bottom = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeForPlayback) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = video.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MutedHeroVideoPlayer(url: String) {
    val context = LocalContext.current
    var hasRenderedFirstFrame by remember(url) { mutableStateOf(false) }
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }
    LaunchedEffect(url) {
        hasRenderedFirstFrame = false
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player.prepare()
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                hasRenderedFirstFrame = true
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = resolveHomeHeroCarouselPreviewAlpha(hasRenderedFirstFrame)
            }
    )
}
