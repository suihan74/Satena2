package com.suihan74.satena2.scene.bookmarks

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.EntryItem
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.StarsCountText
import com.suihan74.satena2.compose.VerticalScrollableIndicator
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.EntryActionHandler
import com.suihan74.satena2.scene.entries.EntryItem
import com.suihan74.satena2.scene.entries.EntryItemEvent
import com.suihan74.satena2.scene.entries.FakeEntryActionHandler
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.hatena.actualUrl
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * ドロワの表示内容
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DrawerContent(
    entryActionHandler: EntryActionHandler,
    entity: Entity,
    launchBrowser: (String)->Unit,
    downStair: ()->Unit,
    upStair: ()->Unit,
    onShowEntryMenu: (DisplayEntry)->Unit,
    onShareEntryMenu: (DisplayEntry)->Unit,
    onShowTag: (String)->Unit
) {
    val entry = remember(entity) { entity.entry }
    val bookmarksEntry = remember(entity) { entity.bookmarksEntry }
    val entryStars = remember(entity) { entity.entryStars }
    val entryStarsCount = remember(entryStars) { entryStars?.allStars?.size ?: 0 }
    val titleScrollableState = rememberScrollState()
    val contentsScrollableState = rememberScrollState()
    val actualUrl = remember(entry) { Uri.decode(entry.actualUrl()) }
    val relatedTags = remember(entity) { bookmarksEntry.tags.take(10) }

    val inlineContentMap = mapOf(
        "tagIcon" to InlineTextContent(
            Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.Center)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tag),
                modifier = Modifier.fillMaxSize(),
                tint = CurrentTheme.drawerOnBackground,
                contentDescription = ""
            )
        },
        "relatedEntriesIcon" to InlineTextContent(
            Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.Center)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_category_site),
                modifier = Modifier.fillMaxSize(),
                tint = CurrentTheme.drawerOnBackground,
                contentDescription = ""
            )
        }
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(CurrentTheme.drawerBackground)
            .padding(bottom = 4.dp)
    ) {
        Box(Modifier.weight(1f)) {
            Column(
                Modifier
                    .padding(vertical = 8.dp)
                    .verticalScroll(contentsScrollableState)
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = entry.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CurrentTheme.drawerOnBackground,
                        modifier = Modifier
                            .height(64.dp)
                            .verticalScroll(titleScrollableState)
                            .weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    AsyncImage(
                        model = entry.imageUrl,
                        contentDescription = "entry image",
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(Modifier.height(7.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = actualUrl,
                            fontSize = 14.sp,
                            color = CurrentTheme.primary,
                            maxLines = 2,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.combinedClickable(
                                onClick = { launchBrowser(actualUrl) }
                            )
                        )
                        if (entryStarsCount > 0) {
                            Spacer(Modifier.height(3.dp))
                            StarsCountText(
                                starsEntry = entryStars!!,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { /* TODO */ }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_add_star),
                            contentDescription = "star button",
                            tint = CurrentTheme.drawerOnBackground
                        )
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = entry.description,
                    fontSize = 12.sp,
                    color = CurrentTheme.drawerOnBackground,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                if (relatedTags.isNotEmpty()) {
                    // タグリスト
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            appendInlineContent(id = "tagIcon")
                            append(stringResource(R.string.bookmark_drawer_tags_title))
                        },
                        inlineContent = inlineContentMap,
                        fontWeight = FontWeight.Bold,
                        color = CurrentTheme.drawerOnBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        relatedTags.forEach { (tag, _/*count*/) ->
                            Box(
                                Modifier
                                    .background(
                                        color = CurrentTheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onShowTag(tag) }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 13.sp,
                                    color = CurrentTheme.onPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
                if (entity.relatedEntries.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            appendInlineContent(id = "relatedEntriesIcon")
                            append(stringResource(R.string.bookmark_drawer_related_entries_title))
                        },
                        inlineContent = inlineContentMap,
                        fontWeight = FontWeight.Bold,
                        color = CurrentTheme.drawerOnBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    entity.relatedEntries.forEach { item ->
                        EntryItem(
                            item = item,
                            imageSize = 48.dp,
                            onClick = {
                                entryActionHandler.onEvent(
                                    item, EntryItemEvent.Click, onShowEntryMenu, onShareEntryMenu
                                )
                            },
                            onLongClick = {
                                entryActionHandler.onEvent(
                                    item, EntryItemEvent.LongClick, onShowEntryMenu,
                                    onShareEntryMenu
                                )
                            },
                            onDoubleClick = {
                                entryActionHandler.onEvent(
                                    item, EntryItemEvent.DoubleClick, onShowEntryMenu,
                                    onShareEntryMenu
                                )
                            },
                            onClickEdge = {
                                entryActionHandler.onEvent(
                                    item, EntryItemEvent.ClickEdge, onShowEntryMenu,
                                    onShareEntryMenu
                                )
                            },
                            onLongClickEdge = {
                                entryActionHandler.onEvent(
                                    item, EntryItemEvent.LongClickEdge, onShowEntryMenu,
                                    onShareEntryMenu
                                )
                            },
                            onDoubleClickEdge = {
                                entryActionHandler.onEvent(
                                    item, EntryItemEvent.DoubleClickEdge, onShowEntryMenu,
                                    onShareEntryMenu
                                )
                            },
                            onClickComment = { _, bookmark ->
                                entryActionHandler.onClickComment(
                                    item, bookmark
                                )
                            },
                            onLongClickComment = { _, bookmark ->
                                entryActionHandler.onLongClickComment(
                                    item, bookmark
                                )
                            },
                        )
                    }
                }
            }

            VerticalScrollableIndicator(
                scrollState = contentsScrollableState,
                gradientColor = CurrentTheme.drawerBackground
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (entity.lowerStairUrl != null) {
                Button(
                    modifier = Modifier.align(Alignment.CenterStart),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = CurrentTheme.primary,
                        contentColor = CurrentTheme.onPrimary
                    ),
                    onClick = downStair
                ) {
                    Text(text = stringResource(id = R.string.bookmark_info_down_stair))
                }
            }
            Button(
                modifier = Modifier.align(Alignment.CenterEnd),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary
                ),
                onClick = upStair
            ) {
                Text(text = stringResource(id = R.string.bookmark_info_up_stair))
            }
        }
    }
}

// ------ //

@Preview
@Composable
private fun DrawerContentPreview() {
    val entity = Entity.EMPTY.copy(
        entry = EntryItem(
            title = "タイトル",
            url = "https://localhost/",
            eid = 1,
            description = "テスト説明文",
            count = 411,
            createdAt = ZonedDateTime.of(2022, 7, 2, 11, 58, 2, 0, ZoneId.of("Asia/Tokyo")).toInstant(),
            _imageUrl = "https://cdn-ak-scissors.b.st-hatena.com/image/square/07a1faec0980ede9967bf49a41c1a33e84cecb1d/height=90;version=1;width=120/https%3A%2F%2Fs.togetter.com%2Fogp%2F471a52737dc0f4ad5b7bcc2ed9093c65-1200x630.png",
            _rootUrl = "https://togetter.com",
            myHotEntryComments = listOf(
                BookmarkResult(
                    user = "myhotentrycomment",
                    comment = "test myhot",
                    commentRaw = "test myhot",
                    tags = emptyList(),
                    timestamp = ZonedDateTime.of(2022, 7, 2, 11, 58, 2, 0, ZoneId.of("Asia/Tokyo")).toInstant(),
                    userIconUrl = "",
                    permalink = "",
                )
            ),
            bookmarkedData =
            BookmarkResult(
                user = "suihan74",
                comment = "test",
                commentRaw = "test",
                tags = listOf("あとで読む"),
                timestamp = ZonedDateTime.of(2022, 7, 2, 11, 58, 2, 0, ZoneId.of("Asia/Tokyo")).toInstant(),
                userIconUrl = "",
                permalink = "",
            )
        )
    )

    Box(
        Modifier.width(300.dp)
    ) {
        DrawerContent(
            entryActionHandler = FakeEntryActionHandler(),
            entity = entity,
            launchBrowser = {},
            downStair = {},
            upStair = {},
            onShowEntryMenu = {},
            onShareEntryMenu = {},
            onShowTag = {}
        )
    }
}
