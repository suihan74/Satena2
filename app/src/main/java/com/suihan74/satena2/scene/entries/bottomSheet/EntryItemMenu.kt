package com.suihan74.satena2.scene.entries.bottomSheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.suihan74.hatena.model.account.Account
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.*
import com.suihan74.satena2.scene.entries.Category
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.EntryItem
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.launch

/**
 * エントリ項目のボトムメニューダイアログコンテンツ
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EntryItemMenuContent(
    item: DisplayEntry?,
    sheetState: ModalBottomSheetState,
    category: Category,
    account: Account?,
    readMarkVisible: Boolean,
    onLaunchBookmarksActivity: (DisplayEntry)->Unit = {},
    onLaunchBrowserActivity: (DisplayEntry)->Unit = {},
    onLaunchOuterBrowser: (DisplayEntry)->Unit = {},
    onShare: (DisplayEntry)->Unit = {},
    onNavigateSiteCategory: (DisplayEntry)->Unit = {},
    onFavorite: (DisplayEntry)->Unit = {},
    onUnFavorite: (DisplayEntry)->Unit = {},
    onCreateNgWord: (DisplayEntry)->Unit = {},
    onReadLater: (DisplayEntry, Boolean)->Unit = { _, _ -> },
    onRead: (DisplayEntry, Boolean)->Unit = { _, _ -> },
    onDeleteReadMark: (DisplayEntry)->Unit = {},
    onDeleteBookmark: (DisplayEntry)->Unit = {}
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
        )
        LazyColumn(
            Modifier.verticalScrollbar(
                state = rememberLazyListState(),
                color = CurrentTheme.primary
            )
        ) {
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_launch_bookmarks_activity)) {
                    coroutineScope.launch {
                        sheetState.hide()
                        onLaunchBookmarksActivity(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_launch_browser_activity)) {
                    coroutineScope.launch {
                        sheetState.hide()
                        onLaunchBrowserActivity(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_open_page_with_apps)) {
                    coroutineScope.launch {
                        sheetState.hide()
                        onLaunchOuterBrowser(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_share)) {
                    coroutineScope.launch {
                        sheetState.hide()
                        onShare(item)
                    }
                }
            }
            if (Category.Site != category) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_site_entries)) {
                        coroutineScope.launch {
                            sheetState.hide()
                            onNavigateSiteCategory(item)
                        }
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_favorite)) {
                    coroutineScope.launch {
                        sheetState.hide()
                        onFavorite(item)
                    }
                }
            }
            item {
                BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_ng_word_setting)) {
                    coroutineScope.launch {
//                        sheetState.hide()
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
                                sheetState.hide()
                                onRead(item, isPrivate.value)
                            }
                        }
                    }
                    else {
                        ReadLaterMenuItem(text = readLaterTag, isPrivate = isPrivate) {
                            coroutineScope.launch {
                                sheetState.hide()
                                onReadLater(item, isPrivate.value)
                            }
                        }
                    }
                }
            }
            if (item.read != null) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_remove_read_mark)) {
                        coroutineScope.launch {
                            sheetState.hide()
                            onDeleteReadMark(item)
                        }
                    }
                }
            }
            if (account != null && item.entry.bookmarkedData?.user == account.name) {
                item {
                    BottomSheetMenuItem(text = stringResource(R.string.entry_menu_dialog_remove_bookmark)) {
                        coroutineScope.launch {
                            onDeleteBookmark(item)
                            sheetState.hide()
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
