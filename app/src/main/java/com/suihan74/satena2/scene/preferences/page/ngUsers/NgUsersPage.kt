package com.suihan74.satena2.scene.preferences.page.ngUsers

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AnimatedListItem
import com.suihan74.satena2.compose.FloatingTextField
import com.suihan74.satena2.compose.FloatingTextFieldColors
import com.suihan74.satena2.compose.SwipeRefreshBox
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.preferences.HatenaUserItem
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.utility.rememberMutableTextFieldValue

/**
 * NG URL/TEXTページ
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NgUsersPage(viewModel: NgUsersViewModel, navigationBarInset: Dp) {
    val clickedItem = remember { mutableStateOf("") }
    val ngUsers = viewModel.ngUsers.collectAsState()
    val lazyListState = viewModel.lazyListState()
    val loading by viewModel.loading.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { viewModel.reload() }
    )

    Box(Modifier.fillMaxSize()) {
        // コンテンツ
        SwipeRefreshBox(
            refreshing = loading,
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(
                        state = lazyListState,
                        color = CurrentTheme.primary
                    )
            ) {
                items(ngUsers.value) { username ->
                    AnimatedListItem {
                        HatenaUserItem(
                            username,
                            viewModel.iconUrl(username),
                            onClick = { clickedItem.value = username },
                            onLongClick = { clickedItem.value = username }
                        )
                    }
                    Divider(
                        color = CurrentTheme.listItemDivider,
                        thickness = 1.dp
                    )
                }
                emptyFooter()
            }
        }

        // 項目検索ボタン
        val isOpen = remember { mutableStateOf(false) }
        val textFieldValue = rememberMutableTextFieldValue()
        val keyboardController = LocalSoftwareKeyboardController.current
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = 16.dp + navigationBarInset,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            FloatingTextField(
                isOpen,
                textFieldValue,
                placeholderText = stringResource(R.string.pref_ng_user_search_text_field_placeholder),
                colors = FloatingTextFieldColors(
                    background = CurrentTheme.primary,
                    foreground = CurrentTheme.onPrimary
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.setSearchQuery(textFieldValue.value.text)
                        keyboardController?.hide()
                    }
                )
            )
        }
        BackHandler(enabled = isOpen.value) { isOpen.value = false }
    }

    ItemMenuDialog(username = clickedItem, viewModel = viewModel)
}

@Preview
@Composable
private fun NgUsersPagePreview() {
    NgUsersPage(
        viewModel = FakeNgUsersViewModel(),
        navigationBarInset = 0.dp
    )
}

// ------ //

/**
 * ユーザーリスト項目に対する操作メニュー
 */
@Composable
private fun ItemMenuDialog(username: MutableState<String>, viewModel: NgUsersViewModel) {
    if (username.value.isBlank()) return
    val isCancelNgDialogOpen = remember { mutableStateOf(false) }
    val iconUrl = viewModel.iconUrl(username.value)

    MenuDialog(
        title = { HatenaUserItem(username.value, iconUrl) },
        positiveButton = dialogButton(R.string.close) { username.value = "" },
        menuItems = listOf(
            menuDialogItem(R.string.pref_user_menu_dialog_recent_bookmarks) { /* TODO */ true },
            menuDialogItem(R.string.pref_user_menu_dialog_cancel_ng) {
                isCancelNgDialogOpen.value = true
                false
            }
        ),
        onDismissRequest = { username.value = "" },
        colors = themedCustomDialogColors(),
        properties = viewModel.dialogProperties()
    )

    CancelNgUserConfirmDialog(
        username = username,
        isOpen = isCancelNgDialogOpen,
        viewModel = viewModel
    )
}

@Preview
@Composable
private fun ItemMenuDialogPreview() {
    val username = remember { mutableStateOf("suihan74") }
    Box(
        Modifier.fillMaxSize()
    ) {
        ItemMenuDialog(
            username = username,
            viewModel = FakeNgUsersViewModel()
        )
    }
}

// ------ //

@Composable
private fun CancelNgUserConfirmDialog(
    username: MutableState<String>,
    isOpen: MutableState<Boolean>,
    viewModel: NgUsersViewModel
) {
    if (!isOpen.value || username.value.isBlank()) return
    val iconUrl = viewModel.iconUrl(username.value)
    CustomDialog(
        title = { HatenaUserItem(username.value, iconUrl) },
        positiveButton = dialogButton(R.string.ok) {
            viewModel.removeNgUser(username.value)
            isOpen.value = false
            username.value = ""
        },
        negativeButton = dialogButton(R.string.cancel) {
            isOpen.value = false
        },
        onDismissRequest = { isOpen.value = false },
        colors = themedCustomDialogColors(),
        properties = viewModel.dialogProperties()
    ) {
        Text(
            text = "ユーザー非表示を解除します。\nよろしいですか？",
            color = CurrentTheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview
@Composable
private fun CancelNgUserConfirmDialogPreview() {
    val username = remember { mutableStateOf("suihan74") }
    val isOpen = remember { mutableStateOf(true) }
    Box(
        Modifier.fillMaxSize()
    ) {
        CancelNgUserConfirmDialog(
            username = username,
            isOpen = isOpen,
            viewModel = FakeNgUsersViewModel()
        )
    }
}
