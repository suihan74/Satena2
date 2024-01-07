package com.suihan74.satena2.scene.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.suihan74.hatena.model.entry.Issue
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomMenuItemButton
import com.suihan74.satena2.compose.HorizontalScrollableIndicator
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun BottomMenu(
    items: List<BottomMenuItem>,
    itemsArrangement: Arrangement.Horizontal,
    signedIn: Boolean,
    category: Category,
    issues: List<Issue>,
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
        contentPadding = PaddingValues(start = 0.dp, end = 88.dp),
        modifier = Modifier.background(Color.Transparent)
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
