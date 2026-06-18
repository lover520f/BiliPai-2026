package com.android.purebilibili.feature.home

internal const val HOME_HERO_CAROUSEL_MAX_ITEMS = 8
internal const val HOME_HERO_CAROUSEL_SIDE_PEEK_DP = 0f
internal const val HOME_HERO_CAROUSEL_PHONE_ASPECT_RATIO = 16f / 9f
internal const val HOME_HERO_CAROUSEL_TABLET_ASPECT_RATIO = 2f
internal const val HOME_HERO_CAROUSEL_WIDE_ASPECT_RATIO = 21f / 9f
internal const val HOME_HERO_CAROUSEL_TABLET_BREAKPOINT_DP = 600f
internal const val HOME_HERO_CAROUSEL_WIDE_BREAKPOINT_DP = 840f
internal const val HOME_HERO_CAROUSEL_MAX_WIDTH_DP = 840f
private const val HOME_HERO_CAROUSEL_TOP_GAP_REDUCTION_DP = 12f

internal data class HomeHeroCarouselCardTransform(
    val rotationY: Float,
    val rotationZ: Float,
    val scale: Float,
    val alpha: Float,
    val cameraDistanceMultiplier: Float,
    val translationXFraction: Float,
    val pivotFractionX: Float,
    val zIndex: Float,
    val contentParallaxFraction: Float,
    val contentScale: Float,
    val edgeShadeAlpha: Float,
    val edgeShadeStartFromLeft: Boolean,
    val shadowElevationFraction: Float
)

internal fun resolveHomeFeedTopPaddingDp(
    reservedTopPaddingDp: Float,
    showHeroCarousel: Boolean
): Float {
    val reductionDp = if (showHeroCarousel) HOME_HERO_CAROUSEL_TOP_GAP_REDUCTION_DP else 0f
    return (reservedTopPaddingDp - reductionDp).coerceAtLeast(0f)
}

internal fun <T> resolveHomeHeroCarouselItemOrNull(
    items: List<T>,
    page: Int
): T? = items.getOrNull(page)

internal fun <T> resolveHomeHeroCarouselItemKey(
    items: List<T>,
    page: Int,
    keySelector: (T) -> String
): String {
    val itemKey = items.getOrNull(page)?.let(keySelector).orEmpty()
    return itemKey.ifBlank { "hero_$page" }
}

internal fun <T> selectHomeHeroCarouselItems(
    items: List<T>,
    maxItems: Int = HOME_HERO_CAROUSEL_MAX_ITEMS
): List<T> {
    if (maxItems <= 0) return emptyList()
    return items.take(maxItems)
}

internal fun <T, K> excludeHomeHeroCarouselItems(
    items: List<T>,
    carouselItems: List<T>,
    keySelector: (T) -> K
): List<T> {
    if (carouselItems.isEmpty()) return items
    val carouselKeys = carouselItems.mapTo(mutableSetOf(), keySelector)
    return items.filterNot { keySelector(it) in carouselKeys }
}

internal fun shouldShowHomeHeroCarousel(
    enabled: Boolean,
    category: HomeCategory,
    itemCount: Int
): Boolean {
    return enabled && category == HomeCategory.RECOMMEND && itemCount > 0
}

internal fun resolveHomeHeroCarouselAspectRatio(containerWidthDp: Float): Float {
    return when {
        containerWidthDp >= HOME_HERO_CAROUSEL_WIDE_BREAKPOINT_DP ->
            HOME_HERO_CAROUSEL_WIDE_ASPECT_RATIO
        containerWidthDp >= HOME_HERO_CAROUSEL_TABLET_BREAKPOINT_DP ->
            HOME_HERO_CAROUSEL_TABLET_ASPECT_RATIO
        else -> HOME_HERO_CAROUSEL_PHONE_ASPECT_RATIO
    }
}

internal fun resolveHomeHeroCarouselWidthDp(containerWidthDp: Float): Float {
    return containerWidthDp.coerceAtMost(HOME_HERO_CAROUSEL_MAX_WIDTH_DP)
}

internal fun resolveHomeHeroCarouselCardTransform(
    pageOffset: Float
): HomeHeroCarouselCardTransform {
    val clampedOffset = pageOffset.coerceIn(-1f, 1f)
    val distance = kotlin.math.abs(clampedOffset)
    return HomeHeroCarouselCardTransform(
        rotationY = 0f,
        rotationZ = 0f,
        scale = 1f - distance * 0.04f,
        alpha = 1f - distance * 0.08f,
        cameraDistanceMultiplier = 8f,
        translationXFraction = 0f,
        pivotFractionX = 0.5f,
        zIndex = 1f - distance * 0.01f,
        contentParallaxFraction = 0f,
        contentScale = 1f,
        edgeShadeAlpha = 0f,
        edgeShadeStartFromLeft = false,
        shadowElevationFraction = 0f
    )
}

internal fun resolveHomeHeroCarouselPreviewAlpha(
    hasRenderedFirstFrame: Boolean
): Float = if (hasRenderedFirstFrame) 1f else 0f
