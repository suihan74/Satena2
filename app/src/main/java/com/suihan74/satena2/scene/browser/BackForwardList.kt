package com.suihan74.satena2.scene.browser

import android.net.Uri
import android.webkit.WebHistoryItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.compose.MarqueeText
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.launch

@Composable
fun BackForwardList(
    viewModel: BrowserViewModel,
    onClickItem: suspend ()->Unit
) {
    val lazyListState = rememberLazyListState()
    val backForwardList = viewModel.backForwardList.collectAsState().value
    val listSize = remember(backForwardList) { backForwardList?.size ?: 0 }
    val items = remember(backForwardList) {
        backForwardList?.let {
            buildList {
                for (i in 0 until listSize) {
                    add(backForwardList.getItemAtIndex(i))
                }
            }
        } ?: emptyList()
    }
    val currentIdx = remember { backForwardList?.currentIndex }

    Column(
        Modifier.fillMaxWidth()
    ) {
        Text(
            text = "履歴",
            fontSize = 18.sp,
            color = CurrentTheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .verticalScrollbar(
                    state = lazyListState,
                    color = CurrentTheme.primary
                )
        ) {
            items.reversed().forEachIndexed { index, item ->
                item {
                    if (currentIdx == items.lastIndex - index) {
                        BackForwardListItem(
                            item = item,
                            background = CurrentTheme.primary,
                            foreground = CurrentTheme.onPrimary,
                            subTextColor = CurrentTheme.onPrimary,
                            onClick = {
                                viewModel.refresh()
                                onClickItem()
                            }
                        )
                    }
                    else {
                        BackForwardListItem(
                            item = item,
                            background = CurrentTheme.background,
                            foreground = CurrentTheme.onBackground,
                            subTextColor = CurrentTheme.grayTextColor,
                            onClick = {
                                viewModel.goBackOrForward(it)
                                onClickItem()
                            }
                        )
                    }
                }
            }
        }
    }
}

// ------ //

@Composable
private fun BackForwardListItem(
    item: WebHistoryItem,
    background: Color,
    foreground: Color,
    subTextColor: Color,
    onClick: suspend (WebHistoryItem)->Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        Modifier
            .clickable { coroutineScope.launch { onClick(item) } }
            .fillMaxWidth()
            .background(background)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        MarqueeText(
            text = item.title,
            fontSize = 14.sp,
            color = foreground,
            gradientColor = background
        )
        MarqueeText(
            text = Uri.decode(item.url),
            fontSize = 12.sp,
            color = subTextColor,
            gradientColor = background
        )
    }
}
