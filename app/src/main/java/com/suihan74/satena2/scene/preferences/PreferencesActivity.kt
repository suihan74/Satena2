package com.suihan74.satena2.scene.preferences

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.LocalLongClickVibrationDuration
import com.suihan74.satena2.compose.Tooltip
import com.suihan74.satena2.compose.VerticalGradientEdge
import com.suihan74.satena2.compose.clickGuard
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.preferences.page.BasicPreferencesPage
import com.suihan74.satena2.scene.preferences.page.ComposablePrefItem
import com.suihan74.satena2.scene.preferences.page.accounts.AccountViewModel
import com.suihan74.satena2.scene.preferences.page.accounts.AccountViewModelImpl
import com.suihan74.satena2.scene.preferences.page.accounts.AccountsPage
import com.suihan74.satena2.scene.preferences.page.accounts.FakeAccountViewModel
import com.suihan74.satena2.scene.preferences.page.accounts.SignInState
import com.suihan74.satena2.scene.preferences.page.accounts.accountPageContents
import com.suihan74.satena2.scene.preferences.page.bookmarks.BookmarkViewModel
import com.suihan74.satena2.scene.preferences.page.bookmarks.BookmarkViewModelImpl
import com.suihan74.satena2.scene.preferences.page.bookmarks.FakeBookmarkViewModel
import com.suihan74.satena2.scene.preferences.page.bookmarks.bookmarkPageContents
import com.suihan74.satena2.scene.preferences.page.browser.BrowserViewModel
import com.suihan74.satena2.scene.preferences.page.browser.BrowserViewModelImpl
import com.suihan74.satena2.scene.preferences.page.browser.FakeBrowserViewModel
import com.suihan74.satena2.scene.preferences.page.browser.browserPageContents
import com.suihan74.satena2.scene.preferences.page.entries.EntryPage
import com.suihan74.satena2.scene.preferences.page.entries.EntryViewModel
import com.suihan74.satena2.scene.preferences.page.entries.EntryViewModelImpl
import com.suihan74.satena2.scene.preferences.page.entries.FakeEntryViewModel
import com.suihan74.satena2.scene.preferences.page.favoriteSites.FakeFavoriteSitesViewModel
import com.suihan74.satena2.scene.preferences.page.favoriteSites.FavoriteSitesPage
import com.suihan74.satena2.scene.preferences.page.favoriteSites.FavoriteSitesViewModel
import com.suihan74.satena2.scene.preferences.page.favoriteSites.FavoriteSitesViewModelImpl
import com.suihan74.satena2.scene.preferences.page.general.FakeGeneralViewModel
import com.suihan74.satena2.scene.preferences.page.general.GeneralViewModel
import com.suihan74.satena2.scene.preferences.page.general.GeneralViewModelImpl
import com.suihan74.satena2.scene.preferences.page.general.generalPageContents
import com.suihan74.satena2.scene.preferences.page.info.FakeInformationViewModel
import com.suihan74.satena2.scene.preferences.page.info.InformationViewModel
import com.suihan74.satena2.scene.preferences.page.info.InformationViewModelImpl
import com.suihan74.satena2.scene.preferences.page.info.informationPageContents
import com.suihan74.satena2.scene.preferences.page.ngUsers.FakeNgUsersViewModel
import com.suihan74.satena2.scene.preferences.page.ngUsers.NgUsersPage
import com.suihan74.satena2.scene.preferences.page.ngUsers.NgUsersViewModel
import com.suihan74.satena2.scene.preferences.page.ngUsers.NgUsersViewModelImpl
import com.suihan74.satena2.scene.preferences.page.ngWords.FakeNgWordsViewModel
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsPage
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsViewModel
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsViewModelImpl
import com.suihan74.satena2.scene.preferences.page.theme.FakeThemeViewModel
import com.suihan74.satena2.scene.preferences.page.theme.ThemePage
import com.suihan74.satena2.scene.preferences.page.theme.ThemeViewModel
import com.suihan74.satena2.scene.preferences.page.theme.ThemeViewModelImpl
import com.suihan74.satena2.scene.preferences.page.userLabel.FakeUserLabelsViewModel
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelsPage
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelsViewModel
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelsViewModelImpl
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2ThemeFullScreen
import com.suihan74.satena2.utility.extension.showToast
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private data class PrefViewModels(
    val information : InformationViewModel,
    val account : AccountViewModel,
    val general : GeneralViewModel,
    val theme : ThemeViewModel,
    val entry : EntryViewModel,
    val bookmark : BookmarkViewModel,
    val browser : BrowserViewModel,
    val favoriteSites : FavoriteSitesViewModel,
    val ngWord : NgWordsViewModel,
    val ngUsers : NgUsersViewModel,
    val userLabels : UserLabelsViewModel
)

