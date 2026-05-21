package com.android.purebilibili.feature.video.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoDetailScreenMethodSizeStructureTest {

    @Test
    fun videoDetailScreenDelegatesLargeDialogsAndMenusToChildComposables() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreen.kt")
        val videoDetailBody = source
            .substringAfter("fun VideoDetailScreen(")
            .substringBefore("@OptIn(ExperimentalHazeMaterialsApi::class)")

        assertTrue(videoDetailBody.contains("VideoDetailFollowGroupDialog(viewModel = viewModel)"))
        assertTrue(videoDetailBody.contains("VideoDetailPlaybackEndedDialog("))
        assertTrue(videoDetailBody.contains("VideoDetailQualitySwitchFailureDialog("))
        assertTrue(videoDetailBody.contains("VideoDetailDanmakuContextMenu("))
        assertTrue(source.contains("private fun VideoDetailFollowGroupDialog("))
        assertTrue(source.contains("private fun VideoDetailPlaybackEndedDialog("))
        assertTrue(source.contains("private fun VideoDetailQualitySwitchFailureDialog("))
        assertTrue(source.contains("private fun VideoDetailDanmakuContextMenu("))
    }

    @Test
    fun videoDetailPlayerContainerUsesHomeSharedTransitionPolicy() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreen.kt")
        val playerContainerSource = source
            .substringAfter("val playerContainerModifier = if (")
            .substringBefore("//  播放器容器包含状态栏高度")

        assertTrue(source.contains("resolveHomeVideoSharedTransitionMotionSpec("))
        assertTrue(source.contains("resolveHomeVideoSharedTransitionCornerSpec("))
        assertTrue(playerContainerSource.contains("homeSharedTransitionMotionSpec.enabled"))
        assertTrue(playerContainerSource.contains("durationMillis = homeSharedTransitionMotionSpec.durationMillis"))
        assertTrue(playerContainerSource.contains("homeSharedTransitionCornerSpec.endCornerDp.dp"))
    }

    @Test
    fun videoDetailShellDoesNotAddEnterSettleRebound() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreen.kt")

        assertFalse(source.contains("resolveVideoDetailEnterSettleSpec("))
        assertFalse(source.contains(".videoDetailEnterSettle("))
        assertTrue(source.contains("resolveVideoDetailContentRevealMotion("))
    }

    private fun loadSource(path: String): String {
        val candidates = listOf(
            File(path),
            File("app", path.removePrefix("app/")),
            File(path.removePrefix("app/")),
            File("..", path)
        )
        return candidates.first { it.exists() }.readText()
    }
}
