package com.suihan74.satena2.scene.browser.drawer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.suihan74.satena2.scene.bookmarks.BookmarksViewModel
import com.suihan74.satena2.scene.bookmarks.DisplayBookmark
import com.suihan74.satena2.scene.browser.BrowserViewModel

enum class BrowserBookmarksContentDestination {
    Confirmation,
    Bookmarks
}

@Composable
fun BookmarksContentHost(
    browserViewModel: BrowserViewModel,
    bookmarksViewModel: BookmarksViewModel,
    bookmarksScrollState: LazyListState,
    drawerState: DrawerState,
    navController: NavHostController,
    onSelectBookmark: (DisplayBookmark)->Unit
) {
    val currentUrl by browserViewModel.currentUrl.collectAsState("")

    NavHost(
        navController = navController,
        startDestination = BrowserBookmarksContentDestination.Confirmation.name,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(BrowserBookmarksContentDestination.Confirmation.name) {
            BookmarksConfirmationContent(
                currentUrl = currentUrl,
                bookmarksViewModel = bookmarksViewModel,
                onConfirmed = {
                    navController.navigate(BrowserBookmarksContentDestination.Bookmarks.name) {
                        bookmarksViewModel.load(currentUrl)
                        popUpTo(0)
                    }
                }
            )
        }
        composable(BrowserBookmarksContentDestination.Bookmarks.name) {
            BookmarksContent(
                bookmarksViewModel = bookmarksViewModel,
                lazyListState = bookmarksScrollState,
                drawerState = drawerState,
                navController = navController,
                onSelectBookmark = onSelectBookmark
            )
        }
    }
}
