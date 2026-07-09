package com.android.purebilibili.core.store

import com.android.purebilibili.core.theme.UiPreset

/**
 * Legacy gate used by bottom-bar liquid glass: the per-surface toggle must be on,
 * and MD3 also needs the global android-native reuse switch.
 */
internal fun resolveEffectiveLiquidGlassEnabled(
    requestedEnabled: Boolean,
    uiPreset: UiPreset,
    androidNativeLiquidGlassEnabled: Boolean = false
): Boolean {
    if (!requestedEnabled) return false
    return uiPreset == UiPreset.IOS || androidNativeLiquidGlassEnabled
}

/**
 * Global liquid-glass reuse master switch ("安卓原生液态玻璃").
 * When enabled, every reusable chrome surface shares the bottom-bar liquid material.
 */
internal fun resolveGlobalLiquidGlassReuseEnabled(
    androidNativeLiquidGlassEnabled: Boolean
): Boolean = androidNativeLiquidGlassEnabled

/**
 * Effective enablement for any chrome surface that can reuse bottom-bar liquid glass.
 *
 * - Global reuse ON → force enabled (master switch for top dock / search / bottom bar /
 *   segmented controls / comment tabs, etc.)
 * - Global reuse OFF → keep the legacy per-surface + preset gate
 */
internal fun resolveSharedLiquidGlassChromeEnabled(
    individualEnabled: Boolean,
    uiPreset: UiPreset,
    androidNativeLiquidGlassEnabled: Boolean
): Boolean {
    if (resolveGlobalLiquidGlassReuseEnabled(androidNativeLiquidGlassEnabled)) {
        return true
    }
    return resolveEffectiveLiquidGlassEnabled(
        requestedEnabled = individualEnabled,
        uiPreset = uiPreset,
        androidNativeLiquidGlassEnabled = false
    )
}

internal fun resolveEffectiveHomeSettings(
    homeSettings: HomeSettings,
    uiPreset: UiPreset
): HomeSettings = when (uiPreset) {
    UiPreset.IOS,
    UiPreset.MD3 -> homeSettings
}
