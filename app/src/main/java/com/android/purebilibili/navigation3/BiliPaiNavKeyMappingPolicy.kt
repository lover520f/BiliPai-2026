package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation.ScreenRoutes
import com.android.purebilibili.navigation.VideoRoute
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal fun BiliPaiNavKey.toLegacyRoute(): String {
    return when (this) {
        BiliPaiNavKey.Home -> ScreenRoutes.Home.route
        BiliPaiNavKey.Dynamic -> ScreenRoutes.Dynamic.route
        BiliPaiNavKey.Search -> ScreenRoutes.Search.route
        BiliPaiNavKey.Settings -> ScreenRoutes.Settings.route
        BiliPaiNavKey.OpenSourceLicenses -> ScreenRoutes.OpenSourceLicenses.route
        BiliPaiNavKey.AppearanceSettings -> ScreenRoutes.AppearanceSettings.route
        BiliPaiNavKey.IconSettings -> ScreenRoutes.IconSettings.route
        BiliPaiNavKey.AnimationSettings -> ScreenRoutes.AnimationSettings.route
        BiliPaiNavKey.PlaybackSettings -> ScreenRoutes.PlaybackSettings.route
        BiliPaiNavKey.PermissionSettings -> ScreenRoutes.PermissionSettings.route
        is BiliPaiNavKey.PluginsSettings -> ScreenRoutes.PluginsSettings.createRoute(importUrl)
        BiliPaiNavKey.BottomBarSettings -> ScreenRoutes.BottomBarSettings.route
        BiliPaiNavKey.SettingsShare -> ScreenRoutes.SettingsShare.route
        BiliPaiNavKey.WebDavBackup -> ScreenRoutes.WebDavBackup.route
        BiliPaiNavKey.TipsSettings -> ScreenRoutes.TipsSettings.route
        BiliPaiNavKey.Login -> ScreenRoutes.Login.route
        BiliPaiNavKey.Profile -> ScreenRoutes.Profile.route
        BiliPaiNavKey.History -> ScreenRoutes.History.route
        BiliPaiNavKey.Favorite -> ScreenRoutes.Favorite.route
        BiliPaiNavKey.WatchLater -> ScreenRoutes.WatchLater.route
        BiliPaiNavKey.Partition -> ScreenRoutes.Partition.route
        BiliPaiNavKey.Story -> ScreenRoutes.Story.route
        BiliPaiNavKey.AudioMode -> ScreenRoutes.AudioMode.route
        is BiliPaiNavKey.VideoDetail -> VideoRoute.createRoute(
            bvid = bvid,
            cid = cid,
            coverUrl = coverUrl,
            startAudio = startAudio,
            autoPortrait = autoPortrait,
            fullscreen = fullscreen,
            resumePositionMs = resumePositionMs,
            commentRootRpid = commentRootRpid
        )
        is BiliPaiNavKey.ArticleDetail -> ScreenRoutes.ArticleDetail.createRoute(articleId, title)
        is BiliPaiNavKey.DynamicDetail -> ScreenRoutes.DynamicDetail.createRoute(dynamicId)
        is BiliPaiNavKey.Space -> ScreenRoutes.Space.createRoute(mid)
        is BiliPaiNavKey.Category -> ScreenRoutes.Category.createRoute(tid, name)
        is BiliPaiNavKey.Live -> ScreenRoutes.Live.createRoute(roomId, title, uname)
        is BiliPaiNavKey.BangumiDetail -> ScreenRoutes.BangumiDetail.createRoute(seasonId, epId)
        is BiliPaiNavKey.Web -> ScreenRoutes.Web.createRoute(url, title)
        is BiliPaiNavKey.Unknown -> route
    }
}

