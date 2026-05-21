package com.android.purebilibili.navigation3

internal fun resolveInitialBiliPaiBackStack(
    firstRoute: String?,
    onboardingRequired: Boolean
): List<BiliPaiNavKey> {
    if (onboardingRequired) {
        return listOf(BiliPaiNavKey.Unknown("onboarding"))
    }
    return listOf(BiliPaiNavKey.MainHost)
}

internal fun pushBiliPaiNavKey(
    currentStack: List<BiliPaiNavKey>,
    key: BiliPaiNavKey
): List<BiliPaiNavKey> {
    val base = currentStack.ifEmpty { listOf(BiliPaiNavKey.MainHost) }
    return if (base.last() == key) base else base + key
}

internal fun popBiliPaiNavKey(
    currentStack: List<BiliPaiNavKey>
): List<BiliPaiNavKey> {
    return if (currentStack.size <= 1) currentStack else currentStack.dropLast(1)
}
