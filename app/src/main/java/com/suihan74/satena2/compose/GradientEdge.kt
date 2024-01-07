package com.suihan74.satena2.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 縦方向のグラデーション
 */
@Composable
fun VerticalGradientEdge(
    topColor: Color,
    bottomColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    Box(
        modifier
            .height(height)
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    0f to topColor, 1f to bottomColor
                )
            )
    )
}

/**
 * 横方向のグラデーション
 */
@Composable
fun HorizontalGradientEdge(
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    width: Dp = 10.dp
) {
    Box(
        modifier
            .width(width)
            .fillMaxHeight()
            .background(
                brush = Brush.horizontalGradient(
                    0f to startColor, 1f to endColor
                )
            )
    )
}

// ------ //

@Composable
fun BoxScope.VerticalScrollableIndicator(
    scrollState: ScrollState,
    gradientColor: Color,
    gradientHeight: Dp = 48.dp
) {
    if (scrollState.value > 0) {
        VerticalGradientEdge(
            topColor = gradientColor,
            bottomColor = Color.Transparent,
            height = gradientHeight,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (scrollState.value < scrollState.maxValue) {
        VerticalGradientEdge(
            topColor = Color.Transparent,
            bottomColor = gradientColor,
            height = gradientHeight,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BoxScope.HorizontalScrollableIndicator(
    scrollState: ScrollState,
    gradientColor: Color,
    gradientWidth: Dp = 48.dp
) {
    if (scrollState.value > 0) {
        HorizontalGradientEdge(
            startColor = gradientColor,
            endColor = Color.Transparent,
            width = gradientWidth,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }

    if (scrollState.value < scrollState.maxValue) {
        HorizontalGradientEdge(
            startColor = Color.Transparent,
            endColor = gradientColor,
            width = gradientWidth,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

// ------ //

@Composable
fun BoxScope.VerticalScrollableIndicator(
    lazyListState: LazyListState,
    gradientColor: Color,
    gradientHeight: Dp = 48.dp
) {
    VerticalScrollableIndicator(
        lazyListState = lazyListState,
        gradientColor = gradientColor,
        topGradientHeight = gradientHeight,
        bottomGradientHeight = gradientHeight
    )
}

@Composable
fun BoxScope.VerticalScrollableIndicator(
    lazyListState: LazyListState,
    gradientColor: Color,
    topGradientHeight: Dp = 48.dp,
    bottomGradientHeight: Dp = 48.dp
) {
    val firstVisibleItemIndex =
        remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val firstVisibleItemScrollOffset =
        remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }

    val totalItemsCount = remember { derivedStateOf { lazyListState.layoutInfo.totalItemsCount } }
    val lastItemInfo = remember { derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull() } }
    val lastItemIndex = remember { derivedStateOf { lastItemInfo.value?.index ?: 0 } }
    val lastItemOffset = remember { derivedStateOf { lastItemInfo.value?.offset ?: 0 } }

    if (firstVisibleItemIndex.value != 0 || firstVisibleItemScrollOffset.value != 0) {
        VerticalGradientEdge(
            topColor = gradientColor,
            bottomColor = Color.Transparent,
            height = topGradientHeight,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (lastItemIndex.value < totalItemsCount.value - 1 || lastItemOffset.value != 0 ) {
        VerticalGradientEdge(
            topColor = Color.Transparent,
            bottomColor = gradientColor,
            height = bottomGradientHeight,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ------ //

@Composable
fun BoxScope.HorizontalScrollableIndicator(
    lazyListState: LazyListState,
    gradientColor: Color,
    gradientWidth: Dp = 48.dp
) {
    HorizontalScrollableIndicator(
        lazyListState = lazyListState,
        gradientColor = gradientColor,
        startGradientWidth = gradientWidth,
        endGradientWidth = gradientWidth
    )
}

@Composable
fun BoxScope.HorizontalScrollableIndicator(
    lazyListState: LazyListState,
    gradientColor: Color,
    startGradientWidth: Dp = 48.dp,
    endGradientWidth: Dp = 48.dp
) {
    val firstVisibleItemIndex =
        remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val firstVisibleItemScrollOffset =
        remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }

    val totalItemsCount = remember { derivedStateOf { lazyListState.layoutInfo.totalItemsCount } }
    val lastItemInfo = remember { derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull() } }
    val lastItemIndex = remember { derivedStateOf { lastItemInfo.value?.index ?: 0 } }
    val lastItemOffset = remember { derivedStateOf { lastItemInfo.value?.offset ?: 0 } }

    if (firstVisibleItemIndex.value != 0 || firstVisibleItemScrollOffset.value != 0) {
        HorizontalGradientEdge(
            startColor = gradientColor,
            endColor = Color.Transparent,
            width = startGradientWidth,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }

    if (lastItemIndex.value < totalItemsCount.value - 1 || lastItemOffset.value != 0 ) {
        HorizontalGradientEdge(
            startColor = Color.Transparent,
            endColor = gradientColor,
            width = endGradientWidth,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
