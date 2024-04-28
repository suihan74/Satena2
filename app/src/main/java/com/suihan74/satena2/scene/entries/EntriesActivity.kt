package com.suihan74.satena2.scene.entries

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Issue
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.*
import com.suihan74.satena2.scene.entries.bottomSheet.BrowserLauncher
import com.suihan74.satena2.scene.entries.bottomSheet.EntryBottomSheetContent
import com.suihan74.satena2.scene.entries.bottomSheet.EntryItemMenuContent
import com.suihan74.satena2.scene.entries.bottomSheet.ExcludedEntriesList
import com.suihan74.satena2.scene.entries.bottomSheet.SearchSettingContent
import com.suihan74.satena2.scene.preferences.page.accounts.SignInState
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionDialog
import com.suihan74.satena2.scene.preferences.page.theme.ThemeViewModelImpl
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2Theme
import com.suihan74.satena2.utility.argument
import com.suihan74.satena2.utility.currentArgument
import com.suihan74.satena2.utility.extension.LocalUseSystemTimeZone
import com.suihan74.satena2.utility.extension.onTrue
import com.suihan74.satena2.utility.extension.trimScheme
import com.suihan74.satena2.utility.hatena.textId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class EntriesActivity : ComponentActivity() {
    private val viewModel by viewModels<EntriesViewModelImpl>()

    private val themeViewModel by viewModels<ThemeViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreateActivity(activityResultRegistry, lifecycle, intent)

        setContent {
            val theme by themeViewModel.currentThemeFlow.collectAsState()
            val navController = rememberNavController()
            val longClickVibrationDuration by viewModel.longClickVibrationDuration.collectAsState(40L)
            val useSystemTimeZone by viewModel.useSystemTimeZone.collectAsState(false)
            val initialTabsMap by viewModel.initialTabs.collectAsState(emptyMap())

            Satena2Theme(theme) {
                CompositionLocalProvider(
                    LocalLongClickVibrationDuration provides longClickVibrationDuration,
                    LocalUseSystemTimeZone provides useSystemTimeZone,
                    LocalInitialTabsMap provides initialTabsMap
                ) {
                    EntriesScene(
                        viewModel = viewModel,
                        navController = navController,
                        onClickCategoryItem = { category -> viewModel.navigate(navController, category) }
                    )
                }
            }
        }
    }
}

// ------ //

/**
 * カテゴリごとの初期表示タブ
 */
val LocalInitialTabsMap = compositionLocalOf { emptyMap<Category, Int>() }

// ------ //

/**
 * 各画面に対応した[LazyListState]の[HashMap]を保存する[Saver]
 */
private val listStateMapSaver: Saver<HashMap<String, LazyListState>, *> = listSaver(
    save = { original ->
        original.map { Triple(it.key, it.value.firstVisibleItemIndex, it.value.firstVisibleItemScrollOffset) }
    },
    restore = { saved ->
        HashMap<String, LazyListState>().also { map ->
            saved.map {
                map[it.first] = LazyListState(
                    firstVisibleItemIndex = it.second,
                    firstVisibleItemScrollOffset = it.third
                )
            }
        }
    }
)

// ------ //

/**
 * バックスタックに存在しなくなった画面用のキャッシュを削除する
 */
private fun clearExpiredCaches(backStackCategories: List<String>, listStateMap: HashMap<String, LazyListState>) {
    val targets = buildList {
        for (key in listStateMap.keys) {
            val expired = backStackCategories.none { key.startsWith(it) }
            if (expired) {
                add(key)
            }
        }
    }
    for (key in targets) {
        listStateMap.remove(key)
    }
}

// ------ //

