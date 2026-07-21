package com.android.purebilibili.core.ui.transition

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.navigation.isVideoCardReturnTargetRoute
import kotlin.math.pow
import kotlin.math.roundToInt

// 景深三层（与 Hero 卡片放大配合，progress 0→1 同源）：
// 1) scale 下沉：列表「退后」；过小无感，过大返回像回弹 → 标定 ~5.5%
// 2) blur：空间纵深（冻结层 + BlurEffect 20px）
// 3) scrim 压暗：聚焦/可读；纯 blur 偏发灰发飘，压暗把注意力钉在飞卡上
// - 冻结层：首帧 record 一次后只改 BlurEffect/scale，禁止 live 重录
// - 压暗全程保留（含 HELD），避免打开完成后景深断裂
// - 返回：景深 progress 与 shared morph 同墙钟、同 Linear
private const val VIDEO_CARD_TRANSITION_MAX_BLUR_RADIUS_PX = 20f
private const val VIDEO_CARD_TRANSITION_BLUR_QUANTUM_PX = 1f
// 压暗：配合更大下沉仍要可读，略强于旧 0.22/0.11，但低于半透明模态（避免脏）。
private const val VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA_DARK = 0.28f
private const val VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA_LIGHT = 0.14f
private const val VIDEO_CARD_TRANSITION_LIGHT_REDUCED_OPENING_SCRIM_ALPHA = 0.08f
// 列表下沉：0.022 几乎看不出；0.055 与卡片放大对仗清晰，返回 Linear 清回仍不「弹」。
private const val VIDEO_CARD_TRANSITION_MAX_CONTENT_SCALE_REDUCTION = 0.055f
/** 景深缩放露出的边缘：至少压到这个 tint 强度，避免浅色主题读成「白条」。 */
private const val VIDEO_CARD_TRANSITION_SCALE_GAP_MIN_TINT_LIGHT = 0.36f
private const val VIDEO_CARD_TRANSITION_SCALE_GAP_MIN_TINT_DARK = 0.44f
private val VIDEO_CARD_TRANSITION_LIGHT_SCRIM_TINT = Color(0xFF8E8E93)
private val VIDEO_CARD_TRANSITION_DARK_GAP_BASE = Color(0xFF121212)

// 开场与返回时长由共享元素速度设置提供；取消仍固定为短恢复动画。
// 与共享元素标准时长对齐，避免景深先清完、封面还在赶路。
internal const val VIDEO_CARD_TRANSITION_BACKGROUND_RETURN_DURATION_MS = 420
internal const val VIDEO_CARD_TRANSITION_BACKGROUND_CANCEL_DURATION_MS = 160

internal enum class VideoCardTransitionBackgroundPhase {
    IDLE,
    OPENING,
    HELD,
    RETURNING
}

internal data class VideoCardTransitionBackgroundFrame(
    val blurRadiusPx: Float,
    val scrimAlpha: Float,
    val contentScale: Float,
    val useLightScrimTint: Boolean = false,
)

internal data class VideoCardTransitionBackgroundState(
    val progressProvider: () -> Float = { 0f },
    val sourceRouteProvider: () -> String? = { null },
    val phaseProvider: () -> VideoCardTransitionBackgroundPhase = {
        VideoCardTransitionBackgroundPhase.IDLE
    },
    val isReturnGestureInProgressProvider: () -> Boolean = { false },
    val isGestureRestoreInProgressProvider: () -> Boolean = { false },
    val isQuickReturnFromDetailProvider: () -> Boolean = { false },
    val motionTierProvider: () -> MotionTier = { MotionTier.Normal },
    val isLightBackgroundProvider: () -> Boolean = { false },
)

internal val LocalVideoCardTransitionBackgroundState = compositionLocalOf {
    VideoCardTransitionBackgroundState()
}

internal fun resolveVideoCardTransitionScrimAlpha(
    progress: Float,
    isLightBackground: Boolean,
    motionTier: MotionTier,
): Float {
    val clamped = progress.coerceIn(0f, 1f)
    val maxAlpha = when {
        isLightBackground && motionTier == MotionTier.Reduced ->
            VIDEO_CARD_TRANSITION_LIGHT_REDUCED_OPENING_SCRIM_ALPHA
        isLightBackground ->
            VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA_LIGHT
        else ->
            VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA_DARK
    }
    return maxAlpha * clamped
}

