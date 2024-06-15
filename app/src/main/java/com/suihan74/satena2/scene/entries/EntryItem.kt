package com.suihan74.satena2.scene.entries

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.EntryItem
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.scene.bookmarks.TagItem
import com.suihan74.satena2.scene.bookmarks.appendStarCountText
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.alsoAs
import com.suihan74.satena2.utility.extension.toVisibility
import com.suihan74.satena2.utility.extension.zonedString
import java.time.ZoneId
import java.time.ZonedDateTime

private val verticalPadding = 12.dp
private val horizontalPadding = 8.dp

@Composable
fun EntryItem(
    item: DisplayEntry,
    imageSize: Dp = 80.dp,
    readMarkVisible: Boolean = true,
    ellipsizeTitle: Boolean = true,
    onClick: ((DisplayEntry)->Unit)? = null,
    onLongClick: ((DisplayEntry)->Unit)? = null,
    onDoubleClick: ((DisplayEntry)->Unit)? = null,
    onClickEdge: (DisplayEntry)->Unit = {},
    onLongClickEdge: (DisplayEntry)->Unit = {},
    onDoubleClickEdge: (DisplayEntry)->Unit = {},
    onClickComment: (DisplayEntry, BookmarkResult)->Unit = { _, _ -> },
    onLongClickComment: (DisplayEntry, BookmarkResult)->Unit = { _, _ -> }
) {
    val entry = item.entry
    val decodedTitle = Uri.decode(entry.title).toString()
    val commentItems = remember(entry) {
        buildList {
            entry.bookmarkedData?.let { add(it) }
            addAll(entry.bookmarksOfFollowings)
            entry.alsoAs<EntryItem> {
                it.myHotEntryComments?.let { items ->
                    addAll(items)
                }
            }
        }
    }
    val usersText = remember(entry) { entry.count.let { if (it == 1) "$it user" else "$it users" } }
    val domainText = remember(entry) {
        Uri.decode(entry.rootUrl.let {
            Regex("""^(https?://)?(.+[^/])/?$""")
                .matchEntire(it)?.groups?.get(2)?.value ?: it
        })
    }
    val markColorTint = ColorFilter.tint(CurrentTheme.grayTextColor.copy(alpha = .75f))

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (onClick != null || onLongClick != null || onDoubleClick != null) {
                    it.combinedClickable(
                        onClick = { onClick?.invoke(item) },
                        onLongClick = { onLongClick?.invoke(item) },
                        onDoubleClick = { onDoubleClick?.invoke(item) },
                    )
                }
                else it
            }
    ) {
        val (favicon, title, image, users, ad, domain, commentArea, readMark, filteredMark, edgeClickArea) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.ic_check_circle_outline),
            contentDescription = "read mark",
            colorFilter = markColorTint,
            modifier = Modifier
                .size(38.dp)
                .constrainAs(readMark) {
                    linkTo(
                        top = title.top,
                        bottom = filteredMark.top,
                        start = favicon.start,
                        end = parent.end,
                        horizontalBias = 0f,
                        verticalBias = .5f
                    )
                    visibility = (readMarkVisible && item.read != null).toVisibility()
                }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_visibility_off),
            contentDescription = "filtered mark",
            colorFilter = markColorTint,
            modifier = Modifier
                .size(38.dp)
                .constrainAs(filteredMark) {
                    linkTo(
                        top = readMark.bottom,
                        bottom = parent.bottom,
                        start = favicon.start,
                        end = parent.end,
                        horizontalBias = 0f
                    )
                    visibility = (item.filterState == FilterState.EXCLUSION).toVisibility()
                }
        )
        SubcomposeAsyncImage(
            model = entry.faviconUrl,
            contentDescription = "entry favicon",
            modifier = Modifier
                .size(16.dp)
                .constrainAs(favicon) {
                    linkTo(
                        top = parent.top,
                        bottom = parent.bottom,
                        start = parent.start,
                        end = title.start,
                        topMargin = verticalPadding,
                        startMargin = horizontalPadding,
                        endMargin = 4.dp,
                        verticalBias = 0f
                    )
                },
            loading = {
                CircularProgressIndicator(
                    color = CurrentTheme.grayTextColor,
                    strokeWidth = 2.dp
                )
            },
            error = {
                Image(
                    painter = painterResource(R.drawable.ic_file),
                    contentDescription = "loading favicon failed",
                    colorFilter = ColorFilter.tint(CurrentTheme.onBackground)
                )
            }
        )
        Text(
            text = decodedTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CurrentTheme.onBackground,
            maxLines =  if (ellipsizeTitle) 3 else Int.MAX_VALUE,
            overflow = if (ellipsizeTitle) TextOverflow.Ellipsis else TextOverflow.Clip,
            lineHeight = TextUnit(22f, TextUnitType.Sp),
            modifier = Modifier
                .constrainAs(title) {
                    linkTo(
                        top = favicon.top,
                        bottom = users.top,
                        start = favicon.end,
                        end = image.start,
                        startMargin = 4.dp,
                        endMargin = 4.dp,
                        verticalBias = 0f,
                    )
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .heightIn(min = with(LocalDensity.current) { (22f * 3.2).sp.toDp() })
        )
        SubcomposeAsyncImage(
            model = entry.imageUrl,
            contentDescription = "entry image",
            modifier = Modifier
                .size(imageSize)
                .constrainAs(image) {
                    linkTo(
                        top = favicon.top,
                        bottom = commentArea.top,
                        start = title.end,
                        end = parent.end,
                        startMargin = 8.dp,
                        endMargin = horizontalPadding,
                        verticalBias = 0f
                    )
                },
            loading = {
                CircularProgressIndicator(
                    color = CurrentTheme.grayTextColor,
                    strokeWidth = 4.dp
                )
            }
        )
        SingleLineText(
            text = usersText,
            fontSize = 13.sp,
            color = CurrentTheme.entryUsers,
            modifier = Modifier
                .constrainAs(users) {
                    linkTo(
                        top = title.bottom,
                        bottom = commentArea.top,
                        start = title.start,
                        end = ad.start,
                        verticalBias = 1f,
                        horizontalBias = 0f,
                        topMargin = 8.dp,
                        bottomGoneMargin = verticalPadding
                    )
                    width = Dimension.wrapContent
                }
        )
        SingleLineText(
            text = "PR",
            fontSize = 10.sp,
            color = CurrentTheme.onPrimary,
            modifier = Modifier
                .background(CurrentTheme.primary)
                .padding(horizontal = 4.dp)
                .constrainAs(ad) {
                    baseline.linkTo(users.baseline)
                    linkTo(
                        start = users.end,
                        end = domain.start,
                        startMargin = 6.dp,
                        endMargin = 6.dp,
                        bias = .5f
                    )
                    visibility = entry.isPr.toVisibility()
                }
        )
        SingleLineText(
            text = domainText,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = CurrentTheme.grayTextColor,
            modifier = Modifier
                .constrainAs(domain) {
                    baseline.linkTo(users.baseline)
                    linkTo(
                        start = ad.end,
                        end = title.end,
                        startMargin = 6.dp,
                        startGoneMargin = 6.dp
                    )
                    width = Dimension.fillToConstraints
                }
        )

        Column(
            Modifier
                .constrainAs(commentArea) {
                    linkTo(
                        top = users.bottom,
                        bottom = parent.bottom,
                        start = title.start,
                        end = image.end,
                        topMargin = 8.dp,
                        bottomMargin = verticalPadding
                    )
                    width = Dimension.fillToConstraints
                    visibility = commentItems
                        .isNotEmpty()
                        .toVisibility()
                }
        ) {
            for (c in commentItems) {
                val starsEntry = remember(item.starsEntries) { item.starsEntries["${c.eid}_${c.user}"] }?.collectAsState()
                CommentItem(
                    item = c,
                    starsEntry = starsEntry?.value,
                    modifier = Modifier.combinedClickable(
                        onClick = { onClickComment(item, c) },
                        onLongClick = { onLongClickComment(item, c) }
                    )
                )
            }
        }

        Box(
            Modifier
                .background(Color.Transparent)
                .combinedClickable(
                    indication = rememberRipple(
                        color = CurrentTheme.ripple,
                        bounded = false
                    ),
                    onClick = { onClickEdge(item) },
                    onLongClick = { onLongClickEdge(item) },
                    onDoubleClick = { onDoubleClickEdge(item) }
                )
                .constrainAs(edgeClickArea) {
                    linkTo(
                        top = parent.top,
                        bottom = commentArea.top,
                        start = title.end,
                        end = parent.end
                    )
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        )
    }
}

