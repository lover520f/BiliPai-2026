package com.android.purebilibili.feature.video.screen

import androidx.compose.runtime.Immutable
import com.android.purebilibili.data.model.response.FavFolder
import com.android.purebilibili.data.model.response.ReplyItem
import com.android.purebilibili.feature.video.note.VideoNoteEditorDocument
import com.android.purebilibili.feature.video.viewmodel.CommentSortMode

@Immutable
internal data class VideoDetailPresentationState(
    val currentBvid: String,
    val selectedTabIndex: Int,
    val isFullscreen: Boolean,
    val isPortraitFullscreen: Boolean,
    val isInPipMode: Boolean,
    val isLeaving: Boolean,
    val isCommentThreadVisible: Boolean
)

@Immutable
internal data class VideoDetailPlaybackActions(
    val changeQuality: (Int) -> Unit,
    val reloadVideo: () -> Unit,
    val switchCdn: () -> Unit,
    val switchCdnTo: (Int) -> Unit,
    val probeCdnCandidates: () -> Unit,
    val setAudioMode: (Boolean) -> Unit,
    val setSleepTimer: (Int?) -> Unit,
    val switchPage: (Int) -> Unit,
    val openDownloadDialog: () -> Unit,
    val showDanmakuSendDialog: () -> Unit,
    val skipSponsorSegment: () -> Unit,
    val dismissSponsorSkipButton: () -> Unit,
    val notifyExplicitSeek: (Long) -> Unit,
    val setVideoCodec: (String) -> Unit,
    val setVideoSecondCodec: (String) -> Unit,
    val setAudioQuality: (Int) -> Unit,
    val applyPlaybackSpeed: (Float) -> Boolean,
    val changeAudioLanguage: (String?) -> Unit,
    val saveCover: () -> Unit,
    val downloadAudio: () -> Unit,
    val selectSubtitleTrack: (String) -> Unit,
    val showFavoriteFolderDialog: () -> Unit,
    val toggleFavoriteFolderSelection: (FavFolder) -> Unit,
    val saveFavoriteFolderSelection: () -> Unit,
    val dismissFavoriteFolderDialog: () -> Unit,
    val createFavoriteFolder: (String, String, Boolean) -> Unit,
    val retryAiSummary: () -> Unit,
    val createVideoNoteDraftFromAiSummary: () -> Unit,
    val openVideoNoteEditor: () -> Unit,
    val closeVideoNoteEditor: () -> Unit,
    val updateVideoNoteEditorDocument: (VideoNoteEditorDocument) -> Unit,
    val insertCurrentPlaybackTimestampIntoNote: () -> Unit,
    val seekTo: (Long) -> Unit,
    val saveVideoNote: (VideoNoteEditorDocument?) -> Unit,
    val deleteVideoNote: () -> Unit,
    val retryVideoNote: () -> Unit,
    val openRootCommentComposer: () -> Unit,
    val replyTo: (ReplyItem) -> Unit,
    val markVideoNotInterested: () -> Unit
)

@Immutable
internal data class VideoDetailEngagementActions(
    val toggleFollow: () -> Unit,
    val toggleFavorite: () -> Unit,
    val toggleLike: () -> Unit,
    val openCoinDialog: () -> Unit,
    val doTripleAction: () -> Unit,
    val toggleWatchLater: () -> Unit
)

@Immutable
internal data class VideoDetailCommentActions(
    val loadComments: () -> Unit,
    val setSortMode: (CommentSortMode) -> Unit,
    val toggleUpOnly: () -> Unit,
    val deleteComment: (Long) -> Unit,
    val startDissolve: (Long) -> Unit,
    val loadMoreSubReplies: () -> Unit,
    val openSubReply: (ReplyItem) -> Unit,
    val openSubReplyConversation: (ReplyItem) -> Unit,
    val closeSubReplyConversation: () -> Unit,
    val closeSubReply: () -> Unit,
    val startSubDissolve: (Long) -> Unit,
    val deleteSubComment: (Long) -> Unit,
    val likeComment: (Long) -> Unit,
    val reportComment: (Long, Int) -> Unit,
    val toggleTopComment: (ReplyItem) -> Unit
)

@Immutable
internal data class VideoDetailNavigationActions(
    val back: () -> Unit,
    val home: () -> Unit,
    val toggleFullscreen: () -> Unit,
    val enterPortraitFullscreen: () -> Unit,
    val enterPip: () -> Unit,
    val navigateToAudioMode: () -> Unit
)