internal fun legacyRouteToBiliPaiNavKey(route: String?): BiliPaiNavKey {
    val normalized = route?.takeIf { it.isNotBlank() } ?: return BiliPaiNavKey.Home
    val routeBase = normalized.substringBefore("?")
    val segments = routeBase.split('/').filter { it.isNotBlank() }
    val query = parseQuery(normalized.substringAfter("?", missingDelimiterValue = ""))

    return when {
        normalized == ScreenRoutes.Home.route -> BiliPaiNavKey.Home
        normalized == ScreenRoutes.Dynamic.route -> BiliPaiNavKey.Dynamic
        normalized == ScreenRoutes.Search.route -> BiliPaiNavKey.Search
        normalized == ScreenRoutes.Settings.route -> BiliPaiNavKey.Settings
        normalized == ScreenRoutes.OpenSourceLicenses.route -> BiliPaiNavKey.OpenSourceLicenses
        normalized == ScreenRoutes.AppearanceSettings.route -> BiliPaiNavKey.AppearanceSettings
        normalized == ScreenRoutes.IconSettings.route -> BiliPaiNavKey.IconSettings
        normalized == ScreenRoutes.AnimationSettings.route -> BiliPaiNavKey.AnimationSettings
        normalized == ScreenRoutes.PlaybackSettings.route -> BiliPaiNavKey.PlaybackSettings
        normalized == ScreenRoutes.PermissionSettings.route -> BiliPaiNavKey.PermissionSettings
        routeBase == "plugins_settings" -> BiliPaiNavKey.PluginsSettings(importUrl = query["importUrl"])
        normalized == ScreenRoutes.BottomBarSettings.route -> BiliPaiNavKey.BottomBarSettings
        normalized == ScreenRoutes.SettingsShare.route -> BiliPaiNavKey.SettingsShare
        normalized == ScreenRoutes.WebDavBackup.route -> BiliPaiNavKey.WebDavBackup
        normalized == ScreenRoutes.TipsSettings.route -> BiliPaiNavKey.TipsSettings
        normalized == ScreenRoutes.Login.route -> BiliPaiNavKey.Login
        normalized == ScreenRoutes.Profile.route -> BiliPaiNavKey.Profile
        normalized == ScreenRoutes.History.route -> BiliPaiNavKey.History
        normalized == ScreenRoutes.Favorite.route -> BiliPaiNavKey.Favorite
        normalized == ScreenRoutes.WatchLater.route -> BiliPaiNavKey.WatchLater
        normalized == ScreenRoutes.Partition.route -> BiliPaiNavKey.Partition
        normalized == ScreenRoutes.Story.route -> BiliPaiNavKey.Story
        normalized == ScreenRoutes.AudioMode.route -> BiliPaiNavKey.AudioMode
        segments.firstOrNull() == VideoRoute.base && segments.size >= 2 -> {
            BiliPaiNavKey.VideoDetail(
                bvid = decodeRouteValue(segments[1]),
                cid = query["cid"]?.toLongOrNull() ?: 0L,
                coverUrl = query["cover"].orEmpty(),
                startAudio = query["startAudio"]?.toBooleanStrictOrNull() ?: false,
                autoPortrait = query["autoPortrait"]?.toBooleanStrictOrNull() ?: false,
                fullscreen = query["fullscreen"]?.toBooleanStrictOrNull() ?: false,
                resumePositionMs = query["resumePositionMs"]?.toLongOrNull() ?: 0L,
                commentRootRpid = query["commentRootRpid"]?.toLongOrNull() ?: 0L,
                sourceRoute = null
            )
        }
        segments.firstOrNull() == "article" && segments.size >= 2 -> {
            BiliPaiNavKey.ArticleDetail(
                articleId = segments[1].toLongOrNull() ?: 0L,
                title = query["title"].orEmpty()
            )
        }
        segments.firstOrNull() == "dynamic_detail" && segments.size >= 2 -> {
            BiliPaiNavKey.DynamicDetail(dynamicId = decodeRouteValue(segments[1]))
        }
        segments.firstOrNull() == "space" && segments.size >= 2 -> {
            BiliPaiNavKey.Space(mid = segments[1].toLongOrNull() ?: 0L)
        }
        segments.firstOrNull() == "category" && segments.size >= 2 -> {
            BiliPaiNavKey.Category(
                tid = segments[1].toIntOrNull() ?: 0,
                name = query["name"].orEmpty()
            )
        }
        segments.firstOrNull() == "live" && segments.size >= 2 -> {
            BiliPaiNavKey.Live(
                roomId = segments[1].toLongOrNull() ?: 0L,
                title = query["title"].orEmpty(),
                uname = query["uname"].orEmpty()
            )
        }
        segments.firstOrNull() == "bangumi" && segments.size >= 2 -> {
            BiliPaiNavKey.BangumiDetail(
                seasonId = segments[1].toLongOrNull() ?: 0L,
                epId = query["epId"]?.toLongOrNull() ?: 0L
            )
        }
        normalized.substringBefore("?") == "web" -> {
            BiliPaiNavKey.Web(
                url = query["url"].orEmpty(),
                title = query["title"].orEmpty()
            )
        }
        else -> BiliPaiNavKey.Unknown(normalized)
    }
}

internal fun isCardReturnTargetNavKey(key: BiliPaiNavKey): Boolean {
    return when (key) {
        BiliPaiNavKey.Home,
        BiliPaiNavKey.Dynamic,
        BiliPaiNavKey.Search,
        BiliPaiNavKey.History,
        BiliPaiNavKey.Favorite,
        BiliPaiNavKey.WatchLater,
        BiliPaiNavKey.Partition,
        is BiliPaiNavKey.DynamicDetail,
        is BiliPaiNavKey.Space,
        is BiliPaiNavKey.Category -> true
        else -> false
    }
}

private fun parseQuery(query: String): Map<String, String> {
    if (query.isBlank()) return emptyMap()
    return query.split('&')
        .mapNotNull { part ->
            val key = part.substringBefore("=", missingDelimiterValue = "")
            if (key.isBlank()) return@mapNotNull null
            val value = part.substringAfter("=", missingDelimiterValue = "")
            decodeRouteValue(key) to decodeRouteValue(value)
        }
        .toMap()
}

private fun decodeRouteValue(value: String): String {
    return URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
