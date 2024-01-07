package com.suihan74.satena2.scene.browser

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.bookmarks.BookmarksViewModel
import com.suihan74.satena2.scene.bookmarks.FakeBookmarksViewModel
import com.suihan74.satena2.scene.browser.drawer.BookmarksContentHost
import com.suihan74.satena2.scene.browser.drawer.HistoryContent
import com.suihan74.satena2.scene.preferences.page.BasicPreferencesPage
import com.suihan74.satena2.scene.preferences.page.browser.browserPageContents
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.launch

enum class DrawerTab(
    @StringRes val titleId : Int,
    @DrawableRes val iconId: Int
) {
    Bookmarks(
        titleId = R.string.browser_drawer_tab_bookmarks,
        iconId = R.drawable.ic_bookmarks
    ),
    Favorites(
        titleId = R.string.browser_drawer_tab_favorites,
        iconId = R.drawable.ic_star
    ),
    History(
        titleId = R.string.browser_drawer_tab_history,
        iconId = R.drawable.ic_category_history
    ),
    Preferences(
        titleId = R.string.browser_drawer_tab_preferences,
        iconId = R.drawable.ic_settings
    )
}

// ------ //

/**
 * ドロワーに表示するコンテンツ
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerContent(
    viewModel: BrowserViewModel,
    bookmarksViewModel: BookmarksViewModel,
    prefsViewModel: PrefsBrowserViewModel,
    drawerState: DrawerState,
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()
    val pages = remember { DrawerTab.values() }
    val bookmarksScrollState = rememberLazyListState()

    Column(
        Modifier
            .fillMaxSize()
            .background(CurrentTheme.drawerBackground)
    ) {
        TabRow(
            pagerState.currentPage,
            backgroundColor = CurrentTheme.tabBackground,
            contentColor = CurrentTheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, item ->
                Tab(
                    selected = pagerState.currentPage == index,
                    selectedContentColor = CurrentTheme.tabSelectedColor,
                    unselectedContentColor = CurrentTheme.tabUnSelectedColor,
                    onClick = {
                        if (pagerState.currentPage == index) {
                            coroutineScope.launch {
                                when(index) {
                                    DrawerTab.Bookmarks.ordinal -> {
                                        bookmarksScrollState.scrollToItem(0, 0)
                                    }
                                    else -> { /* todo */ }
                                }
                            }
                        }
                        else {
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        }
                    }
                ) {
                    Image(
                        painterResource(id = item.iconId),
                        contentDescription = "tab icon: ${stringResource(item.titleId)}",
                        colorFilter = ColorFilter.tint(
                            if (pagerState.currentPage == index) CurrentTheme.tabSelectedColor
                            else CurrentTheme.tabUnSelectedColor
                        ),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .size(24.dp)
                    )
                }
            }
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState
        ) { page ->
            when(page) {
                DrawerTab.Bookmarks.ordinal -> {
                    BookmarksContentHost(
                        browserViewModel = viewModel,
                        bookmarksViewModel = bookmarksViewModel,
                        bookmarksScrollState = bookmarksScrollState,
                        drawerState = drawerState
                    )
                }

                DrawerTab.History.ordinal -> {
                    HistoryContent(
                        viewModel = viewModel,
                        drawerState = drawerState
                    )
                }

                DrawerTab.Preferences.ordinal -> {
                    BasicPreferencesPage(
                        state = prefsViewModel.lazyListState(),
                        contents = browserPageContents(prefsViewModel)
                    )
                }

                else -> {
                    // todo
                    Box(Modifier.fillMaxSize()) {}
                }
            }
        }
    }
}

// ------ //

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun DrawerContentPreview() {
    val coroutineScope = rememberCoroutineScope()
    DrawerContent(
        viewModel = FakeBrowserViewModel(coroutineScope),
        bookmarksViewModel = FakeBookmarksViewModel(),
        prefsViewModel = com.suihan74.satena2.scene.preferences.page.browser.FakeBrowserViewModel(),
        drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
        pagerState = rememberPagerState(initialPage = 0) { 4 }
    )
}
