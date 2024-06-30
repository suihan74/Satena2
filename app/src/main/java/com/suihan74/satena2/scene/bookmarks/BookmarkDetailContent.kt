package com.suihan74.satena2.scene.bookmarks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.suihan74.satena2.compose.CombinedIconButton
import com.suihan74.satena2.compose.DrawerDraggableArea
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.SwipeRefreshBox
import com.suihan74.satena2.compose.VerticalScrollableIndicator
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.zonedString
import com.suihan74.satena2.utility.hatena.hatenaUserIconUrl
import kotlinx.coroutines.launch
import java.time.Instant

private enum class DetailTab(
    val iconId: Int,
    val text: String
) {
    StarsTo(iconId = R.drawable.ic_star, text = "To"),
    StarsFrom(iconId = R.drawable.ic_star, text = "From"),
    MentionsTo(iconId = R.drawable.ic_comment, text = "To"),
    MentionsFrom(iconId = R.drawable.ic_comment, text = "From")
}

// ------ //

@Composable
fun BookmarkDetailContent(
    viewModel: BookmarksViewModel,
    navController: NavHostController,
    item: DisplayBookmark?,
    mentionsTo: List<DisplayBookmark>,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit
) {
    Scaffold(
        floatingActionButton = {
            val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            item?.let {
                FloatingActionButton(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    modifier = Modifier.padding(bottom = navigationBarHeight),
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
    ) { paddingValues ->
        item?.let { displayItem ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(CurrentTheme.background)
                    .padding(paddingValues)
            ) {
                val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Spacer(
                    Modifier.height(statusBarHeight)
                )
                BookmarkArea(
                    item = displayItem,
                    modifier = Modifier.fillMaxWidth(),
                    onShowBookmarkItemMenu = onShowBookmarkItemMenu
                )
                MentionsArea(
                    viewModel = viewModel,
                    navController = navController,
                    item = displayItem,
                    mentionsTo = mentionsTo,
                    onShowBookmarkItemMenu = onShowBookmarkItemMenu,
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
                    .padding(paddingValues)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookmarkArea(
    item: DisplayBookmark,
    modifier: Modifier = Modifier,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit
) {
    val bookmark = remember(item) { item.bookmark }
    val starsEntry by item.starsEntry.collectAsState()

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
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (bookmark.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.Start),
                            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Top),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (tag in bookmark.tags) {
                                TagItem(
                                    text = tag,
                                    background = CurrentTheme.grayTextColor,
                                    foreground = CurrentTheme.background
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                    SingleLineText(
                        annotatedString =
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
                        color = CurrentTheme.grayTextColor,
                        fontSize = 13.sp
                    )
                }
                CombinedIconButton(
                    modifier = Modifier.padding(12.dp),
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
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel,
    navController: NavHostController,
    item: DisplayBookmark,
    mentionsTo: List<DisplayBookmark>,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit,
) {
    val tabs = remember {
        buildList {
            add(DetailTab.StarsTo)
            add(DetailTab.StarsFrom)
            if (mentionsTo.isNotEmpty()) {
                add(DetailTab.MentionsTo)
            }
            if (item.mentions.isNotEmpty()) {
                add(DetailTab.MentionsFrom)
            }
        }
    }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 14.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = tab.iconId),
                                contentDescription = "tab icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tab.text,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                userScrollEnabled = true
            ) { page ->
                val lazyListState = lazyListStates[page]
                when (tabs[page]) {
                    DetailTab.StarsTo -> StarsToContent(
                        mentions = starsTo,
                        lazyListState = lazyListState,
                        onClickItem = { m ->
                            if (m.bookmark != null) {
                                viewModel.showBookmarkDetail(
                                    user = m.user,
                                    navController = navController
                                )
                            }
                        }
                    )

                    DetailTab.StarsFrom -> {
                        StarsFromContent(
                            lazyListState = lazyListState
                        )
                    }

                    DetailTab.MentionsTo -> {
                        MentionsContent(
                            mentions = mentionsTo,
                            lazyListState = lazyListState,
                            onClickItem = {
                                viewModel.showBookmarkDetail(
                                    user = it.bookmark.user,
                                    navController = navController
                                )
                            },
                            onLongClickItem = onShowBookmarkItemMenu
                        )
                    }

                    DetailTab.MentionsFrom -> {
                        MentionsContent(
                            mentions = item.mentions,
                            lazyListState = lazyListState,
                            onClickItem = {
                                viewModel.showBookmarkDetail(
                                    user = it.bookmark.user,
                                    navController = navController
                                )
                            },
                            onLongClickItem = onShowBookmarkItemMenu
                        )
                    }
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
    lazyListState: LazyListState,
    onClickItem: (Mention)->Unit = {},
    onLongClickItem: (Mention)->Unit = {}
) {
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
                state = lazyListState,
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
private fun StarsFromContent(
    lazyListState: LazyListState
) {
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MentionsContent(
    mentions: List<DisplayBookmark>,
    lazyListState: LazyListState,
    onClickItem: (DisplayBookmark)->Unit = {},
    onLongClickItem: (DisplayBookmark)->Unit = {}
) {
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
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(
                        state = lazyListState,
                        color = CurrentTheme.primary
                    ),
            ) {
                items(items = mentions) {
                    BookmarkItem(
                        item = it,
                        showMentions = false,
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
        mentionsTo = emptyList(),
        onShowBookmarkItemMenu = {}
    )
}
