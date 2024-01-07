package com.suihan74.satena2.scene.entries

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.hatena.model.entry.MaintenanceEntry
import com.suihan74.satena2.compose.*
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.appendRegex
import com.suihan74.satena2.utility.extension.zonedString

/**
 * メンテナンス情報リスト表示用の画面
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MaintenanceInformationContent(
    id: String,
    viewModel: EntriesViewModel,
    entries: List<MaintenanceEntry>,
    loading: Boolean,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = {
            viewModel.swipeRefresh(
                Destination(category = Category.Maintenance, tabIndex = 0)
            )
        }
    )
    val dialogTarget = remember { mutableStateOf<MaintenanceEntry?>(null) }

    LaunchedEffect(true) {
        viewModel.swipeRefresh(
            Destination(category = Category.Maintenance, tabIndex = 0)
        )
    }

    Box {
        SwipeRefreshBox(
            refreshing = loading,
            state = pullRefreshState,
        ) {
            AdditionalLoadableLazyColumn(
                items = entries,
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(
                        state = lazyListState,
                        color = CurrentTheme.primary
                    ),
                footer = { emptyFooter() }
            ) { item ->
                MaintenanceItem(
                    entry = item,
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
@OptIn(ExperimentalTextApi::class)
@Composable
fun MaintenanceItem(
    entry: MaintenanceEntry,
    onLongClick: (MaintenanceEntry)->Unit
) {
    val bodyVisible = remember { mutableStateOf(false) }
    val timestampText = buildString {
        append(entry.createdAt.zonedString("uuuu-MM-dd HH:mm:ss"))
        if (entry.updatedAt.isAfter(entry.createdAt)) {
            append(
                " (更新:",
                entry.updatedAt.zonedString("uuuu-MM-dd HH:mm:ss"),
                ")"
            )
        }
    }
    val titleText = remember(entry) { entry.annotatedTitle() }
    val linkColor = CurrentTheme.primary
    val bodyText = remember(entry) { entry.annotatedBody(linkColor) }
    val clickableTextState = rememberClickableTextState(
        onClick = { bodyVisible.value = !bodyVisible.value },
        onLongClick = { onLongClick(entry) }
    )

    Column(
        Modifier
            .fillMaxWidth()
            .combinedClickable(clickableTextState = clickableTextState)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = titleText,
                color = CurrentTheme.onBackground,
                fontSize = 15.sp
            )
            SingleLineText(
                text = timestampText,
                color = CurrentTheme.grayTextColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (bodyVisible.value) {
                ClickableText(
                    text = bodyText,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    style = LocalTextStyle.current.copy(
                        color = CurrentTheme.onBackground,
                        fontSize = 13.sp
                    ),
                    state = clickableTextState,
                    onClick = {
                        bodyText.getUrlAnnotations(it, it).firstOrNull()?.let { link ->
                            // todo
                            Log.i("anno", "clicked: ${link.item.url}")
                            true
                        } ?: false
                    },
                    onLongClick = {
                        bodyText.getUrlAnnotations(it, it).firstOrNull()?.let { link ->
                            // todo
                            true
                        } ?: false
                    }
                )
            }
        }
        Divider(
            Modifier.fillMaxWidth(),
            color = CurrentTheme.listItemDivider,
            thickness = 1.dp
        )
    }
}

/**
 * タイトルに含まれる【完了】【復旧済み】を色付け
 */
private fun MaintenanceEntry.annotatedTitle() = buildAnnotatedString {
    val resolvedRegex = Regex("""【.*(完了|復旧済).*】""")
    val match = resolvedRegex.find(title)
        ?: run {
            append(title)
            return@buildAnnotatedString
        }
    if (match.range.first > 0) {
        append(title.substring(0, match.range.first))
    }
    withStyle(SpanStyle(color = Color.Green)) {
        append(title.substring(match.range))
    }
    if (match.range.last < title.lastIndex) {
        append(title.substring(match.range.last + 1))
    }
}

@OptIn(ExperimentalTextApi::class)
private fun MaintenanceEntry.annotatedBody(linkColor: Color) = buildAnnotatedString {
    val linkRegex = Regex("""<a href="(.+)">(.*)</a>""")
    appendRegex(linkRegex, body) { m ->
        val url = m.groupValues[1]
        val text = m.groupValues[2]
        withAnnotation(urlAnnotation = UrlAnnotation(url)) {
            withStyle(
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(text)
            }
        }
    }
}
