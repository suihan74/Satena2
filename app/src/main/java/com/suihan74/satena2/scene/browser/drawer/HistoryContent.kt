package com.suihan74.satena2.scene.browser.drawer

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AdditionalLoadableLazyColumn
import com.suihan74.satena2.compose.MarqueeText
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.VerticalScrollableIndicator
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.model.browser.History
import com.suihan74.satena2.scene.browser.BrowserViewModel
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.zonedString
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryContent(
    viewModel: BrowserViewModel,
    drawerState: DrawerState
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = remember { LazyListState() }

    val rawHistories by viewModel.histories.collectAsState()
    val displayHistories = remember(rawHistories) {
        rawHistories
            .sortedByDescending { it.log.visitedAt }
            .groupBy { it.log.visitedAt.atZone(ZoneId.systemDefault()).toLocalDate() }
    }

    val dateFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.browser_histories_section))

    Box(
        Modifier.fillMaxSize()
    ) {
        AdditionalLoadableLazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(
                    state = lazyListState,
                    color = CurrentTheme.primary
                ),
            onAppearLastItem = {
                viewModel.additionalLoadHistories()
            },
        ) {
            for ((date, histories) in displayHistories) {
                item {
                    Section(
                        title = dateFormatter.format(date)
                    )
                }

                items(histories) {
                    HistoryItem(
                        history = it,
                        faviconDir = viewModel.faviconPath,
                        onClick = { h ->
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            viewModel.loadUrl(h.page.page.url)
                        },
                        onLongClick = { h ->

                        }
                    )
                    Divider(
                        color = CurrentTheme.listItemDivider,
                        thickness = 1.dp
                    )
                }
            }
            emptyFooter()
        }
        VerticalScrollableIndicator(
            lazyListState = lazyListState,
            gradientColor = CurrentTheme.drawerBackground
        )
    }
}

// ------ //

@Composable
private fun HistoryItem(
    history: History,
    faviconDir: String,
    onClick: (History)->Unit,
    onLongClick: (History)->Unit
) {
    val faviconPath = history.page.faviconInfo?.let { "$faviconDir/${it.filename}" }

    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(history) },
                onLongClick = { onLongClick(history) }
            )
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        AsyncImage(
            model = faviconPath,
            error = painterResource(id = R.drawable.ic_file),
            contentDescription = "favicon",
            modifier = Modifier
                .padding(top = 4.dp)
                .size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Column(
            Modifier.weight(1f)
        ) {
            MarqueeText(
                text = history.page.page.title,
                fontSize = 16.sp,
                color = CurrentTheme.drawerOnBackground,
                gradientColor = CurrentTheme.drawerBackground
            )
            Spacer(Modifier.height(2.dp))
            MarqueeText(
                text = Uri.decode(history.page.page.url),
                fontSize = 13.sp,
                color = CurrentTheme.grayTextColor,
                gradientColor = CurrentTheme.drawerBackground
            )
            Spacer(Modifier.height(2.dp))
            SingleLineText(
                text = history.log.visitedAt.zonedString("uuuu/MM/dd hh:mm"),
                fontSize = 13.sp,
                color = CurrentTheme.grayTextColor
            )
        }
    }
}