private data class PrefContents(
    val information: List<ComposablePrefItem>,
    val general: List<ComposablePrefItem>,
    val account: List<ComposablePrefItem>,
    val bookmark: List<ComposablePrefItem>,
    val browser: List<ComposablePrefItem>,
) {
    private var _allItems: List<Pair<String, @Composable ()->Unit>>? = null
    fun allItems(context: Context) : List<Pair<String, @Composable ()->Unit>> {
        if (_allItems != null) {
            return _allItems!!
        }
        val items = buildList {
            fun addItems(items: List<ComposablePrefItem>) {
                for (item in items) {
                    val str =
                        if (item.first == 0) ""
                        else context.getString(item.first)
                    add(str to item.second)
                }
            }
            addItems(information)
            addItems(general)
            addItems(account)
            addItems(bookmark)
            addItems(browser)
        }
        _allItems = items
        return items
    }
}

@AndroidEntryPoint
class PreferencesActivity : ComponentActivity() {
    private val informationViewModel by viewModels<InformationViewModelImpl>()
    private val accountViewModel by viewModels<AccountViewModelImpl>()
    private val generalViewModel by viewModels<GeneralViewModelImpl>()
    private val themeViewModel by viewModels<ThemeViewModelImpl>()
    private val entryViewModel by viewModels<EntryViewModelImpl>()
    private val bookmarkViewModel by viewModels<BookmarkViewModelImpl>()
    private val browserViewModel by viewModels<BrowserViewModelImpl>()
    private val favoriteSitesViewModel by viewModels<FavoriteSitesViewModelImpl>()
    private val ngWordViewModel by viewModels<NgWordsViewModelImpl>()
    private val ngUsersViewModel by viewModels<NgUsersViewModelImpl>()
    private val userLabelsViewModel by viewModels<UserLabelsViewModelImpl>()

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val viewModels = PrefViewModels(
            information = informationViewModel,
            account = accountViewModel,
            general = generalViewModel,
            theme = themeViewModel,
            entry = entryViewModel,
            bookmark = bookmarkViewModel,
            browser = browserViewModel,
            favoriteSites = favoriteSitesViewModel,
            ngWord = ngWordViewModel,
            ngUsers = ngUsersViewModel,
            userLabels = userLabelsViewModel
        )

        // 初期化処理が必要なViewModelの処理
        generalViewModel.onCreateActivity(activityResultRegistry, lifecycle)

