package com.suihan74.satena2.scene.entries.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.EntriesViewModel
import com.suihan74.satena2.scene.entries.EntryItem
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * 除外されたエントリ一覧を表示するボトムシートコンテンツ
 */
@Composable
fun ExcludedEntriesList(
    items: List<DisplayEntry>,
    onClickItem: ((DisplayEntry)->Unit)? = null,
    onLongClickItem: ((DisplayEntry)->Unit)? = null,
    onDoubleClickItem: ((DisplayEntry)->Unit)? = null,
    onClickItemEdge: (DisplayEntry)->Unit = {},
    onLongClickItemEdge: (DisplayEntry)->Unit = {},
    onDoubleClickItemEdge: (DisplayEntry)->Unit = {},
    onClickItemComment: (DisplayEntry, BookmarkResult)->Unit = { _, _ -> },
    onLongClickItemComment: (DisplayEntry, BookmarkResult)->Unit = { _, _ -> }
) {
    val lazyListState = rememberLazyListState()

    Column {
        Text(
            text = stringResource(R.string.excluded_entries_list_title),
            fontSize = 18.sp,
            color = CurrentTheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollbar(
                    state = lazyListState,
                    color = CurrentTheme.primary
                )
        ) {
            items(items) { entry ->
                EntryItem(
                    item = entry,
                    onClick = onClickItem,
                    onLongClick = onLongClickItem,
                    onDoubleClick = onDoubleClickItem,
                    onClickEdge = onClickItemEdge,
                    onLongClickEdge = onLongClickItemEdge,
                    onDoubleClickEdge = onDoubleClickItemEdge,
                    onClickComment = onClickItemComment,
                    onLongClickComment = onLongClickItemComment
                )
                Divider(
                    color = CurrentTheme.listItemDivider,
                    thickness = 1.dp
                )
            }
        }
    }
}
