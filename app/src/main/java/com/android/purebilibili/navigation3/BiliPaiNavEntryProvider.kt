package com.android.purebilibili.navigation3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

private const val BILI_PAI_NAV_ROUTE_BASE_METADATA_KEY = "biliPaiNavRouteBase"

internal fun biliPaiNavEntryProvider(
    sourceMetadata: BiliPaiNavSourceMetadata,
    visibleBottomBarRoutes: Set<String> = emptySet(),
    content: @Composable (BiliPaiNavKey) -> Unit
): (BiliPaiNavKey) -> NavEntry<BiliPaiNavKey> {
    return entryProvider(
        fallback = { key ->
            NavEntry(
                key = key,
                metadata = biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes),
                content = content
            )
        }
    ) {
        entry<BiliPaiNavKey.MainHost>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Home>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Dynamic>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Search>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.SearchTrending>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.TopicDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Settings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.OpenSourceLicenses>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.AppearanceSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.IconSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.AnimationSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.PlaybackSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.PermissionSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.PluginsSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.BottomBarSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.SettingsShare>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.WebDavBackup>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.TipsSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Login>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Profile>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.History>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Favorite>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.WatchLater>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Onboarding>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Following>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.DownloadList>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.OfflineVideoPlayer>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.LiveList>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.LiveSearch>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.LiveArea>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.LiveAreaDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.LiveFollowing>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Inbox>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.ReplyMe>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.AtMe>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.LikeMe>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.SystemNotice>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Chat>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Partition>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Story>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.AudioMode>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.SeasonSeriesDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Bangumi>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.BangumiPlayer>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.MusicDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.NativeMusic>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.VideoDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.ArticleDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.DynamicDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Space>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Category>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Live>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.BangumiDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Web>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
        entry<BiliPaiNavKey.Unknown>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata, visibleBottomBarRoutes) }, content = content)
    }
}

internal fun biliPaiNavEntryMetadata(
    key: BiliPaiNavKey,
    sourceMetadata: BiliPaiNavSourceMetadata,
    visibleBottomBarRoutes: Set<String> = emptySet()
): Map<String, Any> {
    val transitions = resolveBiliPaiNavEntryRouteTransitions(
        key = key,
        sourceMetadata = sourceMetadata
    )
    return mapOf(
        BILI_PAI_NAV_ROUTE_BASE_METADATA_KEY to key.routeBase
    ) + NavDisplay.transitionSpec {
        val transition = resolveBiliPaiNavEntryForwardRouteTransition(
            defaultTransition = transitions.forward,
            fromRoute = initialState.biliPaiRouteBase(),
            toRoute = targetState.biliPaiRouteBase(),
            visibleBottomBarRoutes = visibleBottomBarRoutes
        )
        resolveBiliPaiNavContentTransform(transition)
    } + NavDisplay.popTransitionSpec {
        resolveBiliPaiNavContentTransform(transitions.pop)
    } + NavDisplay.predictivePopTransitionSpec {
        resolveBiliPaiNavContentTransform(transitions.predictivePop)
    }
}

internal fun resolveBiliPaiNavEntryForwardRouteTransition(
    defaultTransition: BiliPaiNavRouteTransition,
    fromRoute: String?,
    toRoute: String?,
    visibleBottomBarRoutes: Set<String>
): BiliPaiNavRouteTransition {
    return defaultTransition
}

private fun androidx.navigation3.scene.Scene<*>.biliPaiRouteBase(): String? {
    return entries
        .lastOrNull()
        ?.metadata
        ?.get(BILI_PAI_NAV_ROUTE_BASE_METADATA_KEY) as? String
}

internal data class BiliPaiNavEntryRouteTransitions(
    val forward: BiliPaiNavRouteTransition,
    val pop: BiliPaiNavRouteTransition,
    val predictivePop: BiliPaiNavRouteTransition
)

internal fun resolveBiliPaiNavEntryRouteTransitions(
    key: BiliPaiNavKey,
    sourceMetadata: BiliPaiNavSourceMetadata
): BiliPaiNavEntryRouteTransitions {
    val recordedMatchingVideoSource = key is BiliPaiNavKey.VideoDetail &&
        sourceMetadata.clickedBoundsRecorded &&
        sourceMetadata.sourceRoute != null &&
        sourceMetadata.sourceRoute == key.sourceRoute &&
        sourceMetadata.sourceKey == "${sourceMetadata.sourceRoute}:${key.bvid}"
    val sharedReadyVideoPush = recordedMatchingVideoSource &&
        sourceMetadata.sharedTransitionReady
    val sharedReadyCardReturnTarget = key !is BiliPaiNavKey.VideoDetail &&
        isCardReturnTargetNavKey(key) &&
        sourceMetadata.sharedTransitionReady
    val forward = when {
        sharedReadyVideoPush -> BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT
        else -> BiliPaiNavRouteTransition.FALLBACK
    }
    val pop = when {
        sharedReadyVideoPush || sharedReadyCardReturnTarget ->
            BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT
        else -> BiliPaiNavRouteTransition.FALLBACK
    }
    return BiliPaiNavEntryRouteTransitions(
        forward = forward,
        pop = pop,
        predictivePop = pop
    )
}
