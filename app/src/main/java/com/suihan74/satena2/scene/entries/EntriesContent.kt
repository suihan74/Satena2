package com.suihan74.satena2.scene.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Issue
import com.suihan74.satena2.compose.*
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
                lazyListState = listState,
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
            MultipleTabsContent(
                id = id,
                viewModel = viewModel,
                navHostController = navHostController,
                listStateMap = listStateMap,
                category = category,
                issue = issue,
                target = t,
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
            Tab(
                selected = pagerState.currentPage == index,
                selectedContentColor = CurrentTheme.tabSelectedColor,
                unselectedContentColor = CurrentTheme.tabUnSelectedColor,
                onClick = {
                    pagerState.settledPage
                    if (pagerState.currentPage == index) {
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

@Preview
@Composable
private fun MultipleTabsContentPreview() {
    MultipleTabsContent(
        id = "",
        viewModel = FakeEntriesViewModel(rememberCoroutineScope()),
        navHostController = NavHostController(LocalContext.current),
        listStateMap = hashMapOf(),
        category = Category.All,
        issue = null,
        target = null
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
    pagerState: PagerState? = null,
    lazyListState: LazyListState = rememberLazyListState(),
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
        loading = loading
    )
}
