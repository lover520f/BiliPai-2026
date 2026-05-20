package com.android.purebilibili.navigation3

internal enum class BiliPaiNavEntryContentRole {
    HOME,
    DYNAMIC,
    SEARCH,
    SETTINGS,
    OPEN_SOURCE_LICENSES,
    APPEARANCE_SETTINGS,
    ICON_SETTINGS,
    ANIMATION_SETTINGS,
    PLAYBACK_SETTINGS,
    PERMISSION_SETTINGS,
    PLUGINS_SETTINGS,
    BOTTOM_BAR_SETTINGS,
    SETTINGS_SHARE,
    WEB_DAV_BACKUP,
    TIPS_SETTINGS,
    PROFILE,
    VIDEO_DETAIL,
    HISTORY,
    FAVORITE,
    WATCH_LATER,
    LOGIN,
    STORY,
    PARTITION,
    CATEGORY,
    SPACE,
    WEB,
    DYNAMIC_DETAIL,
    ARTICLE_DETAIL,
    LIVE,
    BANGUMI_DETAIL,
    DEFERRED_LEGACY_ROUTE
}

internal fun resolveBiliPaiNavEntryContentRole(key: BiliPaiNavKey): BiliPaiNavEntryContentRole {
    return when (key) {
        BiliPaiNavKey.Home -> BiliPaiNavEntryContentRole.HOME
        BiliPaiNavKey.Dynamic -> BiliPaiNavEntryContentRole.DYNAMIC
        BiliPaiNavKey.Search -> BiliPaiNavEntryContentRole.SEARCH
        BiliPaiNavKey.Settings -> BiliPaiNavEntryContentRole.SETTINGS
        BiliPaiNavKey.OpenSourceLicenses -> BiliPaiNavEntryContentRole.OPEN_SOURCE_LICENSES
        BiliPaiNavKey.AppearanceSettings -> BiliPaiNavEntryContentRole.APPEARANCE_SETTINGS
        BiliPaiNavKey.IconSettings -> BiliPaiNavEntryContentRole.ICON_SETTINGS
        BiliPaiNavKey.AnimationSettings -> BiliPaiNavEntryContentRole.ANIMATION_SETTINGS
        BiliPaiNavKey.PlaybackSettings -> BiliPaiNavEntryContentRole.PLAYBACK_SETTINGS
        BiliPaiNavKey.PermissionSettings -> BiliPaiNavEntryContentRole.PERMISSION_SETTINGS
        is BiliPaiNavKey.PluginsSettings -> BiliPaiNavEntryContentRole.PLUGINS_SETTINGS
        BiliPaiNavKey.BottomBarSettings -> BiliPaiNavEntryContentRole.BOTTOM_BAR_SETTINGS
        BiliPaiNavKey.SettingsShare -> BiliPaiNavEntryContentRole.SETTINGS_SHARE
        BiliPaiNavKey.WebDavBackup -> BiliPaiNavEntryContentRole.WEB_DAV_BACKUP
        BiliPaiNavKey.TipsSettings -> BiliPaiNavEntryContentRole.TIPS_SETTINGS
        BiliPaiNavKey.Profile -> BiliPaiNavEntryContentRole.PROFILE
        is BiliPaiNavKey.VideoDetail -> BiliPaiNavEntryContentRole.VIDEO_DETAIL
        BiliPaiNavKey.History -> BiliPaiNavEntryContentRole.HISTORY
        BiliPaiNavKey.Favorite -> BiliPaiNavEntryContentRole.FAVORITE
        BiliPaiNavKey.WatchLater -> BiliPaiNavEntryContentRole.WATCH_LATER
        BiliPaiNavKey.Login -> BiliPaiNavEntryContentRole.LOGIN
        BiliPaiNavKey.Story -> BiliPaiNavEntryContentRole.STORY
        BiliPaiNavKey.Partition -> BiliPaiNavEntryContentRole.PARTITION
        is BiliPaiNavKey.Category -> BiliPaiNavEntryContentRole.CATEGORY
        is BiliPaiNavKey.Space -> BiliPaiNavEntryContentRole.SPACE
        is BiliPaiNavKey.Web -> BiliPaiNavEntryContentRole.WEB
        is BiliPaiNavKey.DynamicDetail -> BiliPaiNavEntryContentRole.DYNAMIC_DETAIL
        is BiliPaiNavKey.ArticleDetail -> BiliPaiNavEntryContentRole.ARTICLE_DETAIL
        is BiliPaiNavKey.Live -> BiliPaiNavEntryContentRole.LIVE
        is BiliPaiNavKey.BangumiDetail -> BiliPaiNavEntryContentRole.BANGUMI_DETAIL
        else -> BiliPaiNavEntryContentRole.DEFERRED_LEGACY_ROUTE
    }
}