/**
 * [EntriesActivity]のUIルート
 *
 * 右からも開けるドロワとScaffoldを併用するために階層化
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun EntriesScene(
    viewModel: EntriesViewModel,
    navController: NavHostController,
    onClickCategoryItem : suspend (Category) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerAlignment by viewModel.drawerAlignment.collectAsState(initial = Alignment.Start)

    var menuState by remember { mutableStateOf(false) }
    val useBottomMenu by viewModel.useBottomMenu.collectAsState(initial = false)

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    var bottomSheetContent by remember { mutableStateOf(EntryBottomSheetContent.Empty) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentCategory = remember(currentBackStackEntry) {
        navController.currentArgument<String>("category")?.let { Category.valueOf(it) } ?: Category.All
    }
    val issues by viewModel.issuesFlow(currentCategory).collectAsState(initial = emptyList())
    val currentIssue = remember(currentBackStackEntry) {
        navController.currentArgument<String>("issue")?.let { code -> issues.firstOrNull { it.code == code } }
    }
    val currentTarget = remember(currentBackStackEntry) {
        navController.currentArgument<String>("target")
    }
    val currentTab = remember(currentBackStackEntry) { mutableIntStateOf(0) }

    // 現在表示中の内容
    val destination = Destination(
        category = currentCategory,
        issue = currentIssue,
        target = currentTarget,
        tabIndex = currentTab.intValue
    )

    // メニュー/共有の対象エントリ
    var menuTargetItem by remember { mutableStateOf<DisplayEntry?>(null) }

    val onShowMenu: (DisplayEntry)->Unit = { item ->
        menuTargetItem = item
        bottomSheetContent = EntryBottomSheetContent.ItemMenu
        coroutineScope.launch {
            bottomSheetState.hide()
            bottomSheetState.show()
        }
    }

    val onShare: (DisplayEntry)->Unit = { item ->
        menuTargetItem = item
        bottomSheetContent = EntryBottomSheetContent.Share
        coroutineScope.launch {
            bottomSheetState.hide()
            bottomSheetState.show()
        }
    }

    // 既読マークを使用するかのフラグ
    val entryReadMarkVisible by viewModel.recordReadEntriesEnabled.collectAsState()

    // ボトムシートの開閉と同時にキーボードを閉じる
    LaunchedEffect(Unit) {
        snapshotFlow { bottomSheetState.isVisible }
            .collect {
                softwareKeyboardController?.hide()
            }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        scrimColor = CurrentTheme.tapGuard,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetBackgroundColor = CurrentTheme.background,
        sheetContent = {
            when (bottomSheetContent) {
                EntryBottomSheetContent.Empty -> { Box(Modifier.fillMaxHeight()) }

                EntryBottomSheetContent.Share -> {
                    EntrySharingContent(entry = menuTargetItem!!.entry)
                }

                EntryBottomSheetContent.Issues -> {
                    Column {
                        Text(
                            text = stringResource(R.string.issues),
                            fontSize = 18.sp,
                            color = CurrentTheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                        LazyColumn(
                            Modifier.verticalScrollbar(
                                state = rememberLazyListState(),
                                color = CurrentTheme.primary
                            )
                        ) {
                            items(issues) { issue ->
                                BottomSheetMenuItem(text = issue.name) {
                                    viewModel.navigate(navController, currentCategory, issue)
                                    coroutineScope.launch {
                                        bottomSheetState.hide()
                                    }
                                }
                            }
                        }
                    }
                }

                EntryBottomSheetContent.ItemMenu -> {
                    val hatenaAccount by viewModel.hatenaAccount.collectAsState()
                    val ngWordEditionDialogVisible = remember { mutableStateOf(false) }

                    EntryItemMenuContent(
                        item = menuTargetItem,
                        sheetState = bottomSheetState,
                        category = currentCategory,
                        account = hatenaAccount,
                        readMarkVisible = entryReadMarkVisible,
                        onLaunchBookmarksActivity = { viewModel.launchBookmarksActivity(it) },
                        onLaunchBrowserActivity = { viewModel.launchBrowserActivity(it) },
                        onLaunchOuterBrowser = { viewModel.openWithOtherApp(it) },
                        onShare = onShare,
                        onNavigateSiteCategory = { viewModel.navigateSiteCategory(it.entry.rootUrl, navController) },
                        onFavorite = { /* todo */ },
                        onUnFavorite = { /* todo */ },
                        onCreateNgWord = { ngWordEditionDialogVisible.value = true },
                        onRead = { entry, isPrivate -> viewModel.readEntry(entry, isPrivate) },
                        onReadLater = { entry, isPrivate -> viewModel.readLaterEntry(entry, isPrivate) },
                        onDeleteReadMark = { viewModel.removeReadMark(it) },
                        onDeleteBookmark = { viewModel.removeBookmark(it) }
                    )

                    ngWordEditionDialogVisible.value.onTrue {
                        NgWordEditionDialog(
                            initialText = menuTargetItem!!.entry.title,
                            initialUrl = menuTargetItem!!.entry.url.trimScheme(),
                            isError = { text, asRegex -> viewModel.isNgRegexError(text, asRegex) },
                            onDismiss = { ngWordEditionDialogVisible.value = false },
                            properties = viewModel.dialogProperties()
                        ) {
                            viewModel.insertNgWord(it).onTrue {
                                bottomSheetState.hide()
                            }
                        }
                    }
                }

                EntryBottomSheetContent.CommentMenu -> {
                    // todo
                    Box(Modifier.height(100.dp)) {}
                }

                EntryBottomSheetContent.SearchSetting -> {
                    val searchSetting by viewModel.searchSettingFlow.collectAsState()
                    SearchSettingContent(
                        value = searchSetting,
                        dialogProperties = viewModel.dialogProperties(),
                        onSearch = {
                            viewModel.search(it)
                            coroutineScope.launch {
                                bottomSheetState.hide()
                            }
                        }
                    )
                }

                EntryBottomSheetContent.SearchMyBookmarksSetting -> {
                    val searchSetting by viewModel.searchMyBookmarksSettingFlow.collectAsState()
                    SearchSettingContent(
                        value = searchSetting,
                        dialogProperties = viewModel.dialogProperties(),
                        onSearch = {
                            viewModel.searchMyBookmarks(it)
                            coroutineScope.launch {
                                bottomSheetState.hide()
                            }
                        }
                    )
                }

                EntryBottomSheetContent.Browser -> {
                    BrowserLauncher(
                        searchAction = { q ->
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                viewModel.searchInBrowser(q)
                            }
                        }
                    )
                }

                EntryBottomSheetContent.ExcludedEntries -> {
                    val excludedEntries by remember(
                        currentCategory,
                        currentIssue,
                        currentTab.intValue,
                        currentTarget
                    ) {
                        viewModel.excludedEntriesFlow(destination)
                    }.collectAsState()

                    ExcludedEntriesList(
                        items = excludedEntries,
                        entryReadMarkVisible = entryReadMarkVisible,
                        onClickItem = {
                            viewModel.onEvent(it, EntryItemEvent.Click, onShowMenu, onShare)
                        },
                        onLongClickItem = {
                            viewModel.onEvent(it, EntryItemEvent.LongClick, onShowMenu, onShare)
                        },
                        onDoubleClickItem = {
                            viewModel.onEvent(it, EntryItemEvent.DoubleClick, onShowMenu, onShare)
                        },
                        onClickItemEdge = {
                            viewModel.onEvent(it, EntryItemEvent.ClickEdge, onShowMenu, onShare)
                        },
                        onLongClickItemEdge = {
                            viewModel.onEvent(it, EntryItemEvent.LongClickEdge, onShowMenu, onShare)
                        },
                        onDoubleClickItemEdge = {
                            viewModel.onEvent(it, EntryItemEvent.DoubleClickEdge, onShowMenu, onShare)
                        },
                        onClickItemComment = { item, b ->
                            viewModel.onClickComment(item, b)
                        },
                        onLongClickItemComment = { item, b ->
                            viewModel.onLongClickComment(item, b)
                        },
                    )
                }
            }
        },
    ) {
        OrientatedModalDrawer(
            modifier = Modifier.fillMaxSize(),
            isRtl = drawerAlignment == Alignment.End,
            width = 300.dp,
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(viewModel) { category ->
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    coroutineScope.launch {
                        onClickCategoryItem(category)
                    }
                }
            }
        ) {
            MainContent(
                viewModel = viewModel,
                navController = navController,
                useBottomMenu = useBottomMenu,
                drawerState = drawerState,
                category = currentCategory,
                issue = currentIssue,
                target = currentTarget,
                entryReadMarkVisible = entryReadMarkVisible,
                onChangeTab = { tabIndex -> currentTab.intValue = tabIndex },
                onOpenIssuesMenu = {
                    bottomSheetContent = EntryBottomSheetContent.Issues
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                },
                onChangeMenuState = { menuState = true },
                onOpenSearchSetting = {
                    bottomSheetContent = when (currentCategory) {
                        Category.Search -> EntryBottomSheetContent.SearchSetting
                        Category.MyBookmarks,
                        Category.SearchMyBookmarks -> EntryBottomSheetContent.SearchMyBookmarksSetting
                        else -> throw IllegalStateException("illegal category has selected")
                    }
                    if (currentCategory == Category.MyBookmarks) {
                        viewModel.navigate(navController, Category.SearchMyBookmarks)
                    }
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                },
                onShowExcludedEntriesBottomSheet = {
                    bottomSheetContent = EntryBottomSheetContent.ExcludedEntries
                    bottomSheetState.show()
                },
                onShowBrowserBottomSheet = {
                    bottomSheetContent = EntryBottomSheetContent.Browser
                    bottomSheetState.show()
                },
                onRefresh = {
                    viewModel.swipeRefresh(destination)
                },
                onAppearLastItem = {
                    viewModel.loadAdditional(destination)
                },
                onClickItem = {
                    viewModel.onEvent(it, EntryItemEvent.Click, onShowMenu, onShare)
                },
                onLongClickItem = {
                    viewModel.onEvent(it, EntryItemEvent.LongClick, onShowMenu, onShare)
                },
                onDoubleClickItem = {
                    viewModel.onEvent(it, EntryItemEvent.DoubleClick, onShowMenu, onShare)
                },
                onClickItemEdge = {
                    viewModel.onEvent(it, EntryItemEvent.ClickEdge, onShowMenu, onShare)
                },
                onLongClickItemEdge = {
                    viewModel.onEvent(it, EntryItemEvent.LongClickEdge, onShowMenu, onShare)
                },
                onDoubleClickItemEdge = {
                    viewModel.onEvent(it, EntryItemEvent.DoubleClickEdge, onShowMenu, onShare)
                },
                onClickItemComment = { item, b -> viewModel.onClickComment(item, b) },
                onLongClickItemComment = { item, b -> viewModel.onLongClickComment(item, b) },
            )
        }
    }

    if (menuState) {
        val closeMenu = { menuState = false }
        MenuContent(viewModel, drawerState, navController, useBottomMenu, closeMenu)
        BackHandler(true, closeMenu)
    }

    if (bottomSheetState.isVisible) {
        if (bottomSheetState.currentValue == ModalBottomSheetValue.HalfExpanded) {
            Box(Modifier.fillMaxSize()) {
                VerticalGradientEdge(
                    topColor = Color.Transparent,
                    bottomColor = CurrentTheme.background,
                    height = 48.dp,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        BackHandler(true) {
            coroutineScope.launch {
                bottomSheetState.hide()
            }
        }
    }
}

@Preview
@Composable
private fun EntriesScenePreview() {
    val navController = rememberNavController()
    val categoryTitles = Category.values().map { stringResource(id = it.textId) }

    EntriesScene(
        viewModel = FakeEntriesViewModel { category ->
            categoryTitles[category.ordinal]
        },
        navController = navController,
        onClickCategoryItem = {}
    )
}

// ----- //

/**
 * ドロワを除いたUIメイン部分
 *
 * [ScaffoldPaddingParameter] (contentのinner padding)を使用するとボトムバーの切り欠き部分が透過されなくなる
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "FlowOperatorInvokedInComposition",
    "RestrictedApi"
)
@Composable
private fun MainContent(
    viewModel: EntriesViewModel,
    navController: NavHostController,
    useBottomMenu: Boolean,
    drawerState: DrawerState,
    category: Category,
    issue: Issue?,
    target: String?,
    entryReadMarkVisible: Boolean,
    onChangeTab: (Int)->Unit = {},
    onOpenIssuesMenu: ()->Unit = {},
    onChangeMenuState: ()->Unit = {},
    onOpenSearchSetting: ()->Unit = {},
    onShowExcludedEntriesBottomSheet: suspend ()->Unit = {},
    onShowBrowserBottomSheet: suspend ()->Unit = {},
    onRefresh: () -> Unit = {},
    onAppearLastItem: () -> Unit = {},
    onClickItem: ((DisplayEntry)->Unit)? = null,
    onLongClickItem: ((DisplayEntry)->Unit)? = null,
    onDoubleClickItem: ((DisplayEntry)->Unit)? = null,
    onClickItemEdge: (DisplayEntry)->Unit = {},
    onLongClickItemEdge: (DisplayEntry)->Unit = {},
    onDoubleClickItemEdge: (DisplayEntry)->Unit = {},
    onClickItemComment: (DisplayEntry, BookmarkResult)->Unit = { _, _ -> },
    onLongClickItemComment: (DisplayEntry, BookmarkResult)->Unit = { _, _ -> },
) {
    val coroutineScope = rememberCoroutineScope()
    // カテゴリorターゲットユーザーをタイトルに表示
    val topBarTitle = remember(category, target) { viewModel.topBarTitle(category, target) }

    // サブカテゴリがある場合はサブタイトル表示
    val topBarSubTitleForIssue by remember(issue) { mutableStateOf(issue?.name) }

    val density = LocalDensity.current
    val toolbarHeightPx = with(density) { 56.dp.roundToPx().toFloat() }
    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    val toolbarHeight = remember { mutableStateOf(56.dp) }
    val scrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            val newOffset = toolbarOffsetHeightPx.floatValue + delta
            toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)
            toolbarHeight.value = with(density) { (toolbarHeightPx + toolbarOffsetHeightPx.floatValue * 2).toDp() }
            return Offset.Zero
        }
    }

    val listStateMap by rememberSaveable(stateSaver = listStateMapSaver) {
        mutableStateOf(HashMap())
    }

    LaunchedEffect(Unit) {
        // 画面遷移ごとにツールバーを展開し直す
        navController.currentBackStackEntryFlow
            .onEach {
                toolbarOffsetHeightPx.floatValue = 0f
                toolbarHeight.value = 56.dp
            }
            .launchIn(this)

        // バックスタックに存在しなくなった画面用のキャッシュを削除する
        navController.currentBackStack
            .onEach {
                val categories = it.mapNotNull { b -> b.argument<String>("category") }
                clearExpiredCaches(categories, listStateMap)
            }
            .launchIn(this)
    }

    Scaffold(
        topBar = {
            Column(
                Modifier.height(toolbarHeight.value)
            ) {
                TopAppBar(
                    backgroundColor = CurrentTheme.titleBarBackground,
                    title = {
                        Column(
                            Modifier.offset {
                                IntOffset(
                                    x = 0,
                                    y = toolbarOffsetHeightPx.floatValue.roundToInt()
                                )
                            }
                        ) {
                            MarqueeText(
                                text = topBarTitle,
                                fontSize = 20.sp,
                                color = CurrentTheme.titleBarOnBackground,
                                gradientColor = CurrentTheme.titleBarBackground
                            )

                            val subTitleText = when (category) {
                                Category.Search -> {
                                    val searchSetting by viewModel.searchSettingFlow.collectAsState()
                                    "${stringResource(searchSetting.searchType.textId)} : ${searchSetting.query}"
                                }
                                Category.SearchMyBookmarks -> {
                                    val searchSetting by viewModel.searchMyBookmarksSettingFlow.collectAsState()
                                    "${stringResource(searchSetting.searchType.textId)} : ${searchSetting.query}"
                                }
                                else -> topBarSubTitleForIssue
                            }
                            subTitleText?.let {
                                MarqueeText(
                                    text = it,
                                    fontSize = 14.sp,
                                    color = CurrentTheme.titleBarOnBackground,
                                    gradientColor = CurrentTheme.titleBarBackground
                                )
                            }
                        }
                    },
                    actions = actions@{
                        if (useBottomMenu) return@actions
                        IconButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .offset { IntOffset(x = 0, y = toolbarOffsetHeightPx.floatValue.roundToInt()) }
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "search",
                                tint = CurrentTheme.titleBarOnBackground
                            )
                        }
                    }
                )
            }
        },
        bottomBar = bottomBar@{
            if (!useBottomMenu) return@bottomBar
            val bottomMenuItems by viewModel.bottomMenuItems.collectAsState(initial = emptyList())
            val arrangement by viewModel.bottomMenuArrangement.collectAsState(Arrangement.Start)
            val hatenaAccount by viewModel.hatenaAccount.collectAsState()
            val issues by viewModel.issuesFlow(category).collectAsState(initial = emptyList())

            BottomMenu(
                items = bottomMenuItems,
                itemsArrangement = arrangement,
                category = category,
                signedIn = hatenaAccount != null,
                issues = issues,
                onClickItem = {
                    coroutineScope.launch {
                        viewModel.onClickBottomMenuItem(
                            item = it,
                            navController = navController,
                            onOpenDrawer = { drawerState.open() },
                            onShowExcludedEntriesBottomSheet = onShowExcludedEntriesBottomSheet
                        )
                    }
                },
                onLongClickItem = {
                    coroutineScope.launch {
                        viewModel.onLongClickBottomMenuItem(
                            item = it,
                            onShowBrowserBottomSheet = onShowBrowserBottomSheet
                        )
                    }
                },
                onClickIssuesItem = onOpenIssuesMenu,
                onClickSearchItem = onOpenSearchSetting
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                backgroundColor = CurrentTheme.primary,
                contentColor = CurrentTheme.onPrimary,
                onClick = { onChangeMenuState() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "open menu",
                    colorFilter = ColorFilter.tint(CurrentTheme.onPrimary)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollConnection)
    ) {
        Box(
            modifier = Modifier
                .background(CurrentTheme.background)
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = "blank",
                modifier = Modifier.fillMaxSize()
            ) {
                // 初期表示カテゴリなどのロード前に表示しておく画面
                composable("blank") {
                    val initialState by viewModel.initialState.collectAsState(initial = null)
                    initialState?.let {
                        viewModel.initialNavigation(it, navController)
                    }

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(CurrentTheme.background)
                    ) {
                        CircularProgressIndicator(
                            color = CurrentTheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                // 各カテゴリ画面
                composable(
                    "entries/{category}?target={target}&issue={issue}",
                    arguments = listOf(
                        navArgument("category") { type = NavType.StringType },
                        navArgument("target") { type = NavType.StringType; defaultValue = "" },
                        navArgument("issue") { type = NavType.StringType; defaultValue = "" }
                    ),
                    enterTransition = {
                        slideInHorizontally(animationSpec = tween(300)) { it }
                    },
                    exitTransition = {
                        slideOutHorizontally(animationSpec = tween(300)) { it }
                    }
                ) { navBackStackEntry ->
                    val c = remember(navBackStackEntry) {
                        navBackStackEntry.arguments
                            ?.getString("category")
                            ?.let { Category.valueOf(it) }!!
                    }
                    val issues by viewModel.issuesFlow(c).collectAsState(initial = emptyList())
                    val pageIssue = remember(navBackStackEntry, issues) {
                        navBackStackEntry.argument<String>("issue")?.let { code -> issues.firstOrNull { it.code == code } }
                    }
                    val argTarget = remember(navBackStackEntry) {
                        navBackStackEntry.argument<String>("target").orEmpty()
                    }

                    EntriesContent(
                        id = navBackStackEntry.id,
                        viewModel = viewModel,
                        listStateMap = listStateMap,
                        navHostController = navController,
                        category = c,
                        issue = pageIssue,
                        target = argTarget,
                        readMarkVisible = entryReadMarkVisible,
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

                    BackHandler(drawerState.isOpen) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                }
            }
            // FAB付近の背景タップを防止
            Box(
                Modifier
                    .background(Color.Transparent)
                    .hoverable(interactionSource = remember { MutableInteractionSource() })
                    .size(90.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

// ------ //

/**
 * メニュー表示中に画面暗転しオーバーラップするUI
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MenuContent(
    viewModel: EntriesViewModel,
    drawerState: DrawerState,
    navController: NavHostController,
    useBottomMenu: Boolean,
    onChangeMenuState: () -> Unit = {},
    initialTransitionState: Boolean = false,
) {
    val visibility = //remember { mutableStateOf(true) }
        remember {
            MutableTransitionState(initialState = false).also { it.targetState = true }
        }
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.hatenaSignInState.collectAsState()

    // 開始アニメーション
    val closeButtonRotation = remember { Animatable(0f) }
    val closeButtonAlpha = remember { Animatable(0f) }
    LaunchedEffect(true) {
        launch {
            closeButtonRotation.animateTo(
                targetValue = 3 * 360f,
                animationSpec = tween(durationMillis = 300)
            )
        }
        launch {
            closeButtonAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180)
            )
        }
    }

    // 終了アニメーション
    val onClose = suspend {
        visibility.targetState = false
        coroutineScope.launch {
            closeButtonRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )
        }
        coroutineScope.launch {
            closeButtonAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 180, delayMillis = 120)
            )
        }
        delay(300)
        onChangeMenuState()
    }

    val onClickCategories = suspend {
        onClose()
        drawerState.snapTo(DrawerValue.Open)
    }

    val onClickSettings = suspend {
        onClose()
        viewModel.launchPreferencesActivity()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                backgroundColor = CurrentTheme.primary,
                contentColor = CurrentTheme.onPrimary,
                onClick = { coroutineScope.launch { onClose() } }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "open menu",
                    colorFilter = ColorFilter.tint(CurrentTheme.onPrimary),
                    modifier = Modifier
                        .alpha(closeButtonAlpha.value)
                        .rotate(closeButtonRotation.value)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true,
        bottomBar = bottomBar@ {
            if (!useBottomMenu) return@bottomBar
            BottomAppBar(
                cutoutShape = CircleShape,
                backgroundColor = Color.Transparent,
                contentPadding = PaddingValues(end = 96.dp),
                elevation = 0.dp
            ) {}
        },
        modifier = Modifier.fillMaxSize(),
        content = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp)
            ) {
                when (state) {
                    SignInState.None -> {
                        MenuItemsWithoutSigning(
                            viewModel = viewModel,
                            visibility = visibility,
                            onClose = onClose,
                            onClickCategories = onClickCategories,
                            onClickSettings = onClickSettings
                        )
                    }

                    SignInState.SignedIn -> {
                        MenuItemsWithSignedIn(
                            viewModel = viewModel,
                            visibility = visibility,
                            navController = navController,
                            onClose = onClose,
                            onClickCategories = onClickCategories,
                            onClickSettings = onClickSettings
                        )
                    }

                    SignInState.Signing -> {
                        MenuItemsWithSigning(
                            visibility = visibility,
                            onClickCategories = onClickCategories,
                            onClickSettings = onClickSettings
                        )
                    }
                }
            }
        },
        backgroundColor = CurrentTheme.tapGuard
    )
}

/**
 * サインインしていない状態でのメニュー項目
 */
@Composable
private fun MenuItemsWithoutSigning(
    viewModel: EntriesViewModel,
    visibility: MutableTransitionState<Boolean>,
    onClose: suspend ()->Unit,
    onClickCategories: suspend ()->Unit,
    onClickSettings: suspend ()->Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visibleState = visibility,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            MenuText(
                2,
                stringResource(R.string.entry_menu_categories),
                R.drawable.ic_category,
            ) {
                coroutineScope.launch { onClickCategories() }
            }
            MenuText(
                1,
                stringResource(R.string.sign_in),
                R.drawable.ic_person_add,
            ) {
                coroutineScope.launch {
                    onClose()
                    viewModel.launchHatenaAuthenticationActivity(context)
                }
            }
            MenuText(
                0,
                stringResource(R.string.entry_menu_preferences),
                R.drawable.ic_settings,
            ) {
                coroutineScope.launch { onClickSettings() }
            }
        }
    }
}

