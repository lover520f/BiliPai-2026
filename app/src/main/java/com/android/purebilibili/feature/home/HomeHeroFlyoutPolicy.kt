package com.android.purebilibili.feature.home

private const val HOME_HERO_FLYOUT_DURATION_MILLIS = 180
private const val HOME_HERO_FLYOUT_TRANSLATION_Y_DP = -56f
private const val HOME_HERO_FLYOUT_END_SCALE = 1.08f

internal data class HomeHeroFlyoutFrame(
    val alpha: Float,
    val scale: Float,
    val translationYDp: Float
)

internal fun shouldRunHomeHeroFlyoutBeforeNavigation(
    request: HomeVideoClickRequest
): Boolean {
    val bvid = request.bvid.trim()
    if (request.source != HomeVideoClickSource.GRID) return false
    if (bvid.isEmpty()) return false

    val isDynamicPlaceholder = request.dynamicId.isNotBlank() &&
        !bvid.startsWith("BV", ignoreCase = true)
    return !isDynamicPlaceholder
}

internal fun resolveHomeHeroFlyoutDurationMillis(): Int {
    return HOME_HERO_FLYOUT_DURATION_MILLIS
}

internal fun resolveHomeHeroFlyoutNavigationDelayMillis(): Long {
    return HOME_HERO_FLYOUT_DURATION_MILLIS.toLong()
}

internal fun resolveHomeHeroFlyoutFrame(progress: Float): HomeHeroFlyoutFrame {
    val normalizedProgress = progress.coerceIn(0f, 1f)
    return HomeHeroFlyoutFrame(
        alpha = 1f - normalizedProgress,
        scale = 1f + (HOME_HERO_FLYOUT_END_SCALE - 1f) * normalizedProgress,
        translationYDp = HOME_HERO_FLYOUT_TRANSLATION_Y_DP * normalizedProgress
    )
}
