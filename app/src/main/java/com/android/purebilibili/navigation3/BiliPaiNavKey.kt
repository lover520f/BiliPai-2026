package com.android.purebilibili.navigation3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface BiliPaiNavKey : NavKey {
    val routeBase: String

    @Serializable
    data object Home : BiliPaiNavKey {
        override val routeBase: String = "home"
    }

    @Serializable
    data object Dynamic : BiliPaiNavKey {
        override val routeBase: String = "dynamic"
    }

    @Serializable
    data object Search : BiliPaiNavKey {
        override val routeBase: String = "search"
    }

    @Serializable
    data object Settings : BiliPaiNavKey {
        override val routeBase: String = "settings"
    }

    @Serializable
    data object OpenSourceLicenses : BiliPaiNavKey {
        override val routeBase: String = "open_source_licenses"
    }

    @Serializable
    data object AppearanceSettings : BiliPaiNavKey {
        override val routeBase: String = "appearance_settings"
    }

    @Serializable
    data object IconSettings : BiliPaiNavKey {
        override val routeBase: String = "icon_settings"
    }

    @Serializable
    data object AnimationSettings : BiliPaiNavKey {
        override val routeBase: String = "animation_settings"
    }

    @Serializable
    data object PlaybackSettings : BiliPaiNavKey {
        override val routeBase: String = "playback_settings"
    }

    @Serializable
    data object PermissionSettings : BiliPaiNavKey {
        override val routeBase: String = "permission_settings"
    }

    @Serializable
    data class PluginsSettings(
        val importUrl: String? = null
    ) : BiliPaiNavKey {
        override val routeBase: String = "plugins_settings"
    }

    @Serializable
    data object BottomBarSettings : BiliPaiNavKey {
        override val routeBase: String = "bottom_bar_settings"
    }

    @Serializable
    data object SettingsShare : BiliPaiNavKey {
        override val routeBase: String = "settings_share"
    }

    @Serializable
    data object WebDavBackup : BiliPaiNavKey {
        override val routeBase: String = "webdav_backup"
    }

    @Serializable
    data object TipsSettings : BiliPaiNavKey {
        override val routeBase: String = "tips_settings"
    }

    @Serializable
    data object Login : BiliPaiNavKey {
        override val routeBase: String = "login"
    }

    @Serializable
    data object Profile : BiliPaiNavKey {
        override val routeBase: String = "profile"
    }

    @Serializable
    data object History : BiliPaiNavKey {
        override val routeBase: String = "history"
    }

    @Serializable
    data object Favorite : BiliPaiNavKey {
        override val routeBase: String = "favorite"
    }

    @Serializable
    data object WatchLater : BiliPaiNavKey {
        override val routeBase: String = "watch_later"
    }

    @Serializable
    data object Partition : BiliPaiNavKey {
        override val routeBase: String = "partition"
    }

    @Serializable
    data object Story : BiliPaiNavKey {
        override val routeBase: String = "story"
    }

    @Serializable
    data object AudioMode : BiliPaiNavKey {
        override val routeBase: String = "audio_mode"
    }

    @Serializable
    data class VideoDetail(
        val bvid: String,
        val cid: Long = 0L,
        val coverUrl: String = "",
        val startAudio: Boolean = false,
        val autoPortrait: Boolean = false,
        val fullscreen: Boolean = false,
        val resumePositionMs: Long = 0L,
        val commentRootRpid: Long = 0L,
        val sourceRoute: String? = null
    ) : BiliPaiNavKey {
        override val routeBase: String = "video"
    }

    @Serializable
    data class ArticleDetail(
        val articleId: Long,
        val title: String = ""
    ) : BiliPaiNavKey {
        override val routeBase: String = "article"
    }

    @Serializable
    data class DynamicDetail(
        val dynamicId: String
    ) : BiliPaiNavKey {
        override val routeBase: String = "dynamic_detail"
    }

    @Serializable
    data class Space(
        val mid: Long
    ) : BiliPaiNavKey {
        override val routeBase: String = "space"
    }

    @Serializable
    data class Category(
        val tid: Int,
        val name: String = ""
    ) : BiliPaiNavKey {
        override val routeBase: String = "category"
    }

    @Serializable
    data class Live(
        val roomId: Long,
        val title: String = "",
        val uname: String = ""
    ) : BiliPaiNavKey {
        override val routeBase: String = "live"
    }

    @Serializable
    data class BangumiDetail(
        val seasonId: Long,
        val epId: Long = 0L
    ) : BiliPaiNavKey {
        override val routeBase: String = "bangumi"
    }

    @Serializable
    data class Web(
        val url: String,
        val title: String = ""
    ) : BiliPaiNavKey {
        override val routeBase: String = "web"
    }

    @Serializable
    data class Unknown(
        val route: String
    ) : BiliPaiNavKey {
        override val routeBase: String = route.substringBefore("?").substringBefore("/")
    }
}
