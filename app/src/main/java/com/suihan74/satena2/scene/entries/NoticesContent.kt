package com.suihan74.satena2.scene.entries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suihan74.hatena.model.account.Notice
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.*
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.zonedString
import com.suihan74.satena2.utility.hatena.annotatedMessage
import com.suihan74.satena2.utility.hatena.users

/**
 * 通知リスト表示用の画面
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoticesContent(
    id: String,
    viewModel: EntriesViewModel,
    notices: List<Notice>,
    loading: Boolean,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = {
            viewModel.swipeRefresh(
                Destination(category = Category.Notices, tabIndex = 0)
            )
        }
    )
    val dialogTarget = remember { mutableStateOf<Notice?>(null) }

    LaunchedEffect(true) {
        viewModel.swipeRefresh(
            Destination(category = Category.Notices, tabIndex = 0)
        )
    }

    Box {
        SwipeRefreshBox(
            refreshing = loading,
            state = pullRefreshState
        ) {
            AdditionalLoadableLazyColumn(
                items = notices,
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(
                        state = lazyListState,
                        color = CurrentTheme.primary
                    ),
                onAppearLastItem = {
                    viewModel.loadAdditional(
                        Destination(
                            category = Category.Notices,
                            tabIndex = 0
                        )
                    )
                },
                footer = { emptyFooter() }
            ) { notice ->
                NoticeItem(
                    notice = notice,
                    onClick = { viewModel.onClick(notice) },
                    onLongClick = { dialogTarget.value = it }
                )
            }
        }
        VerticalScrollableIndicator(
            lazyListState = lazyListState,
            gradientColor = CurrentTheme.background,
            topGradientHeight = 48.dp,
            bottomGradientHeight = 80.dp
        )
    }

    // 項目メニューダイアログ
    if (dialogTarget.value != null) {
        // todo
    }
}

// ------ //

/**
 * 通知アイテム
 */
@Composable
fun NoticeItem(
    notice: Notice,
    onClick: (Notice)->Unit,
    onLongClick: (Notice)->Unit
) {
    val context = LocalContext.current
    val iconUrl = notice.users.first().let {
        "https://cdn1.www.st-hatena.com/users/$it/profile.gif"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(notice) },
                onLongClick = { onLongClick(notice) },
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(iconUrl)
                .error(R.drawable.ic_file)
                .build(),
            contentDescription = "icon",
            modifier = Modifier.size(48.dp)
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            Text(
                text = notice.annotatedMessage(),
                color = CurrentTheme.onBackground,
                fontSize = 14.sp
            )
            SingleLineText(
                text = notice.modified.zonedString("yyyy-MM-dd HH:mm"),
                color = CurrentTheme.grayTextColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