        setContent {
            val theme by themeViewModel.currentThemeFlow.collectAsState()
            val longClickVibrationDuration by generalViewModel.longClickVibrationDuration.collectAsState()

            // 無限ループ`HorizontalPager`がバグる件
            // https://issuetracker.google.com/issues/326887746

            val signedIn by accountViewModel.signedInHatena.collectAsState()
            val categories = remember(signedIn) {
                when (signedIn) {
                    SignInState.SignedIn -> PreferencesCategory.entries
                    else -> {
                        PreferencesCategory.entries.filter {
                            when (it) {
                                PreferencesCategory.Followings, PreferencesCategory.NgUsers -> false
                                else -> true
                            }
                        }
                    }
                }
            }

            val contents = PrefContents(
                information = informationPageContents(informationViewModel),
                general = generalPageContents(generalViewModel),
                account = accountPageContents(accountViewModel),
                bookmark = bookmarkPageContents(bookmarkViewModel),
                browser = browserPageContents(browserViewModel)
            )

            LaunchedEffect(Unit) {
                generalViewModel.launchRequestNotificationPermission.collect { isOn ->
                    if (isOn) {
                        generalViewModel.launchRequestNotificationPermission.value = false
                        generalViewModel.requestNotificationPermission(requestNotificationPermission)
                    }
                }
            }

            CompositionLocalProvider(
                LocalLongClickVibrationDuration provides longClickVibrationDuration
            ) {
                Satena2ThemeFullScreen(theme) {
                    val systemUiController = rememberSystemUiController()
                    systemUiController.setStatusBarColor(
                        color = CurrentTheme.titleBarBackground,
                        darkIcons = CurrentTheme.titleBarBackground.luminance() > .5f
                    )
                    PreferencesScene(
                        viewModels,
                        categories,
                        contents
                    )
                }
            }
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        generalViewModel.backgroundCheckingNoticesEnabled

        return super.onCreateView(name, context, attrs)
    }

    // ------ //

    /**
     * 通知権限リクエスト処理ランチャ
     */
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        lifecycleScope.launch {
            if (result) {
                showToast(R.string.request_notification_permission_granted_msg, Toast.LENGTH_SHORT)
            }
            else {
                showToast(R.string.request_notification_permission_denied_msg, Toast.LENGTH_SHORT)
                generalViewModel.backgroundCheckingNoticesEnabled.value = false
            }
        }
    }
}

