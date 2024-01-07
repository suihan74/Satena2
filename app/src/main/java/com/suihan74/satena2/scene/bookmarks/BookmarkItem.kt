package com.suihan74.satena2.scene.bookmarks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.star.StarColor
import com.suihan74.hatena.model.star.StarCount
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.*
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.model.userLabel.User
import com.suihan74.satena2.model.userLabel.UserAndLabels
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.ifNotNullOrEmpty
import com.suihan74.satena2.utility.extension.zonedString
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

@OptIn(ExperimentalTextApi::class)
@Composable
fun BookmarkItem(
    item: DisplayBookmark,
    clickable: Boolean = true,
    onClick: (DisplayBookmark)->Unit = {},
    onLongClick: (DisplayBookmark)->Unit = {},
    onDoubleClick: (DisplayBookmark)->Unit = {},
    onClickLink: (String)->Unit = {}
) {
    val bookmark = item.bookmark
    val starsEntry by item.starsEntry.collectAsState()
    val labels by item.labels.collectAsState(initial = null)

    val context = LocalContext.current
    val primaryColor = CurrentTheme.primary

    val annotatedComment = remember(bookmark, primaryColor.value) {
        buildAnnotatedComment(bookmark, primaryColor)
    }
    val clickableTextState = rememberClickableTextState(
        onClick = { onClick(item) },
        onLongClick = { onLongClick(item) },
        onDoubleClick = { onDoubleClick(item) }
    )

    val grayTextColor = CurrentTheme.grayTextColor
    val annotatedUserName = remember(bookmark, labels?.labels) {
        buildAnnotatedString {
            append(bookmark.user)
            labels?.labels.ifNotNullOrEmpty { l ->
                withStyle(
                    style = SpanStyle(
                        color = grayTextColor,
                        fontSize = 12.sp
                    )
                ) {
                    appendInlineContent(id = "labelIconId", alternateText = "UserLabelsIcon")
                    append(l.joinToString(separator = ",") { it.name })
                }
            }
        }
    }
    val userNameInlineContentMap = mapOf(
        "labelIconId" to InlineTextContent(
            Placeholder(12.sp, 12.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_person),
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(color = grayTextColor),
                contentDescription = ""
            )
        }
    )

    Row(
        modifier = Modifier
            .combinedClickable(
                enabled = clickable,
                clickableTextState = clickableTextState,
            )
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(top = 2.dp)) {
            AsyncImage(
                ImageRequest.Builder(context)
                    .data(bookmark.userIconUrl)
                    .build(),
                contentDescription = "user icon",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(48.dp)
            )
            if (item.ignoredUser || item.filtered) {
                Row(Modifier.padding(vertical = 1.dp)) {
                    if (item.ignoredUser) {
                        Image(
                            painterResource(id = R.drawable.ic_voice_over_off),
                            contentDescription = "ignored user",
                            colorFilter = ColorFilter.tint(CurrentTheme.grayTextColor),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    if (item.filtered) {
                        Image(
                            painterResource(id = R.drawable.ic_visibility_off),
                            contentDescription = "muted bookmark",
                            colorFilter = ColorFilter.tint(CurrentTheme.grayTextColor),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                if (bookmark.isPrivate) {
                    Image(
                        painterResource(R.drawable.ic_lock),
                        contentDescription = "private mark",
                        colorFilter = ColorFilter.tint(CurrentTheme.onBackground),
                        modifier = Modifier.size(14.sp.value.dp)
                    )
                }

                SingleLineText(
                    annotatedString = annotatedUserName,
                    color = CurrentTheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    inlineContent = userNameInlineContentMap
                )
            }

            Spacer(Modifier.height(2.dp))

            // todo: リンク部分以外のクリックを検知できるようにはしたが、透過ではないのでrippleが表示されない
            ClickableText(
                text = annotatedComment,
                style = LocalTextStyle.current.copy(
                    color = CurrentTheme.onBackground,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                state = if (clickable) clickableTextState else rememberClickableTextState {},
                onClick = {
                    annotatedComment.getUrlAnnotations(it, it).firstOrNull()?.let { link ->
                        onClickLink(link.item.url)
                        true
                    } ?: false
                },
                onLongClick = {
                    annotatedComment.getUrlAnnotations(it, it).firstOrNull()?.let { link ->
                        onClickLink(link.item.url)
                        true
                    } ?: false
                }
            )

            Spacer(Modifier.height(2.dp))

            if (bookmark.tags.isNotEmpty()) {
                Row {
                    Image(
                        painterResource(R.drawable.ic_tag),
                        contentDescription = "tag header icon",
                        colorFilter = ColorFilter.tint(CurrentTheme.grayTextColor),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(13.dp)  // todo
                    )
                    Text(
                        text = bookmark.tags.joinToString(separator = ", "),
                        color = CurrentTheme.grayTextColor,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
            }

            Text(
                text =
                    if (starsEntry.url.isNotBlank()) {
                        buildTimestampAndStarsText(
                            timestamp = bookmark.timestamp,
                            starsEntry = starsEntry
                        )
                    }
                    else {
                        buildTimestampAndStarsText(
                            timestamp = bookmark.timestamp,
                            starCount = bookmark.starCount
                        )
                    },
                fontSize = 13.sp
            )

            if (item.mentions.isNotEmpty()) {
                Column(Modifier.fillMaxWidth()) {
                    for (m in item.mentions) {
                        MentionItem(item = m)
                    }
                }
            }
        }
    }
}

/**
 * タイムスタンプ+スターリスト部分の装飾テキストを作成する
 */
@Composable
private fun buildTimestampAndStarsText(
    timestamp: Instant,
    starCount: List<StarCount>
) : AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = CurrentTheme.grayTextColor)) {
        append(timestamp.zonedString("yyyy-MM-dd HH:mm"))
    }
    appendStarCountText(starCount = starCount)
}

/**
 * タイムスタンプ+スターリスト部分の装飾テキストを作成する
 */
@Composable
private fun buildTimestampAndStarsText(
    timestamp: Instant,
    starsEntry: StarsEntry
) : AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = CurrentTheme.grayTextColor)) {
        append(timestamp.zonedString("yyyy-MM-dd HH:mm"))
    }
    appendStarCountText(starsEntry = starsEntry)
}

