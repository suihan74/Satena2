package com.suihan74.satena2.scene.browser

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewNavigator
import com.google.accompanist.web.rememberSaveableWebViewState
import com.suihan74.hatena.HatenaClient
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BookmarkSharingContent
import com.suihan74.satena2.compose.CombinedIconButton
import com.suihan74.satena2.compose.OrientatedModalDrawer
import com.suihan74.satena2.compose.SharingContent
import com.suihan74.satena2.compose.SwipeRefreshBox
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.bookmarks.BookmarkItemMenuContent
import com.suihan74.satena2.scene.bookmarks.BookmarkTagsMenuContent
import com.suihan74.satena2.scene.bookmarks.BookmarkUrlsMenuContent
import com.suihan74.satena2.scene.bookmarks.BookmarksViewModel
import com.suihan74.satena2.scene.bookmarks.BookmarksViewModelImpl
import com.suihan74.satena2.scene.bookmarks.DisplayBookmark
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2Theme
import com.suihan74.satena2.utility.extension.add
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

typealias PrefsBrowserViewModel = com.suihan74.satena2.scene.preferences.page.browser.BrowserViewModel
typealias PrefsBrowserViewModelImpl = com.suihan74.satena2.scene.preferences.page.browser.BrowserViewModelImpl

/**
 * アプリ内ブラウザ
 */
@AndroidEntryPoint
class BrowserActivity : ComponentActivity() {
    private val viewModel by viewModels<BrowserViewModelImpl>()

    private val bookmarksViewModel by viewModels<BookmarksViewModelImpl>()

    private val prefsViewModel by viewModels<PrefsBrowserViewModelImpl>()

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreateActivity(activityResultRegistry, lifecycle)
        bookmarksViewModel.onCreateActivity(activityResultRegistry, lifecycle)
        val initialUrl = viewModel.initialUrl(intent)
        if (savedInstanceState == null) {
            viewModel.loadUrl(initialUrl)
        }

        setContent {
            val theme by viewModel.theme.collectAsState()
            Satena2Theme(theme) {
                BrowserContent(
                    viewModel = viewModel,
                    bookmarksViewModel = bookmarksViewModel,
                    prefsViewModel = prefsViewModel,
                    onFinishActivity = { finish() }
                )
            }
        }
    }

    /** 戻るボタン長押しで「戻る/進む」履歴リストを表示する */
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                lifecycleScope.launch {
                    viewModel.backForwardLongPressed.emit(true)
                }
                true
            }
            else -> super.onKeyLongPress(keyCode, event)
        }
    }
}

// ------- //

