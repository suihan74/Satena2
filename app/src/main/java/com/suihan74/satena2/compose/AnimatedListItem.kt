package com.suihan74.satena2.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * 挿入アニメーションを含む`LazyColumn`の項目
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.AnimatedListItem(
    modifier: Modifier = Modifier,
    durationMillis: Int = 180,
    content: @Composable ()->Unit
) {
    val visibility = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibility,
        enter = fadeIn(animationSpec = tween(durationMillis)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> fullWidth / 4 }
                ),
        modifier = modifier.animateItemPlacement(),
    ) {
        content()
    }
}
