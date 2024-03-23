package com.suihan74.satena2.scene.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Issue
import com.suihan74.satena2.compose.AdditionalLoadableLazyColumn
import com.suihan74.satena2.compose.SwipeRefreshBox
import com.suihan74.satena2.compose.VerticalScrollableIndicator
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


private fun lazyListState(
    viewModel: EntriesViewModel,
    listStateMap: HashMap<String, LazyListState>,
    destination: Destination
) : LazyListState {
    val key = viewModel.getMapKey(destination)
    return listStateMap.getOrPut(key) { LazyListState() }
}

@Composable
private fun rememberLazyListState(
    viewModel: EntriesViewModel,
    listStateMap: HashMap<String, LazyListState>,
    destination: Destination
) : LazyListState {
    return remember {
        lazyListState(
            viewModel,
            listStateMap,
            destination
        )
    }
}

// ------ //

/**
 * カテゴリの種類によってタブ表示を使い分ける
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntriesContent(
    id: String,
    viewModel: EntriesViewModel,
    navHostController: NavHostController,
    listStateMap: HashMap<String, LazyListState>,
    category: Category,
    issue: Issue?,
    target: String? = null,  // todo: 呼び出し元で空文字で置換してしまっていてnullになることがない
    readMarkVisible: Boolean,
    onChangeTab: (Int) -> Unit = {},
    onRefresh: () -> Unit = {},
    onAppearLastItem: () -> Unit = {},
    onClickItem: ((DisplayEntry)->Unit)? = null,
    onLongClickItem: ((DisplayEntry)->Unit)? = null,
    onDoubleClickItem: ((DisplayEntry)->Unit)? = null,
    onClickItemEdge: (DisplayEntry)->Unit = {},
    onLongClickItemEdge: (DisplayEntry)->Unit = {},
    onDoubleClickItemEdge: (DisplayEntry)->Unit = {},
    onClickItemComment: (DisplayEntry,BookmarkResult)->Unit = { _, _ -> },
    onLongClickItemComment: (DisplayEntry,BookmarkResult)->Unit = { _, _ -> },
) {
    when {
        Category.Notices == category -> {
            val destination = Destination(category = Category.Notices, tabIndex = 0)
            val notices by viewModel.noticesFlow.collectAsState()
            val listState = rememberLazyListState(viewModel, listStateMap, destination)
            val loading by viewModel.loadingStateFlow(destination).collectAsState()

            NoticesContent(
                id = id,
                viewModel = viewModel,
                notices = notices,
                loading = loading,
                lazyListState = listState
            )
        }

        Category.Maintenance == category -> {
            val destination = Destination(category = Category.Maintenance, tabIndex = 0)
            val maintenanceEntries by viewModel.maintenanceEntriesFlow.collectAsState()
            val listState = rememberLazyListState(viewModel, listStateMap, destination)
            val loading by viewModel.loadingStateFlow(destination).collectAsState()

            MaintenanceInformationContent(
                id = id,
                viewModel = viewModel,
                entries = maintenanceEntries,
                loading = loading,
                lazyListState = listState
            )
        }

        category.singleTab -> {
            val destination = Destination(
                category = category,
                issue = issue,
                target = if (target.isNullOrBlank()) null else target,
                tabIndex = 0
            )
            val entries by viewModel.entriesFlow(destination).collectAsState()
            val listState = rememberLazyListState(viewModel, listStateMap, destination)
            val loading by viewModel.loadingStateFlow(destination).collectAsState()

            SingleTabContent(
                id = id,
                viewModel = viewModel,
                navHostController = navHostController,
                entries = entries,
                loading = loading,
                onRefresh = onRefresh,
                onAppearLastItem = onAppearLastItem,
                pagerState = null,
                lazyListState = listState,
                readMarkVisible = readMarkVisible,
                onClickItem = onClickItem,
                onLongClickItem = onLongClickItem,
                onDoubleClickItem = onDoubleClickItem,
                onClickItemEdge = onClickItemEdge,
                onLongClickItemEdge = onLongClickItemEdge,
                onDoubleClickItemEdge = onDoubleClickItemEdge,
                onClickItemComment = onClickItemComment,
                onLongClickItemComment = onLongClickItemComment
            )
        }

        else -> {
            val t = if (target.isNullOrBlank()) null else target
            BoxWithConstraints {
                val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
                val screenHeight = with(LocalDensity.current) { constraints.maxHeight.toDp() }
                val pageSize = remember(screenWidth, screenHeight) {
                    if (screenHeight < screenWidth) PageSize.Fixed(screenWidth / 2)
                    else PageSize.Fill
                }

                MultipleTabsContent(
                    id = id,
                    viewModel = viewModel,
                    navHostController = navHostController,
                    listStateMap = listStateMap,
                    category = category,
                    issue = issue,
                    target = t,
                    readMarkVisible = readMarkVisible,
                    pageSize = pageSize,
                    onChangeTab = onChangeTab,
                    onRefresh = onRefresh,
                    onAppearLastItem = onAppearLastItem,
                    onClickItem = onClickItem,
                    onLongClickItem = onLongClickItem,
                    onDoubleClickItem = onDoubleClickItem,
                    onClickItemEdge = onClickItemEdge,
                    onLongClickItemEdge = onLongClickItemEdge,
                    onDoubleClickItemEdge = onDoubleClickItemEdge,
                    onClickItemComment = onClickItemComment,
                    onLongClickItemComment = onLongClickItemComment
                )
            }
        }
    }
}

// ------ //

/**
 * 複数タブ用のコンテンツ
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun MultipleTabsContent(
    id: String,
    viewModel: EntriesViewModel,
    navHostController: NavHostController,
    listStateMap: HashMap<String, LazyListState>,
    category: Category,
    issue: Issue?,
    target: String? = null,
    readMarkVisible: Boolean,
    pageSize: PageSize,
    onChangeTab: (Int)->Unit = {},
    onRefresh: ()->Unit = {},
    onAppearLastItem: ()->Unit = {},
    onClickItem: ((DisplayEntry)->Unit)? = null,
    onLongClickItem: ((DisplayEntry)->Unit)? = null,
    onDoubleClickItem: ((DisplayEntry)->Unit)? = null,
    onClickItemEdge: (DisplayEntry)->Unit = {},
    onLongClickItemEdge: (DisplayEntry)->Unit = {},
    onDoubleClickItemEdge: (DisplayEntry)->Unit = {},
    onClickItemComment: (DisplayEntry,BookmarkResult)->Unit = { _, _ -> },
    onLongClickItemComment: (DisplayEntry,BookmarkResult)->Unit = { _, _ -> },
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val initialTabsMap = LocalInitialTabsMap.current

    val tabs = remember { category.tabs(context) }
    val pagerState = rememberPagerState(initialPage = initialTabsMap.getOrDefault(category, 0)) { tabs.size }

    LaunchedEffect(Unit) {
        val tabPosition = initialTabsMap.getOrDefault(category, 0)
        pagerState.scrollToPage(tabPosition, 0f)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }
            .collect { onChangeTab(it) }
    }

    val tabsContent = @Composable @UiComposable {
        tabs.forEachIndexed { index, caption ->
            val destination = Destination(
                category = category,
                issue = issue,
                target = target,
                tabIndex = index
            )
            val selected =
                if (pageSize == PageSize.Fill) pagerState.currentPage == index
                else pagerState.currentPage == index || pagerState.currentPage + 1 == index

            Tab(
                selected = selected,
                selectedContentColor = CurrentTheme.tabSelectedColor,
                unselectedContentColor = CurrentTheme.tabUnSelectedColor,
                onClick = {
                    pagerState.settledPage
                    if (selected) {
                        val lazyListState = lazyListState(viewModel, listStateMap, destination)
                        coroutineScope.launch {
                            runCatching {
                                lazyListState.scrollToItem(0, 0)
                            }
                        }
                    }
                    else {
                        coroutineScope.launch {
                            runCatching {
                                pagerState.scrollToPage(index)
                            }
                        }
                    }
                }
            ) {
                Text(
                    text = caption,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
            }
        }
    }

    Column(
        Modifier.fillMaxSize()
    ) {
        // 「タイムカプセル」カテゴリはタブが多いのでスクロール可能にする
        if (category == Category.Memorial15th) {
            ScrollableTabRow(
                pagerState.currentPage,
                backgroundColor = CurrentTheme.tabBackground,
                contentColor = CurrentTheme.primary,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                tabs = tabsContent
            )
        }
        else {
            TabRow(
                pagerState.currentPage,
                backgroundColor = CurrentTheme.tabBackground,
                contentColor = CurrentTheme.primary,
                modifier = Modifier.fillMaxWidth(),
                tabs = tabsContent
            )
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            pageSize = pageSize,
            state = pagerState
        ) { page ->
            val destination = Destination(
                category = category,
                issue = issue,
                target = target,
                tabIndex = page
            )
            val entries by viewModel.entriesFlow(destination).collectAsState()
            val lazyListState = rememberLazyListState(viewModel, listStateMap, destination)
            val loading by viewModel.loadingStateFlow(destination).collectAsState()

            SingleTabContent(
                id = id,
                viewModel = viewModel,
                navHostController = navHostController,
                tabIndex = page,
                pagerState = pagerState,
                lazyListState = lazyListState,
                readMarkVisible = readMarkVisible,
                entries = entries,
                loading = loading,
                onRefresh = onRefresh,
                onAppearLastItem = onAppearLastItem,
                onClickItem = onClickItem,
                onLongClickItem = onLongClickItem,
                onDoubleClickItem = onDoubleClickItem,
                onClickItemEdge = onClickItemEdge,
                onLongClickItemEdge = onLongClickItemEdge,
                onDoubleClickItemEdge = onDoubleClickItemEdge,
                onClickItemComment = onClickItemComment,
                onLongClickItemComment = onLongClickItemComment
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun MultipleTabsContentPreview() {
    MultipleTabsContent(
        id = "",
        viewModel = FakeEntriesViewModel(rememberCoroutineScope()),
        navHostController = NavHostController(LocalContext.current),
        listStateMap = hashMapOf(),
        category = Category.All,
        readMarkVisible = true,
        issue = null,
        target = null,
        pageSize = PageSize.Fill
    )
}

// ------ //

/**
 * 単一タブ・複数タブの各タブ内容部分のコンテンツ
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SingleTabContent(
    id: String,
    viewModel: EntriesViewModel,
    navHostController: NavHostController,
    entries: List<DisplayEntry>,
    loading: Boolean,
    tabIndex: Int = 0,
    pagerState: PagerState?,
    lazyListState: LazyListState,
    readMarkVisible: Boolean,
    onRefresh: ()->Unit = {},
    onAppearLastItem: ()->Unit = {},
    onClickItem: ((DisplayEntry)->Unit)? = null,
    onLongClickItem: ((DisplayEntry)->Unit)? = null,
    onDoubleClickItem: ((DisplayEntry)->Unit)? = null,
    onClickItemEdge: (DisplayEntry)->Unit = {},
    onLongClickItemEdge: (DisplayEntry)->Unit = {},
    onDoubleClickItemEdge: (DisplayEntry)->Unit = {},
    onClickItemComment: (DisplayEntry,BookmarkResult)->Unit = { _, _ -> },
    onLongClickItemComment: (DisplayEntry,BookmarkResult)->Unit = { _, _ -> },
) {
    val pullRefreshState = rememberPullRefreshState(loading, onRefresh)

    // 外部から伝播されたスクロール操作を反映する
    LaunchedEffect(Unit) {
        viewModel.scrollingFlow
            .onEach {
                // 現在表示されているカテゴリか確認
                val currentId = navHostController.currentBackStackEntry?.id ?: return@onEach
                if (currentId != id) return@onEach
                // 現在表示されているタブか確認
                val currentTab = pagerState?.currentPage ?: 0
                if (currentTab != tabIndex) return@onEach
                // 上端or下端へ移動
                val targetIndex = when (it) {
                    Scroll.ToTop -> 0
                    Scroll.ToBottom -> lazyListState.layoutInfo.totalItemsCount - 1
                }
                lazyListState.scrollToItem(targetIndex)
            }
            .launchIn(this)
    }

    Box {
        SwipeRefreshBox(
            refreshing = loading,
            state = pullRefreshState
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
                key = { it.entry.url },
                onAppearLastItem = { onAppearLastItem() },
                footer = { emptyFooter() }
            ) { entry ->
                EntryItem(
                    item = entry,
                    readMarkVisible = readMarkVisible,
                    onClick = onClickItem,
                    onLongClick = onLongClickItem,
                    onDoubleClick = onDoubleClickItem,
                    onClickEdge = onClickItemEdge,
                    onLongClickEdge = onLongClickItemEdge,
                    onDoubleClickEdge = onDoubleClickItemEdge,
                    onClickComment = onClickItemComment,
                    onLongClickComment = onLongClickItemComment
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

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun SingleTabContentPreview() {
    val vm = FakeEntriesViewModel(rememberCoroutineScope())
    val loading by vm.loadingStateFlow(Destination(category = Category.All, tabIndex = 0)).collectAsState()
    SingleTabContent(
        id = "",
        viewModel = vm,
        navHostController = NavHostController(LocalContext.current),
        entries = emptyList(),
        loading = loading,
        readMarkVisible = true,
        pagerState = null,
        lazyListState = rememberLazyListState()
    )
}
