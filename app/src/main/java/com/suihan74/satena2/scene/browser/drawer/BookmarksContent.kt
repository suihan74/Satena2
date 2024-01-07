package com.suihan74.satena2.scene.browser.drawer

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.bookmarks.BookmarksTab
import com.suihan74.satena2.scene.bookmarks.BookmarksTabContent
import com.suihan74.satena2.scene.bookmarks.BookmarksViewModel
import com.suihan74.satena2.scene.bookmarks.FakeBookmarksViewModel
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * ブラウザのドロワに表示するブクマリスト+投稿エリア
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BookmarksContent(
    bookmarksViewModel: BookmarksViewModel,
    lazyListState: LazyListState,
    drawerState: DrawerState,
    navController: NavController
) {
    var additionalAreaVisible by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(BookmarksTab.RECENT) }
    val items by remember(currentTab) {
        when (currentTab) {
            BookmarksTab.DIGEST -> bookmarksViewModel.popularBookmarksFlow
            BookmarksTab.RECENT -> bookmarksViewModel.recentBookmarksFlow
            BookmarksTab.ALL -> bookmarksViewModel.allBookmarksFlow
            BookmarksTab.CUSTOM -> bookmarksViewModel.allBookmarksFlow //todo
        }
    }.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = CurrentTheme.drawerBackground,
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            // 投稿ボタン
            FloatingActionButton(
                backgroundColor = CurrentTheme.primary,
                contentColor = CurrentTheme.onPrimary,
                modifier = Modifier.size(40.dp),
                onClick = {
                    bookmarksViewModel.openPostActivity()
                    //additionalAreaVisible = !additionalAreaVisible
                }
            ) {
                if (additionalAreaVisible) {
                    Image(
                        painterResource(R.drawable.ic_close),
                        contentDescription = "close",
                        colorFilter = ColorFilter.tint(CurrentTheme.onPrimary)
                    )
                }
                else {
                    Text(text = "B!")
                }
            }
        },
        bottomBar = {
            val menuHeight = 40.dp
            val contentHeight = 200.dp
            val bottomBarHeight = remember { Animatable(initialValue = menuHeight.value) }

            LaunchedEffect(Unit) {
                snapshotFlow { additionalAreaVisible }
                    .collect {
                        val targetDp =
                            if (it) menuHeight + contentHeight
                            else menuHeight
                        bottomBarHeight.animateTo(
                            targetValue = targetDp.value,
                            animationSpec = tween(160)
                        )
                    }
            }

            BottomAppBar(
                cutoutShape = CircleShape,
                backgroundColor = CurrentTheme.tabBackground,
                modifier = Modifier.height(bottomBarHeight.value.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                Column {
                    Row(
                        Modifier.height(menuHeight)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Transparent)
                                .width(118.dp)
                                .fillMaxHeight()
                                .clickable {
                                    additionalAreaVisible = !additionalAreaVisible
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(currentTab.textId),
                                color = CurrentTheme.onBackground,
                                fontSize = 16.sp
                            )
                        }
                    }
                    if (additionalAreaVisible) {
                        Column(
                            Modifier.height(contentHeight)
                        ) {
                            BookmarksTab.values().forEach { tab ->
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentTab = tab
                                            additionalAreaVisible = false
                                        }
                                ) {
                                    Text(
                                        text = stringResource(tab.textId),
                                        color =
                                            if (tab == currentTab) CurrentTheme.primary
                                            else CurrentTheme.onBackground,
                                        fontSize = 16.sp,
                                        modifier = Modifier
                                            .padding(
                                                top = 8.dp,
                                                bottom = 8.dp,
                                                start = 16.dp
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) {
        BookmarksTabContent(
            viewModel = bookmarksViewModel,
            tab = currentTab,
            lazyListState = lazyListState,
            bookmarks = items,
            navController = navController
        )
    }

    BackHandler(drawerState.isOpen && additionalAreaVisible) {
        additionalAreaVisible = false
    }
}

// ------ //

@Preview(device = "spec:width=300dp,height=850.9dp,dpi=440")
@Composable
private fun BookmarksContentPreview() {
    BookmarksContent(
        bookmarksViewModel = FakeBookmarksViewModel(),
        lazyListState = rememberLazyListState(),
        drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
        navController = rememberNavController()
    )
}
