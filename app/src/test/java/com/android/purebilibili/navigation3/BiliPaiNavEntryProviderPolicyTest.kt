package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation.ScreenRoutes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BiliPaiNavEntryProviderPolicyTest {

    @Test
    fun sharedReadyMetadataDisablesRouteLayerForReturnTarget() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = BiliPaiNavKey.Home,
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "home:BV1",
                sourceRoute = "home",
                clickedBoundsRecorded = true,
                cardFullyVisible = true
            )
        )

        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.pop)
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.predictivePop)
    }

    @Test
    fun homeVideoPushUsesNoOpRouteLayerWithRecordedBounds() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "home:BV1",
                sourceRoute = "home",
                clickedBoundsRecorded = true,
                cardFullyVisible = true
            )
        )

        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.pop)
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.predictivePop)
    }

    @Test
    fun homeVideoPushWithInvisibleSourceKeepsFallbackRouteLayer() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "home:BV1",
                sourceRoute = "home",
                clickedBoundsRecorded = true,
                cardFullyVisible = false
            )
        )

        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.pop)
        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.predictivePop)
    }

    @Test
    fun bottomTabForwardNavigationKeepsFallbackBecausePagerOwnsTabMotion() {
        val visibleRoutes = setOf(
            ScreenRoutes.Home.route,
            ScreenRoutes.Dynamic.route,
            ScreenRoutes.History.route,
            ScreenRoutes.Profile.route
        )

        assertEquals(
            BiliPaiNavRouteTransition.FALLBACK,
            resolveBiliPaiNavEntryForwardRouteTransition(
                defaultTransition = BiliPaiNavRouteTransition.FALLBACK,
                fromRoute = ScreenRoutes.Home.route,
                toRoute = ScreenRoutes.Profile.route,
                visibleBottomBarRoutes = visibleRoutes
            )
        )
        assertEquals(
            BiliPaiNavRouteTransition.FALLBACK,
            resolveBiliPaiNavEntryForwardRouteTransition(
                defaultTransition = BiliPaiNavRouteTransition.FALLBACK,
                fromRoute = ScreenRoutes.Search.route,
                toRoute = ScreenRoutes.Profile.route,
                visibleBottomBarRoutes = visibleRoutes
            )
        )
    }

    @Test
    fun homeVideoPushWithoutRecordedBoundsKeepsForwardFallback() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "home:BV1",
                sourceRoute = "home",
                clickedBoundsRecorded = false,
                cardFullyVisible = true
            )
        )

        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.pop)
        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.predictivePop)
    }

    @Test
    fun videoPushWithStaleSharedSourceKeepsForwardFallback() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = BiliPaiNavKey.VideoDetail(bvid = "BV2", sourceRoute = "home"),
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "home:BV1",
                sourceRoute = "home",
                clickedBoundsRecorded = true,
                cardFullyVisible = true
            )
        )

        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.pop)
        assertEquals(BiliPaiNavRouteTransition.FALLBACK, transitions.predictivePop)
    }

    @Test
    fun nonHomeVideoPushUsesNoOpRouteLayerWithMatchingVisibleSourceCard() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "history"),
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "history:BV1",
                sourceRoute = "history",
                clickedBoundsRecorded = true,
                cardFullyVisible = true
            )
        )

        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.pop)
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.predictivePop)
    }

    @Test
    fun providerUsesTypedVideoEntryContentKey() {
        val provider = biliPaiNavEntryProvider(
            sourceMetadata = BiliPaiNavSourceMetadata(),
            content = {}
        )
        val key = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "search")
        val entry = provider(key)

        assertEquals(key.toString(), entry.contentKey)
        assertTrue(entry.metadata.isNotEmpty())
    }
}
