package com.suihan74.satena2.scene.bookmarks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomMenuItemButton
import com.suihan74.satena2.compose.BottomSearchContent
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomMenu(
    pagerState: PagerState,
    lazyListStates: List<LazyListState>,
    myBookmark : DisplayBookmark?,
    signedIn: Boolean,
    initialSearchQuery: String,
    onNavigateMyBookmark: ()->Unit,
    onOpenSetting: () -> Unit,
    onSearch: (String)->Unit,
) {
    val bottomBarNavController = rememberNavController()
    val transitionAnim = tween<IntOffset>(300)
    val enterTransition = slideIn(animationSpec = transitionAnim) { IntOffset(0, it.height) }
    val exitTransition = slideOut(animationSpec = transitionAnim) { IntOffset(0, it.height) }
    val contentPadding = PaddingValues(
        start = 0.dp,
        end = if (signedIn) 88.dp else 0.dp
    )

    BottomAppBar(
        cutoutShape = CircleShape,
        backgroundColor = CurrentTheme.bottomBarBackground,
        contentPadding = contentPadding,
        modifier = Modifier.background(Color.Transparent)
    ) {
        NavHost(
            navController = bottomBarNavController,
            startDestination = "main"
        ) {
            composable(
                "main",
                enterTransition = { enterTransition },
                exitTransition = { exitTransition }
            ) {
                MainContent(
                    pagerState = pagerState,
                    searchQuery = initialSearchQuery,
                    onOpenSetting = onOpenSetting,
                    onSearch = onSearch,
                    onOpenScrollMenu = { bottomBarNavController.navigate("scroll") },
                    onOpenSearchContent = { bottomBarNavController.navigate("search") }
                )
            }
            composable(
                "scroll",
                enterTransition = { enterTransition },
                exitTransition = { exitTransition }
            ) {
                ScrollMenuContent(
                    lazyListState = lazyListStates[pagerState.currentPage],
                    myBookmark = myBookmark,
                    onNavigateMyBookmark = onNavigateMyBookmark,
                    onClose = { bottomBarNavController.popBackStack("main", false) }
                )
            }
            composable(
                "search",
                enterTransition = { enterTransition },
                exitTransition = { exitTransition }
            ) {
                BottomSearchContent(
                    initialSearchQuery = initialSearchQuery,
                    onSearch = onSearch,
                    onClose = { bottomBarNavController.popBackStack("main", false) }
                )
            }
        }
    }
}

// ------ //

/**
 * メインメニュー
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
    pagerState: PagerState,
    searchQuery: String,
    onOpenSetting: ()->Unit,
    onSearch: (String)->Unit,
    onOpenScrollMenu: ()->Unit,
    onOpenSearchContent: ()->Unit
) {
    val itemsArrangement: Arrangement.Horizontal = Arrangement.End
    val scrollState = rememberScrollState()
    val reverseScrolling = remember(itemsArrangement) { itemsArrangement == Arrangement.Start }
    val settingButtonVisible = remember(pagerState.currentPage) {
        when (pagerState.currentPage) {
            BookmarksTab.DIGEST.ordinal, BookmarksTab.CUSTOM.ordinal -> true
            else -> false
        }
    }

    Row(
        horizontalArrangement = itemsArrangement,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(
                state = scrollState,
                reverseScrolling = reverseScrolling
            )
    ) {
        AnimatedVisibility(
            visible = settingButtonVisible,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            BottomMenuItemButton(
                iconId = R.drawable.ic_settings,
                textId = R.string.preferences,
                onClick = onOpenSetting
            )
        }
        BottomMenuItemButton(
            iconId = R.drawable.ic_category_search,
            textId = R.string.search,
            tint =
                if (searchQuery.isBlank()) CurrentTheme.bottomBarOnBackground
                else CurrentTheme.primary,
            onClick = onOpenSearchContent,
            onLongClick =
                if (searchQuery.isBlank()) null
                else { { onSearch("") } }
        )
        BottomMenuItemButton(
            iconId = R.drawable.ic_menu,
            textId = R.string.bookmark_bottom_menu_open_scroll_menu,
            onClick = onOpenScrollMenu
        )
    }
}

// ------ //

/**
 * スクロールメニュー
 */
@Composable
private fun ScrollMenuContent(
    lazyListState: LazyListState,
    myBookmark: DisplayBookmark?,
    onNavigateMyBookmark: ()->Unit,
    onClose: ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val itemsArrangement: Arrangement.Horizontal = Arrangement.End
    val reverseScrolling = remember(itemsArrangement) { itemsArrangement == Arrangement.Start }
    val scrollState = rememberScrollState()

    Row(
        horizontalArrangement = itemsArrangement,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(
                state = scrollState,
                reverseScrolling = reverseScrolling
            )
    ) {
        BottomMenuItemButton(
            iconId = R.drawable.ic_vertical_align_top,
            textId = R.string.scroll_to_top,
            onClick = {
                coroutineScope.launch {
                    lazyListState.scrollToItem(0, 0)
                    onClose()
                }
            }
        )
        if (myBookmark != null) {
            BottomMenuItemButton(
                iconId = R.drawable.ic_bookmark,
                textId = R.string.my_bookmarks,
                onClick = {
                    onNavigateMyBookmark()
                    onClose()
                }
            )
        }
        BottomMenuItemButton(
            iconId = R.drawable.ic_vertical_align_bottom,
            textId = R.string.scroll_to_bottom,
            onClick = {
                val lastIndex = lazyListState.layoutInfo.totalItemsCount - 1
                if (lastIndex < 0) {
                    onClose()
                }
                else {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(lastIndex, 0)
                        onClose()
                    }
                }
            }
        )
        BottomMenuItemButton(
            iconId = R.drawable.ic_close,
            textId = R.string.close,
            onClick = { onClose() }
        )
    }
}
