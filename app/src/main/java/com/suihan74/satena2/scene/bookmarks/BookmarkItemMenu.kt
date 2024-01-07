package com.suihan74.satena2.scene.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuItem
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.launch

/**
 * ブクマ項目のボトムメニューダイアログコンテンツ
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarkItemMenuContent(
    item: DisplayBookmark?,
    sheetState: ModalBottomSheetState,
    onShowRecentBookmarks: (DisplayBookmark)->Unit = {},
    onShowBookmarksToItem: (DisplayBookmark)->Unit = {},
    onShowUserLabelDialog: (DisplayBookmark)->Unit = {},
    onShare: (DisplayBookmark)->Unit = {},
    onFollow: (DisplayBookmark)->Unit = {},
    onIgnore: (DisplayBookmark)->Unit = {},
) {
    if (item == null) {
        Box(Modifier.fillMaxHeight())
        return
    }
    val coroutineScope = rememberCoroutineScope()
    Column {
        Spacer(Modifier.height(12.dp))
        BookmarkItem(item = item, clickable = false)
        LazyColumn(
            Modifier.verticalScrollbar(
                state = rememberLazyListState(),
                color = CurrentTheme.primary
            )
        ) {
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_show_recent_bookmarks)) {
                    onShowRecentBookmarks(item)
                    coroutineScope.launch { sheetState.hide() }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_show_bookmarks_to_bookmark)) {
                    onShowBookmarksToItem(item)
                    coroutineScope.launch { sheetState.hide() }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_share)) {
                    coroutineScope.launch {
                        sheetState.hide()
                        onShare(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_follow)) {
                    onFollow(item)
                    coroutineScope.launch { sheetState.hide() }
                }
            }
            item {
                BottomSheetMenuItem(
                    text = stringResource(
                        if (item.ignoredUser) R.string.bookmark_menu_unmute
                        else R.string.bookmark_menu_mute
                    )
                ) {
                    onIgnore(item)
                    coroutineScope.launch { sheetState.hide() }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_add_ng_word)) {}
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_report)) {}
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_manage_user_labels)) {
                    onShowUserLabelDialog(item)
                }
            }
        }
    }
}
