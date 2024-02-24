package com.suihan74.satena2.scene.bookmarks

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuItem
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * 選択したブクマにつけられたタグ一覧を表示するダイアログ
 */
@Composable
fun BookmarkUrlsMenuContent(
    item: DisplayBookmark?,
    onSelectUrl: (String)->Unit = {}
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
            items(item.urls) { link ->
                BottomSheetMenuItem(
                    text = Uri.decode(link.url),
                    icon = painterResource(id = R.drawable.ic_link)
                ) {
                    onSelectUrl(link.url)
                }
            }
        }
    }
}
