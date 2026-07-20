package com.android.purebilibili.navigation3

private const val QUICK_RETURN_THRESHOLD_MILLIS = 500L

internal data class BiliPaiReturnSessionState(
    val isReturningFromDetail: Boolean = false,
    val isQuickReturnFromDetail: Boolean = false,
    val lastVideoSourceRoute: String? = null,
    val lastVideoSourceKey: String? = null,
    /**
     * 进入相关推荐详情前的列表来源（如 home:BV_A）。
     * related 会把 last* 写成 video/BV_A:BV_B，pop 回父详情后需恢复，否则再回列表会丢共享元素。
     */
    val previousListVideoSourceRoute: String? = null,
    val previousListVideoSourceKey: String? = null,
    val detailEnteredAtMillis: Long? = null
) {
    fun recordVideoSource(source: BiliPaiVideoSource): BiliPaiReturnSessionState {
        val relatedDetailSource = source.route?.substringBefore("?")?.startsWith("video/") == true
        val preserveListSource = relatedDetailSource &&
            lastVideoSourceRoute?.substringBefore("?")?.startsWith("video/") != true
        return copy(
            previousListVideoSourceRoute = if (preserveListSource) {
                lastVideoSourceRoute
            } else {
                previousListVideoSourceRoute
            },
            previousListVideoSourceKey = if (preserveListSource) {
                lastVideoSourceKey
            } else {
                previousListVideoSourceKey
            },
            lastVideoSourceRoute = source.route,
            lastVideoSourceKey = source.key
        )
    }

    fun restoreListVideoSourceAfterRelatedReturn(): BiliPaiReturnSessionState {
        val restoredRoute = previousListVideoSourceRoute ?: return this
        return copy(
            lastVideoSourceRoute = restoredRoute,
            lastVideoSourceKey = previousListVideoSourceKey,
            previousListVideoSourceRoute = null,
            previousListVideoSourceKey = null
        )
    }

    fun recordVideoSourceRoute(sourceRoute: String?): BiliPaiReturnSessionState {
        return copy(
            lastVideoSourceRoute = normalizeBiliPaiVideoSourceRoute(sourceRoute),
            lastVideoSourceKey = null
        )
    }

    fun markDetailEntered(nowMillis: Long): BiliPaiReturnSessionState {
        return copy(
            isReturningFromDetail = false,
            isQuickReturnFromDetail = false,
            detailEnteredAtMillis = nowMillis
        )
    }

    fun markReturning(nowMillis: Long): BiliPaiReturnSessionState {
        val elapsed = detailEnteredAtMillis?.let { nowMillis - it } ?: Long.MAX_VALUE
        return copy(
            isReturningFromDetail = true,
            isQuickReturnFromDetail = elapsed in 0L..QUICK_RETURN_THRESHOLD_MILLIS
        )
    }

    fun clearReturning(): BiliPaiReturnSessionState {
        return copy(
            isReturningFromDetail = false,
            isQuickReturnFromDetail = false
        )
    }
}