/**
 * アプリ内ブラウザ画面のコンテンツ
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun BrowserContent(
    viewModel: BrowserViewModel,
    bookmarksViewModel: BookmarksViewModel,
    prefsViewModel: PrefsBrowserViewModel,
    onFinishActivity: ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerAlignment by viewModel.drawerAlignment.collectAsState(initial = Alignment.Start)
    val drawerPagerState = rememberPagerState(initialPage = 0) { 4 }
    val drawerGestureEnabled = remember { mutableStateOf(true) }

    val bottomNavController = rememberNavController()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val focusManager = LocalFocusManager.current

    val url by viewModel.currentUrl.collectAsState(initial = "")
    val entryUrl = remember(url) { if (url.isBlank()) url else HatenaClient.getEntryUrl(url) }
    val title by viewModel.webChromeClient.titleFlow.collectAsState(initial = "" to "")

    var bookmarkMenuTarget by remember {
        mutableStateOf<DisplayBookmark?>(null)
    }

    LaunchedEffect(Unit) {
        // ボトムシート表示状態の変化時に処理
        snapshotFlow { bottomSheetState.currentValue }
            .onEach {
                // 開閉時に表示中画面で既に当たっているフォーカスを解除する
                focusManager.clearFocus()
                // ボトムメニューを閉じる際にコンテンツの遷移状態を初期化する
                if (ModalBottomSheetValue.Hidden == it) {
                    bottomNavController.navigate("empty") {
                        popUpTo(0)
                    }
                }
            }
            .launchIn(this)

        // ドロワー表示状態の変化時に処理
        snapshotFlow { drawerState.currentValue }
            .onEach {
                // 開閉時に表示中画面で既に当たっているフォーカスを解除する
                focusManager.clearFocus()
            }
            .launchIn(this)
    }

    // 戻るボタン長押しで履歴を表示
    LaunchedEffect(Unit) {
        viewModel.backForwardLongPressed
            .collect {
                if (bottomSheetState.isVisible || !it) return@collect
                showHistoryBottomSheet(bottomSheetState, bottomNavController)
            }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetBackgroundColor = CurrentTheme.background,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        scrimColor = CurrentTheme.tapGuard,
        sheetContent = {
            NavHost(
                navController = bottomNavController,
                startDestination = "empty"
            ) {
                composable("empty") {
                    Box(Modifier.height(1.dp))
                }
                // 基本のメニュー
                composable("basicMenu") {
                    AddressBarMenuContent(
                        viewModel,
                        bottomSheetState,
                        bottomNavController,
                        drawerState,
                        drawerPagerState,
                        onFinishActivity
                    )
                }
                // 戻る/進む履歴スタック
                composable("historyStack") {
                    BackForwardList(
                        viewModel,
                        onClickItem = { bottomSheetState.hide() }
                    )
                }
                // 共有メニュー
                composable("share") {
                    SharingContent(
                        title = stringResource(R.string.browser_sharing_title),
                        rawUrl = url,
                        rawEntryUrl = entryUrl,
                        text = title.second
                    )
                }
                // 表示中ページのリソースリスト
                composable("resourcesList") {
                    val items by viewModel.resourceUrls.collectAsState()
                    ResourcesList(
                        items = items,
                        onInsertBlockedResource = { viewModel.insertBlockedResource(it) }
                    )
                }
                // todo: ブクマメニュー
                composable("bookmarkMenu") {
                    BookmarkItemMenuContent(
                        coroutineScope = coroutineScope,
                        item = bookmarkMenuTarget,
                        onShowRecentBookmarks = {
                            // todo
                        },
                        onShowBookmarksToItem = {
                            // todo
                        },
                        onShowUserLabelDialog = {
                            bottomSheetState.hide()
                            //bottomSheetContent = BottomSheetContent.UserLabel
                            bottomSheetState.show()
                        },
                        onSelectUrlsMenu = {
                            bottomNavController.navigate("bookmarkUrlsMenu")
                        },
                        onSelectTagsMenu = {
                            bottomNavController.navigate("bookmarkTagsMenu")
                        },
                        onSelectNgWordsMenu = {
                            // todo
                        },
                        onFollow = {
                            // todo
                        },
                        onIgnore = {
                            // todo
                        },
                        onShare = {
                            bottomNavController.navigate("shareBookmarkMenu")
                        },
                        onReport = {
                            // todo
                        }
                    )
                }
                composable("bookmarkUrlsMenu") {
                    BookmarkUrlsMenuContent(
                        item = bookmarkMenuTarget,
                        onSelectUrl = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
//                                viewModel.openBrowser(it)
                            }
                        }
                    )
                }
                composable("bookmarkTagsMenu") {
                    BookmarkTagsMenuContent(
                        item = bookmarkMenuTarget,
                        onSelectTag = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
//                                viewModel.launchEntriesActivityForTag(it)
                            }
                        }
                    )
                }
                composable("shareBookmarkMenu") {
                    Column(Modifier.height(300.dp)) {
                        bookmarkMenuTarget?.let {
                            BookmarkSharingContent(bookmark = it.bookmark)
                        }
                    }
                }
            }
        }
    ) {
        OrientatedModalDrawer(
            modifier = Modifier.fillMaxSize(),
            isRtl = drawerAlignment == Alignment.End,
            width = 300.dp,
            gesturesEnabled = drawerGestureEnabled.value,
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    viewModel = viewModel,
                    bookmarksViewModel = bookmarksViewModel,
                    prefsViewModel = prefsViewModel,
                    drawerState = drawerState,
                    pagerState = drawerPagerState,
                    onSelectBookmark = {
                        bookmarkMenuTarget = it
                        bottomNavController.navigate("bookmarkMenu") {
                            popUpTo(0)
                        }
                        coroutineScope.launch {
                            bottomSheetState.show()
                        }
                    }
                )
            }
        ) {
            MainContent(
                viewModel = viewModel,
                webViewCaptureBackPresses =
                    bottomSheetState.currentValue == ModalBottomSheetValue.Hidden && drawerState.isClosed,
                onShowMenu = {
                    bottomNavController.navigate("basicMenu") {
                        popUpTo(0)
                    }
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                },
                onShowBackForwardList = {
                    coroutineScope.launch {
                        showHistoryBottomSheet(bottomSheetState, bottomNavController)
                    }
                }
            )
        }
    }

    BackHandler(bottomSheetState.isVisible) {
        coroutineScope.launch {
            bottomSheetState.hide()
        }
    }

    BackHandler(drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }
}

// ------ //

/**
 * アプリ内ブラウザ画面のコンテンツ
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainContent(
    viewModel: BrowserViewModel,
    webViewCaptureBackPresses: Boolean,
    onShowMenu: ()->Unit,
    onShowBackForwardList: ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val webViewState = rememberSaveableWebViewState()
    val webViewNavigator = com.google.accompanist.web.rememberWebViewNavigator(coroutineScope)

    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val progress by viewModel.webChromeClient.progress.collectAsState()
    val refreshing by viewModel.swipeRefreshingFlow.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            softwareKeyboardController?.hide()
            focusManager.clearFocus()
            viewModel.refresh()
        }
    )
    val addressBarAlignment by viewModel.addressBarAlignment.collectAsState(initial = Alignment.Bottom)
    val isAddressBarTop = remember(addressBarAlignment) { addressBarAlignment == Alignment.Top }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (webView, progressBar, addressBar) = createRefs()

        SwipeRefreshBox(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier
                .constrainAs(webView) {
                    linkTo(
                        top = if (isAddressBarTop) addressBar.bottom else parent.top,
                        bottom = if (isAddressBarTop) parent.bottom else addressBar.top,
                        start = parent.start,
                        end = parent.end
                    )
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        ) {
            WebView(
                state = webViewState,
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ),
                navigator = webViewNavigator,
                client = viewModel.webViewClient,
                chromeClient = viewModel.webChromeClient,
                captureBackPresses = webViewCaptureBackPresses,
                onCreated = { viewModel.onCreated(it, coroutineScope) },
                onDispose = { viewModel.onDispose() },
                factory = { context ->
                    NestedScrollWebView(context).also {
                        ViewCompat.setNestedScrollingEnabled(it, true)
                    }
                }
            )
        }
        // ページ読み込み進捗バー
        LinearProgressIndicator(
            progress = progress,
            color = CurrentTheme.primary,
            modifier = Modifier
                .constrainAs(progressBar) {
                    if (isAddressBarTop) {
                        top.linkTo(addressBar.bottom)
                    }
                    else {
                        bottom.linkTo(webView.bottom)
                    }
                    linkTo(
                        start = parent.start,
                        end = parent.end
                    )
                    width = Dimension.fillToConstraints
                    height = Dimension.value(3.dp)
                    visibility =
                        if (progress < 1f) Visibility.Visible
                        else Visibility.Gone
                }
        )
        // アドレスバー部分
        TopAppBar(
            backgroundColor = CurrentTheme.titleBarBackground,
            modifier = Modifier
                .constrainAs(addressBar) {
                    linkTo(
                        top = if (isAddressBarTop) parent.top else webView.bottom,
                        bottom = if (isAddressBarTop) webView.top else parent.bottom,
                        start = parent.start,
                        end = parent.end
                    )
                    width = Dimension.fillToConstraints
                },
            title = {
                AddressBar(
                    viewModel = viewModel,
                    webViewNavigator = webViewNavigator,
                    onLongClickBackForward = onShowBackForwardList
                )
            },
            actions = {
                IconButton(onClick = onShowMenu) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "search",
                        tint = CurrentTheme.titleBarOnBackground
                    )
                }
            }
        )
    }
}

// ------ //

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AddressBar(
    viewModel: BrowserViewModel,
    webViewNavigator: WebViewNavigator,
    onLongClickBackForward: ()->Unit = {}
) {
    val state = viewModel.webViewClient.state

    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val addressText = remember {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange.Zero))
    }
    LaunchedEffect(Unit) {
        viewModel.currentUrl
            .onEach {
                Log.i("webView", "currentUrl: $it")
                addressText.value = addressText.value.copy(text = Uri.decode(it))
            }
            .launchIn(this)
    }

    var keepWholeSelection by remember { mutableStateOf(false) }
    if (keepWholeSelection) {
        SideEffect {
            keepWholeSelection = false
        }
    }

    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Search
    )
    val keyboardActions = KeyboardActions {
        softwareKeyboardController?.hide()
        addressText.value = addressText.value.copy(selection = TextRange.Zero)
        focusManager.clearFocus()
        viewModel.enterAddressBarText(addressText.value.text)
    }
    val visualTransformation = VisualTransformation.None
    val interactionSource = remember { MutableInteractionSource() }

    val colors = TextFieldDefaults.textFieldColors(
        textColor = CurrentTheme.titleBarOnBackground,
        placeholderColor = CurrentTheme.titleBarOnBackground.copy(alpha = .6f),
        cursorColor = CurrentTheme.titleBarOnBackground,
        trailingIconColor = CurrentTheme.titleBarOnBackground,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )
    val selectionColor = TextSelectionColors(
        handleColor = CurrentTheme.titleBarOnBackground,
        backgroundColor = CurrentTheme.titleBarOnBackground.copy(alpha = .3f)
    )

    val canGoBack = webViewNavigator.canGoBack
    val canGoForward = webViewNavigator.canGoForward

    val favicon = remember(state.pageIcon) { state.pageIcon?.asImageBitmap() ?: ImageBitmap(24, 24) }

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        if (state.pageIcon == null) {
            CircularProgressIndicator(
                color = CurrentTheme.titleBarOnBackground,
                backgroundColor = Color.Transparent,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
        else {
            Image(
                favicon,
                contentDescription = "page icon",
                modifier = Modifier.size(24.dp),
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .swipeable(
                    state = rememberSwipeableState(0),
                    anchors = mapOf(0f to 0, 400f to 1),
                    orientation = Orientation.Horizontal
                )
        ) {
            CompositionLocalProvider(
                LocalTextSelectionColors provides selectionColor
            ) {
                BasicTextField(
                    value = addressText.value,
                    onValueChange = {
                        if (keepWholeSelection) {
                            keepWholeSelection = false
                            addressText.value = addressText.value.copy(
                                selection = TextRange(0, addressText.value.text.length)
                            )
                        }
                        else {
                            addressText.value = it
                        }
                    },
                    textStyle = TextStyle.Default.copy(
                        fontSize = 15.sp,
                        color = colors.textColor(enabled = true).value
                    ),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    cursorBrush = SolidColor(colors.cursorColor(isError = false).value),
                    // ドロワ表示切替を回避するためにswipeableを設定している
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (it.isFocused) {
                                addressText.value = addressText.value.copy(
                                    selection = TextRange(0, addressText.value.text.length)
                                )
                                keepWholeSelection = true
                            }
                        }
                        .background(
                            color = colors.backgroundColor(enabled = true).value,
                            shape = RoundedCornerShape(50)
                        )
                        .fillMaxSize()
                ) { innerTextField ->
                    Row {
                        TextFieldDefaults.TextFieldDecorationBox(
                            value = addressText.value.text,
                            innerTextField = innerTextField,
                            placeholder = { Text(stringResource(R.string.browser_address_bar_placeholder)) },
                            enabled = true,
                            singleLine = true,
                            visualTransformation = visualTransformation,
                            interactionSource = interactionSource,
                            colors = colors,
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 32.dp),
                        )
                    }
                }
            }

            CombinedIconButton(
                onClick = { viewModel.goBackOrForward(-1) },
                onLongClick = onLongClickBackForward,
                enabled = canGoBack,
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
            ) {
                if (canGoBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "go back",
                        tint = CurrentTheme.titleBarOnBackground,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            CombinedIconButton(
                onClick = { viewModel.goBackOrForward(1) },
                onLongClick = onLongClickBackForward,
                enabled = canGoForward,
                modifier = Modifier
                    .width(24.dp)
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
            ) {
                if (canGoForward) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "go forward",
                        tint = CurrentTheme.titleBarOnBackground,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

        }
    }
}

@Preview
@Composable
private fun AddressBarPreview() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = FakeBrowserViewModel(coroutineScope = coroutineScope)
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CurrentTheme.titleBarBackground,
        title = {
            AddressBar(
                viewModel = viewModel,
                webViewNavigator = WebViewNavigator(coroutineScope)
            )
        },
        actions = {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = "search",
                    tint = CurrentTheme.titleBarOnBackground
                )
            }
        }
    )
}

// ------ //

/**
 * アドレスバーのメニューボタン押下時に出てくるボトムバーのコンテンツ
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun AddressBarMenuContent(
    viewModel: BrowserViewModel,
    bottomSheetState: ModalBottomSheetState,
    navController: NavHostController,
    drawerState: DrawerState,
    drawerPagerState: PagerState,
    onFinishActivity: ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val resBlockEnabled by viewModel.urlBlockingEnabled.collectAsState(initial = true)
    val jsEnabled by viewModel.javascriptEnabled.collectAsState(initial = true)
    val menuItems = buildList<Pair<Int, suspend ()->Unit>> {
        add(
            R.string.browser_menu_back_forward to {
                navController.navigate("historyStack")
            },
            R.string.browser_menu_launch_bookmarks_activity to {
                bottomSheetState.hide()
                viewModel.launchBookmarkActivity()
            },
            R.string.browser_menu_open_other_app to {
                bottomSheetState.hide()
                viewModel.openWithOtherApp()
            },
            R.string.share to {
                navController.navigate("share")
            },
            R.string.browser_menu_gyotaku to {
                viewModel.openPageArchive()
                bottomSheetState.hide()
            },
            R.string.browser_menu_block_settings to {
                navController.navigate("resourcesList")
            },
            (if (resBlockEnabled) R.string.browser_menu_block_on else R.string.browser_menu_block_off) to {
                viewModel.toggleUrlBlockingEnabled()
            },
            (if (jsEnabled) R.string.browser_menu_javascript_on else R.string.browser_menu_javascript_off) to {
                viewModel.toggleJavaScriptEnabled()
            },
            R.string.browser_menu_preferences to {
                bottomSheetState.hide()
                drawerPagerState.scrollToPage(DrawerTab.Preferences.ordinal, 0f)
                drawerState.open()
            },
            R.string.browser_menu_close to {
                onFinishActivity()
            }
        )
    }

    Column {
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .verticalScrollbar(
                    state = lazyListState,
                    color = CurrentTheme.primary
                )
        ) {
            items(menuItems) { (textId, action) ->
                BottomMenuItem(
                    text = stringResource(textId),
                    onClick = { coroutineScope.launch { action() } }
                )
            }
        }
    }
}

// ------ //

/**
 * 戻る/進むリストに遷移した状態でボトムシートを表示する
 */
@OptIn(ExperimentalMaterialApi::class)
private suspend fun showHistoryBottomSheet(
    bottomSheetState: ModalBottomSheetState,
    navController: NavController
) {
    navController.navigate("historyStack") {
        popUpTo(0)
    }
    delay(200L)
    bottomSheetState.show()
}

// ------ //

@Composable
private fun BottomMenuItem(
    text: String,
    onClick: suspend ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Text(
        text = text,
        fontSize = 16.sp,
        color = CurrentTheme.drawerOnBackground,
        modifier = Modifier
            .clickable { coroutineScope.launch { onClick() } }
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp)
    )
}

@Preview
@Composable
private fun BottomMenuItemPreview() {
    Box(Modifier.background(CurrentTheme.background)) {
        BottomMenuItem(text = "Test Hello World", onClick = {})
    }
}