@Suppress("UNUSED_PARAMETER")
internal fun resolveVideoCardTransitionContentScale(
    progress: Float,
    phase: VideoCardTransitionBackgroundPhase,
    motionTier: MotionTier,
    isGestureRestoreInProgress: Boolean,
): Float {
    if (phase == VideoCardTransitionBackgroundPhase.IDLE || motionTier == MotionTier.Reduced) {
        return 1f
    }
    val depthProgress = resolveVideoCardTransitionDepthProgress(
        progress = progress,
        phase = phase,
    )
    return 1f - VIDEO_CARD_TRANSITION_MAX_CONTENT_SCALE_REDUCTION * depthProgress
}

internal fun resolveVideoCardTransitionBackgroundFrame(
    progress: Float,
    phase: VideoCardTransitionBackgroundPhase,
    motionTier: MotionTier = MotionTier.Normal,
    isLightBackground: Boolean = false,
    isGestureRestoreInProgress: Boolean = false,
    sdkInt: Int = Build.VERSION.SDK_INT,
): VideoCardTransitionBackgroundFrame {
    val clamped = progress.coerceIn(0f, 1f)
    val depthProgress = resolveVideoCardTransitionDepthProgress(
        progress = clamped,
        phase = phase,
    )
    val blurStrength = resolveVideoCardTransitionBlurStrength(depthProgress)
    val maxBlurRadiusPx = resolveVideoCardTransitionMaxBlurRadiusPx(motionTier)
    // 仅系统减弱动画(Reduced) / API<31 跳过 GPU 模糊；不按机型降级峰值。
    val rawBlurRadiusPx = if (
        phase != VideoCardTransitionBackgroundPhase.IDLE &&
        maxBlurRadiusPx > 0f &&
        sdkInt >= Build.VERSION_CODES.S
    ) {
        maxBlurRadiusPx * blurStrength
    } else {
        0f
    }

    return VideoCardTransitionBackgroundFrame(
        blurRadiusPx = quantizeVideoCardTransitionBlurRadius(
            radiusPx = rawBlurRadiusPx,
            maxRadiusPx = maxBlurRadiusPx,
        ),
        scrimAlpha = when (phase) {
            VideoCardTransitionBackgroundPhase.OPENING,
            VideoCardTransitionBackgroundPhase.HELD,
            VideoCardTransitionBackgroundPhase.RETURNING ->
                resolveVideoCardTransitionScrimAlpha(
                    progress = depthProgress,
                    isLightBackground = isLightBackground,
                    motionTier = motionTier,
                )
            VideoCardTransitionBackgroundPhase.IDLE -> 0f
        },
        contentScale = resolveVideoCardTransitionContentScale(
            progress = clamped,
            phase = phase,
            motionTier = motionTier,
            isGestureRestoreInProgress = isGestureRestoreInProgress,
        ),
        useLightScrimTint = isLightBackground,
    )
}

/**
 * 预测式返回手势进行中时，把系统回退进度(0→1)映射为背景虚化进度(1→0)。
 *
 * - 手势起点(0)保持满虚化，与 [VideoCardTransitionBackgroundPhase.HELD] 无缝衔接；
 * - 拖到底(1)则背景基本清晰，从而让全屏 GPU 模糊随手势实时消退，
 *   与共享元素 morph 落位同步，避免提交返回后再补一段独立模糊。
 */
internal fun resolveVideoCardTransitionBackgroundGestureProgress(
    backProgress: Float
): Float {
    val clamped = backProgress.coerceIn(0f, 1f)
    return 1f - clamped
}

/**
 * [VideoCardTransitionBackgroundPhase.OPENING] 阶段预测式返回：以当前开场虚化进度为起点，
 * 随手势线性消退至清晰。与 HELD 满值起点的 [resolveVideoCardTransitionBackgroundGestureProgress] 区分。
 */
internal fun resolveVideoCardTransitionBackgroundOpeningGestureProgress(
    openingBlurProgress: Float,
    backProgress: Float,
): Float {
    val clampedOpening = openingBlurProgress.coerceIn(0f, 1f)
    val clampedBack = backProgress.coerceIn(0f, 1f)
    return clampedOpening * (1f - clampedBack)
}

