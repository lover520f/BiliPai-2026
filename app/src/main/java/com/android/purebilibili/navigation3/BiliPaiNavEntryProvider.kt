package com.android.purebilibili.navigation3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

internal fun biliPaiNavEntryProvider(
    sourceMetadata: BiliPaiNavSourceMetadata,
    content: @Composable (BiliPaiNavKey) -> Unit
): (BiliPaiNavKey) -> NavEntry<BiliPaiNavKey> {
    return entryProvider(
        fallback = { key ->
            NavEntry(
                key = key,
                metadata = biliPaiNavEntryMetadata(key, sourceMetadata),
                content = content
            )
        }
    ) {
        entry<BiliPaiNavKey.Home>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Dynamic>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Search>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Settings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.OpenSourceLicenses>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.AppearanceSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.IconSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.AnimationSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.PlaybackSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.PermissionSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.PluginsSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.BottomBarSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.SettingsShare>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.WebDavBackup>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.TipsSettings>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Login>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Profile>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.History>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Favorite>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.WatchLater>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Partition>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Story>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.AudioMode>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.VideoDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.ArticleDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.DynamicDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Space>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Category>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Live>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.BangumiDetail>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Web>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
        entry<BiliPaiNavKey.Unknown>(metadata = { key -> biliPaiNavEntryMetadata(key, sourceMetadata) }, content = content)
    }
}

internal fun biliPaiNavEntryMetadata(
    key: BiliPaiNavKey,
    sourceMetadata: BiliPaiNavSourceMetadata
): Map<String, Any> {
    val noOpReturn = key is BiliPaiNavKey.VideoDetail || sourceMetadata.sharedTransitionReady
    val transition = if (noOpReturn) {
        BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT
    } else {
        BiliPaiNavRouteTransition.FALLBACK
    }
    return NavDisplay.transitionSpec {
        resolveBiliPaiNavContentTransform(BiliPaiNavRouteTransition.FALLBACK)
    } + NavDisplay.popTransitionSpec {
        resolveBiliPaiNavContentTransform(transition)
    } + NavDisplay.predictivePopTransitionSpec {
        resolveBiliPaiNavContentTransform(transition)
    }
}