// ------ //

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommentItem(
    item: BookmarkResult?,
    starsEntry: StarsEntry?,
    modifier: Modifier
) {
    if (item == null) return
    Row(
        modifier = modifier
            .background(CurrentTheme.entryCommentBackground)
            .fillMaxWidth()
            .padding(all = 6.dp)
    ) {
        AsyncImage(
            model = item.userIconUrl,
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
                    text = item.user,
                    fontSize = 12.sp,
                    color = CurrentTheme.entryCommentOnBackground,
                )
                if (item.private == true) {
                    Image(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = "private icon",
                        colorFilter = ColorFilter.tint(CurrentTheme.entryCommentOnBackground),
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(with(LocalDensity.current) { 11.sp.toDp() })
                    )
                }
                SingleLineText(
                    text = item.timestamp.zonedString("yyyy-MM-dd HH:mm"),
                    fontSize = 12.sp,
                    color = CurrentTheme.entryCommentOnBackground,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (item.comment.isNotBlank()) {
                Text(
                    text = buildStarsText(
                        comment = item.comment,
                        starsEntry = starsEntry
                    ),
                    fontSize = 12.sp,
                    color = CurrentTheme.entryCommentOnBackground,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (item.tags.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.Start),
                    verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Top),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (tag in item.tags) {
                        TagItem(
                            text = tag,
                            background = CurrentTheme.grayTextColor,
                            foreground = CurrentTheme.entryCommentBackground
                        )
                    }
                }
            }
        }
    }
}