internal fun isVideoCardTransitionBackgroundGesturePhase(
    phase: VideoCardTransitionBackgroundPhase,
): Boolean {
    return phase == VideoCardTransitionBackgroundPhase.HELD ||
        phase == VideoCardTransitionBackgroundPhase.OPENING
}

internal fun resolveVideoCardTransitionBackgroundGestureBlurProgress(
    phase: VideoCardTransitionBackgroundPhase,
    currentBlurProgress: Float,
    backProgress: Float,
): Float {
    return when (phase) {
        VideoCardTransitionBackgroundPhase.HELD ->
            resolveVideoCardTransitionBackgroundGestureProgress(backProgress)
        VideoCardTransitionBackgroundPhase.OPENING ->
            resolveVideoCardTransitionBackgroundOpeningGestureProgress(
                openingBlurProgress = currentBlurProgress,
                backProgress = backProgress,
            )
        else -> currentBlurProgress
    }
}

/**
 * 景深返回与共享元素使用同一个满进度时长；被打断时只按实际剩余进度缩短，
 * 不再切换到另一套“快速返回”节奏。
 */
internal fun resolveVideoCardTransitionReturnFullDurationMillis(
    baseDurationMillis: Int,
): Int {
    return baseDurationMillis.coerceAtLeast(0)
}

/**
 * 返回动画提交时，若手势已消解部分虚化(startProgress < 1)，剩余 [RETURNING] 动画按比例缩短。
 *
 * 默认 [minDurationMs] 仅用于**非 morph 对齐**的取消收尾；与 shell morph 同墙钟时请用
 * [resolveMorphAlignedDepthClearDurationMs]（min=0，禁止把糊拖过落位）。
 */
internal fun resolveVideoCardTransitionBackgroundReturnDurationMs(
    startProgress: Float,
    fullDurationMs: Int = VIDEO_CARD_TRANSITION_BACKGROUND_RETURN_DURATION_MS,
    minDurationMs: Int = VIDEO_CARD_TRANSITION_BACKGROUND_CANCEL_DURATION_MS
): Int {
    val clamped = startProgress.coerceIn(0f, 1f)
    val safeFull = fullDurationMs.coerceAtLeast(0)
    val raw = (safeFull * clamped).roundToInt()
    if (minDurationMs <= 0) return raw.coerceIn(0, safeFull.coerceAtLeast(0))
    val safeMin = minDurationMs.coerceAtMost(safeFull.coerceAtLeast(minDurationMs))
    return raw.coerceIn(safeMin, safeFull.coerceAtLeast(safeMin))
}

/**
 * 景深消糊时长与 shared morph 剩余时长锁步。
 *
 * - morphRemainingMs：shell bounds 后半段（或满程）墙钟
 * - blurStartProgress：提交时剩余模糊 1→0
 * - 结果 = morph * blur，Linear 播完时二者同时到 0，避免「卡已落位、背景还糊」
 */
internal fun resolveMorphAlignedDepthClearDurationMs(
    morphRemainingMs: Int,
    blurStartProgress: Float,
): Int {
    return resolveVideoCardTransitionBackgroundReturnDurationMs(
        startProgress = blurStartProgress,
        fullDurationMs = morphRemainingMs.coerceAtLeast(0),
        minDurationMs = 0,
    )
}

/**
 * OPENING 中途被返回打断时，必须从当前 progress 反转，禁止先补完进场再关。
 */
internal fun shouldInterruptVideoCardOpeningOnReturn(
    phase: VideoCardTransitionBackgroundPhase,
): Boolean = phase == VideoCardTransitionBackgroundPhase.OPENING

/**
 * 是否立刻掐掉景深模糊，避免封面落位后仍带 BlurEffect 闪一下。
 *
 * - 打断 [OPENING]：shared 常先落位，景深按比例消糊会拖尾 → 必 snap
 * - [HELD]/[RETURNING] + 快速返回会话：同样可能落位快于消糊
 */
internal fun shouldSnapClearVideoCardDepthBlurOnQuickReturn(
    isQuickReturnFromDetail: Boolean,
    phase: VideoCardTransitionBackgroundPhase,
): Boolean {
    if (phase == VideoCardTransitionBackgroundPhase.OPENING) return true
    if (!isQuickReturnFromDetail) return false
    return phase == VideoCardTransitionBackgroundPhase.HELD ||
        phase == VideoCardTransitionBackgroundPhase.RETURNING
}