/**
 * サインイン状態でのメニュー項目
 */
@Composable
private fun MenuItemsWithSignedIn(
    viewModel: EntriesViewModel,
    visibility: MutableTransitionState<Boolean>,
    navController: NavHostController,
    onClose: suspend ()->Unit,
    onClickCategories: suspend ()->Unit,
    onClickSettings: suspend ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visibleState = visibility,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            MenuText(
                3,
                stringResource(R.string.entry_menu_notices),
                R.drawable.ic_notifications,
            ) {
                coroutineScope.launch {
                    onClose()
                    viewModel.navigate(navController, Category.Notices)
                }
            }
            MenuText(
                2,
                stringResource(R.string.entry_menu_categories),
                R.drawable.ic_category,
            ) {
                coroutineScope.launch { onClickCategories() }
            }
            MenuText(
                1,
                stringResource(R.string.entry_menu_my_bookmarks),
                R.drawable.ic_mybookmarks,
            ) {
                coroutineScope.launch {
                    onClose()
                    viewModel.navigate(navController, Category.MyBookmarks)
                }
            }
            MenuText(
                0,
                stringResource(R.string.entry_menu_preferences),
                R.drawable.ic_settings,
            ) {
                coroutineScope.launch { onClickSettings() }
            }
        }
    }
}

