package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.store.HomeCardBadgeEffectMode

internal enum class HomeVideoBadgeStyle {
    GLASS,
    PLAIN
}

internal data class HomeVideoGlassBadgeStylePolicy(
    val coverStyle: HomeVideoBadgeStyle,
    val infoStyle: HomeVideoBadgeStyle
)

/**
 * Resolved runtime look for card badges.
 *
 * - [glassEnabled]/[blurEnabled] drive [resolveHomeGlassPillStyle] alphas
 * - Soft glass: glass on, blur off → denser translucent pill
 * - Light blur: glass+blur on → more frosted; degraded to soft glass while scrolling
 */
internal data class HomeCardBadgeEffectVisual(
    val coverStyle: HomeVideoBadgeStyle,
    val infoStyle: HomeVideoBadgeStyle,
    val glassEnabled: Boolean,
    val blurEnabled: Boolean,
    val effectiveMode: HomeCardBadgeEffectMode
)

internal fun resolveHomeCardBadgeEffectVisual(
    mode: HomeCardBadgeEffectMode,
    scrollLiteModeEnabled: Boolean
): HomeCardBadgeEffectVisual {
    val effective = when {
        mode == HomeCardBadgeEffectMode.OFF -> HomeCardBadgeEffectMode.OFF
        // Real-time-ish frosted look is expensive in a dense LazyGrid; keep soft glass while scrolling.
        mode == HomeCardBadgeEffectMode.LIGHT_BLUR && scrollLiteModeEnabled ->
            HomeCardBadgeEffectMode.SOFT_GLASS
        else -> mode
    }
    return when (effective) {
        HomeCardBadgeEffectMode.OFF -> HomeCardBadgeEffectVisual(
            coverStyle = HomeVideoBadgeStyle.PLAIN,
            infoStyle = HomeVideoBadgeStyle.PLAIN,
            glassEnabled = false,
            blurEnabled = false,
            effectiveMode = effective
        )
        HomeCardBadgeEffectMode.SOFT_GLASS -> HomeCardBadgeEffectVisual(
            coverStyle = HomeVideoBadgeStyle.GLASS,
            infoStyle = HomeVideoBadgeStyle.GLASS,
            glassEnabled = true,
            blurEnabled = false,
            effectiveMode = effective
        )
        HomeCardBadgeEffectMode.LIGHT_BLUR -> HomeCardBadgeEffectVisual(
            coverStyle = HomeVideoBadgeStyle.GLASS,
            infoStyle = HomeVideoBadgeStyle.GLASS,
            glassEnabled = true,
            blurEnabled = true,
            effectiveMode = effective
        )
    }
}

/**
 * Back-compat entry used by older call sites that only pass boolean glass flags.
 */
internal fun resolveHomeVideoGlassBadgeStylePolicy(
    showCoverGlassBadges: Boolean,
    showInfoGlassBadges: Boolean
): HomeVideoGlassBadgeStylePolicy {
    val mode = if (showCoverGlassBadges || showInfoGlassBadges) {
        HomeCardBadgeEffectMode.SOFT_GLASS
    } else {
        HomeCardBadgeEffectMode.OFF
    }
    val visual = resolveHomeCardBadgeEffectVisual(
        mode = mode,
        scrollLiteModeEnabled = false
    )
    return HomeVideoGlassBadgeStylePolicy(
        coverStyle = if (showCoverGlassBadges) visual.coverStyle else HomeVideoBadgeStyle.PLAIN,
        infoStyle = if (showInfoGlassBadges) visual.infoStyle else HomeVideoBadgeStyle.PLAIN
    )
}
