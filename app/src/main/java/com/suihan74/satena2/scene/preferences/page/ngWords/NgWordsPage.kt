package com.suihan74.satena2.scene.preferences.page.ngWords

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AnimatedListItem
import com.suihan74.satena2.compose.MultiToggleButton
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.model.ignoredEntry.IgnoreTarget
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntryType
import com.suihan74.satena2.scene.preferences.PrefItemDefaults
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionDialog
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedMultiToggleButtonColors

/**
 * NG URL/TEXTページ
 */
@Composable
fun NgWordsPage(
    viewModel: NgWordsViewModel,
    navigationBarInset: Dp
) {
    val isMenuDialogVisible = remember { mutableStateOf(false) }
    val isNgWordSettingDialogVisible = remember { mutableStateOf(false) }

    val clickedItem = remember { mutableStateOf<IgnoredEntry?>(null) }
    val selectedTabIndex = remember { mutableIntStateOf(viewModel.currentTab.value) }
    val currentTabItems = viewModel.currentTabItems.collectAsState()

    val lazyListState = viewModel.lazyListState()

    ConstraintLayout(
        Modifier.fillMaxSize()
    ) {
        val (tabs, addButton) = createRefs()

        // コンテンツ
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(
                    state = lazyListState,
                    color = CurrentTheme.primary
                )
        ) {
            items(
                currentTabItems.value,
                key = { it.id }
            ) {
                AnimatedListItem {
                    NgSettingItem(entry = it) {
                        clickedItem.value = it
                        isMenuDialogVisible.value = true
                    }
                }
                Divider(
                    color = CurrentTheme.listItemDivider,
                    thickness = 1.dp
                )
            }
            emptyFooter(
                height = 112.dp + navigationBarInset
            )
        }

        // URL/TEXTタブ切替えボタン
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .constrainAs(tabs) {
                    linkTo(
                        top = addButton.top,
                        bottom = addButton.bottom
                    )
                    linkTo(start = parent.start, end = addButton.start, bias = .5f)
                    width = Dimension.fillToConstraints
                }
        ) {
            MultiToggleButton(
                items = IgnoredEntryType.entries.map { it.name },
                selectedIndex = selectedTabIndex,
                colors = themedMultiToggleButtonColors()
            ) { index -> viewModel.changeCurrentTab(index) }
        }

        // 項目追加ボタン
        FloatingActionButton(
            backgroundColor = CurrentTheme.primary,
            contentColor = CurrentTheme.onPrimary,
            modifier = Modifier
                .constrainAs(addButton) {
                    linkTo(
                        top = parent.top,
                        bottom = parent.bottom,
                        bias = 1f,
                        bottomMargin = 24.dp + navigationBarInset
                    )
                    linkTo(start = tabs.end, end = parent.end, endMargin = 16.dp)
                },
            onClick = {
                clickedItem.value = null
                isNgWordSettingDialogVisible.value = true
            }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "add an item"
            )
        }
    }
    Dialogs(viewModel, selectedTabIndex.intValue, clickedItem, isMenuDialogVisible, isNgWordSettingDialogVisible)
}

@Composable
private fun Dialogs(
    viewModel: NgWordsViewModel,
    selectedTabIndex: Int,
    clickedItem: MutableState<IgnoredEntry?>,
    isMenuDialogVisible: MutableState<Boolean>,
    isNgWordSettingDialogVisible: MutableState<Boolean>
) {
    // 項目長押しメニュー
    val item = clickedItem.value
    if (isMenuDialogVisible.value) {
        item!!
        MenuDialog(
            titleText = item.query,
            menuItems = listOf(
                menuDialogItem("編集") {
                    isNgWordSettingDialogVisible.value = true
                    true
                },
                menuDialogItem("削除") {
                    viewModel.delete(item)
                },
            ),
            negativeButton = dialogButton(R.string.close) { isMenuDialogVisible.value = false },
            onDismissRequest = { isMenuDialogVisible.value = false },
            colors = themedCustomDialogColors(),
            properties = viewModel.dialogProperties()
        )
    }

    // 非表示設定 追加/更新ダイアログ
    if (isNgWordSettingDialogVisible.value) {
        NgWordEditionDialog(
            item = item,
            initialTabIndex = selectedTabIndex,
            isError = { text, asRegex -> viewModel.isNgRegexError(text, asRegex) },
            properties = viewModel.dialogProperties(),
            onDismiss = { isNgWordSettingDialogVisible.value = false }
        ) {
            if (item == null) viewModel.insertNgSetting(it)
            else viewModel.updateNgSetting(item, it)
        }
    }
}

// ------ //

@Preview
@Composable
private fun NgWordsPagePreview() {
    val coroutineScope = rememberCoroutineScope()
    NgWordsPage(
        viewModel = FakeNgWordsViewModel(coroutineScope),
        navigationBarInset = 0.dp
    )
}

// ------ //

/**
 * NGワード設定項目
 */
@Composable
private fun NgSettingItem(
    entry: IgnoredEntry,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = onClick,
                onClick = onClick
            )
            .padding(
                vertical = PrefItemDefaults.listItemVerticalPadding,
                horizontal = PrefItemDefaults.listItemHorizontalPadding
            )
    ) {
        Text(
            text = entry.query,
            color = CurrentTheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (entry.type == IgnoredEntryType.TEXT) {
            if (entry.target.contains(IgnoreTarget.ENTRY)) {
                TargetBadge(caption = stringResource(R.string.ng_word_setting_target_entry))
            }
            if (entry.target.contains(IgnoreTarget.BOOKMARK)) {
                TargetBadge(caption = stringResource(R.string.ng_word_setting_target_bookmark))
            }
        }
    }
}

// ------ //

/**
 * NGワード設定対象の表示
 */
@Composable
private fun TargetBadge(caption: String) {
    Box(
        Modifier
            .padding(start = 6.dp)
            .background(color = CurrentTheme.primary, shape = RoundedCornerShape(4.dp))
    ) {
        Text(
            text = caption,
            fontSize = 11.sp,
            color = CurrentTheme.onPrimary,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
        )
    }
}

// ------ //

@Preview
@Composable
private fun NgSettingItemPreview() {
    NgSettingItem(
        IgnoredEntry.createDummy(
            type = IgnoredEntryType.TEXT,
            query = "test"
        )
    )
}