// ------ //

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreferencesScene(
    viewModels : PrefViewModels,
    categories: List<PreferencesCategory>,
    contents: PrefContents
) {
    val pagerSize = remember(categories) { categories.size } //Int.MAX_VALUE
    val startIndex = 0 //pagerSize / 2
    val pagerState = rememberPagerState(initialPage = startIndex) { pagerSize }
    val pageCount = remember(categories) { categories.size }
    val currentCategory = remember(pagerState.currentPage) {
        categories[(pagerState.currentPage - startIndex).floorMod(pageCount)]
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(CurrentTheme.background)
    ) {
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Spacer(
            Modifier.height(statusBarHeight)
        )

        // トップバー
        TopBar(
            category = currentCategory,
            onSearchQueryChange = {
                searchQuery = it
            }
        )

        // トップバー下端のセパレータ（上下）
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(CurrentTheme.primary)
        )

        Box(
            Modifier.fillMaxSize()
        ) {
            MainContents(
                viewModels = viewModels,
                categories = categories,
                pagerState = pagerState,
                startIndex = startIndex,
                pageCount = pageCount,
                contents = contents
            )

            if (searchQuery.isNotEmpty()) {
                SearchContents(
                    contents = contents,
                    query = searchQuery
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreferencesScenePreview() {
    val coroutineScope = rememberCoroutineScope()
    val categories = remember { PreferencesCategory.entries }

    val viewModels = PrefViewModels(
        information = FakeInformationViewModel(),
        account = FakeAccountViewModel(),
        general = FakeGeneralViewModel(),
        theme = FakeThemeViewModel(),
        entry = FakeEntryViewModel(),
        bookmark = FakeBookmarkViewModel(),
        browser = FakeBrowserViewModel(),
        favoriteSites = FakeFavoriteSitesViewModel(),
        ngWord = FakeNgWordsViewModel(coroutineScope),
        ngUsers = FakeNgUsersViewModel(),
        userLabels = FakeUserLabelsViewModel()
    )

    val contents = PrefContents(
        information = informationPageContents(viewModels.information),
        general = generalPageContents(viewModels.general),
        account = accountPageContents(viewModels.account),
        bookmark = bookmarkPageContents(viewModels.bookmark),
        browser = browserPageContents(viewModels.browser)
    )

    PreferencesScene(
        viewModels,
        categories,
        contents
    )
}

// ------ //

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainContents(
    viewModels : PrefViewModels,
    categories: List<PreferencesCategory>,
    contents: PrefContents,
    pagerState: PagerState,
    startIndex: Int,
    pageCount: Int,
) {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Row(Modifier.fillMaxSize()) {
        // カテゴリ一覧
        Box(
            Modifier
                .width(48.dp)
                .fillMaxHeight()
        ) {
            Categories(
                categories,
                pagerState,
                startIndex,
                Modifier.fillMaxSize()
            )

            VerticalGradientEdge(
                topColor = Color.Transparent,
                bottomColor = CurrentTheme.background,
                height = navigationBarHeight * 1.5f,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clickGuard()
            )
        }

        // セパレータ（左右）
        Spacer(
            Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(CurrentTheme.primary)
        )

        // コンテンツ部分
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            val p = (index - startIndex).floorMod(pageCount)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(categories[p].iconId),
                    contentDescription = "current category icon",
                    colorFilter = ColorFilter.tint(CurrentTheme.grayTextColor),
                    modifier = Modifier
                        .size(72.dp)
                        .alpha(.15f)
                )

                when (categories[p]) {
                    PreferencesCategory.Info -> {
                        BasicPreferencesPage(
                            state = viewModels.information.lazyListState(),
                            contents = contents.information
                        )
                    }

                    PreferencesCategory.Accounts -> {
                        AccountsPage(
                            state = viewModels.account.lazyListState(),
                            contents = contents.account,
                            navigationBarInset = navigationBarHeight,
                            onReload = { viewModels.account.reload() }
                        )
                    }

                    PreferencesCategory.General -> {
                        BasicPreferencesPage(
                            state = viewModels.general.lazyListState(),
                            contents = contents.general
                        )
                    }

                    PreferencesCategory.Theme -> {
                        ThemePage(
                            viewModel = viewModels.theme,
                            pagerState = pagerState,
                            navigationBarInset = navigationBarHeight
                        )
                    }

                    PreferencesCategory.Entry -> {
                        EntryPage(viewModel = viewModels.entry)
                    }

                    PreferencesCategory.Bookmark -> {
                        BasicPreferencesPage(
                            state = viewModels.bookmark.lazyListState(),
                            contents = contents.bookmark
                        )
                    }

                    PreferencesCategory.Browser -> {
                        BasicPreferencesPage(
                            state = viewModels.browser.lazyListState(),
                            contents = contents.browser
                        )
                    }

                    PreferencesCategory.FavoriteSites -> {
                        FavoriteSitesPage(
                            viewModel = viewModels.favoriteSites,
                            navigationBarInset = navigationBarHeight
                        )
                   }

                    PreferencesCategory.NgWords -> {
                        NgWordsPage(
                            viewModel = viewModels.ngWord,
                            navigationBarInset = navigationBarHeight
                        )
                    }

                    PreferencesCategory.NgUsers -> {
                        NgUsersPage(
                            viewModel = viewModels.ngUsers,
                            navigationBarInset = navigationBarHeight
                        )
                    }

                    PreferencesCategory.UserLabels -> {
                        UserLabelsPage(
                            viewModel = viewModels.userLabels,
                            pagerState = pagerState,
                            navigationBarInset = navigationBarHeight
                        )
                    }

                    else -> {}
                }

                VerticalGradientEdge(
                    topColor = Color.Transparent,
                    bottomColor = CurrentTheme.background,
                    height = navigationBarHeight * 1.5f,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickGuard()
                )
            }
        }
    }
}

@Composable
private fun SearchContents(
    contents: PrefContents,
    query: String
) {
    val context = LocalContext.current
    val contentItems = remember { contents.allItems(context) }
    val displayItems = remember(query) {
        buildList {
            // 検索ヒットしたアイテムが属するサブカテゴリも表示する
            var currentSubCategory: Pair<String, @Composable ()->Unit>? = null
            for (item in contentItems) {
                if (item.first.isBlank()) {
                    currentSubCategory = item
                }
                else if (item.first.contains(query)) {
                    currentSubCategory?.let {
                        add(it)
                    }
                    add(item)
                    currentSubCategory = null
                }
            }
        }
    }

    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .background(CurrentTheme.background)
            .verticalScrollbar(lazyListState)
            .fillMaxSize()
    ) {
        items(displayItems) {
            it.second()
        }
    }
}

