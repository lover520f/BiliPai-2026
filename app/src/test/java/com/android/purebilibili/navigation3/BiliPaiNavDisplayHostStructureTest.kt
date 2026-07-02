package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiNavDisplayHostStructureTest {

    @Test
    fun navDisplayHostOwnsNavigation3RenderingAndSharedTransitionScope() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("NavDisplay("))
        assertTrue(source.contains("entryProvider"))
        assertTrue(source.contains("LocalNavAnimatedContentScope.current"))
        assertTrue(source.contains("ProvideAnimatedVisibilityScope("))
        assertTrue(source.contains("LocalVideoCardSharedElementSourceRoute provides key.toLegacyRoute()"))
        assertTrue(source.contains("sharedTransitionScope = sharedTransitionScope"))
        assertTrue(source.contains("VideoSharedTransitionBackdropHost("))
        assertTrue(source.contains("VideoCardContainerTransformOverlay("))
        assertTrue(source.contains("videoCardTransitionController"))
        assertTrue(source.contains("LocalVideoCardTransitionSession"))
        assertTrue(source.contains("predictivePopTransitionSpec"))
    }

    @Test
    fun navDisplayHostScopesEntryStateWithLifecycleNavigation3Decorator() {
        val source = navDisplayHostSource()
        val buildFile = buildFileSource()

        assertTrue(buildFile.contains("androidx.lifecycle:lifecycle-viewmodel-navigation3:"))
        // 上游 navigationevent-compose 被 Gradle exclude 掉，转而使用项目内 vendored 源码
        // (app/src/main/java/androidx/navigationevent/compose/)，以便在 onBackCompleted 回调
        // 内对 transitionState 提交时序做精确控制。
        assertTrue(buildFile.contains("exclude(group = \"androidx.navigationevent\", module = \"navigationevent-compose\")"))
        assertFalse(buildFile.contains("androidx.navigationevent:navigationevent-compose:"))
        assertFalse(buildFile.contains("androidx.navigationevent:navigationevent-compose"))
        assertTrue(source.contains("rememberDecoratedNavEntries("))
        assertTrue(source.contains("rememberSceneState("))
        assertTrue(source.contains("rememberSaveableStateHolderNavEntryDecorator"))
        assertTrue(source.contains("rememberViewModelStoreNavEntryDecorator"))
    }

    @Test
    fun navDisplayHostHoistsNavigationEventStateIntoNavDisplay() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("rememberNavigationEventState("))
        assertTrue(source.contains("NavigationBackHandler("))
        assertTrue(source.contains("onBackCompleted = performBack"))
        assertTrue(source.contains("onBackCancelled"))
        assertTrue(source.contains("navigationEventState = navigationEventState"))
        assertTrue(source.contains("sceneState = sceneState"))
        kotlin.test.assertFalse(source.contains("NavDisplay(\n        backStack = safeBackStack"))
    }

    @Test
    fun navDisplayHostPreservesApplicationExtrasForEntryViewModels() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("ProvideNavigation3ViewModelApplicationExtras("))
        assertTrue(source.contains("LocalViewModelStoreOwner provides patchedOwner"))
        assertTrue(source.contains("APPLICATION_KEY"))
    }

    @Test
    fun navDisplayHostDoesNotRegisterClassicBackInterceptor() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("NavDisplay("))
        assertTrue(source.contains("onBack = { performBack { } }"))
        assertTrue(source.contains("onBack()"))
        assertFalse(source.contains("import androidx.activity.compose.BackHandler"))
        assertFalse(source.contains("BackHandler(enabled"))
    }

    @Test
    fun navDisplayHostIntegratesPredictiveBackHandlerDecorator() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("resolveBiliPaiPredictiveBackAnimationHandler"))
        assertTrue(source.contains("predictiveBackAnimationDecorator"))
        assertTrue(source.contains("NavEntryDecorator("))
        assertTrue(source.contains("onPredictivePopTransitionSpec"))
        assertFalse(source.contains("LocalVideo" + "PredictiveReturnState"))
        assertFalse(source.contains("onPredictiveBackGestureChange"))
    }

    @Test
    fun navDisplayHostRoutesPredictivePopThroughHandlerPolicy() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("val popRouteTransition = remember("))
        assertTrue(source.contains("resolveBiliPaiPredictiveBackAnimationHandler"))
        assertFalse(source.contains("resolveBiliPaiNavPopContentTransform(popRouteTransition)"))
    }

    private fun navDisplayHostSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"),
            File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt")
        ).first { it.exists() }.readText()
    }

    private fun buildFileSource(): String {
        return listOf(
            File("app/build.gradle.kts"),
            File("build.gradle.kts")
        ).first { it.exists() }.readText()
    }
}