internal fun shouldApplyVideoCardTransitionBackgroundToRoute(
    entryRoute: String?,
    sourceRoute: String?,
    activeMainHostRoute: String?
): Boolean {
    val normalizedEntryRoute = normalizeVideoCardTransitionRoute(entryRoute) ?: return false
    val normalizedSourceRoute = normalizeVideoCardTransitionRoute(sourceRoute) ?: return false
    if (!isVideoCardReturnTargetRoute(normalizedSourceRoute)) return false
    if (normalizedEntryRoute == normalizedSourceRoute) return true
    val normalizedActiveMainHostRoute = normalizeVideoCardTransitionRoute(activeMainHostRoute)
    if (
        normalizedEntryRoute == "main_host" &&
        normalizedActiveMainHostRoute == normalizedSourceRoute
    ) {
        return true
    }
    // 首页顶栏内嵌分区：共享元素 source 是 partition，视觉宿主仍是 home / main_host(home)。
    if (normalizedSourceRoute == "partition") {
        if (normalizedEntryRoute == "home") return true
        if (normalizedEntryRoute == "main_host" && normalizedActiveMainHostRoute == "home") {
            return true
        }
    }
    return false
}

/**
 * 视频卡片过渡期间 Nav 层全屏 backdrop：填补 sharedBounds morph / 预测式返回
 * 在屏幕边缘露出的窗口底色，视觉上延续首页虚化后的色调。
 */
internal data class VideoCardTransitionNavBackdropFrame(
    val scrimAlpha: Float,
    val useLightScrimTint: Boolean,
)

internal fun shouldShowVideoCardTransitionNavBackdrop(
    cardTransitionEnabled: Boolean,
    phase: VideoCardTransitionBackgroundPhase,
    isVideoDetailOnStack: Boolean,
    isReturningToVideoDetail: Boolean = false,
): Boolean {
    if (!cardTransitionEnabled || isReturningToVideoDetail) return false
    // pop 提交后栈顶已是来源页，但共享壳仍在 overlay 中回收；背景必须留到 RETURNING 结束。
    if (phase == VideoCardTransitionBackgroundPhase.RETURNING) return true
    if (!isVideoDetailOnStack) return false
    return phase == VideoCardTransitionBackgroundPhase.HELD ||
        phase == VideoCardTransitionBackgroundPhase.OPENING
}

internal fun resolveVideoCardTransitionNavBackdropFrame(
    progress: Float,
    phase: VideoCardTransitionBackgroundPhase,
    isLightBackground: Boolean,
): VideoCardTransitionNavBackdropFrame {
    val clamped = progress.coerceIn(0f, 1f)
    val scrimAlpha = when (phase) {
        VideoCardTransitionBackgroundPhase.OPENING,
        VideoCardTransitionBackgroundPhase.HELD,
        VideoCardTransitionBackgroundPhase.RETURNING ->
            resolveVideoCardTransitionScrimAlpha(
                progress = clamped,
                isLightBackground = isLightBackground,
                motionTier = MotionTier.Normal,
            )
        else -> 0f
    }
    return VideoCardTransitionNavBackdropFrame(
        scrimAlpha = scrimAlpha,
        useLightScrimTint = isLightBackground,
    )
}

internal fun resolveVideoCardTransitionNavBackdropColor(
    baseBackgroundColor: Color,
    frame: VideoCardTransitionNavBackdropFrame,
): Color {
    return resolveVideoCardTransitionScaleGapFillColor(
        isLightBackground = frame.useLightScrimTint,
        scrimAlpha = frame.scrimAlpha,
        baseBackgroundColor = baseBackgroundColor,
    )
}

/**
 * 景深 scale<1 时，缩放层四周会露出父级/窗口底色。
 * 用与 blur scrim 同向的不透明填充盖住空隙，避免预测性返回读成右侧白条。
 */
internal fun shouldDrawVideoCardTransitionScaleGapFill(contentScale: Float): Boolean {
    return contentScale < 0.999f
}

