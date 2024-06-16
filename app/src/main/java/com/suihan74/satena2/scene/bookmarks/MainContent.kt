package com.suihan74.satena2.scene.bookmarks

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AdditionalLoadableLazyColumn
import com.suihan74.satena2.compose.MarqueeText
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.SwipeRefreshBox
import com.suihan74.satena2.compose.Tab
import com.suihan74.satena2.compose.VerticalScrollableIndicator
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.VibratorCompat
import kotlinx.coroutines.launch
import java.lang.Integer.max

/**
 * 各タブに対応した[LazyListState]の[HashMap]を保存する[Saver]
 */
private val listStatesSaver: Saver<List<LazyListState>, *> = listSaver(
    save = { original ->
        original.map { it.firstVisibleItemIndex to it.firstVisibleItemScrollOffset }
    },
    restore = { saved ->
        saved.map {
            LazyListState(
                firstVisibleItemIndex = it.first,
                firstVisibleItemScrollOffset = it.second
            )
        }
    }
)

// ------ //

/**
 * ブクマ画面のメインコンテンツ
 *
 * 各タブとそれに対応するブクマリスト
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarksMainContent(
    viewModel: BookmarksViewModel,
    entity: Entity,
    navController: NavController,
    onClickTopBar: ()->Unit = {},
    onLongClickTopBar: ()->Unit = {},
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit = {},
    onOpenBottomSetting: ()->Unit = {}
) {
    val hatenaAccount by viewModel.hatenaAccountFlow.collectAsState(initial = null)
    val pagerState = rememberPagerState(initialPage = 0) { 4 }
    val lazyListStates by rememberSaveable(stateSaver = listStatesSaver) {
        mutableStateOf(BookmarksTab.entries.map { LazyListState() })
    }
    val myBookmark by viewModel.myBookmarkFlow.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeTab(pagerState)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TitleBarContent(entity) },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = CurrentTheme.titleBarBackground,
                    scrolledContainerColor = CurrentTheme.titleBarBackground,
                    titleContentColor = CurrentTheme.titleBarOnBackground,
                    navigationIconContentColor = CurrentTheme.titleBarOnBackground,
                    actionIconContentColor = CurrentTheme.titleBarOnBackground
                ),
                modifier = Modifier
                    .combinedClickable(
                        onClick = onClickTopBar,
                        onLongClick = onLongClickTopBar
                    )
            )
        },
        bottomBar = {
            val searchQuery by viewModel.searchQueryFlow.collectAsState()
            BottomMenu(
                pagerState = pagerState,
                lazyListStates = lazyListStates,
                myBookmark = myBookmark,
                signedIn = hatenaAccount != null,
                initialSearchQuery = searchQuery,
                onNavigateMyBookmark = {
                    viewModel.showBookmarkDetail(
                        user = myBookmark!!.bookmark.user,
                        navController = navController
                    )
                },
                onOpenSetting = onOpenBottomSetting,
                onSearch = { q -> viewModel.setSearchQuery(q) }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = hatenaAccount != null,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    onClick = { viewModel.openPostActivity() }
                ) {
                    Text(
                        text = stringResource(R.string.post_button_text),
                        fontSize = 20.sp,
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true,
        modifier = Modifier
            .background(CurrentTheme.background)
            .fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .background(CurrentTheme.background)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .fillMaxSize()
        ) {
            BookmarksPager(
                viewModel = viewModel,
                pagerState = pagerState,
                lazyListStates = lazyListStates,
                entity = entity,
                navController = navController,
                onShowBookmarkItemMenu = onShowBookmarkItemMenu
            )
        }
    }
}

/**
 * タイトルバーに表示する内容
 */