/**
 * サインイン処理中のメニュー項目
 */
@Composable
private fun MenuItemsWithSigning(
    visibility: MutableTransitionState<Boolean>,
    onClickCategories: suspend ()->Unit,
    onClickSettings: suspend ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()
    AnimatedVisibility(
        visibleState = visibility,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            MenuText(
                1,
                stringResource(R.string.entry_menu_categories),
                R.drawable.ic_category,
            ) {
                coroutineScope.launch { onClickCategories() }
            }
            MenuText(
                0,
                stringResource(R.string.entry_menu_preferences),
                R.drawable.ic_settings,
            ) {
                coroutineScope.launch { onClickSettings() }
            }
        }
    }
}

// ------ //

@Preview
@Composable
private fun MenuContentPreview() {
    val vm = FakeEntriesViewModel()
    val useBottomMenu = vm.useBottomMenu.collectAsState(initial = false).value
    MenuContent(
        vm,
        drawerState = DrawerState(DrawerValue.Closed),
        NavHostController(LocalContext.current),
        useBottomMenu,
        onChangeMenuState = {},
        initialTransitionState = true
    )
}

// ------ //

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedVisibilityScope.MenuText(
    index: Int,
    text: String,
    @DrawableRes resId: Int,
    onClick: ()->Unit = {}
) {
    val labelDuration = 120
    val fabDuration = 80
    val enterLabelDelay = fabDuration + index * 35
    val exitFabDelay = labelDuration + index * 35
    val exitLabelDelay = index * 35

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 24.dp)
            .animateEnterExit(
                enter = slideInVertically(
                    animationSpec = tween(fabDuration),
                    initialOffsetY = { fullHeight -> fullHeight * (index + 1) }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(fabDuration, exitFabDelay),
                    targetOffsetY = { fullHeight -> fullHeight * (index + 1) }
                )
            )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = CurrentTheme.onTapGuard,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .animateEnterExit(
                    enter =
                    fadeIn(animationSpec = tween(labelDuration, enterLabelDelay)) +
                            slideInHorizontally(
                                animationSpec = tween(labelDuration, enterLabelDelay),
                                initialOffsetX = { fullWidth -> fullWidth }
                            ),
                    exit =
                    fadeOut(animationSpec = tween(labelDuration, fabDuration)) +
                            slideOutHorizontally(
                                animationSpec = tween(labelDuration, exitLabelDelay),
                                targetOffsetX = { fullWidth -> fullWidth }
                            )
                )
        )
        FloatingActionButton(
            backgroundColor = CurrentTheme.primary,
            contentColor = CurrentTheme.onPrimary,
            modifier = Modifier.size(40.dp),
            elevation = FloatingActionButtonDefaults.elevation(0.dp),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = "menu: $text",
            )
        }
    }
}

@Preview
@Composable
private fun MenuTextPreview() {
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(true) },
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        MenuText(
            index = 0,
            text = "メニュー項目",
            resId = R.drawable.ic_settings,
            //        remember { mutableStateOf(false) }
        )
    }
}
