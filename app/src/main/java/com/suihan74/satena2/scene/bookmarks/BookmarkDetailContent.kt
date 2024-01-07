package com.suihan74.satena2.scene.bookmarks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.*
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.zonedString
import com.suihan74.satena2.utility.hatena.hatenaUserIconUrl
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun BookmarkDetailContent(
    viewModel: BookmarksViewModel,
    navController: NavHostController,
    item: DisplayBookmark?,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit
) {
    Scaffold(
        floatingActionButton = {
            item?.let {
                FloatingActionButton(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    onClick = { /* todo */ }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "open menu",
                        colorFilter = ColorFilter.tint(CurrentTheme.onPrimary)
                    )
                }
            }
        },
        drawerGesturesEnabled = false,
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true,
        modifier = Modifier.fillMaxSize()
    ) {
        item?.let { displayItem ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(CurrentTheme.background)
                    .padding(it)
            ) {
                BookmarkArea(
                    viewModel = viewModel,
                    item = displayItem,
                    modifier = Modifier.fillMaxWidth(),
                    onShowBookmarkItemMenu = onShowBookmarkItemMenu
                )
                MentionsArea(
                    viewModel = viewModel,
                    navController = navController,
                    item = displayItem,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
            }
        } ?: run {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(CurrentTheme.background)
                    .padding(it)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CurrentTheme.primary,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

// ------ //

@Composable
private fun BookmarkArea(
    viewModel: BookmarksViewModel,
    item: DisplayBookmark,
    modifier: Modifier = Modifier,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit
) {
    val bookmark = remember(item) { item.bookmark }
    Row(
        modifier
            .padding(8.dp)
    ) {
        AsyncImage(
            bookmark.userIconUrl,
            contentDescription = "user icon",
            modifier = Modifier.size(48.dp)
        )
        Column(Modifier.padding(start = 8.dp)) {
            SingleLineText(
                text = bookmark.user,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CurrentTheme.onBackground
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = bookmark.comment,
                color = CurrentTheme.onBackground,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(2.dp))
            Row {
                SingleLineText(
                    text = bookmark.timestamp.zonedString("yyyy-MM-dd HH:mm"),
                    color = CurrentTheme.grayTextColor,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                CombinedIconButton(
                    onClick = { onShowBookmarkItemMenu(item) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = "bookmark menu button",
                        tint = CurrentTheme.onBackground
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MentionsArea(
    viewModel: BookmarksViewModel,
    navController: NavHostController,
    item: DisplayBookmark,
    modifier: Modifier = Modifier
) {
    val tabs = remember { listOf("to", "from") }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val drawerAlignment by viewModel.drawerAlignmentFlow.collectAsState(initial = Alignment.Start)
    val lazyListStates = remember {
        buildList {
            repeat(tabs.size) {
                add(LazyListState())
            }
        }
    }

    val starsTo by viewModel.starsTo(item = item).collectAsState()

    Box(modifier) {
        Column(Modifier.fillMaxSize()) {
            TabRow(
                pagerState.currentPage,
                backgroundColor = CurrentTheme.tabBackground,
                contentColor = CurrentTheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        selectedContentColor = CurrentTheme.tabSelectedColor,
                        unselectedContentColor = CurrentTheme.tabUnSelectedColor,
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage == index) {
                                    lazyListStates[index].scrollToItem(0, 0)
                                }
                                else {
                                    pagerState.scrollToPage(index)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )
                    }
                }
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> StarsToContent(
                        mentions = starsTo,
                        onClickItem = { m ->
                            if (m.bookmark != null) {
                                viewModel.showBookmarkDetail(user = m.user, navController = navController)
                            }
                        }
                    )

                    1 -> StarsFromContent()
                }
            }
        }

        DrawerDraggableArea(alignment = drawerAlignment)
    }
}

// ------ //

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StarsToContent(
    mentions: List<Mention>,
    onClickItem: (Mention)->Unit = {},
    onLongClickItem: (Mention)->Unit = {}
) {
    val lazyListState = rememberLazyListState()
    val loading by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { /* todo */ }
    )

    Box(Modifier.fillMaxSize()) {
        SwipeRefreshBox(
            refreshing = loading,
            state = pullRefreshState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(
                        state = lazyListState,
                        color = CurrentTheme.primary
                    ),
            ) {
                items(items = mentions) {
                    MentionItem(
                        mention = it,
                        onClick = onClickItem,
                        onLongClick = onLongClickItem
                    )
                    Divider(
                        color = CurrentTheme.listItemDivider,
                        thickness = 1.dp
                    )
                }
                emptyFooter()
            }
        }
        VerticalScrollableIndicator(
            lazyListState = lazyListState,
            gradientColor = CurrentTheme.background,
            topGradientHeight = 48.dp,
            bottomGradientHeight = 80.dp
        )
    }
}

@Composable
private fun StarsFromContent() {
}

// ------ //

/**
 * メンションリスト項目
 */
@Composable
private fun MentionItem(
    mention: Mention,
    onClick: (Mention)->Unit = {},
    onLongClick: (Mention)->Unit = {}
) {
    val context = LocalContext.current
    val userIconUrl = remember { hatenaUserIconUrl(mention.user) }
    val starsText = remember { buildAnnotatedString { appendStarCountText(mention.stars) } }
    val bookmark = mention.bookmark

    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(mention) },
                onLongClick = { onLongClick(mention) }
            )
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        AsyncImage(
            ImageRequest.Builder(context)
                .data(userIconUrl)
                .build(),
            contentDescription = "user icon",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                SingleLineText(
                    text = mention.user,
                    color = CurrentTheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (bookmark != null) {
                    SingleLineText(
                        text = bookmark.timestamp.zonedString("yyyy-MM-dd HH:mm"),
                        color = CurrentTheme.grayTextColor,
                        fontSize = 14.sp
                    )
                }
            }
            Text(
                text = starsText,
                fontSize = 13.sp
            )
            if (!bookmark?.comment.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = mention.bookmark?.comment.orEmpty(),
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ------ //

@Preview
@Composable
private fun BookmarkDetailContentPreview() {
    val context = LocalContext.current
    BookmarkDetailContent(
        viewModel = FakeBookmarksViewModel(),
        navController = remember { NavHostController(context) },
        item = DisplayBookmark(
            bookmark = Bookmark(
                _user = Bookmark.User("suihan74", ""),
                comment = "comment",
                isPrivate = false,
                link = "",
                tags = emptyList(),
                timestamp = Instant.now(),
                starCount = emptyList()
            )
        ),
        onShowBookmarkItemMenu = {}
    )
}
