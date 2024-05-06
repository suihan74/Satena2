package com.suihan74.satena2.scene.bookmarks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuItem
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * ブクマ項目のボトムメニューダイアログコンテンツ
 */
@Composable
fun BookmarkItemMenuContent(
    item: DisplayBookmark?,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onShowRecentBookmarks: suspend (DisplayBookmark)->Unit,
    onShowBookmarksToItem: suspend (DisplayBookmark)->Unit,
    onShowUserLabelDialog: suspend (DisplayBookmark)->Unit,
    onSelectUrlsMenu: suspend (DisplayBookmark)->Unit,
    onSelectTagsMenu: suspend (DisplayBookmark)->Unit,
    onSelectNgWordsMenu: suspend (DisplayBookmark)->Unit,
    onShare: suspend (DisplayBookmark)->Unit,
    onFollow: suspend (DisplayBookmark)->Unit,
    onIgnore: suspend (DisplayBookmark)->Unit,
    onReport: suspend (DisplayBookmark)->Unit,
    onDeleteMyBookmark: suspend (DisplayBookmark)->Unit
) {
    if (item == null) {
        Box(Modifier.fillMaxHeight())
        return
    }
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
                    coroutineScope.launch {
                        onShowRecentBookmarks(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_show_bookmarks_to_bookmark)) {
                    coroutineScope.launch {
                        onShowBookmarksToItem(item)
                    }
                }
            }
            if (item.urls.isNotEmpty()) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_links)) {
                        coroutineScope.launch {
                            onSelectUrlsMenu(item)
                        }
                    }
                }
            }
            if (item.bookmark.tags.isNotEmpty()) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.tag)) {
                        coroutineScope.launch {
                            onSelectTagsMenu(item)
                        }
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_share)) {
                    coroutineScope.launch {
                        onShare(item)
                    }
                }
            }
            if (!item.isMyBookmark) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_follow)) {
                        coroutineScope.launch {
                            onFollow(item)
                        }
                    }
                }
                item {
                    BottomSheetMenuItem(
                        text = stringResource(
                            if (item.ignoredUser) R.string.bookmark_menu_unmute
                            else R.string.bookmark_menu_mute
                        )
                    ) {
                        coroutineScope.launch {
                            onIgnore(item)
                        }
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_add_ng_word)) {
                    coroutineScope.launch {
                        onSelectNgWordsMenu(item)
                    }
                }
            }
            if (item.isMyBookmark) {
                item {
                    BottomSheetMenuItem(
                        text = stringResource(R.string.bookmark_menu_delete_my_bookmark),
                        color = Color(0xFD, 0x82, 0x82, 0xFF)
                    ) {
                        coroutineScope.launch {
                            onDeleteMyBookmark(item)
                        }
                    }
                }
            }
            else {
                item {
                    BottomSheetMenuItem(
                        text = stringResource(R.string.bookmark_menu_report),
                        color = Color(0xFD, 0x82, 0x82, 0xFF)
                    ) {
                        coroutineScope.launch {
                            onReport(item)
                        }
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.bookmark_menu_manage_user_labels)) {
                    coroutineScope.launch {
                        onShowUserLabelDialog(item)
                    }
                }
            }
        }
    }
}