// ------ //

/**
 * トップバー
 */
@Composable
private fun TopBar(
    category: PreferencesCategory,
    onSearchQueryChange: (String)->Unit
) {
    var isSearchViewEnabled by remember { mutableStateOf(false) }
    val textFieldValue = rememberMutableTextFieldValue(text = "")

    BackHandler(enabled = isSearchViewEnabled) {
        isSearchViewEnabled = false
        onSearchQueryChange("")
    }

    TopAppBar(
        backgroundColor = CurrentTheme.titleBarBackground,
        title = {
            if (isSearchViewEnabled) {
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusRequester = focusKeyboardRequester()
                val keyboardOptions = remember { KeyboardOptions(imeAction = ImeAction.Search) }
                val keyboardActions = remember { KeyboardActions(onSearch = { keyboardController?.hide() }) }

                TextField(
                    value = textFieldValue.value,
                    onValueChange = {
                        textFieldValue.value = it
                        onSearchQueryChange(it.text)
                    },
                    placeholder = {
                        Text(stringResource(R.string.search_setting_sheet_query_placeholder))
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = CurrentTheme.onPrimary,
                        placeholderColor = CurrentTheme.onPrimary.copy(alpha = .6f),
                        cursorColor = CurrentTheme.onPrimary,
                        focusedIndicatorColor = CurrentTheme.onPrimary,
                    ),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
            else {
                Text(
                    text = stringResource(
                        R.string.pref_title,
                        stringResource(category.textId)
                    ),
                    fontSize = 20.sp,
                    color = CurrentTheme.titleBarOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    isSearchViewEnabled = !isSearchViewEnabled
                    if (!isSearchViewEnabled) {
                        onSearchQueryChange("")
                    }
                }
            ) {
                val iconVector =
                    if (isSearchViewEnabled) Icons.Filled.Close
                    else Icons.Filled.Search

                Icon(
                    iconVector,
                    contentDescription = "search",
                    tint = CurrentTheme.titleBarOnBackground
                )
            }
        }
    )
}

// ------ //

/**
 * 設定カテゴリリスト
 */
@Suppress("SameParameterValue")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Categories(
    categories: List<PreferencesCategory>,
    pagerState: PagerState,
    startIndex: Int,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(categories) {
            PrefCategoryItem(it, pagerState, startIndex)
        }
    }
}

/**
 * 設定カテゴリ項目
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PrefCategoryItem(
    category: PreferencesCategory,
    pagerState: PagerState,
    startIndex: Int
) {
    val coroutineScope = rememberCoroutineScope()
    val categories = PreferencesCategory.entries.toTypedArray()
    val currentPage = (pagerState.currentPage - startIndex).floorMod(categories.size)
    val background =
        if (currentPage == category.ordinal) CurrentTheme.primary
        else Color.Transparent
    val foreground =
        if (currentPage == category.ordinal) CurrentTheme.background
        else CurrentTheme.onBackground

    val showTooltip = remember { mutableStateOf(false) }

    Box {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(background)
                .combinedClickable(
                    onClick = {
                        val prev =
                            (pagerState.currentPage - startIndex).floorMod(categories.size)
                        val diff = category.ordinal - prev
                        if (diff != 0) {
                            coroutineScope.launch {
                                pagerState.scrollToPage(pagerState.currentPage + diff)
                            }
                        }
                    },
                    onLongClick = {
                        showTooltip.value = true
                    }
                )
        ) {
            Image(
                painter = painterResource(id = category.iconId),
                contentDescription = stringResource(id = category.textId),
                colorFilter = ColorFilter.tint(foreground),
                modifier = Modifier
                    .padding(8.dp)
            )
        }

        Tooltip(expanded = showTooltip) {
            Text(text = stringResource(id = category.textId))
        }
    }
}

// ------ //

/**
 * 無限（※厳密には有限）ループするページャのページ算出用途
 */
private fun Int.floorMod(other: Int) = when(other) {
    0 -> this
    else -> this - floorDiv(other) * other
}
