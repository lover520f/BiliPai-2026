package com.android.purebilibili.feature.home

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class HomeHeroFlyoutStructureTest {

    @Test
    fun homeScreenDelaysNavigationUntilHeroFlyoutFinishes() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")

        assertTrue(source.contains("pendingHeroFlyoutRequest"))
        assertTrue(source.contains("shouldRunHomeHeroFlyoutBeforeNavigation(request)"))
        assertTrue(source.contains("delay(resolveHomeHeroFlyoutNavigationDelayMillis())"))
        assertTrue(source.contains("onVideoClick(pendingRequest)"))
        assertTrue(source.contains("heroFlyoutBvid = pendingHeroFlyoutRequest?.bvid"))
    }

    @Test
    fun ordinaryHomeVideoCardConsumesHeroFlyoutState() {
        val pageSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeCategoryPage.kt")
        val cardSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt")

        assertTrue(pageSource.contains("heroFlyoutBvid: String? = null"))
        assertTrue(pageSource.contains("heroFlyoutActive = heroFlyoutBvid == video.bvid"))
        assertTrue(cardSource.contains("heroFlyoutActive: Boolean = false"))
        assertTrue(cardSource.contains("resolveHomeHeroFlyoutFrame("))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