internal fun resolveVideoCardTransitionScaleGapFillColor(
    isLightBackground: Boolean,
    scrimAlpha: Float,
    baseBackgroundColor: Color = if (isLightBackground) {
        Color.White
    } else {
        VIDEO_CARD_TRANSITION_DARK_GAP_BASE
    },
): Color {
    val tint = if (isLightBackground) {
        VIDEO_CARD_TRANSITION_LIGHT_SCRIM_TINT
    } else {
        Color.Black
    }
    val minTint = if (isLightBackground) {
        VIDEO_CARD_TRANSITION_SCALE_GAP_MIN_TINT_LIGHT
    } else {
        VIDEO_CARD_TRANSITION_SCALE_GAP_MIN_TINT_DARK
    }
    val fraction = maxOf(scrimAlpha, minTint).coerceIn(0f, 1f)
    return lerp(
        start = baseBackgroundColor,
        stop = tint,
        fraction = fraction,
    )
}

/**
 * 是否用「冻结 display list + 动态 blur/scale」路径。
 * Reduced / API<31 走轻量 scrim-only，避免无收益的 layer 开销。
 */
internal fun shouldUseVideoCardTransitionSnapshotBlur(
    phase: VideoCardTransitionBackgroundPhase,
    motionTier: MotionTier,
    realtimeBlurEnabled: Boolean = true,
    sdkInt: Int = Build.VERSION.SDK_INT,
): Boolean {
    if (phase == VideoCardTransitionBackgroundPhase.IDLE) return false
    if (motionTier == MotionTier.Reduced) return false
    if (!realtimeBlurEnabled) return false
    return sdkInt >= Build.VERSION_CODES.S
}

/**
 * 每帧内多次读取同一 frame 时，用 (progress, phase, …) 缓存避免重复纯函数计算。
 */
private class VideoCardTransitionBackgroundFrameCache {
    private var lastProgress = Float.NaN
    private var lastPhase: VideoCardTransitionBackgroundPhase? = null
    private var lastMotionTier: MotionTier? = null
    private var lastIsLightBackground: Boolean? = null
    private var lastGestureRestoreInProgress: Boolean? = null
    private var cached = VideoCardTransitionBackgroundFrame(
        blurRadiusPx = 0f,
        scrimAlpha = 0f,
        contentScale = 1f,
    )

    fun resolve(
        progress: Float,
        phase: VideoCardTransitionBackgroundPhase,
        motionTier: MotionTier,
        isLightBackground: Boolean,
        isGestureRestoreInProgress: Boolean,
    ): VideoCardTransitionBackgroundFrame {
        if (
            progress != lastProgress ||
            phase != lastPhase ||
            motionTier != lastMotionTier ||
            isLightBackground != lastIsLightBackground ||
            isGestureRestoreInProgress != lastGestureRestoreInProgress
        ) {
            lastProgress = progress
            lastPhase = phase
            lastMotionTier = motionTier
            lastIsLightBackground = isLightBackground
            lastGestureRestoreInProgress = isGestureRestoreInProgress
            cached = resolveVideoCardTransitionBackgroundFrame(
                progress = progress,
                phase = phase,
                motionTier = motionTier,
                isLightBackground = isLightBackground,
                isGestureRestoreInProgress = isGestureRestoreInProgress,
            )
        }
        return cached
    }
}

/**
 * 冻结层状态：开场首帧 record 后停止重录 feed，只对静态 display list
 * 更新 scale / BlurEffect / scrim，实现「看起来实时的动态模糊」与稳帧共存。
 */
private class VideoCardTransitionSnapshotLayerState {
    val frameCache = VideoCardTransitionBackgroundFrameCache()
    var freezeRecording: Boolean = false
    var hasRecordedContent: Boolean = false
    var lastBlurRadiusPx: Float = Float.NaN

    fun reset() {
        freezeRecording = false
        hasRecordedContent = false
        lastBlurRadiusPx = Float.NaN
    }
}

/**
 * 是否对来源页做每帧 live 重录。
 *
 * 真机 gfxinfo：OPENING/RETURNING 全页 live 重录+模糊会把 p90/p99 拉到百毫秒级
 *（Slow UI thread / Slow issue draw commands）。默认关闭 live，改用冻结层 +
 * 进度驱动 BlurEffect——仍是动态模糊观感，成本可控。
 */
