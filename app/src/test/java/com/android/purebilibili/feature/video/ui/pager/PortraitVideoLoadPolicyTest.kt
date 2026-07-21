package com.android.purebilibili.feature.video.ui.pager

import com.android.purebilibili.data.model.response.Dash
import com.android.purebilibili.data.model.response.DashAudio
import com.android.purebilibili.data.model.response.DashVideo
import com.android.purebilibili.data.model.response.Durl
import com.android.purebilibili.data.model.response.PlayUrlData
import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.ViewInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortraitVideoLoadPolicyTest {

    @Test
    fun playbackTargetQuality_prefersUserSettingOverFallback() {
        assertEquals(64, resolvePortraitPlaybackTargetQuality())
        assertEquals(80, resolvePortraitPlaybackTargetQuality(preferredQuality = 80))
        assertEquals(125, resolvePortraitPlaybackTargetQuality(preferredQuality = 125))
        assertEquals(127, resolvePortraitPlaybackTargetQuality(preferredQuality = 127))
        assertEquals(64, resolvePortraitPlaybackTargetQuality(preferredQuality = 0))
        assertEquals(64, resolvePortraitPlaybackTargetQuality(preferredQuality = -1))
    }

    @Test
    fun qualityLabel_coversPremiumAndAutoTiers() {
        assertEquals("自动", resolvePortraitQualityLabel(127))
        assertEquals("HDR", resolvePortraitQualityLabel(125))
        assertEquals("4K", resolvePortraitQualityLabel(120))
        assertEquals("1080P", resolvePortraitQualityLabel(80))
        assertEquals("720P", resolvePortraitQualityLabel(64))
    }

    @Test
    fun availableQualityIds_unionsDashAndAcceptSortedDesc() {
        assertEquals(
            listOf(125, 120, 80, 64, 32),
            resolvePortraitAvailableQualityIds(
                acceptQualities = listOf(125, 120, 80, 64, 32),
                dashVideoIds = listOf(80, 64)
            )
        )
    }

    @Test
    fun displayedQualityId_prefersExactRequestedDashTrack() {
        assertEquals(
            80,
            resolvePortraitDisplayedQualityId(
                requestedQuality = 80,
                returnedQuality = 64,
                dashVideoIds = listOf(80, 64)
            )
        )
        assertEquals(
            64,
            resolvePortraitDisplayedQualityId(
                requestedQuality = 125,
                returnedQuality = 64,
                dashVideoIds = listOf(64, 32)
            )
        )
    }

    @Test
    fun parallelBootstrap_enablesWhenFeedProvidesCid() {
        assertTrue(
            shouldUsePortraitParallelPlaybackBootstrap(
                bvid = "BV1test",
                requestedCid = 12345L
            )
        )
    }

    @Test
    fun parallelBootstrap_disablesWhenCidMissing() {
        assertFalse(
            shouldUsePortraitParallelPlaybackBootstrap(
                bvid = "BV1test",
                requestedCid = 0L
            )
        )
    }

    @Test
    fun parallelBootstrap_disablesForAidFallbackIdentifier() {
        assertFalse(
            shouldUsePortraitParallelPlaybackBootstrap(
                bvid = "av12345",
                requestedCid = 67890L
            )
        )
    }

    @Test
    fun pagePlaybackIdentity_readsCidFromRelatedVideo() {
        val identity = resolvePortraitPagePlaybackIdentity(
            RelatedVideo(
                bvid = "BV1related",
                aid = 42L,
                cid = 99L
            )
        )

        assertEquals("BV1related", identity?.bvid)
        assertEquals(42L, identity?.aid)
        assertEquals(99L, identity?.cid)
    }

    @Test
    fun pagePlaybackIdentity_readsCidFromViewInfo() {
        val identity = resolvePortraitPagePlaybackIdentity(
            ViewInfo(
                bvid = "BV1view",
                aid = 7L,
                cid = 8L
            )
        )

        assertEquals("BV1view", identity?.bvid)
        assertEquals(7L, identity?.aid)
        assertEquals(8L, identity?.cid)
    }

    @Test
    fun playbackStreamUrls_prefersBestDashTrackOverFirstTrack() {
        val playData = PlayUrlData(
            dash = Dash(
                video = listOf(
                    DashVideo(
                        id = 64,
                        baseUrl = "https://cdn.example/64.m4s",
                        codecs = "avc1.64001E"
                    ),
                    DashVideo(
                        id = 80,
                        baseUrl = "https://cdn.example/80.m4s",
                        codecs = "hev1.1.6.L120.90"
                    )
                ),
                audio = listOf(
                    DashAudio(
                        id = 30232,
                        baseUrl = "https://cdn.example/audio.m4s"
                    )
                )
            )
        )

        val urls = resolvePortraitPlaybackStreamUrls(
            playData = playData,
            targetQuality = 64,
            isHevcSupported = true,
            isAv1Supported = false
        )

        assertEquals("https://cdn.example/64.m4s", urls?.videoUrl)
        assertEquals("https://cdn.example/audio.m4s", urls?.audioUrl)
    }

    @Test
    fun playbackStreamUrls_honorsPreferredHighQualityTrack() {
        val playData = PlayUrlData(
            dash = Dash(
                video = listOf(
                    DashVideo(
                        id = 64,
                        baseUrl = "https://cdn.example/64.m4s",
                        codecs = "avc1.64001E"
                    ),
                    DashVideo(
                        id = 80,
                        baseUrl = "https://cdn.example/80.m4s",
                        codecs = "hev1.1.6.L120.90"
                    )
                ),
                audio = listOf(
                    DashAudio(
                        id = 30232,
                        baseUrl = "https://cdn.example/audio.m4s"
                    )
                )
            )
        )

        val urls = resolvePortraitPlaybackStreamUrls(
            playData = playData,
            targetQuality = 80,
            isHevcSupported = true,
            isAv1Supported = false
        )

        assertEquals("https://cdn.example/80.m4s", urls?.videoUrl)
    }

    @Test
    fun playbackStreamUrls_fallsBackToProgressiveUrl() {
        val urls = resolvePortraitPlaybackStreamUrls(
            playData = PlayUrlData(
                durl = listOf(Durl(url = "https://cdn.example/progressive.mp4"))
            )
        )

        assertEquals("https://cdn.example/progressive.mp4", urls?.videoUrl)
        assertNull(urls?.audioUrl)
    }

    @Test
    fun playUrlPreloadCount_defaultsToOneOnWifiWithoutExperimentalFlag() {
        assertEquals(
            1,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = false,
                isWifi = true,
                availableTargets = 3
            )
        )
    }

    @Test
    fun playUrlPreloadCount_expandsToTwoWhenExperimentalFlagEnabled() {
        assertEquals(
            2,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = true,
                isWifi = true,
                availableTargets = 5
            )
        )
    }

    @Test
    fun playUrlPreloadCount_skipsCellularAndEmptyTargets() {
        assertEquals(
            0,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = true,
                isWifi = false,
                availableTargets = 3
            )
        )
        assertEquals(
            0,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = true,
                isWifi = true,
                availableTargets = 0
            )
        )
    }

    @Test
    fun swipePrefetchTargetPage_triggersWhenSwipingDownPastThreshold() {
        assertEquals(
            2,
            resolvePortraitSwipePrefetchTargetPage(
                isScrollInProgress = true,
                currentPage = 1,
                currentPageOffsetFraction = -0.3f,
                lastPageIndex = 4
            )
        )
    }

    @Test
    fun swipePrefetchTargetPage_triggersWhenSwipingUpPastThreshold() {
        assertEquals(
            0,
            resolvePortraitSwipePrefetchTargetPage(
                isScrollInProgress = true,
                currentPage = 1,
                currentPageOffsetFraction = 0.3f,
                lastPageIndex = 4
            )
        )
    }

    @Test
    fun swipePrefetchTargetPage_ignoresSmallOffsetAndSettledPager() {
        assertNull(
            resolvePortraitSwipePrefetchTargetPage(
                isScrollInProgress = true,
                currentPage = 1,
                currentPageOffsetFraction = -0.1f,
                lastPageIndex = 4
            )
        )
        assertNull(
            resolvePortraitSwipePrefetchTargetPage(
                isScrollInProgress = false,
                currentPage = 1,
                currentPageOffsetFraction = -0.8f,
                lastPageIndex = 4
            )
        )
    }

    @Test
    fun earlyPlaybackPage_requiresHigherOffsetThanPrefetch() {
        assertNull(
            resolvePortraitEarlyPlaybackPage(
                isScrollInProgress = true,
                currentPage = 1,
                currentPageOffsetFraction = -0.3f,
                lastPageIndex = 4
            )
        )
        assertEquals(
            2,
            resolvePortraitEarlyPlaybackPage(
                isScrollInProgress = true,
                currentPage = 1,
                currentPageOffsetFraction = -0.6f,
                lastPageIndex = 4
            )
        )
    }

    @Test
    fun playUrlPreloadTargets_collectsUpcomingPagesWithoutDuplicates() {
        val targets = resolvePortraitPlayUrlPreloadTargets(
            committedPage = 0,
            pageItems = listOf(
                RelatedVideo(bvid = "BV1", cid = 1L),
                RelatedVideo(bvid = "BV2", cid = 2L),
                RelatedVideo(bvid = "BV2", cid = 2L),
                RelatedVideo(bvid = "BV3", cid = 3L)
            ),
            preloadCount = 2
        )

        assertEquals(2, targets.size)
        assertEquals("BV2", targets[0].bvid)
        assertEquals(2L, targets[0].cid)
        assertEquals("BV3", targets[1].bvid)
    }
}
