package com.suihan74.satena2.scene.entries

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.hatena.model.entry.Issue
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomMenuItemButton
import com.suihan74.satena2.compose.HorizontalScrollableIndicator
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun BottomMenu(
    modifier: Modifier = Modifier,
    items: List<BottomMenuItem>,
    itemsArrangement: Arrangement.Horizontal,
    signedIn: Boolean,
    category: Category,
    issues: List<Issue>,
    contentPaddingValues: PaddingValues,
    onClickItem: (BottomMenuItem)->Unit = {},
    onLongClickItem: (BottomMenuItem)->Unit = {},
    onClickIssuesItem: ()->Unit = {},
    onClickSearchItem: ()->Unit = {},
) {
    val scrollState = rememberScrollState()
    val reverseScrolling = remember(itemsArrangement) { itemsArrangement == Arrangement.Start }

    BottomAppBar(
        cutoutShape = CircleShape,
        backgroundColor = CurrentTheme.bottomBarBackground,
        contentPadding = contentPaddingValues,
        modifier = modifier
            .background(Color.Transparent)
    ) {
        Box {
            Row(
                horizontalArrangement = itemsArrangement,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(
                        state = scrollState,
                        reverseScrolling = reverseScrolling
                    )
            ) {
                // カテゴリによって自動で追加する項目
                if (issues.isNotEmpty()) {
                    BottomMenuItemButton(
                        iconId = R.drawable.ic_view_list,
                        textId = R.string.issues,
                        onClick = onClickIssuesItem
                    )
                }

                when (category) {
                    Category.Search,
                    Category.MyBookmarks,
                    Category.SearchMyBookmarks -> {
                        BottomMenuItemButton(
                            iconId = R.drawable.ic_category_search,
                            textId = R.string.category_search,
                            onClick = onClickSearchItem
                        )
                    }

                    else -> {}
                }

                // ユーザーが設定した項目
                for (item in items) {
                    if (item.requireSignedIn && !signedIn) continue
                    BottomMenuItemButton(
                        iconId = item.iconId,
                        textId = item.textId,
                        onClick = { onClickItem(item) },
                        onLongClick = { tooltipVisible ->
                            if (item.longClickable) {
                                onLongClickItem(item)
                            }
                            else {
                                tooltipVisible.value = true
                            }
                        }
                    )
                }
            }
            HorizontalScrollableIndicator(
                scrollState = scrollState,
                gradientColor = CurrentTheme.bottomBarBackground
            )
        }
    }
}

/**
 * ボトムメニュー全項目をグリッド表示
 */
@Composable
fun BottomMenuItemsGrid(
    onClickItem: (BottomMenuItem)->Unit
) {
    val items = remember { BottomMenuItem.entries }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(64.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(CurrentTheme.bottomBarBackground)
            .fillMaxSize()
    ) {
        items(items) { item ->
            GridBottomMenuItem(
                item = item,
                onClickItem = { onClickItem(it) }
            )
        }
    }
}

/**
 * カテゴリグリッド項目
 */
@Composable
private fun GridBottomMenuItem(
    item: BottomMenuItem,
    onClickItem: (BottomMenuItem) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(CurrentTheme.drawerBackground)
            .size(width = 64.dp, height = 112.dp)
            .clickable { onClickItem(item) }
    ) {
        Image(
            painter = painterResource(item.iconId),
            contentDescription = "bottom menu icon",
            colorFilter = ColorFilter.tint(CurrentTheme.drawerOnBackground),
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 12.dp)
                .size(32.dp)
        )
        Text(
            text = stringResource(item.textId),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = CurrentTheme.drawerOnBackground,
            modifier = Modifier
        )
    }
}