internal fun shouldLiveRecordVideoCardTransitionSnapshot(
    phase: VideoCardTransitionBackgroundPhase,
): Boolean {
    return false
}

/**
 * 卡片开合景深：
 * - OPENING：首帧 record 一次后立刻冻结，BlurEffect/scale 跟进度（完整 20px 观感）
 * - HELD / RETURNING：复用冻结层，不每帧重录 feed
 * - IDLE：释放并恢复普通绘制
 * - Reduced / API 31 以下：不模糊，仅 scrim（无障碍/系统设置，非机型降级）
 */
@Composable
internal fun Modifier.videoCardTransitionBackgroundEffect(
    progressProvider: () -> Float,
    phaseProvider: () -> VideoCardTransitionBackgroundPhase,
    isGestureRestoreInProgressProvider: () -> Boolean = { false },
    motionTierProvider: () -> MotionTier = { MotionTier.Normal },
    isLightBackgroundProvider: () -> Boolean = { false },
    realtimeBlurEnabledProvider: () -> Boolean = { true },
): Modifier {
    val contentLayer = rememberGraphicsLayer()
    val snapshotState = remember { VideoCardTransitionSnapshotLayerState() }
    val phase = phaseProvider()
    val motionTier = motionTierProvider()
    val useSnapshotBlur = shouldUseVideoCardTransitionSnapshotBlur(
        phase = phase,
        motionTier = motionTier,
        realtimeBlurEnabled = realtimeBlurEnabledProvider(),
    )

    LaunchedEffect(phase, useSnapshotBlur) {
        if (!useSnapshotBlur) {
            snapshotState.reset()
            return@LaunchedEffect
        }
        when (phase) {
            VideoCardTransitionBackgroundPhase.OPENING -> {
                // 允许首帧立刻 record（完整模糊观感）；draw 侧只录一次后冻结。
                snapshotState.freezeRecording = false
                snapshotState.hasRecordedContent = false
                withFrameNanos { }
                snapshotState.freezeRecording = true
            }
            VideoCardTransitionBackgroundPhase.HELD,
            VideoCardTransitionBackgroundPhase.RETURNING -> {
                if (!snapshotState.hasRecordedContent) {
                    snapshotState.freezeRecording = false
                    withFrameNanos { }
                }
                snapshotState.freezeRecording = true
            }
            VideoCardTransitionBackgroundPhase.IDLE -> snapshotState.reset()
        }
    }

    val liveRecordingActive = useSnapshotBlur &&
        shouldLiveRecordVideoCardTransitionSnapshot(
            phase = phase,
        )
    VideoCardTransitionLiveBlurHitchLogger(
        phaseProvider = phaseProvider,
        liveRecordingActive = liveRecordingActive,
    )

    return this.drawWithContent {
        val activePhase = phaseProvider()
        val activeProgress = progressProvider()
        val activeMotionTier = motionTierProvider()
        val frame = snapshotState.frameCache.resolve(
            progress = activeProgress,
            phase = activePhase,
            motionTier = activeMotionTier,
            isLightBackground = isLightBackgroundProvider(),
            isGestureRestoreInProgress = isGestureRestoreInProgressProvider(),
        )
        val snapshotBlurActive = shouldUseVideoCardTransitionSnapshotBlur(
            phase = activePhase,
            motionTier = activeMotionTier,
            realtimeBlurEnabled = realtimeBlurEnabledProvider(),
        )

        if (!snapshotBlurActive) {
            // IDLE / Reduced / 低版本：正常绘制内容；需要时只叠 scrim。
            drawContent()
            if (frame.scrimAlpha > 0.001f) {
                val scrimColor = if (frame.useLightScrimTint) {
                    VIDEO_CARD_TRANSITION_LIGHT_SCRIM_TINT
                } else {
                    Color.Black
                }
                drawRect(scrimColor.copy(alpha = frame.scrimAlpha))
            }
            return@drawWithContent
        }

        // 只 record 一次：立刻有完整模糊观感，又避免 OPENING 每帧重录 feed 卡顿。
        if (!snapshotState.hasRecordedContent) {
            contentLayer.record {
                this@drawWithContent.drawContent()
            }
            if (size.width > 0f && size.height > 0f) {
                snapshotState.hasRecordedContent = true
                snapshotState.freezeRecording = true
            }
        }

        contentLayer.pivotOffset = Offset(size.width / 2f, size.height / 2f)
        contentLayer.scaleX = frame.contentScale
        contentLayer.scaleY = frame.contentScale
        if (frame.blurRadiusPx != snapshotState.lastBlurRadiusPx) {
            snapshotState.lastBlurRadiusPx = frame.blurRadiusPx
            contentLayer.renderEffect = if (frame.blurRadiusPx > 0.01f) {
                BlurEffect(
                    radiusX = frame.blurRadiusPx,
                    radiusY = frame.blurRadiusPx,
                    edgeTreatment = TileMode.Clamp,
                )
            } else {
                null
            }
        }
        if (shouldDrawVideoCardTransitionScaleGapFill(frame.contentScale)) {
            drawRect(
                resolveVideoCardTransitionScaleGapFillColor(
                    isLightBackground = frame.useLightScrimTint,
                    scrimAlpha = frame.scrimAlpha,
                )
            )
        }
        drawLayer(contentLayer)

        if (frame.scrimAlpha > 0.001f) {
            val scrimColor = if (frame.useLightScrimTint) {
                VIDEO_CARD_TRANSITION_LIGHT_SCRIM_TINT
            } else {
                Color.Black
            }
            drawRect(scrimColor.copy(alpha = frame.scrimAlpha))
        }
    }
}

