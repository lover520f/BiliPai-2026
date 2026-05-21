package com.android.purebilibili.navigation3

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.android.purebilibili.core.ui.ProvideAnimatedVisibilityScope
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute

@Composable
internal fun BiliPaiNavDisplayHost(
    backStack: List<BiliPaiNavKey>,
    motionMode: BiliPaiNavMotionMode,
    sourceMetadata: BiliPaiNavSourceMetadata,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    visibleBottomBarRoutes: Set<String> = emptySet(),
    content: @Composable (BiliPaiNavKey) -> Unit
) {
    val safeBackStack = remember(backStack) {
        backStack.ifEmpty { listOf(BiliPaiNavKey.MainHost) }
    }
    val application = LocalContext.current.applicationContext as Application
    val scopedContent: @Composable (BiliPaiNavKey) -> Unit = remember(content, application) {
        { key ->
            ProvideAnimatedVisibilityScope(
                animatedVisibilityScope = LocalNavAnimatedContentScope.current
            ) {
                CompositionLocalProvider(
                    LocalVideoCardSharedElementSourceRoute provides key.toLegacyRoute()
                ) {
                    ProvideNavigation3ViewModelApplicationExtras(application) {
                        content(key)
                    }
                }
            }
        }
    }
    val entryProvider = remember(sourceMetadata, visibleBottomBarRoutes, scopedContent) {
        biliPaiNavEntryProvider(
            sourceMetadata = sourceMetadata,
            visibleBottomBarRoutes = visibleBottomBarRoutes,
            content = scopedContent
        )
    }
    val predictivePopRouteTransition = remember(motionMode, sourceMetadata, safeBackStack) {
        resolveBiliPaiNavDisplayPredictivePopRouteTransition(
            motionMode = motionMode,
            sourceMetadata = sourceMetadata,
            fromKey = safeBackStack.lastOrNull(),
            toKey = safeBackStack.getOrNull(safeBackStack.lastIndex - 1)
        )
    }

    NavDisplay(
        backStack = safeBackStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        sharedTransitionScope = sharedTransitionScope,
        transitionSpec = {
            resolveBiliPaiNavContentTransform(BiliPaiNavRouteTransition.FALLBACK)
        },
        popTransitionSpec = {
            resolveBiliPaiNavContentTransform(BiliPaiNavRouteTransition.FALLBACK)
        },
        predictivePopTransitionSpec = {
            resolveBiliPaiNavContentTransform(predictivePopRouteTransition)
        },
        entryProvider = entryProvider
    )

    BackHandler(
        enabled = shouldInterceptSystemBackForNavigation3(
            mode = motionMode,
            appBackActionRequiresInterception = false
        ),
        onBack = onBack
    )
}

@Composable
private fun ProvideNavigation3ViewModelApplicationExtras(
    application: Application,
    content: @Composable () -> Unit
) {
    val navEntryOwner = LocalViewModelStoreOwner.current
    if (navEntryOwner == null) {
        content()
        return
    }

    val patchedOwner = remember(navEntryOwner, application) {
        buildNavigation3ViewModelStoreOwner(navEntryOwner, application)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides patchedOwner) {
        content()
    }
}

private fun buildNavigation3ViewModelStoreOwner(
    navEntryOwner: ViewModelStoreOwner,
    application: Application
): ViewModelStoreOwner {
    val defaultFactoryOwner = navEntryOwner as? HasDefaultViewModelProviderFactory
    val defaultCreationExtras = defaultFactoryOwner?.defaultViewModelCreationExtras
        ?: CreationExtras.Empty
    val patchedCreationExtras = MutableCreationExtras(defaultCreationExtras).apply {
        set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, application)
    }

    return object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = navEntryOwner.viewModelStore
        override val defaultViewModelProviderFactory =
            defaultFactoryOwner?.defaultViewModelProviderFactory
                ?: ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        override val defaultViewModelCreationExtras: CreationExtras = patchedCreationExtras
    }
}
