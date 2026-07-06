package com.android.purebilibili.feature.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchMotionBudgetPolicyTest {

    @Test
    fun activeSearchInteraction_reducesBudget() {
        assertEquals(
            SearchMotionBudget.REDUCED,
            resolveSearchMotionBudget(
                hasQuery = true,
                isSearching = true,
                isScrolling = false
            )
        )
        assertEquals(
            SearchMotionBudget.REDUCED,
            resolveSearchMotionBudget(
                hasQuery = true,
                isSearching = false,
                isScrolling = true
            )
        )
    }

    @Test
    fun scrollingResults_keepsHazeEnabled() {
        val budget = resolveSearchMotionBudget(
            hasQuery = true,
            isSearching = false,
            isScrolling = true
        )

        assertEquals(SearchMotionBudget.REDUCED, budget)
        assertTrue(
            shouldEnableSearchHazeSource(
                isSearching = false
            )
        )
    }

    @Test
    fun idleSearchState_keepsFullBudgetAndHaze() {
        val budget = resolveSearchMotionBudget(
            hasQuery = false,
            isSearching = false,
            isScrolling = false
        )

        assertEquals(SearchMotionBudget.FULL, budget)
        assertTrue(
            shouldEnableSearchHazeSource(
                isSearching = false,
                startupSettled = true
            )
        )
    }

    @Test
    fun activeSearchRequest_disablesHazeSource() {
        assertFalse(
            shouldEnableSearchHazeSource(
                isSearching = true,
                startupSettled = true
            )
        )
    }

    @Test
    fun startupPending_forcesReducedMotionAndDisablesHaze() {
        assertEquals(
            SearchMotionBudget.REDUCED,
            resolveEffectiveSearchMotionBudget(
                startupSettled = false,
                baseBudget = SearchMotionBudget.FULL
            )
        )
        assertFalse(
            shouldEnableSearchHazeSource(
                isSearching = false,
                startupSettled = false
            )
        )
    }

    @Test
    fun landingBootstrap_onlyRunsAfterStartupSettlesOnEmptyLandingState() {
        assertFalse(
            shouldBootstrapSearchLandingData(
                startupSettled = false,
                showResults = false,
                query = ""
            )
        )
        assertFalse(
            shouldBootstrapSearchLandingData(
                startupSettled = true,
                showResults = true,
                query = ""
            )
        )
        assertFalse(
            shouldBootstrapSearchLandingData(
                startupSettled = true,
                showResults = false,
                query = "test"
            )
        )
        assertTrue(
            shouldBootstrapSearchLandingData(
                startupSettled = true,
                showResults = false,
                query = ""
            )
        )
    }

    @Test
    fun autoFocus_waitsUntilStartupSettles() {
        assertFalse(
            shouldAutoFocusSearchField(
                startupSettled = false,
                query = ""
            )
        )
        assertFalse(
            shouldAutoFocusSearchField(
                startupSettled = true,
                query = "abc"
            )
        )
        assertTrue(
            shouldAutoFocusSearchField(
                startupSettled = true,
                query = ""
            )
        )
    }

    @Test
    fun scrollingResults_shouldNotForceLowHeaderBlurBudget() {
        assertFalse(
            shouldForceLowBudgetSearchHeaderBlur(
                isSearching = false,
                isScrollingResults = true
            )
        )
    }

    @Test
    fun activeSearchRequest_shouldForceLowHeaderBlurBudget() {
        assertTrue(
            shouldForceLowBudgetSearchHeaderBlur(
                isSearching = true,
                isScrollingResults = false
            )
        )
    }

    @Test
    fun searchCardTransition_disabledWhenGloballyDisabled() {
        assertFalse(
            resolveEffectiveSearchCardTransitionEnabled(
                cardTransitionEnabled = false,
                motionBudget = SearchMotionBudget.FULL,
                isReturningFromVideoDetail = true
            )
        )
    }

    @Test
    fun searchCardTransition_followsBudgetWhenNotReturning() {
        assertTrue(
            resolveEffectiveSearchCardTransitionEnabled(
                cardTransitionEnabled = true,
                motionBudget = SearchMotionBudget.FULL,
                isReturningFromVideoDetail = false
            )
        )
        assertFalse(
            resolveEffectiveSearchCardTransitionEnabled(
                cardTransitionEnabled = true,
                motionBudget = SearchMotionBudget.REDUCED,
                isReturningFromVideoDetail = false
            )
        )
    }

    @Test
    fun searchCardTransition_forcedOnDuringReturnEvenWhenBudgetReduced() {
        // 返回视频详情期间强制保持共享元素，避免列表 settle 把 budget 降为 REDUCED 后来源卡片卸载 sharedBounds。
        assertTrue(
            resolveEffectiveSearchCardTransitionEnabled(
                cardTransitionEnabled = true,
                motionBudget = SearchMotionBudget.REDUCED,
                isReturningFromVideoDetail = true
            )
        )
    }

    @Test
    fun newSearchSession_resetsResultScroll() {
        assertTrue(
            shouldResetSearchResultScroll(
                searchSessionId = 3L,
                showResults = true,
                lastResetSessionId = 2L
            )
        )
    }

    @Test
    fun restoredSearchScreen_doesNotResetExistingResultScroll() {
        assertFalse(
            shouldResetSearchResultScroll(
                searchSessionId = 3L,
                showResults = true,
                lastResetSessionId = 3L
            )
        )
    }
}
