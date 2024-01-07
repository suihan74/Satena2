package com.suihan74.satena2.scene.browser.drawer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suihan74.satena2.scene.bookmarks.BookmarksViewModel
import com.suihan74.satena2.scene.browser.BrowserViewModel

@Composable
fun BookmarksContentHost(
    browserViewModel: BrowserViewModel,
    bookmarksViewModel: BookmarksViewModel,
    bookmarksScrollState: LazyListState,
    drawerState: DrawerState
) {
    val navController = rememberNavController()
    val currentUrl by browserViewModel.currentUrl.collectAsState("")

    LaunchedEffect(Unit) {
        browserViewModel.currentUrl
            .collect {
                navController.navigate("confirmation") {
                    popUpTo(0)
                }
            }
    }

    NavHost(
        navController = navController,
        startDestination = "confirmation",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("confirmation") {
            BookmarksConfirmationContent(
                currentUrl = currentUrl,
                bookmarksViewModel = bookmarksViewModel,
                onConfirmed = {
                    navController.navigate("bookmarks") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable("bookmarks") {
            LaunchedEffect(Unit) {
                bookmarksViewModel.load(currentUrl)
            }
            BookmarksContent(
                bookmarksViewModel = bookmarksViewModel,
                lazyListState = bookmarksScrollState,
                drawerState = drawerState,
                navController = navController
            )
        }
    }
}
