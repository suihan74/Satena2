package com.suihan74.satena2.scene.entries.bottomSheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.EntryItem
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.launch

/**
 * エントリ項目のボトムメニューダイアログコンテンツ
 */
@Composable
fun CommentMenuContent(
    item: DisplayEntry?,
    readMarkVisible: Boolean,
    onDismissRequest: suspend ()->Unit,
    onLaunchBookmarksActivity: (DisplayEntry)->Unit,
    onEditBookmark: (DisplayEntry)->Unit,
    onDeleteBookmark: (DisplayEntry)->Unit
) {
    if (item == null) {
        Box(Modifier.height(1.dp)) {}
        return
    }

    val coroutineScope = rememberCoroutineScope()

    Column {
        Spacer(Modifier.height(12.dp))
        EntryItem(
            item = item,
            readMarkVisible = readMarkVisible,
            ellipsizeTitle = false
        )
        LazyColumn(
            Modifier.verticalScrollbar(
                state = rememberLazyListState(),
                color = CurrentTheme.primary
            )
        ) {
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_comment_menu_show_bookmark)) {
                    coroutineScope.launch {
                        onDismissRequest()
                        onLaunchBookmarksActivity(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_comment_menu_edit_bookmark)) {
                    coroutineScope.launch {
                        onEditBookmark(item)
                        onDismissRequest()
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_comment_menu_remove_bookmark)) {
                    coroutineScope.launch {
                        onDeleteBookmark(item)
                        onDismissRequest()
                    }
                }
            }
            emptyFooter(height = 32.dp)
        }
    }
}
