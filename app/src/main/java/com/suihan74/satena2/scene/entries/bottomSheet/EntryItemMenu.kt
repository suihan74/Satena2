package com.suihan74.satena2.scene.entries.bottomSheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.suihan74.hatena.model.account.Account
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuItem
import com.suihan74.satena2.compose.CombinedIconButton
import com.suihan74.satena2.compose.Tooltip
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.entries.Category
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.EntryItem
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.launch

/**
 * エントリ項目のボトムメニューダイアログコンテンツ
 */
@Composable
fun EntryItemMenuContent(
    item: DisplayEntry?,
    category: Category,
    account: Account?,
    readMarkVisible: Boolean,
    onDismissRequest: suspend ()->Unit,
    onLaunchBookmarksActivity: (DisplayEntry)->Unit,
    onLaunchBrowserActivity: (DisplayEntry)->Unit,
    onLaunchOuterBrowser: (DisplayEntry)->Unit,
    onShare: (DisplayEntry)->Unit,
    onNavigateSiteCategory: (DisplayEntry)->Unit,
    onFavorite: (DisplayEntry)->Unit,
    onUnFavorite: (DisplayEntry)->Unit,
    onCreateNgWord: (DisplayEntry)->Unit,
    onReadLater: (DisplayEntry, Boolean)->Unit,
    onRead: (DisplayEntry, Boolean)->Unit,
    onDeleteReadMark: (DisplayEntry)->Unit,
    onDeleteBookmark: (DisplayEntry)->Unit
) {
    if (item == null) {
        Box(Modifier.height(1.dp)) {}
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val signedIn = account != null

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
                BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_launch_bookmarks_activity)) {
                    coroutineScope.launch {
                        onDismissRequest()
                        onLaunchBookmarksActivity(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_launch_browser_activity)) {
                    coroutineScope.launch {
                        onDismissRequest()
                        onLaunchBrowserActivity(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_open_page_with_apps)) {
                    coroutineScope.launch {
                        onDismissRequest()
                        onLaunchOuterBrowser(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_share)) {
                    coroutineScope.launch {
                        onDismissRequest()
                        onShare(item)
                    }
                }
            }
            if (Category.Site != category) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_site_entries)) {
                        coroutineScope.launch {
                            onDismissRequest()
                            onNavigateSiteCategory(item)
                        }
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_favorite)) {
                    coroutineScope.launch {
                        onDismissRequest()
                        onFavorite(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_ng_word_setting)) {
                    coroutineScope.launch {
                        onCreateNgWord(item)
                    }
                }
            }
            if (signedIn) {
                item {
                    val isPrivate = remember { mutableStateOf(false) }
                    val readLaterTag = stringResource(R.string.read_later)
                    if (item.entry.bookmarkedData?.tags?.contains(readLaterTag) == true) {
                        ReadLaterMenuItem(text = stringResource(R.string.read), isPrivate = isPrivate) {
                            coroutineScope.launch {
                                onDismissRequest()
                                onRead(item, isPrivate.value)
                            }
                        }
                    }
                    else {
                        ReadLaterMenuItem(text = readLaterTag, isPrivate = isPrivate) {
                            coroutineScope.launch {
                                onDismissRequest()
                                onReadLater(item, isPrivate.value)
                            }
                        }
                    }
                }
            }
            if (item.read != null) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_remove_read_mark)) {
                        coroutineScope.launch {
                            onDismissRequest()
                            onDeleteReadMark(item)
                        }
                    }
                }
            }
            if (account != null && item.entry.bookmarkedData?.user == account.name) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.entry_item_menu_remove_bookmark)) {
                        coroutineScope.launch {
                            onDeleteBookmark(item)
                            onDismissRequest()
                        }
                    }
                }
            }
            emptyFooter(height = 32.dp)
        }
    }
}

@Composable
private fun ReadLaterMenuItem(
    text: String,
    isPrivate: MutableState<Boolean>,
    onClick: ()->Unit = {}
) {
    val privateTooltipVisible = remember { mutableStateOf(false) }
    BottomSheetMenuItem(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f)
            )
            CombinedIconButton(
                onClick = { isPrivate.value = !isPrivate.value },
                onLongClick = { privateTooltipVisible.value = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = "private",
                    tint =
                        if (isPrivate.value) CurrentTheme.onBackground
                        else CurrentTheme.grayTextColor
                )
                Tooltip(expanded = privateTooltipVisible) {
                    Text(stringResource(id = R.string.post_tooltip_private))
                }
            }
        }
    }
}