// ------ //

@Preview
@Composable
private fun BookmarkItemPreview() {
    val bookmark = remember { Bookmark(
        _user = Bookmark.User("suihan74", "https://cdn.profile-image.st-hatena.com/users/suihan74/profile.png"),
        comment = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest https://b.hatena.ne.jp",
        isPrivate = true,
        link = "",
        tags = listOf("あとで読むあとで読むあとで読むあとで読むあとで読むあとで読む", "test"),
        timestamp = Instant.now(),
        starCount = listOf(StarCount(StarColor.YELLOW, 11), StarCount(StarColor.RED, 3))
    ) }
    val displayBookmark = remember {
        DisplayBookmark(
            bookmark = bookmark,
            tweetsAndClicks = null,
            bookmarksCount = 0,
        )
    }
    Box(
        Modifier
            .width(300.dp)
            .background(CurrentTheme.background)
    ) {
        BookmarkItem(item = displayBookmark)
    }
}


@Preview
@Composable
private fun NoTagsBookmarkItemPreview() {
    val bookmark = remember { Bookmark(
        _user = Bookmark.User("suihan74", "https://cdn.profile-image.st-hatena.com/users/suihan74/profile.png"),
        comment = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest",
        isPrivate = true,
        link = "",
        tags = emptyList(),
        timestamp = Instant.now(),
        starCount = emptyList()
    ) }
    val displayBookmark = remember {
        DisplayBookmark(
            bookmark = bookmark,
            tweetsAndClicks = null,
            bookmarksCount = 0
        )
    }
    Box(
        Modifier
            .width(300.dp)
            .background(CurrentTheme.background)
    ) {
        BookmarkItem(item = displayBookmark)
    }
}

// ------ //

/**
 * コメントで言及されているブクマの情報
 */
@Composable
private fun MentionItem(item: DisplayBookmark) {
    val b = item.bookmark
    val starsEntry by item.starsEntry.collectAsState()
    Row(
        modifier = Modifier
            .background(CurrentTheme.entryCommentBackground)
            .fillMaxWidth()
            .padding(all = 6.dp)
    ) {
        AsyncImage(
            model = b.userIconUrl,
            contentDescription = "user icon",
            modifier = Modifier.size(24.dp, 24.dp)
        )
        Column(
            Modifier.padding(start = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleLineText(
                    text = b.user,
                    fontSize = 12.sp,
                    color = CurrentTheme.entryCommentOnBackground,
                )
                SingleLineText(
                    text = b.timestamp.zonedString("yyyy-MM-dd HH:mm"),
                    fontSize = 12.sp,
                    color = CurrentTheme.entryCommentOnBackground,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (b.comment.isNotBlank()) {
                Text(
                    text = buildAnnotatedString {
                        append(b.comment, "\n")
                        appendStarCountText(starsEntry = starsEntry)
                    },
                    fontSize = 12.sp,
                    color = CurrentTheme.entryCommentOnBackground,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ------ //

@Preview
@Composable
private fun EmptyBookmarkItemPreview() {
    val bookmark = remember { Bookmark(
        _user = Bookmark.User("suihan74", "https://cdn.profile-image.st-hatena.com/users/suihan74/profile.png"),
        comment = "",
        isPrivate = true,
        link = "",
        tags = emptyList(),
        timestamp = Instant.now(),
        starCount = emptyList()
    ) }
    val displayBookmark = remember {
        DisplayBookmark(
            bookmark = bookmark,
            tweetsAndClicks = null,
            bookmarksCount = 0,
            labels = MutableStateFlow(
                UserAndLabels().apply {
                    user = User(name = "suihan74")
                    labels = listOf(Label(name = "test", id = 0))
                }
            )
        )
    }
    Box(
        Modifier
            .width(300.dp)
            .background(CurrentTheme.background)
    ) {
        BookmarkItem(item = displayBookmark)
    }
}