// ------ //

/**
 * コメント+スターリスト部分の装飾テキストを作成する
 */
private fun buildStarsText(
    comment: String,
    starsEntry: StarsEntry?
) : AnnotatedString = buildAnnotatedString {
    append(comment)
    if (starsEntry == null) return@buildAnnotatedString
    appendStarCountText(starsEntry = starsEntry)
}

// ------ //

@SuppressLint("NewApi")
@Preview
@Composable
private fun EntryItemPreview() {
    Box(
        modifier = Modifier
            .background(CurrentTheme.background)
    ) {
        EntryItem(fakeDisplayEntry(comments = false))
    }
}

@SuppressLint("NewApi")
@Preview
@Composable
private fun EntryItemWithCommentsPreview() {
    Box(
        modifier = Modifier
            .background(CurrentTheme.background)
    ) {
        EntryItem(fakeDisplayEntry(comments = true))
    }
}

fun fakeDisplayEntry(comments: Boolean = true) =
    DisplayEntry(
        entry = EntryItem(
            title = "インド人社員から『日本の最強インド料理店リスト』のデータを入手してしまった「かなりガチなリスト」「全データ公開はよ」",
            url = "https://b.hatena.ne.jp/entry?url=https%3A%2F%2Ftogetter.com%2Fli%2F1909907",
            eid = 4721811278762449986,
            description = "リンク My Site 東京練馬の南インド料理ケララバワン ケララバワンは店主の故郷、南インドのケララ州の家庭料理を中心に、身体に優しいインド料理をご提供しています。また、テイクアウトのほか、Uber Eats、出前...",
            count = 411,
            createdAt = ZonedDateTime.of(2022, 7, 2, 11, 58, 2, 0, ZoneId.of("Asia/Tokyo")).toInstant(),
            _imageUrl = "https://cdn-ak-scissors.b.st-hatena.com/image/square/07a1faec0980ede9967bf49a41c1a33e84cecb1d/height=90;version=1;width=120/https%3A%2F%2Fs.togetter.com%2Fogp%2F471a52737dc0f4ad5b7bcc2ed9093c65-1200x630.png",
            _rootUrl = "https://togetter.com",
            myHotEntryComments =
                if (comments)
                    listOf(
                        BookmarkResult(
                            user = "myhotentrycomment",
                            comment = "test myhot",
                            commentRaw = "test myhot",
                            tags = emptyList(),
                            timestamp = ZonedDateTime.of(2022, 7, 2, 11, 58, 2, 0, ZoneId.of("Asia/Tokyo")).toInstant(),
                            userIconUrl = "",
                            permalink = "",
                        ),
                        BookmarkResult(
                            user = "suihan74",
                            comment = "",
                            commentRaw = "",
                            tags = listOf(),
                            timestamp = ZonedDateTime.of(2022, 7, 2, 11, 58, 2, 0, ZoneId.of("Asia/Tokyo")).toInstant(),
                            userIconUrl = "",
                            permalink = "",
                        )
                    )
                else null,
            bookmarkedData =
                if (comments)
                    BookmarkResult(
                        user = "suihan74",
                        comment = "test",
                        commentRaw = "test",
                        tags = listOf("あとで読む"),
                        timestamp = ZonedDateTime.of(2022, 7, 2, 11, 58, 2, 0, ZoneId.of("Asia/Tokyo")).toInstant(),
                        userIconUrl = "",
                        permalink = "",
                    )
                else null
        ),
        read = null,
        filterState = FilterState.VALID
    )
