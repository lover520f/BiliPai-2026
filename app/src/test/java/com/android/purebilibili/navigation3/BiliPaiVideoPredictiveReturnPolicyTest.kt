package com.android.purebilibili.navigation3

import androidx.compose.ui.geometry.Rect
import com.android.purebilibili.core.store.PredictiveBackAnimationStyle
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiVideoPredictiveReturnPolicyTest {

    @Test
    fun homeVideoWithSharedSourceAndPredictiveGesture_enablesCardRecycle() {
        assertTrue(
            shouldEnableVideoPredictiveReturnToCard(
                currentKey = BiliPaiNavKey.VideoDetail("BV1", sourceRoute = "home"),
                previousKey = BiliPaiNavKey.Home,
                predictiveBackAnimationStyle = PredictiveBackAnimationStyle.AOSP,
                sourceMetadata = BiliPaiNavSourceMetadata(
                    sourceKey = "home:BV1",
                    sourceRoute = "home",
                    clickedBoundsRecorded = true,
                    cardFullyVisible = true
                ),
                sourceBounds = Rect(24f, 360f, 360f, 610f)
            )
        )
    }

    @Test
    fun staleOrNonHomeSource_disablesCardRecycle() {
        val sourceBounds = Rect(24f, 360f, 360f, 610f)

        assertFalse(
            shouldEnableVideoPredictiveReturnToCard(
                currentKey = BiliPaiNavKey.VideoDetail("BV2", sourceRoute = "home"),
                previousKey = BiliPaiNavKey.Home,
                predictiveBackAnimationStyle = PredictiveBackAnimationStyle.AOSP,
                sourceMetadata = BiliPaiNavSourceMetadata(
                    sourceKey = "home:BV1",
                    sourceRoute = "home",
                    clickedBoundsRecorded = true,
                    cardFullyVisible = true
                ),
                sourceBounds = sourceBounds
            )
        )
        assertFalse(
            shouldEnableVideoPredictiveReturnToCard(
                currentKey = BiliPaiNavKey.VideoDetail("BV1", sourceRoute = "history"),
                previousKey = BiliPaiNavKey.History,
                predictiveBackAnimationStyle = PredictiveBackAnimationStyle.AOSP,
                sourceMetadata = BiliPaiNavSourceMetadata(
                    sourceKey = "history:BV1",
                    sourceRoute = "history",
                    clickedBoundsRecorded = true,
                    cardFullyVisible = true
                ),
                sourceBounds = sourceBounds
            )
        )
    }

    @Test
    fun noPredictiveStyle_disablesCardRecycle() {
        val sourceMetadata = BiliPaiNavSourceMetadata(
            sourceKey = "home:BV1",
            sourceRoute = "home",
            clickedBoundsRecorded = true,
            cardFullyVisible = true
        )
        val sourceBounds = Rect(24f, 360f, 360f, 610f)

        assertFalse(
            shouldEnableVideoPredictiveReturnToCard(
                currentKey = BiliPaiNavKey.VideoDetail("BV1", sourceRoute = "home"),
                previousKey = BiliPaiNavKey.Home,
                predictiveBackAnimationStyle = PredictiveBackAnimationStyle.NONE,
                sourceMetadata = sourceMetadata,
                sourceBounds = sourceBounds
            )
        )
    }

    @Test
    fun disabledSharedElementTransitionStillAllowsPredictiveCardRecycle() {
        val sourceMetadata = BiliPaiNavSourceMetadata(
            sourceKey = "home:BV1",
            sourceRoute = "home",
            clickedBoundsRecorded = true,
            cardFullyVisible = true
        )

        assertTrue(
            shouldEnableVideoPredictiveReturnToCard(
                currentKey = BiliPaiNavKey.VideoDetail("BV1", sourceRoute = "home"),
                previousKey = BiliPaiNavKey.Home,
                predictiveBackAnimationStyle = PredictiveBackAnimationStyle.AOSP,
                sourceMetadata = sourceMetadata,
                sourceBounds = Rect(24f, 360f, 360f, 610f)
            )
        )
    }
}
