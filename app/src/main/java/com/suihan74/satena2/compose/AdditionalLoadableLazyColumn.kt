package com.suihan74.satena2.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter

/**
 * スクロールバーを描画
 */
@SuppressLint("ComposableModifierFactory")
@Composable
fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = MaterialTheme.colors.primary
) : Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(duration),
        label = ""
    )
    return drawWithContent {
        drawContent()
        val layoutInfo = state.layoutInfo
        val first = layoutInfo.visibleItemsInfo.firstOrNull { it.key != "section" }
        if (first != null && (state.isScrollInProgress || alpha > 0.0f)) {
            val viewportSize = this.size.height
            val averageItemSize = layoutInfo.visibleItemsInfo.sumOf { it.size } / layoutInfo.visibleItemsInfo.size
            val estimatedTotalSize = averageItemSize * layoutInfo.totalItemsCount
            val viewportOffset = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
            val scrollbarOffsetY = viewportSize * viewportOffset / estimatedTotalSize
            val scrollbarHeight = viewportSize * viewportSize / estimatedTotalSize
            drawRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

// ------ //

/**
 * 末端へのスクロールによる追加ロードに対応したリストUI
 */
@Composable
inline fun <T> AdditionalLoadableLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top
        else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    noinline onAppearLastItem: ((Int) -> Unit)? = null,
    noinline key: ((T)->Any)? = null,
    crossinline footer: (LazyListScope.()->Unit) = {},
    crossinline itemContent: @Composable (LazyItemScope.(item: T) -> Unit)
) {
    LazyColumn(
        modifier = modifier,
        state = state.apply { OnAppearLastItem(onAppearLastItem) },
        contentPadding = contentPadding,
        flingBehavior = flingBehavior,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        reverseLayout = reverseLayout,
    ) {
        items(items, itemContent = itemContent, key = key)
        this.footer()
    }
}

/**
 * 末端へのスクロールによる追加ロードに対応したリストUI
 */
@Composable
fun AdditionalLoadableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top
        else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    onAppearLastItem: ((Int) -> Unit)? = null,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state.apply { OnAppearLastItem(onAppearLastItem) },
        contentPadding = contentPadding,
        flingBehavior = flingBehavior,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        reverseLayout = reverseLayout,
        content = content
    )
}

// ------ //

@Composable
fun LazyListState.OnAppearLastItem(onAppearLastItem: ((Int) -> Unit)?) {
    val isReachedToListEnd by remember {
        derivedStateOf {
            layoutInfo.visibleItemsInfo.size < layoutInfo.totalItemsCount &&
                layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { isReachedToListEnd }
            .filter { it }
            .collect {
                onAppearLastItem?.invoke(layoutInfo.totalItemsCount)
            }
    }
}
