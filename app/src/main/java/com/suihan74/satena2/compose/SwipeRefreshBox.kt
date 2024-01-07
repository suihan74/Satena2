package com.suihan74.satena2.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * ひっぱって更新する領域の設定を簡略化するやつ
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeRefreshBox(
    refreshing: Boolean,
    state: PullRefreshState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indicatorBackgroundColor: Color = CurrentTheme.primary,
    indicatorContentColor: Color = CurrentTheme.onPrimary,
    content: @Composable ()->Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(state = state, enabled = enabled)
    ) {
        content()

        PullRefreshIndicator(
            refreshing = refreshing,
            state = state,
            backgroundColor = indicatorBackgroundColor,
            contentColor = indicatorContentColor,
            scale = true,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