@Composable
private fun TitleBarContent(entity: Entity) {
    if (entity == Entity.EMPTY) {
        return
    }

    val entry = entity.entry
    val title = remember(entry) { Uri.decode(entry.title) }
    val numUsers = remember(entity.bookmarksEntry) {
        entry.count.let { u ->
            "$u user${if (u != 1) "s" else ""}"
        }
    }
    val numComments = remember(entity.bookmarksEntry) {
        entity.bookmarksEntry.bookmarks.count { it.comment.isNotBlank() }
    }
    val numPrivates = remember(entry, entity.bookmarksEntry) {
        max(0, entry.count - entity.bookmarksEntry.bookmarks.count())
    }

    Column {
        // ページタイトル
        MarqueeText(
            text = title,
            fontSize = 18.sp,
            color = CurrentTheme.titleBarOnBackground,
            gradientColor = CurrentTheme.titleBarBackground
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.faviconUrl,
                contentDescription = "favicon",
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(8.dp))
            // 全ブクマ数
            SingleLineText(
                text = numUsers,
                fontSize = 13.sp,
                color = CurrentTheme.titleBarOnBackground
            )
            Spacer(Modifier.width(12.dp))
            // コメント数
            Image(
                painterResource(id = R.drawable.ic_comment),
                contentDescription = "number of comments",
                colorFilter = ColorFilter.tint(CurrentTheme.titleBarOnBackground),
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(2.dp))
            SingleLineText(
                text = numComments.toString(),
                fontSize = 13.sp,
                color = CurrentTheme.titleBarOnBackground
            )
            // プライベートブクマ数
            if (numPrivates > 0) {
                Spacer(Modifier.width(8.dp))
                Image(
                    painterResource(id = R.drawable.ic_lock),
                    contentDescription = "number of private bookmarks",
                    colorFilter = ColorFilter.tint(CurrentTheme.titleBarOnBackground),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(2.dp))
                SingleLineText(
                    text = numPrivates.toString(),
                    fontSize = 13.sp,
                    color = CurrentTheme.titleBarOnBackground
                )
            }
        }
    }
}

// ------ //