/**
 * 进场/持有/返回：景深 progress 一律线性同源。
 *
 * 返回不再做 soft-clear 二次映射——shared morph 是 Linear，再 remap 会让模糊层
 * 落后于壳落位。遗留 [softClearVideoCardTransitionDepth] 仅供测试/兼容读取。
 */
internal fun resolveVideoCardTransitionDepthProgress(
    progress: Float,
    phase: VideoCardTransitionBackgroundPhase = VideoCardTransitionBackgroundPhase.OPENING,
): Float {
    @Suppress("UNUSED_PARAMETER")
    val ignored = phase
    return progress.coerceIn(0f, 1f)
}

/**
 * 遗留 soft-clear 曲线（主路径 RETURNING 已改线性锁步 morph）。
 * depth = 1 - (1 - p)^1.2，p=0.5 时约 0.56。
 */
internal fun softClearVideoCardTransitionDepth(progress: Float): Float {
    val remaining = (1f - progress.coerceIn(0f, 1f))
    val easedRemaining = remaining.toDouble().pow(1.2).toFloat()
    return (1f - easedRemaining).coerceIn(0f, 1f)
}

private fun resolveVideoCardTransitionBlurStrength(progress: Float): Float {
    // 与景深进度同源：模糊与背景下沉同步建立/消退，避免“先糊后沉”的分层错位。
    return progress.coerceIn(0f, 1f)
}

/**
 * 开合景深峰值模糊半径。
 * - Reduced（仅系统减弱动画）：0
 * - Normal / Enhanced：统一 20px，**不按机型降级**
 */
internal fun resolveVideoCardTransitionMaxBlurRadiusPx(
    motionTier: MotionTier,
): Float {
    return when (motionTier) {
        MotionTier.Reduced -> 0f
        MotionTier.Normal,
        MotionTier.Enhanced -> VIDEO_CARD_TRANSITION_MAX_BLUR_RADIUS_PX
    }
}

internal fun resolveVideoCardTransitionBlurQuantumPx(
    motionTier: MotionTier,
): Float {
    @Suppress("UNUSED_PARAMETER")
    val ignored = motionTier
    return VIDEO_CARD_TRANSITION_BLUR_QUANTUM_PX
}

private fun quantizeVideoCardTransitionBlurRadius(
    radiusPx: Float,
    maxRadiusPx: Float,
): Float {
    if (radiusPx <= 0f || maxRadiusPx <= 0f) return 0f
    return ((radiusPx / VIDEO_CARD_TRANSITION_BLUR_QUANTUM_PX).roundToInt() *
        VIDEO_CARD_TRANSITION_BLUR_QUANTUM_PX)
        .coerceIn(0f, maxRadiusPx)
}

private fun normalizeVideoCardTransitionRoute(route: String?): String? {
    val normalized = route?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (normalized.startsWith("home?category=")) {
        "home"
    } else {
        normalized.substringBefore("?")
    }
}
