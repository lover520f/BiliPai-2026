package com.android.purebilibili.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals

class BiliPaiNavEntryContentPolicyTest {

    @Test
    fun topLevelKeysResolveToDedicatedContentRoles() {
        assertEquals(BiliPaiNavEntryContentRole.HOME, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Home))
        assertEquals(BiliPaiNavEntryContentRole.DYNAMIC, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Dynamic))
        assertEquals(BiliPaiNavEntryContentRole.SEARCH, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Search))
        assertEquals(BiliPaiNavEntryContentRole.SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Settings))
        assertEquals(BiliPaiNavEntryContentRole.PROFILE, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Profile))
        assertEquals(BiliPaiNavEntryContentRole.HISTORY, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.History))
        assertEquals(BiliPaiNavEntryContentRole.FAVORITE, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Favorite))
        assertEquals(BiliPaiNavEntryContentRole.WATCH_LATER, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.WatchLater))
        assertEquals(BiliPaiNavEntryContentRole.LOGIN, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Login))
        assertEquals(BiliPaiNavEntryContentRole.STORY, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Story))
        assertEquals(BiliPaiNavEntryContentRole.PARTITION, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Partition))
        assertEquals(BiliPaiNavEntryContentRole.SPACE, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Space(1L)))
        assertEquals(BiliPaiNavEntryContentRole.WEB, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Web("https://example.com")))
        assertEquals(BiliPaiNavEntryContentRole.DYNAMIC_DETAIL, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.DynamicDetail("1")))
        assertEquals(BiliPaiNavEntryContentRole.ARTICLE_DETAIL, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.ArticleDetail(1L)))
        assertEquals(BiliPaiNavEntryContentRole.LIVE, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Live(1L)))
        assertEquals(BiliPaiNavEntryContentRole.BANGUMI_DETAIL, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.BangumiDetail(1L)))
        assertEquals(BiliPaiNavEntryContentRole.OPEN_SOURCE_LICENSES, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.OpenSourceLicenses))
        assertEquals(BiliPaiNavEntryContentRole.APPEARANCE_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.AppearanceSettings))
        assertEquals(BiliPaiNavEntryContentRole.ICON_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.IconSettings))
        assertEquals(BiliPaiNavEntryContentRole.ANIMATION_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.AnimationSettings))
        assertEquals(BiliPaiNavEntryContentRole.PLAYBACK_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.PlaybackSettings))
        assertEquals(BiliPaiNavEntryContentRole.PERMISSION_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.PermissionSettings))
        assertEquals(BiliPaiNavEntryContentRole.PLUGINS_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.PluginsSettings()))
        assertEquals(BiliPaiNavEntryContentRole.BOTTOM_BAR_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.BottomBarSettings))
        assertEquals(BiliPaiNavEntryContentRole.SETTINGS_SHARE, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.SettingsShare))
        assertEquals(BiliPaiNavEntryContentRole.WEB_DAV_BACKUP, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.WebDavBackup))
        assertEquals(BiliPaiNavEntryContentRole.TIPS_SETTINGS, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.TipsSettings))
    }

    @Test
    fun videoDetailKeyResolvesToDedicatedContentRole() {
        assertEquals(
            BiliPaiNavEntryContentRole.VIDEO_DETAIL,
            resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.VideoDetail("BV1"))
        )
    }

    @Test
    fun remainingDetailKeysStayDeferredUntilTheirLegacyRouteBodiesAreExtracted() {
        assertEquals(BiliPaiNavEntryContentRole.CATEGORY, resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Category(1)))
        assertEquals(
            BiliPaiNavEntryContentRole.DEFERRED_LEGACY_ROUTE,
            resolveBiliPaiNavEntryContentRole(BiliPaiNavKey.Unknown("download"))
        )
    }
}