/**
 * メイン領域のタブとページャ
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarksPager(
    viewModel: BookmarksViewModel,
    pagerState: PagerState,
    lazyListStates: List<LazyListState>,
    entity: Entity,
    navController: NavController,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit
) {
    val context = LocalContext.current
    val tabs = remember { BookmarksTab.entries }
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        TabRow(
            pagerState.currentPage,
            backgroundColor = CurrentTheme.tabBackground,
            contentColor = CurrentTheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tab ->
                val tabName = stringResource(id = tab.textId)
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
                    },
                    onLongClick = {
                        VibratorCompat.vibrateOneShot(context)
                        viewModel.onLongClickTab(tab)
                    }
                ) {
                    Text(
                        text = tabName,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                }
            }
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState
        ) { page ->
            when (page) {
                BookmarksTab.DIGEST.ordinal -> {
                    DigestBookmarksTabContent(
                        viewModel = viewModel,
                        tab = tabs[page],
                        lazyListState = lazyListStates[page],
                        myBookmark = viewModel.myBookmarkFlow.collectAsState().value,
                        popularBookmarks = viewModel.popularBookmarksFlow.collectAsState().value,
                        followingBookmarks = viewModel.followingBookmarksFlow.collectAsState().value,
                        navController = navController,
                        onShowBookmarkItemMenu = onShowBookmarkItemMenu
                    )
                }

                else -> {
                    val items =
                        when (page) {
                            BookmarksTab.RECENT.ordinal -> viewModel.recentBookmarksFlow.collectAsState().value

                            BookmarksTab.ALL.ordinal -> viewModel.allBookmarksFlow.collectAsState().value

                            BookmarksTab.CUSTOM.ordinal -> viewModel.customBookmarksFlow.collectAsState().value

                            else -> remember(entity.bookmarks) {
                                entity.bookmarks.map {
                                    DisplayBookmark(
                                        it
                                    )
                                }
                            }
                        }

                    BookmarksTabContent(
                        viewModel = viewModel,
                        tab = tabs[page],
                        lazyListState = lazyListStates[page],
                        bookmarks = items,
                        navController = navController,
                        onShowBookmarkItemMenu = onShowBookmarkItemMenu
                    )
                }
            }
        }
    }
}

// ------ //

/**
 * 各ページの内容。ブクマのリスト
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarksTabContent(
    viewModel: BookmarksViewModel,
    tab: BookmarksTab,
    lazyListState: LazyListState,
    bookmarks: List<DisplayBookmark>,
    navController: NavController,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit = {}
) {
    val loading by viewModel.loadingFlow.collectAsState()
    val additionalLoadable by viewModel.additionalLoadableFlow.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { viewModel.refresh(tab) }
    )

    Box {
        SwipeRefreshBox(
            refreshing = loading,
            state = pullRefreshState
        ) {
            AdditionalLoadableLazyColumn(
                items = bookmarks,
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(
                        state = lazyListState,
                        color = CurrentTheme.primary
                    ),
                key = { it.bookmark.user },
                onAppearLastItem = { viewModel.loadAdditional(tab) },
                footer = {
                    bookmarksFooter(
                        loading = loading,
                        additionalLoadable = additionalLoadable,
                        onLoad = { viewModel.loadAdditional(tab) }
                    )
                }
            ) { item ->
                BookmarkItem(
                    item = item,
                    onClick = { viewModel.onClick(bookmark = it, navController = navController) },
                    onLongClick = {
                        viewModel.onLongClick(
                            bookmark = it,
                            onShowBookmarkItemMenu = onShowBookmarkItemMenu
                        )
                    },
                    onClickLink = { viewModel.onClickLink(it) }
                )
                Divider(
                    color = CurrentTheme.listItemDivider,
                    thickness = 1.dp
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
}

private fun LazyListScope.bookmarksFooter(
    loading: Boolean,
    additionalLoadable: Boolean,
    onLoad : ()->Unit
) {
    val height = 80.dp
    if (!loading && additionalLoadable) {
        this.item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clickable { onLoad() }
            ) {
                Text(
                    text = stringResource(R.string.bookmark_load_additional),
                    color = CurrentTheme.onBackground,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }
        }
    }
    else {
        emptyFooter(height = height)
    }
}

/**
 * 「注目」ブクマリスト
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun DigestBookmarksTabContent(
    viewModel: BookmarksViewModel,
    tab: BookmarksTab,
    lazyListState: LazyListState,
    myBookmark: DisplayBookmark?,
    popularBookmarks: List<DisplayBookmark>,
    followingBookmarks: List<DisplayBookmark>,
    navController: NavController,
    onShowBookmarkItemMenu: (DisplayBookmark)->Unit
) {
    val loading by viewModel.loadingFlow.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { viewModel.refresh(tab) }
    )

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
            // マイブクマ
            if (myBookmark != null) {
                stickyHeader(key = "section_mybookmark") {
                    Box(Modifier.background(CurrentTheme.background)) {
                        Section(stringResource(R.string.my_bookmarks))
                    }
                }
                item(key = "mybookmark") {
                    BookmarkItem(
                        item = myBookmark,
                        onClick = { viewModel.onClick(bookmark = it, navController = navController) },
                        onClickLink = { viewModel.onClickLink(it) },
                        onLongClick = {
                            viewModel.onLongClick(
                                bookmark = it,
                                onShowBookmarkItemMenu = onShowBookmarkItemMenu
                            )
                        }
                    )
                }
            }

            // フォローしているユーザーのブクマ
            if (followingBookmarks.isNotEmpty()) {
                stickyHeader(key = "section_following") {
                    Box(Modifier.background(CurrentTheme.background)) {
                        Section(stringResource(R.string.bookmark_digest_following_section))
                    }
                }
                val lastItem = followingBookmarks.last()
                items(
                    followingBookmarks,
                    key = { "following/" + it.bookmark.user }
                ) { item ->
                    BookmarkItem(
                        item = item,
                        onClick = { viewModel.onClick(bookmark = it, navController = navController) },
                        onClickLink = { viewModel.onClickLink(it) },
                        onLongClick = {
                            viewModel.onLongClick(
                                bookmark = it,
                                onShowBookmarkItemMenu = onShowBookmarkItemMenu
                            )
                        }
                    )
                    if (lastItem !== item || popularBookmarks.isEmpty()) {
                        Divider(
                            color = CurrentTheme.listItemDivider,
                            thickness = 1.dp
                        )
                    }
                }
            }

            // 人気ブクマ
            if (popularBookmarks.isNotEmpty()) {
                stickyHeader(key = "section_popular") {
                    Box(Modifier.background(CurrentTheme.background)) {
                        Section(stringResource(R.string.bookmark_digest_popular_section))
                    }
                }

                items(
                    popularBookmarks,
                    key = { "popular/" + it.bookmark.user }
                ) { item ->
                    BookmarkItem(
                        item = item,
                        onClick = { viewModel.onClick(bookmark = it, navController = navController) },
                        onClickLink = { viewModel.onClickLink(it) },
                        onLongClick = {
                            viewModel.onLongClick(
                                bookmark = it,
                                onShowBookmarkItemMenu = onShowBookmarkItemMenu
                            )
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
    }
}
