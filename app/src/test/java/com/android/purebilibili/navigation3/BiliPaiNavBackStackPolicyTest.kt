package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation.ScreenRoutes
import kotlin.test.Test
import kotlin.test.assertEquals

class BiliPaiNavBackStackPolicyTest {

    @Test
    fun initialBackStack_usesOnboardingWhenRequired() {
        assertEquals(
            listOf(BiliPaiNavKey.Unknown("onboarding")),
            resolveInitialBiliPaiBackStack(
                firstRoute = ScreenRoutes.Home.route,
                onboardingRequired = true
            )
        )
    }

    @Test
    fun initialBackStack_usesMainHostForMainApp() {
        assertEquals(
            listOf(BiliPaiNavKey.MainHost),
            resolveInitialBiliPaiBackStack(
                firstRoute = ScreenRoutes.Profile.route,
                onboardingRequired = false
            )
        )
    }

    @Test
    fun push_skipsDuplicateTopEntry() {
        val stack = listOf(BiliPaiNavKey.MainHost)

        assertEquals(stack, pushBiliPaiNavKey(stack, BiliPaiNavKey.MainHost))
    }

    @Test
    fun pop_keepsRootEntry() {
        assertEquals(
            listOf(BiliPaiNavKey.MainHost),
            popBiliPaiNavKey(listOf(BiliPaiNavKey.MainHost))
        )
        assertEquals(
            listOf(BiliPaiNavKey.MainHost),
            popBiliPaiNavKey(listOf(BiliPaiNavKey.MainHost, BiliPaiNavKey.VideoDetail("BV1")))
        )
    }
}
